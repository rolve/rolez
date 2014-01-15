package ch.trick17.simplejpf.test;

import static org.junit.Assert.fail;
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.Verify;

import java.util.AbstractList;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.slf4j.LoggerFactory;

/**
 * An extension of {@link JpfTest} which provides a way to verify that certain
 * code regions may be executed in parallel.
 * <p>
 * For example, a test could be written like this:
 * 
 * <pre>
 * &#064;Test
 * public void myTest() {
 *     // Verify that regions 0 and 1 may be executed in parallel:
 *     if(verifyParallelism(new int[][]{{0, 1}})) {
 *         new Thread() {
 *             public void run() {
 *                 region(0);
 *             }
 *         }.start();
 *         
 *         region(1);
 *     }
 * }
 * </pre>
 * 
 * @author Michael Faes
 */
public abstract class JpfParallelismTest extends JpfTest {
    
    private boolean verifyParallelism = false;
    private String regionEvents = "";
    
    protected final void region(final int id) {
        if(verifyParallelism)
            synchronized(this) {
                final String event = "S" + id;
                if(regionEvents.contains(event))
                    throw new IllegalArgumentException(
                            "duplicate region identifier: " + id);
                regionEvents += event;
            }
        
        if(verifyParallelism)
            synchronized(this) {
                regionEvents += "E" + id;
            }
    }
    
    /**
     * Convenience method to either verify that there are no correctness
     * property violations or to verify the given parallelism, depending on the
     * given {@link VerifyMode}.
     * 
     * @param mode
     *            If {@link VerifyMode#CORRECTNESS}, then
     *            {@link #verifyNoPropertyViolation()} is called, otherwise
     *            {@link #verifyParallelExcept(int[][])} is called.
     * @param seqGroups
     *            The sequential groups specification
     * @return <code>true</code> if being executed in JPF, <code>false</code>
     *         otherwise
     * @throws AssertionError
     *             According to the called <code>verify</code> method
     */
    protected boolean verify(final VerifyMode mode, final int[]... seqGroups) {
        if(mode == VerifyMode.PARALLELISM)
            return verifyParallelExcept(seqGroups);
        else
            return verifyNoPropertyViolation();
    }
    
    /**
     * Verifies that there is a thread interleaving for which the
     * {@link #region(int) regions} in the calling method may be executed in
     * parallel, according to the given "parallel groups" specification. More
     * precisely, this method verifies that there is at least one (single)
     * thread interleaving, for which it holds that for each parallel group, all
     * regions in the group are executed in parallel.
     * 
     * @param parSpec
     *            The parallel groups specification, as <code>int</code> arrays
     * @return <code>true</code> if being executed in JPF, <code>false</code>
     *         otherwise
     * @throws AssertionError
     *             If the given parallelism specification is not met
     * @see JpfTest
     */
    protected boolean verifyParallel(final int[]... parSpec) {
        return verifyParallel(parSpec, false);
    }
    
    /**
     * Verifies that there is a thread interleaving for which the
     * {@link #region(int) regions} in the calling method may be executed in
     * parallel, according to the given "sequential groups" specification. More
     * precisely, this method verifies that there is at least one (single)
     * thread interleaving, for which it holds that all pairs of regions are
     * executed in parallel except for the ones given: for each sequential
     * group, no pair of regions in the group needs to be executed in parallel.
     * 
     * @param seqSpec
     *            The sequential groups specification, as <code>int</code>
     *            arrays
     * @return <code>true</code> if being executed in JPF, <code>false</code>
     *         otherwise
     * @throws AssertionError
     *             If the given parallelism specification is not met
     * @see JpfTest
     */
    protected boolean verifyParallelExcept(final int[]... seqSpec) {
        return verifyParallel(seqSpec, true);
    }
    
    private boolean verifyParallel(final int[][] spec, final boolean seq) {
        if(spec.length == 0)
            LoggerFactory.getLogger(JpfParallelismTest.class).warn(
                    "empty parallelism specification");
        
        verifyParallelism = true;
        if(Verify.isRunningInJPF())
            return true;
        else {
            final ParListener l = new ParListener(spec, seq);
            runJpf(l);
            
            if(!l.unmatchedParRegions.isEmpty()) {
                final Set<Set<RegionPair>> parRegions = l.possibleParRegions;
                
                final StringBuilder msg = new StringBuilder(
                        "Found parallel regions:");
                for(final Set<RegionPair> set : parRegions) {
                    final ArrayList<RegionPair> list = new ArrayList<>(set);
                    Collections.sort(list);
                    msg.append("\n    ").append(list);
                }
                LoggerFactory.getLogger(JpfParallelismTest.class).info(
                        msg.toString());
                
                final StringBuilder sets = new StringBuilder();
                for(final Set<RegionPair> set : l.unmatchedParRegions)
                    sets.append(set).append(", ");
                sets.setLength(sets.length() - 2);
                fail("JPF could not find an interleaving with the parallel regions "
                        + sets);
            }
            return false;
        }
    }
    
    private final class ParListener extends ListenerAdapter {
        
        final String testMethod = getCaller();
        
        final int[][] spec;
        final boolean seq;
        
        final Set<Set<RegionPair>> possibleParRegions = new HashSet<>();
        Set<Set<RegionPair>> unmatchedParRegions = null;
        
        public ParListener(final int[][] spec, final boolean seq) {
            this.spec = spec;
            this.seq = seq;
        }
        
        @Override
        public void methodExited(final VM vm, final ThreadInfo thread,
                final MethodInfo method) {
            if(isTestMethod(method)) {
                final int testRef = thread.getTopFrame().getThis();
                final ElementInfo instance = vm.getHeap().get(testRef);
                final String eventsString = ((ElementInfo) instance
                        .getFieldValueObject("regionEvents")).asString();
                
                final List<Integer> regions = new ArrayList<>();
                final Set<RegionPair> parRegions = new HashSet<>();
                final Deque<Integer> currentRegions = new ArrayDeque<>();
                
                @SuppressWarnings("resource") final Scanner scanner = new Scanner(
                        eventsString);
                scanner.useDelimiter("(?=[SE])");
                while(scanner.hasNext()) {
                    final String event = scanner.next();
                    final int id = Integer.parseInt(event.substring(1));
                    if(event.charAt(0) == 'S') {
                        assert !regions.contains(id);
                        regions.add(id);
                        for(final Integer other : currentRegions)
                            parRegions.add(new RegionPair(other, id));
                        currentRegions.addFirst(id);
                    }
                    else {
                        assert event.charAt(0) == 'E';
                        final boolean removed = currentRegions.remove(id);
                        assert removed;
                    }
                }
                addWithoutSubsets(possibleParRegions, parRegions);
                
                if(spec == null)
                    unmatchedParRegions = Collections.emptySet();
                else {
                    final Set<RegionPair> specParRegions = parRegionsFromSpec(regions);
                    specParRegions.removeAll(parRegions);
                    if(specParRegions.isEmpty())
                        unmatchedParRegions = Collections.emptySet();
                    else {
                        if(unmatchedParRegions == null)
                            unmatchedParRegions = new HashSet<>(Arrays
                                    .asList(specParRegions));
                        else
                            addWithoutSupersets(unmatchedParRegions,
                                    specParRegions);
                    }
                }
                
                if(unmatchedParRegions.isEmpty())
                    vm.getSearch().terminate();
            }
        }
        
        private boolean isTestMethod(final MethodInfo method) {
            return method.getClassName().equals(
                    JpfParallelismTest.this.getClass().getName())
                    && method.getName().equals(testMethod);
        }
        
        private <E> void addWithoutSubsets(final Set<Set<E>> sets,
                final Set<E> set) {
            for(final Set<E> other : sets)
                if(isSubset(set, other))
                    return;
            
            final Iterator<Set<E>> i = sets.iterator();
            while(i.hasNext())
                if(isSubset(i.next(), set))
                    i.remove();
            
            sets.add(set);
        }
        
        private <E> void addWithoutSupersets(final Set<Set<E>> sets,
                final Set<E> set) {
            for(final Set<E> other : sets)
                if(isSubset(other, set))
                    return;
            
            final Iterator<Set<E>> i = sets.iterator();
            while(i.hasNext())
                if(isSubset(set, i.next()))
                    i.remove();
            
            sets.add(set);
        }
        
        private <E> boolean isSubset(final Set<E> set, final Set<E> other) {
            for(final E e : set)
                if(!other.contains(e))
                    return false;
            return true;
        }
        
        private Set<RegionPair> parRegionsFromSpec(final List<Integer> regions) {
            final Set<RegionPair> specPairs = new HashSet<>();
            for(final int[] group : spec)
                specPairs.addAll(pairs(asList(group)));
            
            final Set<RegionPair> parPairs;
            if(seq) {
                /* Specification contains sequential regions */
                parPairs = pairs(regions);
                parPairs.removeAll(specPairs);
            }
            else
                /* Specification contains parallel regions */
                parPairs = specPairs;
            return parPairs;
        }
        
        private Set<RegionPair> pairs(final List<Integer> group) {
            final Set<RegionPair> result = new HashSet<RegionPair>();
            for(int i = 0; i < group.size(); i++)
                for(int j = 0; j < i; j++)
                    result.add(new RegionPair(group.get(i), group.get(j)));
            return result;
        }
        
        private List<Integer> asList(final int[] is) {
            return new AbstractList<Integer>() {
                @Override
                public Integer get(final int i) {
                    return is[i];
                }
                
                @Override
                public int size() {
                    return is.length;
                }
            };
        }
    }
    
    private static class RegionPair implements Comparable<RegionPair> {
        
        private final int first;
        private final int second;
        
        public RegionPair(final int first, final int second) {
            if(first < second) {
                this.first = first;
                this.second = second;
            }
            else {
                this.first = second;
                this.second = first;
            }
        }
        
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + first;
            result = prime * result + second;
            return result;
        }
        
        @Override
        public boolean equals(final Object obj) {
            if(this == obj)
                return true;
            if(obj == null)
                return false;
            if(!(obj instanceof RegionPair))
                return false;
            final RegionPair other = (RegionPair) obj;
            if(first != other.first)
                return false;
            if(second != other.second)
                return false;
            return true;
        }
        
        @Override
        public String toString() {
            return "(" + first + ", " + second + ")";
        }
        
        public int compareTo(final RegionPair other) {
            final int compareFirst = Integer.compare(first, other.first);
            if(compareFirst != 0)
                return compareFirst;
            else
                return Integer.compare(second, other.second);
        }
    }
    
    protected static enum VerifyMode {
        CORRECTNESS,
        PARALLELISM;
        
        @Override
        public String toString() {
            return name().toLowerCase().replace('_', ' ');
        }
    }
}

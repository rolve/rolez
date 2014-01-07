package ch.trick17.simplejpf.test;

import static org.junit.Assert.fail;
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.Verify;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
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
     * Verifies that there is a thread interleaving for which the
     * {@link #region(int) regions} in the calling method may be executed in
     * parallel, according to the given "parallel groups". More precisely, this
     * method verifies that there is at least one (single) thread interleaving,
     * for which it holds that for each parallel group, all regions in the group
     * are executed in parallel.
     * 
     * @param parGroups
     *            The parallel groups, as <code>int</code>-arrays.
     * @return <code>true</code> if being executed in JPF, <code>false</code>
     *         otherwise
     * @throws AssertionError
     *             If the given parallelism specification is not met
     * @see JpfTest
     */
    protected boolean verifyParallelism(final int[]... parGroups) {
        verifyParallelism = true;
        if(Verify.isRunningInJPF())
            return true;
        else {
            final Set<Set<ParRegionPair>> parRegionSets = computeParRegions();
            sets: for(final Set<ParRegionPair> set : parRegionSets) {
                for(final int[] group : parGroups) {
                    for(final ParRegionPair pair : allPairs(group)) {
                        if(!set.contains(pair)) {
                            /* Fail: try next interleaving */
                            continue sets;
                        }
                    }
                }
                /*
                 * Specification was met for this interleaving, return
                 * immediately:
                 */
                return false;
            }
            fail("JPF could not find an interleaving with the specified parallelism");
            return false;
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
     *            {@link #verifyParallelism(int[][])} is called.
     * @param parGroups
     *            The parallel groups specification
     * @return <code>true</code> if being executed in JPF, <code>false</code>
     *         otherwise
     * @throws AssertionError
     *             According to the called <code>verify</code> method
     */
    protected boolean verify(final VerifyMode mode, final int[]... parGroups) {
        if(mode == VerifyMode.PARALLELISM)
            return verifyParallelism(parGroups);
        else
            return verifyNoPropertyViolation();
    }
    
    private static Iterable<ParRegionPair> allPairs(final int[] group) {
        final Set<ParRegionPair> result = new HashSet<ParRegionPair>();
        for(int i = 0; i < group.length; i++)
            for(int j = 0; j < i; j++)
                result.add(new ParRegionPair(group[i], group[j]));
        return result;
    }
    
    /**
     * Runs the current test method in JPF and records the regions that are
     * executed in parallel.
     * 
     * @return A set that contains, for each possible thread interleaving, the
     *         set of pairs of regions that were being executed in parallel.
     */
    private Set<Set<ParRegionPair>> computeParRegions() {
        final ParListener l = new ParListener();
        runJpf(l);
        
        final Set<Set<ParRegionPair>> parRegions = l.getPossibleParRegions();
        
        final StringBuilder msg = new StringBuilder(
                "Possible parallel regions:");
        for(final Set<ParRegionPair> set : parRegions) {
            final ArrayList<ParRegionPair> list = new ArrayList<>(set);
            Collections.sort(list);
            msg.append("\n    ").append(list);
        }
        LoggerFactory.getLogger(JpfParallelismTest.class).info(msg.toString());
        
        return parRegions;
    }
    
    private final class ParListener extends ListenerAdapter {
        
        private final String testMethod;
        private final Set<Set<ParRegionPair>> possibleParRegions = new HashSet<>();
        
        ParListener() {
            testMethod = getCaller();
        }
        
        @Override
        public void methodExited(final VM vm, final ThreadInfo thread,
                final MethodInfo method) {
            if(isTestMethod(method)) {
                final int testRef = thread.getTopFrame().getThis();
                final ElementInfo instance = vm.getHeap().get(testRef);
                final String eventsString = ((ElementInfo) instance
                        .getFieldValueObject("regionEvents")).asString();
                
                if(eventsString.isEmpty())
                    addWithoutSubsets(possibleParRegions,
                            new HashSet<ParRegionPair>());
                else {
                    final String[] events = eventsString
                            .split("(?<!^)(?=[SE])");
                    
                    final Set<ParRegionPair> parRegions = new HashSet<>();
                    final Deque<Integer> currentRegions = new ArrayDeque<>();
                    for(final String event : events) {
                        final int id = Integer.parseInt(event.substring(1));
                        if(event.charAt(0) == 'S') {
                            for(final Integer other : currentRegions)
                                parRegions.add(new ParRegionPair(other, id));
                            currentRegions.addFirst(id);
                        }
                        else {
                            assert event.charAt(0) == 'E';
                            final boolean removed = currentRegions.remove(id);
                            assert removed;
                        }
                    }
                    addWithoutSubsets(possibleParRegions, parRegions);
                }
            }
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
        
        private <E> boolean isSubset(final Set<E> set, final Set<E> other) {
            for(final E e : set)
                if(!other.contains(e))
                    return false;
            return true;
        }
        
        private boolean isTestMethod(final MethodInfo method) {
            return method.getClassName().equals(
                    JpfParallelismTest.this.getClass().getName())
                    && method.getName().equals(testMethod);
        }
        
        Set<Set<ParRegionPair>> getPossibleParRegions() {
            return Collections.unmodifiableSet(possibleParRegions);
        }
    }
    
    private static class ParRegionPair implements Comparable<ParRegionPair> {
        
        private final int first;
        private final int second;
        
        public ParRegionPair(final int first, final int second) {
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
            if(!(obj instanceof ParRegionPair))
                return false;
            final ParRegionPair other = (ParRegionPair) obj;
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
        
        public int compareTo(final ParRegionPair other) {
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

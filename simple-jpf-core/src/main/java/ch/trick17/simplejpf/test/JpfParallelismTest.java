package ch.trick17.simplejpf.test;

import static org.junit.Assert.fail;
import gov.nasa.jpf.Error;
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.Verify;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public abstract class JpfParallelismTest extends JpfUnitTest {
    
    private String regionEvents = "";
    
    protected final void region(final int id) {
        synchronized(this) {
            final String event = "S" + id;
            if(regionEvents.contains(event))
                throw new IllegalArgumentException(
                        "duplicate region identifier: " + id);
            regionEvents += event;
        }
        
        synchronized(this) {
            regionEvents += "E" + id;
        }
    }
    
    protected boolean verifyParallelism(final int[][] parGroups) {
        if(Verify.isRunningInJPF())
            return true;
        else {
            final Set<Set<ParRegionPair>> parRegionSets = computeParRegions();
            
            for(final int[] group : parGroups) {
                pairs: for(final ParRegionPair pair : allPairs(group)) {
                    for(final Set<ParRegionPair> set : parRegionSets)
                        if(set.contains(pair))
                            continue pairs;
                    fail(String
                            .format("JPF could not execute regions %d and %d in parallel.",
                                    pair.first, pair.second));
                }
            }
            return false;
        }
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
        final ParListener listener = new ParListener();
        
        final Error error = runJpf(listener).getLastError();
        if(error != null)
            fail("JPF found unexpected errors: " + error.getDescription());
        
        return listener.getParRegions();
    }
    
    private final class ParListener extends ListenerAdapter {
        
        private final String testMethod;
        private final Set<String> eventStrings = new HashSet<>();
        
        ParListener() {
            testMethod = getCaller();
        }
        
        @Override
        public void methodExited(final VM vm, final ThreadInfo thread,
                final MethodInfo method) {
            if(isTestMethod(method)) {
                final int testRef = thread.getTopFrame().getThis();
                final ElementInfo instance = vm.getHeap().get(testRef);
                final ElementInfo events = (ElementInfo) instance
                        .getFieldValueObject("regionEvents");
                eventStrings.add(events.asString());
            }
        }
        
        private boolean isTestMethod(final MethodInfo method) {
            return method.getClassName().equals(
                    JpfParallelismTest.this.getClass().getName())
                    && method.getName().equals(testMethod);
        }
        
        Set<Set<ParRegionPair>> getParRegions() {
            final Set<Set<ParRegionPair>> result = new HashSet<>();
            for(final String string : eventStrings) {
                if(!string.isEmpty()) {
                    final String[] events = string.split("(?<!^)(?=[SE])");
                    
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
                    result.add(parRegions);
                }
            }
            return result;
        }
    }
    
    private static class ParRegionPair {
        
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
            return "[" + first + " -> " + second + "]";
        }
    }
    
}

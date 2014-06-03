package ch.trick17.peppl.lib;

import static java.util.Collections.newSetFromMap;
import static org.junit.Assert.assertEquals;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;

import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.LockSupport;

import ch.trick17.peppl.lib.SliceRange;
import ch.trick17.peppl.lib.TestSlice.Op;
import ch.trick17.simplejpf.SimpleJpf;

public class BuggyArrayGuarding {
    
    static final String DFSEARCH = "gov.nasa.jpf.search.DFSearch";
    static final String INTERLEAVING =
            "gov.nasa.jpf.search.heuristic.Interleaving";
    
    public static void main(final String[] args) {
        Config.enableLogging(true);
        final Config config = SimpleJpf.createConfig();
        config.setTarget(Test.class.getName());
        
        final Map<String, String> results = new LinkedHashMap<>();
        for(final String search : new String[]{DFSEARCH /* , INTERLEAVING */}) {
            config.setProperty("search.class", search);
            config.printEntries();
            
            final JPF jpf = new JPF(config);
            jpf.run();
            if(jpf.foundErrors())
                results.put(search, jpf.getLastError().getDescription());
            else
                results.put(search, "no errors");
        }
        
        System.out.println();
        for(final Entry<String, String> result : results.entrySet())
            System.out.println(result.getKey() + ": " + result.getValue());
    }
    
    /* Test class */
    
    public static class Test {
        public static void main(final String[] args) {
            final Thread main = Thread.currentThread();
            
            final int[] data = new int[]{0, 1, 2};
            final TestSlice a = new TestSlice(SliceRange.forArray(data), data);
            final TestSlice slice1 = a.slice(0, 2, 1);
            final TestSlice slice2 = a.slice(1, 3, 1);
            
            final TestSlice.Op share = new TestSlice.Op() {
                public void process(final TestSlice slice) {
                    slice.sharedCount.incrementAndGet();
                }
            };
            slice1.processViews(share, newIdentitySet());
            
            new Thread() {
                @Override
                public void run() {
                    assertEquals(1, slice1.data[1]);
                    
                    for(final TestSlice s : slice1.subslices)
                        s.sharedCount.decrementAndGet();
                    LockSupport.unpark(main);
                }
            }.start();
            
            for(final TestSlice s : slice2.subslices)
                while(s.sharedCount.get() > 0)
                    LockSupport.park();
            
            slice2.data[1] = 2;
        }
    }
    
    private static Set<TestSlice> newIdentitySet() {
        return newSetFromMap(new IdentityHashMap<TestSlice, Boolean>());
    }
}

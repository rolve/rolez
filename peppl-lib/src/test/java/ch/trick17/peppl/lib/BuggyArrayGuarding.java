package ch.trick17.peppl.lib;

import static org.junit.Assert.assertEquals;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import ch.trick17.peppl.lib.SomeClasses.Int;
import ch.trick17.peppl.lib.guard.Array;
import ch.trick17.peppl.lib.guard.IntArray;
import ch.trick17.peppl.lib.guard.IntSlice;
import ch.trick17.peppl.lib.guard.Slice;
import ch.trick17.simplejpf.SimpleJpf;

public class BuggyArrayGuarding {
    
    static final String DFSEARCH = "gov.nasa.jpf.search.DFSearch";
    static final String INTERLEAVING =
            "gov.nasa.jpf.search.heuristic.Interleaving";
    
    public static void main(final String[] args) {
        Config.enableLogging(true);
        final Config config = SimpleJpf.createConfig();
        config.setTarget(Test2.class.getName());
        
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
    
    public static class Test1 {
        public static void main(final String[] args) {
            final Array<Int> a = new Array<>(new Int[3]);
            for(int i = 0; i < a.data.length; i++)
                a.data[i] = new Int(i);
            
            final Slice<Int> slice1 = a.slice(0, 2, 1);
            final Slice<Int> slice2 = a.slice(1, 3, 1);
            slice1.share();
            new Thread() {
                @Override
                public void run() {
                    assertEquals(1, slice1.data[1].value);
                    slice1.releaseShared();
                }
            }.start();
            
            slice2.guardReadWrite();
            slice2.data[1] = new Int(100);
        }
    }
    
    public static class Test2 {
        public static void main(final String[] args) {
            final IntArray a = new IntArray(0, 1, 2);
            
            final IntSlice slice1 = a.slice(0, 2, 1);
            final IntSlice slice2 = a.slice(1, 3, 1);
            slice1.share();
            new Thread() {
                @Override
                public void run() {
                    assertEquals(1, slice1.data[1]);
                    slice1.releaseShared();
                }
            }.start();
            
            slice2.guardReadWrite();
            slice2.data[1] = 2;
        }
    }
}

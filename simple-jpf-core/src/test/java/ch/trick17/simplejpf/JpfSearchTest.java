package ch.trick17.simplejpf;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.LockSupport;

import ch.trick17.simplejpf.test.JpfTest;

public class JpfSearchTest {
    
    private static final String DFSEARCH = "gov.nasa.jpf.search.DFSearch";
    private static final String INTERLEAVING = "gov.nasa.jpf.search.heuristic.Interleaving";
    
    public static void main(final String[] args) throws IOException {
        Config.enableLogging(true);
        final Config config = JPF.createConfig(new String[0]);
        config.load(JpfTest.class.getResourceAsStream("jpf.properties"));
        config.setProperty("classpath", System.getProperty("java.class.path"));
        config.setTarget(Test2.class.getName());
        
        final Map<String, String> results = new LinkedHashMap<>();
        for(final String search : new String[]{DFSEARCH, INTERLEAVING}) {
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
            final Thread main = Thread.currentThread();
            final Int i = new Int();
            
            i.blocked = true;
            new Thread() {
                @Override
                public void run() {
                    i.value++;
                    i.blocked = false;
                    LockSupport.unpark(main);
                }
            }.start();
            
            while(i.blocked)
                LockSupport.park();
            
            i.blocked = true;
            new Thread() {
                @Override
                public void run() {
                    i.value++;
                    i.blocked = false;
                    LockSupport.unpark(main);
                }
            }.start();
            
            while(i.blocked)
                LockSupport.park();
            
            if(i.value != 2)
                throw new AssertionError();
        }
        
        static class Int {
            volatile Boolean blocked = false;
            int value;
        }
    }
    
    public static class Test2 {
        
        public static void main(final String[] args) {
            final Owned o = new Owned();
            
            o.prevOwners.addFirst(o.owner);
            o.owner = null;
            new Thread() {
                @Override
                public void run() {
                    o.owner = Thread.currentThread();
                    
                    o.prevOwners.addFirst(o.owner);
                    o.owner = null;
                    new Thread() {
                        @Override
                        public void run() {
                            o.owner = o.prevOwners.removeFirst();
                            LockSupport.unpark(o.owner);
                        }
                    }.start();
                    
                    while(o.owner != Thread.currentThread())
                        LockSupport.park();
                    
                    o.owner = o.prevOwners.removeFirst();
                    LockSupport.unpark(o.owner);
                }
            }.start();
            
            while(o.owner != Thread.currentThread())
                LockSupport.park();
        }
        
        static class Owned {
            volatile Thread owner = Thread.currentThread();
            final Deque<Thread> prevOwners = new ArrayDeque<Thread>();
        }
    }
}

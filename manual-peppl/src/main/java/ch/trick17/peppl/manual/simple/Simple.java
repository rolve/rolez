package ch.trick17.peppl.manual.simple;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.Callable;

import ch.trick17.peppl.lib.Mutable;
import ch.trick17.peppl.lib.TaskSystem;
import ch.trick17.peppl.lib._Mutable;

/**
 * This is the main task which is run when the program begins execution. The
 * compiler adds a bootstrapping main-Method to the main-Task, so the JVM can
 * start it.
 * 
 * @author Michael Faes
 */
public class Simple implements Callable<Void> {
    
    private static final TaskSystem S = TaskSystem.getDefault();
    
    public static void main(final String[] args) {
        // Compiler generates the bootstrapping code:
        S.run(new Simple()).get();
        // get() Propagates exceptions to the main thread
    }
    
    @Override
    public Void call() {
        final @Mutable Container c = new Container();
        
        int i = c.get(); // No guard required, static analysis finds
                         // that read access is available
        
        c.set(i + 10); // No guard required, static analysis finds
                       // that write access is available
        
        // A task that requires write access to container is run. Programmer
        // writes "ReadWriteTask(c);", compiler generates the following:
        c.pass();
        S.run(new ReadWriteTask(c));
        
        // c is now inaccessible
        doSomeWork();
        
        c.guardReadWrite(); // read or write access might not be
                            // available: compiler adds guard
        i = c.get();
        assertEquals(20, i);
        
        c.set(i + 10); // No guard required, static analysis finds that
                       // access is already guarded above
        
        // A task that only requires read access is run.
        c.share();
        S.run(new ReadTask(c));
        
        i = c.get(); // No guard required, static analysis finds that
                     // read access is available
        assertEquals(30, i);
        
        randomMethod(c); // No guard required, method handles guarding
        
        c.guardReadWrite(); // write access may not be available:
                            // compiler adds guard
        c.set(c.get() + 10);
        
        assertEquals(40, c.get());
        return null;
    }
    
    public void randomMethod(final Container c) {
        System.gc(); // Since we have a potentially time-consuming call here,
                     // compiler does not make the method unguarded.
        
        c.guardRead(); // read access might not be available:
                       // compiler adds guard
        final int i = c.get();
        assertEquals(30, i);
    }
    
    private static class ReadWriteTask implements Callable<Void> {
        
        private final @_Mutable Container c;
        
        // Programmer declares task parameter "c" mutable
        public ReadWriteTask(@Mutable final Container c) {
            this.c = c;
        }
        
        @Override
        public Void call() {
            c.registerNewOwner(); // added by the compiler
            
            final int i = c.get(); // No guard required, static analysis
                                   // finds that read access is available
            assertEquals(10, i);
            
            c.set(i + 10); // No guard required, static analysis
                           // finds that write access is available
            
            c.releasePassed(); // added by the compiler
            
            doSomeWork();
            return null;
        }
    }
    
    private static class ReadTask implements Callable<Void> {
        
        private final Container c;
        
        // Programmer (implicitly) declares task parameter "c" shared
        public ReadTask(final Container c) {
            this.c = c;
        }
        
        @Override
        public Void call() {
            final int i = c.get(); // No guard required, static analysis
                                   // finds that read access is available
            assertEquals(30, i);
            
            c.releaseShared(); // added here, static analysis
                               // finds that c is not used anymore
            
            doSomeWork();
            return null;
        }
    }
    
    private static void doSomeWork() {
        try {
            Thread.sleep(1000);
        } catch(final InterruptedException e) {
            // Ignore
        }
    }
}

package ch.trick17.peppl.manual.simple;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import ch.trick17.peppl.lib.Guardian;
import ch.trick17.peppl.lib.Mutable;
import ch.trick17.peppl.lib.TaskSystem;
import ch.trick17.peppl.lib._Mutable;

/**
 * The main task is run when the program begins execution. The compiler adds a
 * bootstrapping main-Method to the main-Task, so the JVM can start it.
 * 
 * @author Michael Faes
 */
public class MainTask implements Callable<Void> {
    
    public static void main(final String[] args) {
        // Compiler generates the bootstrapping code:
        TaskSystem.get().runTask(new MainTask());
    }
    
    @Override
    public Void call() {
        final @Mutable Container c = new Container();
        
        int i = c.get(); // No guard required, static analysis finds
                         // that read access is available
        
        c.set(i + 10); // No guard required, static analysis finds
                       // that write access is available
        
        // A task that requires write access to container is run. Programmer
        // writes "HelperTask(c);", compiler generates the following:
        Guardian.get().pass(c);
        TaskSystem.get().runTask(new ReadWriteTask(c));
        
        // c is now inaccessible
        doSomeWork();
        
        Guardian.get().guardReadWrite(c); // read or write access might not be
                                          // available: compiler adds guard
        i = c.get();
        
        c.set(i + 10); // No guard required, static analysis finds that
                       // access is already guarded above
        
        // A task that only requires read access is run.
        // No guard is required, static analysis finds that (read) access is
        // already guarded above
        Guardian.get().share(c);
        TaskSystem.get().runTask(new ReadTask(c));
        
        i = c.get(); // No guard required, static analysis finds that
                     // read access is available
        
        randomMethod(c); // No guard required, method handles guarding
        
        Guardian.get().guardReadWrite(c); // write access may not be available:
                                          // compiler adds guard
        c.set(c.get() + 10);
        
        System.out.println("MainTask: " + c.get());
        return null;
    }
    
    public void randomMethod(final Container c) {
        System.gc(); // Since we have a potentially time-consuming call here,
                     // compiler does not make the method unguarded.
        
        Guardian.get().guardRead(c); // read access might not be available:
                                     // compiler adds guard
        final int i = c.get();
        
        System.out.println("randomMethod: " + i); // Non-deterministic call
    }
    
    private static class ReadWriteTask implements Callable<Void> {
        
        private final @_Mutable Container c;
        
        // Programmer declares task parameter "c" mutable
        public ReadWriteTask(@Mutable final Container c) {
            this.c = c;
        }
        
        @Override
        public Void call() {
            Guardian.get().registerNewOwner(c); // added by the compiler
            
            final int i = c.get(); // No guard required, static analysis
                                   // finds that read access is available
            
            c.set(i + 10); // No guard required, static analysis
                           // finds that write access is available
            
            Guardian.get().releasePassed(c); // added by the compiler
            
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
            
            Guardian.get().releaseShared(c); // added here, static analysis
                                             // finds that c is not used anymore
            
            doSomeWork();
            
            System.out.println("ReadTask: " + i); // Non-deterministic call
            return null;
        }
    }
    
    private static final AtomicInteger workCounter = new AtomicInteger(0);
    
    private static void doSomeWork() {
        final int workUnit = workCounter.incrementAndGet();
        System.out.println("Doing work " + workUnit);
        try {
            Thread.sleep(1000);
        } catch(final InterruptedException e) {
            // Ignore
        }
        System.out.println("Work " + workUnit + " done.");
    }
}

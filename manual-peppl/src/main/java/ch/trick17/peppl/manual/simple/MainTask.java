package ch.trick17.peppl.manual.simple;

import java.util.concurrent.atomic.AtomicInteger;

import ch.trick17.peppl.manual.lib.Guardian;
import ch.trick17.peppl.manual.lib.Mutable;
import ch.trick17.peppl.manual.lib.Task;
import ch.trick17.peppl.manual.lib.TaskSystem;
import ch.trick17.peppl.manual.lib._Mutable;

/**
 * The main task is run when the program begins execution. The compiler adds a
 * bootstrapping main-Method to the main-Task, so the JVM can start it.
 * 
 * @author Michael Faes
 */
public class MainTask extends Task<Void> {
    
    public static void main(final String[] args) {
        // Compiler generates the bootstrapping code:
        TaskSystem.runTask(new MainTask());
    }
    
    @Override
    protected Void compute() {
        final @Mutable Container c = new Container();
        
        int i = c.get(); // No guard required, static analysis finds
                         // that read access is available
        
        c.set(i + 10); // No guard required, static analysis finds
                       // that write access is available
        
        // A task that requires write access to container is run. Programmer
        // writes "HelperTask(c);", compiler generates the following:
        final Task<Void> task1 = new ReadWriteTask(c);
        Guardian.pass(c);
        TaskSystem.runTask(task1);
        
        // c is now inaccessible
        doSomeWork();
        
        Guardian.guardReadWrite(c); // read or write access might not be
                                    // available: compiler adds guard
        i = c.get();
        
        c.set(i + 10); // No guard required, static analysis finds that
                       // access is already guarded above
        
        // A task that only requires read access is run.
        // No guard is required, static analysis finds that (read) access is
        // already guarded above
        final Task<Void> task2 = new ReadTask(c);
        Guardian.share(c);
        TaskSystem.runTask(task2);
        
        i = c.get(); // No guard required, static analysis finds that
                     // read access is available
        
        randomMethod(c); // No guard required, method handles guarding
        
        Guardian.guardReadWrite(c); // write access may not be available:
                                    // compiler adds guard
        c.set(c.get() + 10);
        
        System.out.println("MainTask: " + c.get());
        return null;
    }
    
    public void randomMethod(final Container c) {
        System.gc(); // Since we have a potentially time-consuming call here,
                     // compiler does not make the method unguarded.
        
        Guardian.guardRead(c); // read access might not be available:
                               // compiler adds guard
        final int i = c.get();
        
        System.out.println("randomMethod: " + i); // Non-deterministic call
    }
    
    private static class ReadWriteTask extends Task<Void> {
        
        private final @_Mutable Container c;
        
        // Programmer declares task parameter "c" mutable
        public ReadWriteTask(@Mutable final Container c) {
            this.c = c;
        }
        
        @Override
        protected Void compute() {
            Guardian.registerNewOwner(c); // added by the compiler
            
            final int i = c.get(); // No guard required, static analysis
                                   // finds that read access is available
            
            c.set(i + 10); // No guard required, static analysis
                           // finds that write access is available
            
            Guardian.releasePassed(c); // added by the compiler
            
            doSomeWork();
            return null;
        }
    }
    
    private static class ReadTask extends Task<Void> {
        
        private final Container c;
        
        // Programmer (implicitly) declares task parameter "c" shared
        public ReadTask(final Container c) {
            this.c = c;
        }
        
        @Override
        protected Void compute() {
            final int i = c.get(); // No guard required, static analysis
                                   // finds that read access is available
            
            Guardian.releaseShared(c); // added here, static analysis finds that
                                       // c is not used anymore
            
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

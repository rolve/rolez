package ch.trick17.peppl.manual.simple;

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
    public Void call() {
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
        
        Guardian.guardReadWrite(c); // read or write access might not be
                                    // available: compiler adds guard
        i = c.get();
        
        c.set(i + 10); // No guard required, static analysis finds that
                       // access is already guarded above
        
        // A task that only requires read access is run.
        final Task<Void> task2 = new ReadTask(c);
        Guardian.share(c);
        TaskSystem.runTask(task2);
        
        i = c.get(); // No guard required, static analysis finds that
                     // read access is available
        
        doSomething(c); // No guard required, method handles guarding
        
        System.out.println(i); // Non-deterministic call
        return null;
    }
    
    public void doSomething(final Container c) {
        System.gc(); // Since we have a potentially time-consuming call here,
                     // compiler does not make the method unguarded.
        
        Guardian.guardRead(c); // read access might not be available:
                               // compiler adds guard
        final int i = c.get();
        
        System.out.println(i); // Non-deterministic call
    }
    
    private static class ReadWriteTask extends Task<Void> {
        
        private final @_Mutable Container c;
        
        // Programmer declares task parameter "c" mutable
        public ReadWriteTask(@Mutable final Container c) {
            this.c = c;
        }
        
        @Override
        public Void call() {
            Guardian.usePassed(c); // added by the compiler
            
            final int i = c.get(); // No guard required, static analysis
                                   // finds that read access is available
            
            c.set(i + 10); // No guard required, static analysis
                           // finds that write access is available
            
            Guardian.release(c); // added by the compiler
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
        public Void call() {
            Guardian.useShared(c); // added by the compiler
            
            final int i = c.get(); // No guard required, static analysis
                                   // finds that read access is available
            
            Guardian.release(c); // added here, static analysis finds that c
                                 // is not used anymore
            
            System.out.println(i); // Non-deterministic call
            return null;
        }
    }
}

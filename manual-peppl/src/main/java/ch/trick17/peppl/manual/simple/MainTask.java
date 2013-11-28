package ch.trick17.peppl.manual.simple;

import ch.trick17.peppl.manual.lib.Mutable;
import ch.trick17.peppl.manual.lib.PepplSystem;
import ch.trick17.peppl.manual.lib.Task;
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
        PepplSystem.runVoidTask(new MainTask());
    }
    
    @Override
    public Void call() {
        final @Mutable Container c = new Container();
        
        int i = c.get(); // No guard required, static analysis infers
                         // that read access is available
        
        c.set(i + 10); // No guard required, static analysis infers
                       // that write access is available
        
        // A task that requires write access to container is run. Programmer
        // writes "HelperTask(c);", compiler generates the following:
        final Task<Void> task1 = new ReadWriteTask(c);
        PepplSystem.pass(this, task1, c);
        PepplSystem.runVoidTask(task1);
        
        // c is now inaccessible
        
        PepplSystem.guardReadWrite(c); // read or write access might not be
                                       // available: compiler adds guard
        i = c.get();
        
        c.set(i + 10); // No guard required, static analysis infers that
                       // access is already guarded above
        
        // A task that only requires read access is run.
        final Task<Void> task2 = new ReadTask(c);
        PepplSystem.share(this, task2, c);
        PepplSystem.runVoidTask(task2);
        
        i = c.get(); // No guard required, static analysis infers that
                     // read access is available
        
        System.out.println(i); // Non-deterministic call
        return null;
    }
    
    private class ReadWriteTask extends Task<Void> {
        
        private final @_Mutable Container container;
        
        // Programmer declares task parameter "c" mutable
        public ReadWriteTask(@Mutable final Container c) {
            this.container = c;
        }
        
        @Override
        public Void call() {
            final int i = container.get();
            container.set(i + 10);
            return null;
        }
    }
    
    private class ReadTask extends Task<Void> {
        
        private final Container container;
        
        // Programmer (implicitly) declares task parameter "c" shared
        public ReadTask(final Container c) {
            this.container = c;
        }
        
        @Override
        public Void call() {
            final int i = container.get(); // No check necessary, container is
                                           // guaranteed to be shared in
            
            System.out.println(i); // Non-deterministic call
            return null;
        }
    }
}

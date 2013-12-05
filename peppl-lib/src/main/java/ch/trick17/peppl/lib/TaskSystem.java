package ch.trick17.peppl.lib;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class TaskSystem {
    
    public <V> Task<V> runTask(final Callable<V> callable) {
        final Task<V> task = new Task<>(callable);
        new Thread(task).start();
        return task;
    }
    
    /*
     * Global default task system
     */
    
    private static final TaskSystem defaultSystem = new TaskSystem();
    
    public static TaskSystem get() {
        return defaultSystem;
    }
    
    public class Task<V> extends FutureTask<V> {
        
        public Task(final Callable<V> callable) {
            super(callable);
        }
        
        @Override
        public V get() {
            try {
                while(true)
                    try {
                        return super.get();
                    } catch(final InterruptedException e) {
                        // Ignore
                    }
            } catch(final ExecutionException e) {
                final Throwable cause = e.getCause();
                if(cause instanceof RuntimeException)
                    throw (RuntimeException) cause;
                else if(cause instanceof Error)
                    throw (Error) cause;
                else
                    throw new AssertionError("Checked exception in task", cause);
            }
        }
    }
}

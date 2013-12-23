package ch.trick17.peppl.lib;

import java.io.Serializable;
import java.util.concurrent.Callable;

public abstract class TaskSystem implements Serializable {
    
    public Task<Void> run(final Runnable runnable) {
        final Task<Void> task = new Task<>(runnable);
        doRun(task);
        return task;
    }
    
    public <V> Task<V> run(final Callable<V> callable) {
        final Task<V> task = new Task<>(callable);
        doRun(task);
        return task;
    }
    
    protected abstract void doRun(final Task<?> task);
    
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
    
    /*
     * Global default task system
     */
    
    private static final TaskSystem defaultSystem = new NewThreadTaskSystem();
    
    public static TaskSystem getDefault() {
        return defaultSystem;
    }
}

package rolez.lang;

import java.io.Serializable;
import java.util.concurrent.Callable;

public abstract class TaskSystem implements Serializable {
    
    public <V> Task<V> start(final Callable<V> callable) {
        final Task<V> task = new Task<>(callable);
        doStart(task);
        return task;
    }
    
    public <V> V run(final Callable<V> callable) {
        Task<V> task = new Task<>(callable);
        task.run();
        /* Propagate exceptions */
        return task.get();
    }
    
    abstract void doStart(final Task<?> task);
    
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
    
    /* Global default task system */
    
    private static final TaskSystem defaultSystem = new NewThreadTaskSystem();
    
    public static TaskSystem getDefault() {
        return defaultSystem;
    }
}

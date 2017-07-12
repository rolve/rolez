package rolez.lang;

import java.io.Serializable;

public abstract class TaskSystem implements Serializable {
    
    public <V> Task<V> start(Task<V> task) {
        doStart(task);
        return task;
    }
    
    public <V> V run(Task<V> task) {
        task.run();
        /* Propagate exceptions */
        return task.get();
    }
    
    abstract void doStart(Task<?> task);
    
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

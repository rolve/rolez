package rolez.lang;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.Callable;

public abstract class TaskSystem implements Serializable {
    
    /**
     * Keeps track of the tasks that are executed within a thread. Whenever a new task is started,
     * it is added as a child to current, i.e., the top task on the stack.
     */
    final transient ThreadLocal<Deque<Task<?>>> localStack = new ThreadLocal<Deque<Task<?>>>() {
        @Override
        protected Deque<Task<?>> initialValue() {
            return new ArrayDeque<>();
        }
    };
    // IMPROVE: Could replace thread-local variable with "current" task variable
    // that is passed to all generated methods. Wouldn't support call-backs
    // from mapped methods though...
    
    public <V> Task<V> start(final Callable<V> callable) {
        return start(new Task<>(callable, this));
    }
    
    public <V> V run(final Callable<V> callable) {
        Task<V> task = new Task<>(callable, this);
        task.run();
        /* Propagate exceptions */
        return task.get();
    }
    
    private <V> Task<V> start(final Task<V> task) {
        final Task<?> current = localStack.get().peek();
        if(current != null)
            current.addChild(task);
        doStart(task);
        return task;
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

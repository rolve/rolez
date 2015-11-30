package ch.trick17.rolez.lang.task;

import java.io.Serializable;
import java.util.Deque;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;

public abstract class TaskSystem implements Serializable {
    
    final transient ThreadLocal<Deque<Task<?>>> localStack = new ThreadLocal<Deque<Task<?>>>() {
        @Override
        protected Deque<Task<?>> initialValue() {
            return new ConcurrentLinkedDeque<Task<?>>();
        }
    };
    
    public Task<Void> start(final Runnable runnable) {
        return start(new Task<Void>(runnable, this));
    }
    
    public <V> Task<V> start(final Callable<V> callable) {
        return start(new Task<>(callable, this));
    }
    
    public void run(final Runnable runnable) {
        new Task<>(runnable, this).run();
    }
    
    public <V> void run(final Callable<V> callable) {
        new Task<>(callable, this).run();
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
    
    private static final TaskSystem defaultSystem = new ThreadPoolTaskSystem();
    
    public static TaskSystem getDefault() {
        return defaultSystem;
    }
}

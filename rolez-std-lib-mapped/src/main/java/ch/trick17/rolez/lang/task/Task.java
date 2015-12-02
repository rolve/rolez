package ch.trick17.rolez.lang.task;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

public class Task<V> implements Runnable {
    
    private final TaskSystem taskSystem;
    private Callable<V> callable;
    private final Sync sync = new Sync();
    
    private volatile V result;
    private volatile Throwable exception;
    
    /**
     * The list of child tasks. Before a task finishes, it waits for all its
     * children to finish.
     */
    private final List<Task<?>> childTasks = new ArrayList<>();
    
    void addChild(final Task<?> task) {
        childTasks.add(task);
    }
    
    Task(final Callable<V> callable, final TaskSystem system) {
        this.callable = callable;
        this.taskSystem = system;
    }
    
    public final void run() {
        Deque<Task<?>> localStack = taskSystem.localStack.get();
        localStack.push(this);
        try {
            result = callable.call();
            /* Wait for child tasks to finish so that exceptions get propagated
             * up the task stack */
            for(final Task<?> task : childTasks)
                task.get();
        } catch(Throwable e) {
            /* Uncomment below to print the exception as soon as the task is
             * finished, in case the parent task does not finish (e.g. because
             * of a deadlock) and the exception is not propagated. */
            // e.printStackTrace();
            result = null;
            exception = e;
        }
        localStack.pop();
        callable = null;
        
        /* Unblock threads waiting to get the result */
        sync.done();
    }
    
    public final V get() {
        /* Block until task is done */
        sync.awaitDone();
        
        Throwable e = exception;
        if(e == null)
            return result;
        else if(e instanceof RuntimeException)
            throw(RuntimeException) e;
        else if(e instanceof Error)
            throw(Error) e;
        else
            throw new AssertionError("Checked exception in task", e);
    }
    
    /**
     * Implements the blocking of threads waiting to get the result of a task.
     * 
     * @author Michael Faes
     */
    private static class Sync extends AbstractQueuedSynchronizer {
        
        private static final int RUNNING = -1;
        private static final int DONE = 1;
        
        Sync() {
            setState(RUNNING);
        }
        
        void awaitDone() {
            acquireShared(IGNORED);
        }
        
        void done() {
            releaseShared(IGNORED);
        }
        
        @Override
        protected int tryAcquireShared(int ignored) {
            return getState();
        }
        
        @Override
        protected boolean tryReleaseShared(int ignored) {
            setState(DONE);
            return true;
        }
        
        private static final int IGNORED = 0;
    }
}

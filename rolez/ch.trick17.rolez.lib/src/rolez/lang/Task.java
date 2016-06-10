package rolez.lang;

import static java.lang.Thread.currentThread;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

public class Task<V> implements Runnable {
    /**
     * Keeps track of the tasks that are executed within a thread. Whenever a new task is started,
     * it is added as a child to current, i.e., the top task on the stack.
     */
    private static final transient ThreadLocal<Deque<Task<?>>> localStack = new ThreadLocal<Deque<Task<?>>>() {
        @Override
        protected Deque<Task<?>> initialValue() {
            return new ArrayDeque<>();
        }
    };
    // IMPROVE: Could replace thread-local variable with "current" task variable
    // that is passed to all generated methods. Wouldn't support call-backs
    // from mapped methods though...
    
    public static Task<?> currentTask() {
        return localStack.get().peek();
    }
    
    private Callable<V> callable;
    private final Sync sync = new Sync();
    
    private volatile V result;
    private volatile Throwable exception;
    
    /**
     * The list of child tasks. Before a task finishes, it waits for all its children to finish.
     */
    private final List<Task<?>> children = new ArrayList<>();
    
    Task(Callable<V> callable) {
        this.callable = callable;
        Task<?> parent = currentTask();
        if(parent != null)
            parent.children.add(this);
    }
    
    public final void run() {
        Deque<Task<?>> stack = localStack.get();
        stack.push(this);
        try {
            result = callable.call();
            /* Wait for child tasks to finish so that exceptions get propagated up the task stack */
            for(final Task<?> task : children)
                task.get();
        } catch(Throwable e) {
            /* Uncomment the following to print the exception as soon as the task is finished, in
             * case the parent task does not finish (e.g. because of a deadlock) and the exception
             * is not propagated. */
            java.lang.System.err.print("[" + currentThread().getName() + "] ");
            e.printStackTrace();
            result = null;
            exception = e;
        }
        stack.pop();
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

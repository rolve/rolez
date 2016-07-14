package rolez.lang;

import static java.lang.Thread.currentThread;
import static java.util.concurrent.locks.LockSupport.unpark;

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
    // IMPROVE: Could replace thread-local variable with "current" task variable that is passed to
    // all generated methods. Wouldn't support call-backs from mapped methods though...
    
    // IMPROVE: To speed this up, could cache current task in a field of a custom thread class or so
    public static Task<?> currentTask() {
        return localStack.get().peek();
    }
    
    private Callable<V> callable;
    private final Sync sync = new Sync();
    
    private Thread executingThread;
    private volatile V result;
    private volatile Throwable exception;
    
    /**
     * The parent task. Parent links are followed to efficiently detect a parent-child relation
     * between two tasks.
     * <p>
     * The "main" task has no parent, i.e., its parent field is <code>null</code> .
     */
    public final Task<?> parent;
    
    /**
     * The list of child tasks. Before a task finishes, it waits for all its children to finish.
     */
    private final List<Task<?>> children = new ArrayList<>();
    
    public Task(Callable<V> callable) {
        this.callable = callable;
        this.parent = currentTask();
        if(parent != null)
            parent.children.add(this);
    }
    
    public final void run() {
        executingThread = currentThread();
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
        if(parent != null)
            unpark(parent.executingThread);
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
    
    boolean isActive() {
        return !sync.isDone();
    }
    
    boolean isDescendantOf(Task<?> other) {
        for(Task<?> ancestor = parent; ancestor != null; ancestor = ancestor.parent)
            if(ancestor == other)
                return true;
        return false;
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
        
        boolean isDone() {
            return getState() == DONE;
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

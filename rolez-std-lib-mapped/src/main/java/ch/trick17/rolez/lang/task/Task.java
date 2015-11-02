package ch.trick17.rolez.lang.task;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class Task<V> extends FutureTask<V> {
    
    /* IMPROVE: Do not inherit from FutureTask, as it does a lot of additional
     * things we do not need. */
    
    private final TaskSystem taskSystem;
    private final Set<Task<?>> childTasks = new HashSet<>();
    
    Task(final Callable<V> callable, final TaskSystem system) {
        super(callable);
        this.taskSystem = system;
    }
    
    Task(final Runnable runnable, final TaskSystem system) {
        super(runnable, null);
        this.taskSystem = system;
    }
    
    @Override
    public final void run() {
        taskSystem.localStack.get().push(this);
        super.run();
    }
    
    @Override
    protected final void done() {
        /* Remove task from local task stack */
        taskSystem.localStack.get().pop();
        
        /* Print the exception as soon as the task is finished, in case the
         * parent task does not finish (e.g. because of a deadlock) and the
         * exception is not propagated. */
        try {
            super.get();
        } catch(final ExecutionException e) {
            e.getCause().printStackTrace();
        } catch(final InterruptedException e) {
            /* Not possible, task has already finished */
            throw new AssertionError(e);
        }
        
        /* Wait for child tasks to finish so that exceptions get propagated up
         * the task stack */
        for(final Task<?> task : childTasks)
            task.get();
    }
    
    @Override
    public final V get() {
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
    
    void addChild(final Task<?> task) {
        childTasks.add(task);
    }
}

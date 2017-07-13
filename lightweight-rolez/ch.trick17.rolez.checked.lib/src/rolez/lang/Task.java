package rolez.lang;

import static java.lang.Thread.currentThread;
import static java.util.Collections.newSetFromMap;
import static java.util.concurrent.locks.LockSupport.unpark;
import static rolez.lang.Guarded.guardReadWrite;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

public abstract class Task<V> implements Runnable {
    
    private static final transient ThreadLocal<Task<?>> currentTask = new ThreadLocal<Task<?>>();
    
    // IMPROVE: Could replace thread-local variable with "current" task variable that is passed to
    // all generated methods. Callbacks from mapped methods could go to a "bridge" method that gets
    // the current task from the thread local. Bridge methods only necessary for methods that
    // override a mapped method (including equals(), hashCode(), toString()).
    
    // IMPROVE: To speed this up, could cache current task in a field of a custom thread class or so
    public static Task<?> currentTask() {
        return currentTask.get();
    }
    
    private static final BitSet usedTaskIds = new BitSet();
    
    /**
     * Returns the next unused task ID and checks that the max number of task IDs is not exceeded.
     * At the moment, there can be at most 64 tasks running at the same time, because a bit set
     * consisting of a single "long" field is used for efficient guarding.
     */
    private static int getUnusedTaskId() {
        synchronized(usedTaskIds) {
            int id = usedTaskIds.nextClearBit(0);
            if(id >= 64)
                throw new AssertionError("too many tasks, maximum is 64");
            usedTaskIds.set(id);
            return id;
        }
    }
    
    private static void releaseTaskId(int id) {
        synchronized(usedTaskIds) {
            usedTaskIds.clear(id);
        }
    }
    
    private final int id = getUnusedTaskId();
    private final Sync sync = new Sync();
    
    private Thread executingThread;
    private volatile V result;
    private volatile Throwable exception;
    
    private List<Guarded> passed;
    /**
     * Contains all objects that are reachable from any passed one when the task starts. At the end
     * of the task, the object graphs may have changed, so some objects may not be reachable
     * anymore. To be able to release those, they are stored here.
     */
    private Set<Guarded> passedReachable;
	private Set<Guarded> sharedReachable;
    
    public Set<Guarded> getPassedReachable() {
        return passedReachable;
    }

    public Set<Guarded> getSharedReachable() {
        return sharedReachable;
    }
    
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
    
    public Task(Object[] passedObjects, Object[] sharedObjects, Object[] pureObjects) {
        this.parent = currentTask();
        if(parent != null)
            parent.children.add(this);
        
        taskStartTransitions(passedObjects, sharedObjects, pureObjects);
    }
    
    /**
     * Executes this task in the current thread. This method first performs some initialization,
     * then performs the {@linkplain #runRolez() computation}, waits for child tasks to finish, and
     * finally wakes up the thread that is executing the parent task.
     */
    public final void run() {
        executingThread = currentThread();
        Task<?> prevTask = currentTask.get();
        currentTask.set(this);
        completeTaskStartTransitions();
        try {
            result = runRolez();
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
        taskFinishTransitions();
        releaseTaskId(id);
        currentTask.set(prevTask);
        
        /* Unblock threads waiting to get the result. Note that not only the parent task, but also
         * tasks started by the parent tasks may have a reference to this task. */
        sync.done();
        if(parent != null)
            unpark(parent.executingThread);
    }
    
    /**
     * Runs the Rolez code of this task. This method is implemented by subclasses that are usually
     * generated by the Rolez compiler.
     */
    protected abstract V runRolez();
    
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
    
    public boolean isDescendantOf(Task<?> other) {
        for(Task<?> ancestor = parent; ancestor != null; ancestor = ancestor.parent)
            if(ancestor == other)
                return true;
        return false;
    }
    
    Thread getExecutingThread() {
        return executingThread;
    }
    
    public long idBits() {
        return 1L << id;
    }
    
    /**
     * Creates a new task and registers it as the {@linkplain #currentTask() currently executing
     * task}. Subsequent Rolez code will behave as if it was executed in that task (but in the
     * current thread!). This method is only intended for specific situations, where Rolez code is
     * invoked from Java and creating a task object from a {@link Callable} is not possible.
     */
    public static void registerNewRootTask() {
        assert currentTask.get() == null;
        Task<Void> task = new Task<Void>(new Object[]{}, new Object[]{}, new Object[]{}) {
            @Override
            protected Void runRolez() {
                return null;
            }
        };
        task.executingThread = currentThread();
        currentTask.set(task);
    }
    
    /**
     * Unregisters a previously {@linkplain #registerNewRootTask() registered root task}. Subsequent
     * Rolez code may not work correctly as there will be no {@linkplain #currentTask() currently
     * executing task}.
     */
    public static void unregisterRootTask() {
        assert currentTask.get().parent == null;
        releaseTaskId(currentTask.get().id);
        currentTask.set(null);
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
    
    /* Transitions */
    
    private void taskStartTransitions(Object[] passedObjects, Object[] sharedObjects, Object[] pureObjects) {
        passed = new ArrayList<>(passedObjects.length);
        for(Object g : passedObjects)
            if(g instanceof Guarded)
                passed.add((Guarded) g);
        
        long idBits = parent == null ? 0L : parent.idBits();
        passedReachable = newIdentitySet();
        for(Guarded g : passed)
            g.guardReadWriteReachable(passedReachable, idBits);
        
        // Objects that are reachable both from a passed and a shared object are effectively *passed*
        sharedReachable = newIdentitySet();
        sharedReachable.addAll(passedReachable);
        for(Object g : sharedObjects)
            if(g instanceof Guarded)
                ((Guarded) g).guardReadOnlyReachable(sharedReachable, idBits);
        sharedReachable.removeAll(passedReachable);
        
        //TODO: What has to be done with pure objects?
        
        /* IMPROVE: Only pass (share) objects that are reachable through chain of readwrite
         * (readonly) references? Would enable programmers to express more parallelism (especially
         * with parameterized classes) and could be more efficient (or less...). */
        for(Guarded g : passedReachable)
            g.pass(this);
        for(Guarded g : sharedReachable)
            g.share(this);
        for(Object g : pureObjects) {
        	if (g instanceof Guarded)
        	((Guarded)g).sharePure(this);
        }
    }
    
    private void completeTaskStartTransitions() {
        for(Guarded g : passedReachable)
            g.completePass();
    }
    
    private void taskFinishTransitions() {
        /* Release all shared objects. No need for guarding, as it's not possible that they have
         * been modified. */
        for(Guarded g : sharedReachable)
            g.releaseShared();
        
        /* Then, find objects that are now reachable from passed objects (and the result object) and
         * release those */
        // IMPROVE: guarding should not be necessary since child tasks are already joined!
        Set<Guarded> newPassedReachable = newIdentitySet();
        for(Guarded g : passed)
            g.guardReadWriteReachable(newPassedReachable, idBits());
        if(result instanceof Guarded)
            ((Guarded) result).guardReadWriteReachable(newPassedReachable, idBits());
        
        for(Guarded g : newPassedReachable)
            g.releasePassed();
        
        /* Finally, release objects that were previously reachable (but not anymore) and notify
         * parent thread. */
        passedReachable.removeAll(newPassedReachable);
        for(Guarded g : passedReachable) {
            guardReadWrite(g);
            g.releasePassed();
        }
        if(parent != null)
            unpark(parent.executingThread);
        
        /* Clear fields to allow task args to be GC'd */
        passed = null;
        passedReachable = null;
        sharedReachable = null;
    }
    
    private static Set<Guarded> newIdentitySet() {
        return newSetFromMap(new IdentityHashMap<Guarded, java.lang.Boolean>());
    }
}
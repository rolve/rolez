package rolez.lang;

import static java.lang.Thread.currentThread;
import static java.util.concurrent.locks.LockSupport.unpark;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

public abstract class Task<V> implements Runnable {

    private static final transient ThreadLocal<Task<?>> currentTask = new ThreadLocal<Task<?>>();

    public static Task<?> currentTask() {
        return currentTask.get();
    }

    private static final BitSet usedTaskIds = new BitSet();
    private static final Task<?>[] registeredTasks = new Task<?>[64];

    /**
     * Returns the next unused task ID and checks that the max number of task IDs is not exceeded.
     * At the moment, there can be at most 64 tasks running at the same time, because a bit set
     * consisting of a single "long" field is used for efficient guarding.
     */
    private static int registerTask(Task<?> task) {
        synchronized(usedTaskIds) {
            int id = usedTaskIds.nextClearBit(0);
            if(id >= 64)
                throw new AssertionError("too many tasks, maximum is 64");
            usedTaskIds.set(id);
            registeredTasks[id] = task;
            return id;
        }
    }

    private static void unregisterTask(int id) {
        synchronized(usedTaskIds) {
            usedTaskIds.clear(id);
            // no need to clear registeredTasks[id] (I think), will be overwritten anyway
        }
    }

    static Task<?> withId(int id) {
        return registeredTasks[id];
    }

    public final int id = registerTask(this);
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
    private List<Guarded> passedReachable;
    private List<Guarded> sharedReachable;

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

    public Task(Object[] passedObjects, Object[] sharedObjects) {
        this.parent = currentTask();
        if(parent != null)
            parent.children.add(this);

        taskStartTransitions(passedObjects, sharedObjects);
    }

    // TODO: Improve eager interference code and adapt this constructor
    public Task(Set<Guarded> passedObjects, Set<Guarded> passedReachable, Set<Guarded> sharedReachable) {
        this.parent = currentTask();
        if(parent != null)
            parent.children.add(this);

        this.passedReachable = new ArrayList<>(passedReachable);
        this.sharedReachable = new ArrayList<>(sharedReachable);

        passed = new ArrayList<>(passedObjects.size());
        for(Guarded g : passedObjects)
            passed.add(g);

        for(Guarded g : passedReachable)
            g.pass(this);
        for(Guarded g : sharedReachable)
            g.share(this);
    }

    /**
     * Executes this task in the current thread. This method first performs some initialization,
     * then performs the {@linkplain #runRolez() computation}, waits for child tasks to finish, and
     * finally wakes up the thread that is executing the parent task.
     */
    public final void run() {
        executingThread = currentThread();
        currentTask.set(this);
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
        unregisterTask(id);
        currentTask.set(parent);

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
            throw (RuntimeException) e;
        else if(e instanceof Error)
            throw (Error) e;
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

    Thread getExecutingThread() {
        return executingThread;
    }

    public long idBits() {
        return idBitsFor(id);
    }

    public static long idBitsFor(int id) {
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
        Task<Void> task = new Task<Void>(new Object[]{}, new Object[]{}) {
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
        unregisterTask(currentTask.get().id);
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

    // TODO: taskStartTransitions version without guarding for when there are no child tasks

    private void taskStartTransitions(Object[] passedObjects, Object[] sharedObjects) {
        passed = new ArrayList<>(passedObjects.length);
        for(Object g : passedObjects)
            if(g instanceof Guarded)
                passed.add((Guarded) g);

        long idBits = parent == null ? 0L : parent.idBits();
        passedReachable = new ArrayList<>(passed.size() * 16);
        for(Guarded g : passed)
            guardAndPassReachable(g, passedReachable, idBits);

        sharedReachable = new ArrayList<>(sharedObjects.length * 16);
        for(Object g : sharedObjects)
            if(g instanceof Guarded)
                guardAndShareReachable((Guarded) g, sharedReachable, idBits);
    }

    private void guardAndPassReachable(Guarded guarded, List<Guarded> collect,
            long currentTaskIdBits) {
        if(!guarded.ownedBy(this)) {
            guarded.guardReadWrite(currentTaskIdBits);
            guarded.pass(this);
            collect.add(guarded);
            for(Object g : guarded.guardedRefs())
                if(g instanceof Guarded)
                    guardAndPassReachable(((Guarded) g), collect, currentTaskIdBits);
        }
    }

    private void guardAndShareReachable(Guarded guarded, List<Guarded> collect,
            long currentTaskIdBits) {
        // Objects that are reachable both from a passed and a shared object
        // are effectively *passed*, so skip these here
        if(!guarded.ownedByOrSharedWith(this)) {
            guarded.guardReadOnly(currentTaskIdBits);
            guarded.share(this);
            collect.add(guarded);
            for(Object g : guarded.guardedRefs())
                if(g instanceof Guarded)
                    guardAndShareReachable(((Guarded) g), collect, currentTaskIdBits);
        }
    }

    private void taskFinishTransitions() {
        // No guarding is necessary anywhere here, because child tasks
        // have already finished

        // Release all shared objects
        for(Guarded g : sharedReachable)
            g.releaseShared(this);

        // Then, find objects that are now reachable from passed objects
        // (and the result object) and release those
        for(Guarded g : passed)
            releasePassedReachable(g);
        if(result instanceof Guarded)
            releasePassedReachable((Guarded) result);

        // Finally, release objects that were previously reachable
        // and notify parent thread
        for(Guarded g : passedReachable)
            if(g.ownedBy(this))
                g.releasePassed(parent);

        if(parent != null)
            unpark(parent.executingThread);

        /* Clear fields to allow task args to be GC'd */
        passed = null;
        passedReachable = null;
        sharedReachable = null;
    }

    private void releasePassedReachable(Guarded guarded) {
        if(guarded.ownedBy(this)) {
            guarded.releasePassed(parent);
            for(Object g : guarded.guardedRefs())
                if(g instanceof Guarded)
                    releasePassedReachable(((Guarded) g));
        }
    }
}
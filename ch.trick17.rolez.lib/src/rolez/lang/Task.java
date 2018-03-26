package rolez.lang;

import static java.lang.Long.numberOfLeadingZeros;
import static java.lang.Thread.currentThread;
import static java.util.concurrent.locks.LockSupport.unpark;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
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
    private final List<Task<?>> children;

    /**
     * @param dummy only used for overloading 
     */
    private Task(Object[] passedObjects, Object[] sharedObjects, int dummy) {
        this.parent = currentTask();
        this.children = new ArrayList<>();
        if(parent != null)
            parent.children.add(this);

        passed = new ArrayList<>(passedObjects.length);
        for(Object g : passedObjects)
            if(g instanceof Guarded)
                passed.add((Guarded) g);

        passedReachable = new ArrayList<>(passed.size() * 16);
        sharedReachable = new ArrayList<>(sharedObjects.length * 16);
    }

    /**
     * Constructor WITHOUT guarding and WITHOUT interference check. Used for
     * normal task starts where there are no child tasks. Note that the
     * guarding does not take place, no matter the value of <code>unguarded</code>.
     * 
     * @param unguarded just there for overloading
     */
    public Task(Object[] passedObjects, Object[] sharedObjects, boolean unguarded) {
        this(passedObjects, sharedObjects, 0);

        long myBits = idBits();
        long parentBits = parent == null ? 0 : parent.idBits();
        for(Guarded g : passed)
            passReachable(g, myBits);
        for(Object g : sharedObjects)
            if(g instanceof Guarded)
                shareReachable((Guarded) g, myBits, parentBits);
    }

    /**
     * Constructor WITH guarding and WITHOUT interference check. Used for
     * normal task starts where there could be child tasks.
     */
    public Task(Object[] passedObjects, Object[] sharedObjects) {
        this(passedObjects, sharedObjects, 0);

        long myBits = idBits();
        long parentBits = parent == null ? 0 : parent.idBits();
        for(Guarded g : passed)
            passReachableGuarded(g, myBits, parentBits);
        for(Object g : sharedObjects)
            if(g instanceof Guarded)
                shareReachableGuarded((Guarded) g, myBits, parentBits);
    }

    /**
     * Constructor WITHOUT guarding and WITH interference check. Used for
     * parallel-and blocks and parfor loops. Note that the guarding does
     * not take place, no matter the value of <code>unguarded</code>.
     * 
     * @param unguarded just there for overloading
     */
    public Task(Object[] passedObjects, Object[] sharedObjects,
            long otherTaskBits, boolean unguarded) {
        this(passedObjects, sharedObjects, 0);

        long myBits = idBits();
        long parentBits = parent == null ? 0 : parent.idBits();
        for(Guarded g : passed)
            passReachableChecked(g, myBits, parentBits, otherTaskBits);
        for(Object g : sharedObjects)
            if(g instanceof Guarded)
                shareReachableChecked((Guarded) g, myBits,
                        parentBits, otherTaskBits);
    }

    /**
     * Constructor WITH guarding and WITH interference check. Used for
     * parallel-and blocks and parfor loops.
     */
    public Task(Object[] passedObjects, Object[] sharedObjects, long otherTaskBits) {
        this(passedObjects, sharedObjects, 0);

        long myBits = idBits();
        long parentBits = parent == null ? 0 : parent.idBits();
        for(Guarded g : passed)
            passReachableGuardedChecked(g, myBits, parentBits, otherTaskBits);
        for(Object g : sharedObjects)
            if(g instanceof Guarded)
                shareReachableGuardedChecked((Guarded) g, myBits,
                        parentBits, otherTaskBits);
    }

    private void passReachable(Guarded guarded, long myBits) {
        if(!guarded.ownedBy(myBits)) {
            guarded.pass(myBits);
            passedReachable.add(guarded);
            for(Object g : guarded.guardedRefs())
                if(g instanceof Guarded)
                    passReachable((Guarded) g, myBits);
        }
    }

    private void shareReachable(Guarded guarded, long myBits, long parentBits) {
        // Objects that are reachable both from a passed and a shared object
        // are effectively *passed*, so skip these here
        if(!guarded.ownedByOrSharedWith(myBits)) {
            guarded.share(myBits, parentBits);
            sharedReachable.add(guarded);
            for(Object g : guarded.guardedRefs())
                if(g instanceof Guarded)
                    shareReachable((Guarded) g, myBits, parentBits);
        }
    }

    private void passReachableGuarded(Guarded guarded, long myBits, long parentBits) {
        if(!guarded.ownedBy(myBits)) {
            guarded.guardPass(parentBits, myBits);
            guarded.pass(myBits);
            passedReachable.add(guarded);
            for(Object g : guarded.guardedRefs())
                if(g instanceof Guarded)
                    passReachableGuarded((Guarded) g, myBits, parentBits);
        }
    }

    private void shareReachableGuarded(Guarded guarded, long myBits, long parentBits) {
        // Objects that are reachable both from a passed and a shared object
        // are effectively *passed*, so skip these here
        if(!guarded.ownedByOrSharedWith(myBits)) {
            guarded.guardShare(parentBits, myBits);
            guarded.share(myBits, parentBits);
            sharedReachable.add(guarded);
            for(Object g : guarded.guardedRefs())
                if(g instanceof Guarded)
                    shareReachableGuarded((Guarded) g, myBits, parentBits);
        }
    }

    private void passReachableChecked(Guarded guarded, long myBits,
            long parentBits, long otherTaskBits) {
        if(!guarded.ownedBy(myBits)) {
            guarded.checkInterferesRw(myBits, otherTaskBits);
            guarded.pass(myBits);
            passedReachable.add(guarded);
            for(Object g : guarded.guardedRefs())
                if(g instanceof Guarded)
                    passReachableGuarded((Guarded) g, myBits, parentBits);
        }
    }

    private void shareReachableChecked(Guarded guarded, long myBits,
            long parentBits, long otherTaskBits) {
        // Objects that are reachable both from a passed and a shared object
        // are effectively *passed*, so skip these here
        if(!guarded.ownedByOrSharedWith(myBits)) {
            guarded.checkInterferesRo(myBits, otherTaskBits);
            guarded.share(myBits, parentBits);
            sharedReachable.add(guarded);
            for(Object g : guarded.guardedRefs())
                if(g instanceof Guarded)
                    shareReachableGuarded((Guarded) g, myBits, parentBits);
        }
    }

    private void passReachableGuardedChecked(Guarded guarded, long myBits,
            long parentBits, long otherTaskBits) {
        if(!guarded.ownedBy(myBits)) {
            guarded.checkInterferesRw(myBits, otherTaskBits);
            guarded.guardPass(parentBits, myBits);
            guarded.pass(myBits);
            passedReachable.add(guarded);
            for(Object g : guarded.guardedRefs())
                if(g instanceof Guarded)
                    passReachableGuarded((Guarded) g, myBits, parentBits);
        }
    }

    private void shareReachableGuardedChecked(Guarded guarded, long myBits,
            long parentBits, long otherTaskBits) {
        // Objects that are reachable both from a passed and a shared object
        // are effectively *passed*, so skip these here
        if(!guarded.ownedByOrSharedWith(myBits)) {
            guarded.checkInterferesRo(myBits, otherTaskBits);
            guarded.guardShare(parentBits, myBits);
            guarded.share(myBits, parentBits);
            sharedReachable.add(guarded);
            for(Object g : guarded.guardedRefs())
                if(g instanceof Guarded)
                    shareReachableGuarded((Guarded) g, myBits, parentBits);
        }
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

    static long idBitsFor(int id) {
        return 1L << id;
    }

    static int idForBits(long bits) {
        return 63 - numberOfLeadingZeros(bits);
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

    private void taskFinishTransitions() {
        // No guarding is necessary anywhere here, because child tasks
        // have already finished
        long myBits = idBits();
        long parentBits = parent == null ? 0 : parent.idBits();

        // Release all shared objects
        for(Guarded g : sharedReachable)
            g.releaseShared(myBits);

        // Then, find objects that are now reachable from passed objects
        // (and the result object) and release those
        for(Guarded g : passed)
            releasePassedReachable(g, myBits, parentBits);
        if(result instanceof Guarded)
            releasePassedReachable((Guarded) result, myBits, parentBits);

        // Finally, release objects that were previously reachable
        // and notify parent thread
        for(Guarded g : passedReachable) {
            if(g.ownedBy(myBits)) {
                g.releasePassed(parentBits);
            }
        }

        if(parent != null)
            unpark(parent.executingThread);

        /* Clear fields to allow task args to be GC'd */
        passed = null;
        passedReachable = null;
        sharedReachable = null;
    }

    private static void releasePassedReachable(Guarded guarded,
            long myBits, long parentBits) {
        if(guarded.ownedBy(myBits)) {
            guarded.releasePassed(parentBits);
            for(Object g : guarded.guardedRefs())
                if(g instanceof Guarded)
                    releasePassedReachable((Guarded) g, myBits, parentBits);
        }
    }
}
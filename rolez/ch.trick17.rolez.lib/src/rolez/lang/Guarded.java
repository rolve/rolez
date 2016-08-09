package rolez.lang;

import static java.lang.Thread.currentThread;
import static java.util.Collections.emptyList;
import static java.util.Collections.newSetFromMap;
import static java.util.concurrent.locks.LockSupport.park;
import static java.util.concurrent.locks.LockSupport.unpark;
import static rolez.lang.Task.currentTask;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Superclass of all guarded objects
 * 
 * @author Michael Faes
 */
public abstract class Guarded {
    
    private volatile Task<?> owner; // volatile, so tasks stuck in a guarding op don't read stale info
    private volatile Thread ownerThread; // same
    private Deque<Thread> prevOwnerThreads;
    
    private AtomicInteger sharedCount; // atomic because tasks can share concurrently
    private Set<Task<?>> readers;
    
    private Deque<Set<Guarded>> prevReachables; // IMPROVE: Could we use weak refs here?
    
    /**
     * Default constructor that does not initialize guarding. Initialization happens lazily, when
     * the object first changes its role. This can improve performance significantly, because any
     * guarding op returns immediately if guarding is not initialized.
     */
    protected Guarded() {}
    
    /**
     * Constructor that <strong>does</strong> initialize guarding, if the given argument is
     * <code>true</code>. Required for object with views, because they can change their effective
     * role without being passed or shared themselves, so lazy initialization doesn't work for them.
     */
    protected Guarded(boolean initializeGuarding) {
        if(initializeGuarding)
            initializeGuarding();
    }
    
    private void initializeGuarding() {
        owner = currentTask();
        ownerThread = currentThread();
        sharedCount = new AtomicInteger(0);
        readers = newSetFromMap(new ConcurrentHashMap<Task<?>, java.lang.Boolean>()); // IMPROVE: Initialize later?
        prevReachables = new ArrayDeque<>();
    }
    
    private boolean guardingInitialized() {
        return owner != null;
    }
    
    /* Role transitions */
    
    private static abstract class Op {
        abstract void process(Guarded guarded);
    }
    
    public final void share(final Task<?> task) {
        processAll(GUARD_READ_ONLY, newIdentitySet());
        
        Op share = new Op() {
            @Override
            public void process(Guarded g) {
                if(!g.guardingInitialized())
                    g.initializeGuarding();
                // IMPROVE: Use Unsafe CAS or, once Java 9 is out (:D), standard API to
                // CAS an int field directly
                g.sharedCount.incrementAndGet();
                if(g.viewLock() != null) {
                    TaskSystem.getDefault();
                    g.readers.add(task);
                }
            }
        };
        processAll(share, newIdentitySet());
    }
    
    public final void pass(final Task<?> task) {
        processAll(GUARD_READ_WRITE, newIdentitySet());
        
        /* Previous owner is only recorded in the root of the object tree... */
        final Thread prevOwner = currentThread();
        if(prevOwnerThreads == null)
            prevOwnerThreads = new ArrayDeque<>();
        prevOwnerThreads.push(prevOwner);
        
        Op pass = new Op() {
            @Override
            public void process(Guarded g) {
                if(!g.guardingInitialized())
                    g.initializeGuarding();
                /* First step of passing */
                g.owner = task;
                g.ownerThread = null;
            }
        };
        
        Set<Guarded> processed = newIdentitySet();
        processAll(pass, processed);
        prevReachables.addFirst(processed);
    }
    
    public final void completePass() {
        processAll(REGISTER_OWNER_THREAD, newIdentitySet());
    }
    
    private static final Op REGISTER_OWNER_THREAD = new Op() {
        @Override
        public void process(Guarded g) {
            if(!g.guardingInitialized())
                g.initializeGuarding();
            assert g.ownerThread == null;
            /* Second step of passing */
            g.ownerThread = currentThread();
        }
    };
    
    public final void releaseShared() {
        processAll(RELEASE_SHARED, newIdentitySet());
    }
    
    private static final Op RELEASE_SHARED = new Op() {
        @Override
        public void process(Guarded g) {
            if(!g.guardingInitialized())
                g.initializeGuarding();
            int count = g.sharedCount.decrementAndGet();
            if(g.viewLock() != null) {
                TaskSystem.getDefault();
                boolean removed = g.readers.remove(currentTask());
                assert removed;
            }
            if(count == 0)
                unpark(g.ownerThread);
        }
    };
    
    public final void releasePassed() {
        /* First, make sure all reachable and previously reachable objects are readwrite */
        Set<Guarded> guardProcessed = newIdentitySet();
        processAll(GUARD_READ_WRITE, guardProcessed);
        processReachables(GUARD_READ_WRITE, guardProcessed);
        
        /* Then, release (or, in the case of newly reachable objects, transfer) reachable objects */
        final Thread parentThread = prevOwnerThreads.pop();
        Op releasePassed = new Op() {
            @Override
            public void process(final Guarded g) {
                if(!g.guardingInitialized())
                    g.initializeGuarding();
                g.owner = g.owner.parent;
                g.ownerThread = parentThread;
            }
        };
        Set<Guarded> processed = newIdentitySet();
        processAll(releasePassed, processed);
        
        /* Then, release rest of the originally reachable object */
        processReachables(releasePassed, processed);
        prevReachables.removeFirst();
        
        /* Finally, notify parent thread in case it is waiting */
        unpark(parentThread);
    }
    
    /* Guarding methods. The public ones are static so that they can return the guarded object with
     * the precise type (which is not possible with instance methods, due to the lack of self
     * types). This simplifies code generation a lot, since guarding can be done within an
     * expression. */
    
    public static <G extends Guarded> G guardReadOnly(G guarded) {
        GUARD_READ_ONLY.process(guarded);
        return guarded;
    }
    
    private static final Op GUARD_READ_ONLY = new Op() {
        @Override
        public void process(Guarded guarded) {
            /* mayRead() reads the volatile owner field written by releasePassed() and
             * releaseShared(). Therefore, there is a happens-before relationship between
             * releasePassed()/releaseShared() and guardRead(). */
            while(!guarded.mayRead())
                park();
        }
    };
    
    public static <G extends Guarded> G guardReadWrite(G guarded) {
        GUARD_READ_WRITE.process(guarded);
        return guarded;
    }
    
    private static final Op GUARD_READ_WRITE = new Op() {
        @Override
        public void process(Guarded guarded) {
            /* mayWrite() reads the volatile owner field written by releasePassed(). Therefore,
             * there is a happens-before relationship between releasePassed() and guardReadWrite(). */
            while(!guarded.mayWrite())
                park();
        }
    };
    
    /* The following two are required for expressions of type java.lang.Object, for which it is only
     * known at runtime whether guarding is needed */
    
    public static <G> G guardReadOnlyIfNeeded(G guarded) {
        if(guarded instanceof Guarded)
            guardReadOnly((Guarded) guarded);
        return guarded;
    }
    
    public static <G> G guardReadWriteIfNeeded(G guarded) {
        if(guarded instanceof Guarded)
            guardReadWrite((Guarded) guarded);
        return guarded;
    }
    
    /* Methods that can be overridden by concrete Guarded classes */
    
    /**
     * Returns all references to mutable and therefore guarded objects that are reachable from this.
     * This may exclude internal objects of the Rolez library.
     * <p>
     * This implementation returns an empty iterable.
     * 
     * @return All references to mutable objects reachable from this. To simplify the implementation
     *         of this method, the {@link Iterable} may return references to non-guarded objects or
     *         even <code>null</code>s.
     */
    protected Iterable<?> guardedRefs() {
        return emptyList();
    }
    
    /**
     * Returns all other views of this object, i.e., objects that provide access to (some of) the
     * data that this object provides access to. Note that the view relation is symmetric: iff A is
     * a view of B, B is a view of A.
     * <p>
     * This implementation returns an empty collection, meaning that by default, guarded objects
     * don't have any other views.
     * 
     * @return All other views of this object. To simplify the implementation of this method, the
     *         {@link Collection} may contain <code>null</code> references.
     */
    protected Collection<? extends Guarded> views() {
        return emptyList();
    }
    
    /**
     * Returns the lock that should be used to synchronize access to the {@linkplain #views() views}
     * . This can be any object, as long as it is the same for all views of some data. It goes
     * without saying that this object must not be locked for any other purpose.
     * <p>
     * If this object cannot have any other views, <code>null</code> should be returned.
     * <p>
     * The default implementation returns <code>null</code>.
     */
    protected Object viewLock() {
        return null;
    }
    
    /* Implementation methods */
    
    private void processAll(Op op, Set<Guarded> processed) {
        if(processed.add(this)) {
            /* First, process references, otherwise "parent" task may replace them */
            for(final Object ref : guardedRefs())
                if(ref instanceof Guarded)
                    ((Guarded) ref).processAll(op, processed);
            op.process(this);
        }
    }
    
    private void processReachables(Op op, Set<Guarded> processed) {
        for(Guarded guarded : prevReachables.getFirst())
            if(!processed.contains(guarded))
                op.process(guarded);
    }
    
    private boolean mayRead() {
        if(!guardingInitialized())
            return true;
        
        return sharedCount.get() > 0
                || (ownerThread == currentThread() && !descWithReadWriteViewExists());
    }
    
    /**
     * Determines if there is a descendant task of the current task that has a (overlapping)
     * readwrite view of the given object.
     */
    private boolean descWithReadWriteViewExists() {
        if(viewLock() == null)
            return false;
        // IMPROVE: Slow path in separate method?
        Task<?> currentTask = currentTask();
        synchronized(viewLock()) {
            for(Guarded v : views())
                if(v.sharedCount.get() == 0) {
                    Task<?> currentOwner = v.owner;
                    if(currentOwner.isActive() && currentOwner.isDescendantOf(currentTask))
                        return true;
                }
        }
        return false;
    }
    
    private boolean mayWrite() {
        if(!guardingInitialized())
            return true;
        
        return ownerThread == currentThread() && sharedCount.get() == 0
                && !descWithReadViewExists() && !descWithReadWriteViewExists();
        // IMPROVE: Combine above two methods for efficiency
    }
    
    /**
     * Determines if there is a descendant task of the current task that has a (overlapping) read
     * view of the given object.
     */
    private boolean descWithReadViewExists() {
        if(viewLock() == null)
            return false;
        Task<?> currentTask = currentTask();
        synchronized(viewLock()) {
            for(Guarded view : views())
                for(Task<?> reader : view.readers)
                    if(reader.isDescendantOf(currentTask))
                        return true;
        }
        return false;
    }
    
    private static Set<Guarded> newIdentitySet() {
        return newSetFromMap(new IdentityHashMap<Guarded, java.lang.Boolean>());
    }
}

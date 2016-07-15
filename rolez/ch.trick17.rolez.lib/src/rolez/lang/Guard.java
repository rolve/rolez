package rolez.lang;

import static java.lang.Thread.currentThread;
import static java.util.Collections.newSetFromMap;
import static java.util.concurrent.locks.LockSupport.park;
import static java.util.concurrent.locks.LockSupport.unpark;
import static rolez.lang.Task.currentTask;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

class Guard {
    
    private volatile Task<?> owner = currentTask(); // volatile, so tasks stuck in a guarding op don't read stale info
    private volatile Thread ownerThread = currentThread(); // same
    private Deque<Thread> prevOwnerThreads = null;
    
    private final AtomicInteger sharedCount = new AtomicInteger(0); // atomic because tasks can share concurrently
    private final Set<Task<?>> readers = newSetFromMap(
            new ConcurrentHashMap<Task<?>, java.lang.Boolean>()); // IMPROVE: Initialize lazily?
    
    private final Deque<Set<Guarded>> prevReachables = new ArrayDeque<>(); // IMPROVE: Could we use weak refs here?
    
    /* Role transition operations */
    
    static abstract class Op {
        abstract void process(Guarded guarded);
    }
    
    void share(Guarded guarded, final Task<?> task) {
        guarded.processAll(GUARD_READ_ONLY, newIdentitySet());
        
        Op share = new Op() {
            @Override
            public void process(Guarded g) {
                // IMPROVE: Use Unsafe CAS or, once Java 9 is out (:D), standard API to
                // CAS an int field directly
                g.getGuard().sharedCount.incrementAndGet();
                if(g.viewLock() != null) {
                    TaskSystem.getDefault();
                    g.getGuard().readers.add(task);
                }
            }
        };
        guarded.processAll(share, newIdentitySet());
    }
    
    void pass(Guarded guarded, final Task<?> task) {
        guarded.processAll(GUARD_READ_WRITE, newIdentitySet());
        
        /* Previous owner is only recorded in the root of the object tree... */
        final Thread prevOwner = currentThread();
        if(prevOwnerThreads == null)
            prevOwnerThreads = new ArrayDeque<>();
        prevOwnerThreads.push(prevOwner);
        
        Op pass = new Op() {
            @Override
            public void process(Guarded g) {
                /* First step of passing */
                g.getGuard().owner = task;
                g.getGuard().ownerThread = null;
            }
        };
        
        Set<Guarded> processed = newIdentitySet();
        guarded.processAll(pass, processed);
        prevReachables.addFirst(processed);
    }
    
    void completePass(Guarded guarded) {
        guarded.processAll(REGISTER_OWNER_THREAD, newIdentitySet());
    }
    
    private static final Op REGISTER_OWNER_THREAD = new Op() {
        @Override
        public void process(Guarded guarded) {
            assert guarded.getGuard().ownerThread == null;
            /* Second step of passing */
            guarded.getGuard().ownerThread = currentThread();
        }
    };
    
    void releaseShared(Guarded guarded) {
        guarded.processAll(RELEASE_SHARED, newIdentitySet());
    }
    
    private static final Op RELEASE_SHARED = new Op() {
        @Override
        public void process(Guarded guarded) {
            int count = guarded.getGuard().sharedCount.decrementAndGet();
            if(guarded.viewLock() != null) {
                TaskSystem.getDefault();
                boolean removed = guarded.getGuard().readers.remove(currentTask());
                assert removed;
            }
            if(count == 0)
                unpark(guarded.getGuard().ownerThread);
        }
    };
    
    void releasePassed(Guarded guarded) {
        /* First, make sure all reachable and previously reachable objects are readwrite */
        Set<Guarded> guardProcessed = newIdentitySet();
        guarded.processAll(GUARD_READ_WRITE, guardProcessed);
        processReachables(GUARD_READ_WRITE, guardProcessed);
        
        /* Then, release (or, in the case of newly reachable objects, transfer) reachable objects */
        final Thread parentThread = prevOwnerThreads.pop();
        Op releasePassed = new Op() {
            @Override
            public void process(final Guarded g) {
                g.getGuard().owner = g.getGuard().owner.parent;
                g.getGuard().ownerThread = parentThread;
            }
        };
        Set<Guarded> processed = newIdentitySet();
        guarded.processAll(releasePassed, processed);
        
        /* Then, release rest of the originally reachable object */
        processReachables(releasePassed, processed);
        prevReachables.removeFirst();
        
        /* Finally, notify parent thread in case it is waiting */
        unpark(parentThread);
    }
    
    /* Guarding methods */
    
    void guardReadOnly(Guarded guarded) {
        GUARD_READ_ONLY.process(guarded);
    }
    
    private static final Op GUARD_READ_ONLY = new Op() {
        @Override
        public void process(Guarded guarded) {
            /* mayRead() reads the volatile owner field written by releasePassed() and
             * releaseShared(). Therefore, there is a happens-before relationship between
             * releasePassed()/releaseShared() and guardRead(). */
            while(!mayRead(guarded))
                park();
        }
    };
    
    void guardReadWrite(Guarded guarded) {
        GUARD_READ_WRITE.process(guarded);
    }
    
    private static final Op GUARD_READ_WRITE = new Op() {
        @Override
        public void process(Guarded guarded) {
            /* mayWrite() reads the volatile owner field written by releasePassed(). Therefore,
             * there is a happens-before relationship between releasePassed() and guardReadWrite(). */
            while(!mayWrite(guarded))
                park();
        }
    };
    
    /* Implementation methods */
    
    // TODO: Redesign Guard and Guarded so that Guarded instances are less passed around explicitly
    private static boolean mayRead(Guarded g) {
        return g.getGuard().sharedCount.get() > 0
                || (g.guard.ownerThread == currentThread() && !descWithReadWriteViewExists(g));
    }
    
    /**
     * Determines if there is a descendant task of the current task that has a (overlapping)
     * readwrite view of the given object.
     */
    private static boolean descWithReadWriteViewExists(Guarded g) {
        if(g.viewLock() == null)
            return false;
        // IMPROVE: Slow path in separate method?
        Task<?> currentTask = currentTask();
        synchronized(g.viewLock()) {
            for(Guarded v : g.views())
                if(v.getGuard().sharedCount.get() == 0) {
                    Task<?> currentOwner = v.guard.owner;
                    if(currentOwner.isActive() && currentOwner.isDescendantOf(currentTask))
                        return true;
                }
        }
        return false;
    }
    
    private static boolean mayWrite(Guarded g) {
        return g.getGuard().ownerThread == currentThread() && g.getGuard().sharedCount.get() == 0
                && !descWithReadViewExists(g) && !descWithReadWriteViewExists(g);
        // IMPROVE: Combine above two methods for efficiency
    }
    
    /**
     * Determines if there is a descendant task of the current task that has a (overlapping) read
     * view of the given object.
     */
    private static boolean descWithReadViewExists(Guarded g) {
        if(g.viewLock() == null)
            return false;
        Task<?> currentTask = currentTask();
        synchronized(g.viewLock()) {
            for(Guarded view : g.views())
                for(Task<?> reader : view.getGuard().readers)
                    if(reader.isDescendantOf(currentTask))
                        return true;
        }
        return false;
    }
    
    private void processReachables(Op op, Set<Guarded> processed) {
        for(Guarded guarded : prevReachables.getFirst())
            if(!processed.contains(guarded))
                op.process(guarded);
    }
    
    private static Set<Guarded> newIdentitySet() {
        return newSetFromMap(new IdentityHashMap<Guarded, java.lang.Boolean>());
    }
}

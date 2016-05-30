package rolez.lang;

import static java.lang.Thread.currentThread;
import static java.util.concurrent.locks.LockSupport.park;
import static java.util.concurrent.locks.LockSupport.unpark;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

class Guard {
    
    private volatile Thread owner = currentThread();
    private Deque<Thread> prevOwners = null;
    // IMPROVE: Use Unsafe CAS or, once Java 9 is out (:D), standard API to CAS an int field directly
    private final AtomicInteger sharedCount = new AtomicInteger(0);
    
    private final Deque<Set<Guarded>> prevReachables = new ArrayDeque<>();
    
    void initializeViewGuard(final Guard viewGuard) {
        while(owner == null) {} // Spin; owner field is only temporarily null
        // IMPROVE: Is spinning really a good idea?
        
        viewGuard.owner = owner;
        viewGuard.prevOwners = prevOwners == null ? null : new ArrayDeque<>(prevOwners);
        viewGuard.sharedCount.set(sharedCount.get());
    }
    
    /* Object state operations */
    
    static abstract class Op {
        abstract void process(Guarded guarded);
    }
    
    void share(final Guarded guarded) {
        guarded.processAll(GUARD_READ_ONLY, newIdentitySet(), false);
        guarded.processAll(SHARE, newIdentitySet(), true);
    }
    
    private static final Op SHARE = new Op() {
        @Override
        public void process(final Guarded guarded) {
            guarded.getGuard().sharedCount.incrementAndGet();
        }
    };
    
    void pass(final Guarded guarded) {
        guarded.processAll(GUARD_READ_WRITE, newIdentitySet(), false);
        
        /* Previous owner is only recorded in the root of the object tree */
        if(prevOwners == null)
            prevOwners = new ArrayDeque<>();
        prevOwners.addFirst(currentThread());
        
        final Set<Guarded> processed = newIdentitySet();
        guarded.processAll(PASS, processed, true);
        prevReachables.addFirst(processed);
    }
    
    private static final Op PASS = new Op() {
        @Override
        public void process(final Guarded guarded) {
            /* First step of passing */
            final Guard guard = guarded.getGuard();
            guard.owner = null;
        }
    };
    
    void registerNewOwner(final Guarded guarded) {
        guarded.processAll(REGISTER_OWNER, newIdentitySet(), false);
    }
    
    private static final Op REGISTER_OWNER = new Op() {
        @Override
        public void process(final Guarded guarded) {
            final Guard guard = guarded.getGuard();
            assert guard.owner == null;
            /* Second step of passing */
            guard.owner = currentThread();
        }
    };
    
    void releaseShared(final Guarded guarded) {
        guarded.processAll(RELEASE_SHARED, newIdentitySet(), true);
    }
    
    private static final Op RELEASE_SHARED = new Op() {
        @Override
        public void process(final Guarded guarded) {
            final Guard guard = guarded.getGuard();
            final int count = guard.sharedCount.decrementAndGet();
            if(count == 0)
                unpark(guard.owner);
        }
    };
    
    void releasePassed(final Guarded guarded) {
        /* First, make sure all reachable and previously reachable objects are readwrite */
        Set<Guarded> guardProcessed = newIdentitySet();
        guarded.processAll(GUARD_READ_WRITE, guardProcessed, false);
        processReachables(GUARD_READ_WRITE, guardProcessed);
        
        /* Then, release (or, in the case of newly reachable objects, transfer) reachable objects */
        final Thread parent = prevOwners.removeFirst();
        final Op releasePassed = new Op() {
            @Override
            public void process(final Guarded g) {
                g.getGuard().owner = parent;
            }
        };
        Set<Guarded> processed = newIdentitySet();
        guarded.processAll(releasePassed, processed, true);
        
        /* Then, release rest of the originally reachable object */
        processReachables(releasePassed, processed);
        prevReachables.removeFirst();
        
        /* Finally, wake up parent thread in case it is waiting */
        unpark(parent);
    }
    
    /* Guarding methods */
    
    void guardReadOnly(final Guarded guarded) {
        GUARD_READ_ONLY.process(guarded);
        if(!guarded.views().isEmpty())
            guarded.processViews(GUARD_READ_ONLY, newIdentitySet());
    }
    
    private static final Op GUARD_READ_ONLY = new Op() {
        @Override
        public void process(final Guarded guarded) {
            /* mayRead() reads the volatile owner field written by releasePassed() and
             * releaseShared(). Therefore, there is a happens-before relationship between
             * releasePassed()/releaseShared() and guardRead(). */
            while(!guarded.getGuard().mayRead())
                park();
        }
    };
    
    void guardReadWrite(final Guarded guarded) {
        GUARD_READ_WRITE.process(guarded);
        if(!guarded.views().isEmpty())
            guarded.processViews(GUARD_READ_WRITE, newIdentitySet());
    }
    
    private static final Op GUARD_READ_WRITE = new Op() {
        @Override
        public void process(final Guarded guarded) {
            /* mayWrite() reads the volatile owner field written by releasePassed(). Therefore,
             * there is a happens-before relationship between releasePassed() and guardReadWrite(). */
            while(!guarded.getGuard().mayWrite())
                park();
        }
    };
    
    /* Implementation methods */
    
    private boolean mayRead() {
        return owner == currentThread() || sharedCount.get() > 0;
    }
    
    private boolean mayWrite() {
        return owner == currentThread() && !(sharedCount.get() > 0);
    }
    
    private void processReachables(final Op op, final Set<Guarded> processed) {
        for(final Guarded guarded : prevReachables.getFirst())
            if(!processed.contains(guarded))
                op.process(guarded);
    }
    
    private static Set<Guarded> newIdentitySet() {
        return Collections.newSetFromMap(new IdentityHashMap<Guarded, java.lang.Boolean>());
    }
}

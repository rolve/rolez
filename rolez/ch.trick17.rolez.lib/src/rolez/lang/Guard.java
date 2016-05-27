package rolez.lang;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

class Guard {
    
    private volatile Thread owner = Thread.currentThread();
    private final Deque<Thread> prevOwners = new ArrayDeque<>();
    // IMPROVE: Use Unsafe CAS or, once Java 9 is out (:D), standard API to CAS an int field directly
    private final AtomicInteger sharedCount = new AtomicInteger(0);
    
    private final Deque<Set<Guarded>> prevReachables = new ArrayDeque<>();
    
    void initializeViewGuard(final Guard viewGuard) {
        while(owner == null) {} // Spin; owner field is only temporarily null
        // IMPROVE: Is spinning really a good idea?
        viewGuard.owner = owner;
        viewGuard.prevOwners.clear();
        viewGuard.prevOwners.addAll(prevOwners);
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
            guard.prevOwners.addFirst(Thread.currentThread());
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
            assert !guard.amOriginalOwner();
            /* Second step of passing */
            guard.owner = Thread.currentThread();
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
                LockSupport.unpark(guard.owner);
        }
    };
    
    void releasePassed(final Guarded guarded) {
        /* First, make sure all reachable and previously reachable objects are readwrite */
        final Set<Guarded> guardProcessed = newIdentitySet();
        guarded.processAll(GUARD_READ_WRITE, guardProcessed, false);
        processReachables(GUARD_READ_WRITE, guardProcessed);
        
        /* Then, release still reachable objects and make "parent" task the owner of newly reachable
         * objects */
        final Thread parent = prevOwners.getFirst();
        final Op transferOwner = new Op() {
            @Override
            public void process(final Guarded g) {
                final Guard guard = g.getGuard();
                if(guard.amOriginalOwner())
                    guard.owner = parent;
                else
                    RELEASE_PASSED.process(g);
            }
        };
        final Set<Guarded> processed = newIdentitySet();
        guarded.processAll(transferOwner, processed, true);
        
        /* Finally, release rest of the originally reachable objects and views */
        processReachables(RELEASE_PASSED, processed);
        prevReachables.removeFirst();
    }
    
    private static final Op RELEASE_PASSED = new Op() {
        @Override
        public void process(final Guarded guarded) {
            final Guard guard = guarded.getGuard();
            assert !guard.amOriginalOwner();
            guard.owner = guard.prevOwners.removeFirst();
            /* IMPROVE: Unpark only once, not for every reachable object. May even save the stack of
             * owners for reachable objects... */
            LockSupport.unpark(guard.owner);
        }
    };
    
    /* Guarding methods */
    
    void guardReadOnly(final Guarded guarded) {
        GUARD_READ_ONLY.process(guarded);
        guarded.processViews(GUARD_READ_ONLY, newIdentitySet());
    }
    
    private static final Op GUARD_READ_ONLY = new Op() {
        @Override
        public void process(final Guarded guarded) {
            /* mayRead() reads the volatile owner field written by releasePassed() and
             * releaseShared(). Therefore, there is a happens-before relationship between
             * releasePassed()/releaseShared() and guardRead(). */
            while(!guarded.getGuard().mayRead())
                LockSupport.park();
        }
    };
    
    void guardReadWrite(final Guarded guarded) {
        GUARD_READ_WRITE.process(guarded);
        guarded.processViews(GUARD_READ_WRITE, newIdentitySet());
    }
    
    private static final Op GUARD_READ_WRITE = new Op() {
        @Override
        public void process(final Guarded guarded) {
            /* mayWrite() reads the volatile owner field written by releasePassed(). Therefore,
             * there is a happens-before relationship between releasePassed() and guardReadWrite(). */
            while(!guarded.getGuard().mayWrite())
                LockSupport.park();
        }
    };
    
    /* Implementation methods */
    
    private boolean mayRead() {
        return owner == Thread.currentThread() || sharedCount.get() > 0;
    }
    
    private boolean mayWrite() {
        return owner == Thread.currentThread() && !(sharedCount.get() > 0);
    }
    
    private boolean amOriginalOwner() {
        return prevOwners.isEmpty();
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

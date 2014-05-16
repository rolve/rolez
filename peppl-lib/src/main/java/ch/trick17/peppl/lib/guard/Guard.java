package ch.trick17.peppl.lib.guard;

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
    private final AtomicInteger sharedCount = new AtomicInteger(0);
    
    private final Deque<Set<Guarded>> prevReachables = new ArrayDeque<>();
    
    void initializeViewGuard(final Guard viewGuard) {
        viewGuard.owner = owner;
        viewGuard.prevOwners.clear();
        viewGuard.prevOwners.addAll(prevOwners);
        viewGuard.sharedCount.set(sharedCount.get());
    }
    
    /* Object state operations */
    
    void share(final Guarded guarded) {
        guarded.processAll(SHARE, newIdentitySet(), false);
    }
    
    private static final GuardOp SHARE = new GuardOp() {
        public void process(final Guarded guarded) {
            jpfWorkaround();
            guarded.getGuard().guardRead(guarded);
            guarded.getGuard().sharedCount.incrementAndGet();
        }
    };
    
    void pass(final Guarded guarded) {
        final Set<Guarded> processed = newIdentitySet();
        guarded.processAll(PASS, processed, false);
        prevReachables.addFirst(processed);
    }
    
    private static final GuardOp PASS = new GuardOp() {
        public void process(final Guarded guarded) {
            jpfWorkaround();
            GUARD_READ_WRITE.process(guarded);
            /* First step of passing */
            final Guard guard = guarded.getGuard();
            guard.owner = null;
            guard.prevOwners.addFirst(Thread.currentThread());
        }
    };
    
    void registerNewOwner(final Guarded guarded) {
        guarded.processAll(REGISTER_OWNER, newIdentitySet(), false);
    }
    
    private static final GuardOp REGISTER_OWNER = new GuardOp() {
        public void process(final Guarded guarded) {
            jpfWorkaround();
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
    
    private static final GuardOp RELEASE_SHARED = new GuardOp() {
        public void process(final Guarded guarded) {
            jpfWorkaround();
            final Guard guard = guarded.getGuard();
            assert guard.isShared();
            final int count = guard.sharedCount.decrementAndGet();
            if(count == 0)
                LockSupport.unpark(guard.owner);
        }
    };
    
    void releasePassed(final Guarded guarded) {
        /* First, make sure all reachable and previously reachable objects are
         * mutable */
        final Set<Guarded> guardProcessed = newIdentitySet();
        guarded.processAll(GUARD_READ_WRITE, guardProcessed, false);
        processReachables(GUARD_READ_WRITE, guardProcessed);
        
        /* Then, release still reachable objects and make "parent" task the
         * owner of newly reachable objects */
        final Thread parent = prevOwners.peekFirst();
        assert parent != null;
        final GuardOp transferOwner = new GuardOp() {
            public void process(final Guarded g) {
                jpfWorkaround();
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
    
    private static final GuardOp RELEASE_PASSED = new GuardOp() {
        public void process(final Guarded guarded) {
            jpfWorkaround();
            final Guard guard = guarded.getGuard();
            assert !guard.amOriginalOwner();
            guard.owner = guard.prevOwners.removeFirst();
            LockSupport.unpark(guard.owner);
        }
    };
    
    /* Guarding methods */
    
    void guardRead(final Guarded guarded) {
        GUARD_READ.process(guarded);
        guarded.processViews(GUARD_READ, newIdentitySet());
    }
    
    private static final GuardOp GUARD_READ = new GuardOp() {
        public void process(final Guarded guarded) {
            jpfWorkaround();
            /* isMutable() and isShared() read volatile (atomic) fields written
             * by releasePassed and releaseShared. Therefore, there is a
             * happens-before relationship between
             * releasePassed()/releaseShared() and guardRead(). */
            final Guard guard = guarded.getGuard();
            while(!(guard.isMutable() || guard.isShared()))
                LockSupport.park();
        }
    };
    
    void guardReadWrite(final Guarded guarded) {
        GUARD_READ_WRITE.process(guarded);
        guarded.processViews(GUARD_READ_WRITE, newIdentitySet());
    }
    
    private static final GuardOp GUARD_READ_WRITE = new GuardOp() {
        public void process(final Guarded guarded) {
            jpfWorkaround();
            /* isMutable() reads the volatile owner field written by
             * releasePassed. Therefore, there is a happens-before relationship
             * between releasePassed() and guardReadWrite(). */
            while(!guarded.getGuard().isMutable())
                LockSupport.park();
        }
    };
    
    /* Implementation methods */
    
    private boolean isMutable() {
        return owner == Thread.currentThread() && !isShared();
    }
    
    private boolean isShared() {
        return sharedCount.get() > 0;
    }
    
    private boolean amOriginalOwner() {
        return prevOwners.isEmpty();
    }
    
    private void processReachables(final GuardOp op,
            final Set<Guarded> processed) {
        final Set<Guarded> reachables = prevReachables.peekFirst();
        assert reachables != null;
        
        for(final Guarded guarded : reachables)
            if(!processed.contains(guarded))
                op.process(guarded);
    }
    
    private static Set<Guarded> newIdentitySet() {
        return Collections
                .newSetFromMap(new IdentityHashMap<Guarded, Boolean>());
    }
    
    /**
     * JPF seems to miss some scheduling relevant points in the program, so this
     * method makes sure that all guarding operations trigger a scheduling
     * choice.
     */
    private static void jpfWorkaround() {
        // IMPROVE: Push JPF developers to fix this!
        synchronized(Guard.class) {}
    }
}

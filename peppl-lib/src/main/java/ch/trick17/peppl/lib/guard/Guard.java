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
    private final Deque<Set<Guarded>> reachables = new ArrayDeque<>();
    
    private final AtomicInteger sharedCount = new AtomicInteger(0);
    
    /* Object state operations */
    
    void share(final Guarded guarded) {
        guarded.processRecursively(SHARE, newIdentitySet());
    }
    
    private static final GuardOp SHARE = new GuardOp() {
        public void process(final Guard guard) {
            guard.guardRead();
            guard.sharedCount.incrementAndGet();
        }
    };
    
    void pass(final Guarded guarded) {
        final Set<Guarded> reachable = newIdentitySet();
        guarded.processRecursively(PASS, reachable);
        reachables.addFirst(reachable);
    }
    
    private static final GuardOp PASS = new GuardOp() {
        public void process(final Guard guard) {
            guard.guardReadWrite();
            /* First step of passing */
            guard.owner = null;
            guard.prevOwners.addFirst(Thread.currentThread());
        }
    };
    
    void registerNewOwner(@SuppressWarnings("unused") final Guarded guarded) {
        processReachables(REGISTER_OWNER);
    }
    
    private static final GuardOp REGISTER_OWNER = new GuardOp() {
        public void process(final Guard guard) {
            assert guard.owner == null;
            assert !guard.amOriginalOwner();
            /* Second step of passing */
            guard.owner = Thread.currentThread();
        }
    };
    
    void releaseShared(final Guarded guarded) {
        guarded.processRecursively(RELEASE_SHARED, newIdentitySet());
    }
    
    private static final GuardOp RELEASE_SHARED = new GuardOp() {
        public void process(final Guard guard) {
            assert guard.isShared();
            guard.sharedCount.decrementAndGet();
            LockSupport.unpark(guard.owner);
        }
    };
    
    void releasePassed(final Guarded guarded) {
        /* First, make "parent" task the owner of newly reachable objects */
        final Thread parent = prevOwners.peekFirst();
        assert parent != null;
        final GuardOp transferOwner = new GuardOp() {
            public void process(final Guard guard) {
                if(guard.amOriginalOwner())
                    guard.owner = parent;
            }
        };
        guarded.processRecursively(transferOwner, newIdentitySet());
        
        /* Second, release originally reachable objects */
        processReachables(RELEASE_PASSED);
        reachables.removeFirst();
    }
    
    private static final GuardOp RELEASE_PASSED = new GuardOp() {
        public void process(final Guard guard) {
            guard.guardReadWrite();
            assert !guard.amOriginalOwner();
            guard.owner = guard.prevOwners.removeFirst();
            LockSupport.unpark(guard.owner);
        }
    };
    
    /* Guarding methods */
    
    void guardRead() {
        /* isMutable() and isShared() read volatile (atomic) fields written by
         * releasePassed and releaseShared. Therefore, there is a happens-before
         * relationship between releasePassed()/releaseShared() and guardRead(). */
        while(!(isMutable() || isShared()))
            LockSupport.park();
    }
    
    void guardReadWrite() {
        /* isMutable() reads the volatile owner field written by releasePassed.
         * Therefore, there is a happens-before relationship between
         * releasePassed() and guardReadWrite(). */
        while(!isMutable())
            LockSupport.park();
    }
    
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
    
    private void processReachables(final GuardOp op) {
        final Set<Guarded> reachable = reachables.peekFirst();
        assert reachable != null;
        
        for(final Guarded guarded : reachable)
            op.process(guarded.getGuard());
    }
    
    private static Set<Guarded> newIdentitySet() {
        return Collections
                .newSetFromMap(new IdentityHashMap<Guarded, Boolean>());
    }
}

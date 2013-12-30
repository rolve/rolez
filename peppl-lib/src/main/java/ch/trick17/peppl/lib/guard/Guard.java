package ch.trick17.peppl.lib.guard;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

public class Guard {
    
    private volatile Thread owner = Thread.currentThread();
    private final Deque<Thread> prevOwners = new ArrayDeque<>();
    
    private final AtomicInteger sharedCount = new AtomicInteger(0);
    
    private final Deque<Set<GuardedObject>> reachables = new ConcurrentLinkedDeque<>();
    
    /*
     * Object state operations
     */
    
    private static abstract class Op {
        abstract void process(Guard guard);
    }
    
    public void share(final GuardedObject o) {
        final Set<GuardedObject> reachable = newIdentitySet();
        processRecusively(o, SHARE, reachable);
        reachables.addFirst(reachable);
    }
    
    private static final Op SHARE = new Op() {
        @Override
        void process(final Guard guard) {
            guard.guardRead();
            guard.sharedCount.incrementAndGet();
        }
    };
    
    public void pass(final GuardedObject o) {
        final Set<GuardedObject> reachable = newIdentitySet();
        processRecusively(o, PASS, reachable);
        reachables.addFirst(reachable);
    }
    
    private static final Op PASS = new Op() {
        @Override
        void process(final Guard guard) {
            guard.guardReadWrite();
            /* First step of passing */
            guard.owner = null;
            guard.prevOwners.addFirst(Thread.currentThread());
        }
    };
    
    public void registerNewOwner(final GuardedObject o) {
        processReachables(REGISTER_OWNER);
    }
    
    private static final Op REGISTER_OWNER = new Op() {
        @Override
        void process(final Guard guard) {
            assert guard.owner == null;
            assert !guard.amOriginalOwner();
            /* Second step of passing */
            guard.owner = Thread.currentThread();
        }
    };
    
    public void releaseShared(final GuardedObject o) {
        processReachables(RELEASE_SHARED);
        reachables.removeFirst();
    }
    
    private static final Op RELEASE_SHARED = new Op() {
        @Override
        void process(final Guard guard) {
            assert guard.isShared();
            guard.sharedCount.decrementAndGet();
            LockSupport.unpark(guard.owner);
        }
    };
    
    public void releasePassed(final GuardedObject o) {
        /* First, make "parent" task the owner of newly reachable objects */
        final Thread parent = prevOwners.peekFirst();
        assert parent != null;
        final Op transferOwner = new Op() {
            @Override
            void process(final Guard guard) {
                if(guard.amOriginalOwner())
                    guard.owner = parent;
            }
        };
        processRecusively(o, transferOwner, newIdentitySet());
        
        /* Second, release originally reachable objects */
        processReachables(RELEASE_PASSED);
        reachables.removeFirst();
    }
    
    private static final Op RELEASE_PASSED = new Op() {
        @Override
        void process(final Guard guard) {
            assert guard.isMutable();
            assert !guard.amOriginalOwner();
            guard.owner = guard.prevOwners.removeFirst();
            LockSupport.unpark(guard.owner);
        }
    };
    
    /*
     * Guarding methods
     */
    
    public void guardRead() {
        /*
         * isMutable() and isShared() read volatile (atomic) fields written by
         * releasePassed and releaseShared. Therefore, there is a happens-before
         * relationship between releasePassed()/releaseShared() and guardRead().
         */
        while(!(isMutable() || isShared()))
            LockSupport.park();
    }
    
    public void guardReadWrite() {
        /*
         * isMutable() reads the volatile owner field written by releasePassed.
         * Therefore, there is a happens-before relationship between
         * releasePassed() and guardReadWrite().
         */
        while(!isMutable())
            LockSupport.park();
    }
    
    /*
     * Implementation methods
     */
    
    private boolean isMutable() {
        return owner == Thread.currentThread() && !isShared();
    }
    
    private boolean isShared() {
        return sharedCount.get() > 0;
    }
    
    private boolean amOriginalOwner() {
        return prevOwners.isEmpty();
    }
    
    private void processReachables(final Op op) {
        final Set<GuardedObject> reachable = reachables.peekFirst();
        assert reachable != null;
        
        for(final GuardedObject object : reachable)
            op.process(object.getGuard());
    }
    
    private static void processRecusively(final GuardedObject o, final Op op,
            final Set<GuardedObject> processed) {
        if(processed.add(o)) {
            /* Process current object */
            op.process(o.getGuard());
            
            /* Process children */
            final List<Field> fields = allRefFields(o);
            for(final Field field : fields) {
                field.setAccessible(true);
                final Object ref;
                try {
                    ref = field.get(o);
                } catch(final IllegalAccessException e) {
                    throw new AssertionError(e);
                }
                if(ref != null) {
                    assert ref instanceof GuardedObject;
                    final GuardedObject other = (GuardedObject) ref;
                    processRecusively(other, op, processed);
                }
            }
        }
    }
    
    private static List<Field> allRefFields(final GuardedObject o) {
        final ArrayList<Field> fields = new ArrayList<>();
        Class<?> currentClass = o.getClass();
        while(currentClass != GuardedObject.class) {
            final Field[] declaredFields = currentClass.getDeclaredFields();
            for(final Field declaredField : declaredFields)
                if(!declaredField.getType().isPrimitive())
                    fields.add(declaredField);
            currentClass = currentClass.getSuperclass();
        }
        return Collections.unmodifiableList(fields);
    }
    
    private static Set<GuardedObject> newIdentitySet() {
        return Collections
                .newSetFromMap(new IdentityHashMap<GuardedObject, Boolean>());
    }
}

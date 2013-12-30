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
    
    public void share(final GuardedObject o) {
        final Set<GuardedObject> reachable = newIdentitySet();
        processRecusively(o, Op.SHARE, reachable);
        reachables.addFirst(reachable);
    }
    
    public void pass(final GuardedObject o) {
        final Set<GuardedObject> reachable = newIdentitySet();
        processRecusively(o, Op.PASS, reachable);
        reachables.addFirst(reachable);
    }
    
    public void registerNewOwner(final GuardedObject o) {
        processReachables(Op.REGISTER_OWNER);
    }
    
    public void releaseShared(final GuardedObject o) {
        processReachables(Op.RELEASE_SHARED);
        reachables.removeFirst();
    }
    
    public void releasePassed(final GuardedObject o) {
        processReachables(Op.RELEASE_PASSED);
        reachables.removeFirst();
    }
    
    private void processRecusively(final GuardedObject o, final Op op,
            final Set<GuardedObject> processed) {
        if(processed.add(o)) {
            /* Process current object */
            o.getGuard().process(op);
            
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
    
    private void processReachables(final Op op) {
        final Set<GuardedObject> reachable = reachables.peekFirst();
        assert reachable != null;
        
        for(final GuardedObject object : reachable)
            object.getGuard().process(op);
    }
    
    private void process(final Op op) {
        switch(op) {
        case SHARE:
            guardRead();
            sharedCount.incrementAndGet();
            break;
        case PASS:
            guardReadWrite();
            /* First step of passing */
            owner = null;
            prevOwners.addFirst(Thread.currentThread());
            break;
        case REGISTER_OWNER:
            assert owner == null;
            assert !amOriginalOwner();
            /* Second step of passing */
            owner = Thread.currentThread();
            break;
        case RELEASE_SHARED:
            assert isShared();
            sharedCount.decrementAndGet();
            LockSupport.unpark(owner);
            break;
        case RELEASE_PASSED:
            assert isMutable();
            assert !amOriginalOwner();
            owner = prevOwners.removeFirst();
            LockSupport.unpark(owner);
            break;
        }
    }
    
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
    
    private boolean isMutable() {
        return owner == Thread.currentThread() && !isShared();
    }
    
    private boolean isShared() {
        return sharedCount.get() > 0;
    }
    
    private boolean amOriginalOwner() {
        return prevOwners.isEmpty();
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
    
    private static enum Op {
        SHARE,
        PASS,
        REGISTER_OWNER,
        RELEASE_SHARED,
        RELEASE_PASSED;
    }
}

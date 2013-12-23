package ch.trick17.peppl.lib;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

public class Guard {
    
    private volatile Thread owner = Thread.currentThread();
    private final ConcurrentLinkedDeque<Thread> prevOwners = new ConcurrentLinkedDeque<>();
    private final AtomicInteger sharedCount = new AtomicInteger(0);
    
    public void pass() {
        guardReadWrite();
        
        /* First step of passing */
        owner = null;
        prevOwners.addFirst(Thread.currentThread());
    }
    
    public void registerNewOwner() {
        assert owner == null;
        assert !amOriginalOwner();
        
        /* Second step of passing */
        owner = Thread.currentThread();
    }
    
    public void share() {
        guardRead();
        
        sharedCount.incrementAndGet();
    }
    
    public void releasePassed() {
        assert isMutable();
        assert !amOriginalOwner();
        
        owner = prevOwners.removeFirst();
        LockSupport.unpark(owner);
    }
    
    public void releaseShared() {
        assert isShared();
        
        sharedCount.decrementAndGet();
        LockSupport.unpark(owner);
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
}

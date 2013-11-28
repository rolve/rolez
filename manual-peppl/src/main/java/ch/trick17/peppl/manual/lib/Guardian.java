package ch.trick17.peppl.manual.lib;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

public class Guardian {
    
    // FIXME: Use implementation which uses object identity!
    // IMPROVE: Might use implementation with fewer concurrency guarantees, as
    // new entries are always distinct from all existing ones
    // FIXME: When to remove entries? Need some sort of weak map...
    private static ConcurrentMap<Object, Record> records = new ConcurrentHashMap<>();
    
    public static void pass(final Object o) {
        final Record newRec = new Record();
        Record record = records.putIfAbsent(o, newRec);
        if(record == null)
            record = newRec;
        
        assert record.isMutable();
        
        /* First step of passing */
        record.owner = null;
        record.prevOwners.add(Thread.currentThread());
    }
    
    public static void registerNewOwner(final Object o) {
        final Record record = records.get(o);
        assert record != null;
        assert record.owner == null;
        assert !record.amOriginalOwner();
        
        /* Second step of passing */
        record.owner = Thread.currentThread();
    }
    
    public static void share(final Object o) {
        final Record newRec = new Record();
        Record record = records.putIfAbsent(o, newRec);
        if(record == null)
            record = newRec;
        
        assert record.isMutable() || record.isShared();
        
        record.sharedCount.incrementAndGet();
    }
    
    public static void releasePassed(final Object o) {
        final Record record = records.get(o);
        assert record != null;
        assert record.isMutable();
        assert !record.amOriginalOwner();
        
        record.owner = record.prevOwners.remove();
        LockSupport.unpark(record.owner);
    }
    
    public static void releaseShared(final Object o) {
        final Record record = records.get(o);
        assert record != null;
        assert record.isShared();
        
        record.sharedCount.decrementAndGet();
        LockSupport.unpark(record.owner);
    }
    
    public static void guardRead(final Object o) {
        final Record record = records.get(o);
        assert record != null;
        
        while(!(record.isMutable() || record.isShared()))
            LockSupport.park();
    }
    
    public static void guardReadWrite(final Object o) {
        final Record record = records.get(o);
        assert record != null;
        
        while(!record.isMutable())
            LockSupport.park();
    }
    
    private static final class Record {
        
        volatile Thread owner = Thread.currentThread();
        final ConcurrentLinkedQueue<Thread> prevOwners = new ConcurrentLinkedQueue<>();
        final AtomicInteger sharedCount = new AtomicInteger(0);
        
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
    
}

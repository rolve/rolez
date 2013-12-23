package ch.trick17.peppl.lib;

import java.io.Serializable;
import java.util.concurrent.locks.LockSupport;

import ch.trick17.peppl.lib.PepplObject.Record;

public class Guardian implements Serializable {
    
    private static final Guardian defaultGuardian = new Guardian();
    
    public static Guardian get() {
        return defaultGuardian;
    }
    
    public void pass(final PepplObject o) {
        Record record = o.record;
        if(record == null) {
            record = new Record();
            o.record = record;
        }
        else
            guardReadWrite(o);
        
        /* First step of passing */
        record.owner = null;
        record.prevOwners.addFirst(Thread.currentThread());
    }
    
    public void registerNewOwner(final PepplObject o) {
        final Record record = o.record;
        assert record != null;
        assert record.owner == null;
        assert !record.amOriginalOwner();
        
        /* Second step of passing */
        record.owner = Thread.currentThread();
    }
    
    public void share(final PepplObject o) {
        Record record = o.record;
        if(record == null) {
            record = new Record();
            record.owner = Thread.currentThread();
            o.record = record;
        }
        else
            guardRead(o);
        
        record.sharedCount.incrementAndGet();
    }
    
    public void releasePassed(final PepplObject o) {
        final Record record = o.record;
        assert record != null;
        assert record.isMutable();
        assert !record.amOriginalOwner();
        
        record.owner = record.prevOwners.removeFirst();
        LockSupport.unpark(record.owner);
    }
    
    public void releaseShared(final PepplObject o) {
        final Record record = o.record;
        assert record != null;
        assert record.isShared();
        
        record.sharedCount.decrementAndGet();
        LockSupport.unpark(record.owner);
    }
    
    public void guardRead(final PepplObject o) {
        final Record record = o.record;
        
        /*
         * isMutable() and isShared() read volatile (atomic) fields written by
         * releasePassed and releaseShared. Therefore, there is a happens-before
         * relationship between releasePassed()/releaseShared() and guardRead().
         */
        if(record != null)
            while(!(record.isMutable() || record.isShared()))
                LockSupport.park();
    }
    
    public void guardReadWrite(final PepplObject o) {
        final Record record = o.record;
        
        /*
         * isMutable() reads the volatile owner field written by releasePassed.
         * Therefore, there is a happens-before relationship between
         * releasePassed() and guardReadWrite().
         */
        if(record != null)
            while(!record.isMutable())
                LockSupport.park();
    }
}

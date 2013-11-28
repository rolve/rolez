package ch.trick17.peppl.manual.lib;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.LockSupport;

public class Guardian {
    
    private static ConcurrentMap<Object, Record> records = new ConcurrentHashMap<>();
    
    public static void pass(final Object o) {
        final Record newRec = new Record();
        Record record = records.putIfAbsent(o, newRec);
        if(record == null)
            record = newRec;
        else {
            assert record.owner == Thread.currentThread();
            record.owner = null;
        }
        
        record.prevOwners.add(Thread.currentThread());
    }
    
    public static void share(final Object o) {
        // TODO
    }
    
    public static void usePassed(final Object o) {
        final Record record = records.get(o);
        assert record != null;
        assert record.owner == null;
        assert !record.prevOwners.isEmpty();
        
        record.owner = Thread.currentThread();
    }
    
    public static void useShared(final Object o) {
        // TODO
    }
    
    public static void release(final Object o) {
        final Record record = records.get(o);
        assert record != null;
        assert record.owner == Thread.currentThread();
        assert !record.prevOwners.isEmpty();
        
        record.owner = record.prevOwners.remove();
        LockSupport.unpark(record.owner);
    }
    
    public static void guardRead(final Object o) {
        // TODO
    }
    
    public static void guardReadWrite(final Object o) {
        final Record record = records.get(o);
        assert record != null;
        
        while(record.owner != Thread.currentThread())
            LockSupport.park();
    }
    
    private static class Record {
        
        volatile Thread owner;
        final ConcurrentLinkedQueue<Thread> prevOwners = new ConcurrentLinkedQueue<>();
    }
}

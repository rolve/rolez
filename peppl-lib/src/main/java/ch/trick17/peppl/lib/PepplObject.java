package ch.trick17.peppl.lib;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class PepplObject {
    
    volatile Record record = null;
    
    static class Record {
        volatile Thread owner = null;
        final ConcurrentLinkedDeque<Thread> prevOwners = new ConcurrentLinkedDeque<Thread>();
        final AtomicInteger sharedCount = new AtomicInteger(0);
        
        boolean isMutable() {
            return owner == Thread.currentThread() && !isShared();
        }
        
        boolean isShared() {
            return sharedCount.get() > 0;
        }
        
        boolean amOriginalOwner() {
            return prevOwners.isEmpty();
        }
    }
}

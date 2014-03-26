package ch.trick17.peppl.lib.task;

import static java.util.Collections.newSetFromMap;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

public final class ThreadPoolTaskSystem extends TaskSystem {
    
    private final int baseSize;
    private final Set<Worker> idleWorkers = newSetFromMap(new ConcurrentHashMap<Worker, Boolean>());
    
    public ThreadPoolTaskSystem() {
        this(Runtime.getRuntime().availableProcessors());
    }
    
    public ThreadPoolTaskSystem(final int baseSize) {
        assert baseSize > 0;
        this.baseSize = baseSize;
    }
    
    public int getBaseSize() {
        return baseSize;
    }
    
    @Override
    protected void start(final Task<?> task) {
        /* Try to find an idle worker */
        boolean success = false;
        for(final Worker worker : idleWorkers) {
            success = worker.tryExecute(task);
            if(success) {
                idleWorkers.remove(worker);
                break;
            }
        }
        
        /* Else create and start a new worker */
        if(!success)
            new Worker(task).start();
    }
    
    private class Worker extends Thread {
        
        final AtomicReference<Task<?>> currentTask;
        
        public Worker(final Task<?> task) {
            setDaemon(true);
            currentTask = new AtomicReference<Task<?>>(task);
        }
        
        boolean tryExecute(final Task<?> task) {
            if(currentTask.compareAndSet(null, task)) {
                LockSupport.unpark(this);
                return true;
            }
            else
                return false;
        }
        
        @Override
        public void run() {
            while(true) {
                final Task<?> current = currentTask.get();
                if(current == null)
                    LockSupport.park();
                else {
                    current.run();
                    currentTask.set(null);
                    if(idleWorkers.size() < baseSize)
                        /* Keep this worker */
                        idleWorkers.add(this);
                    else
                        /* Kill this worker */
                        break;
                }
            }
        }
    }
    
    @Override
    public String toString() {
        return super.toString() + "[" + getBaseSize() + "]";
    }
}

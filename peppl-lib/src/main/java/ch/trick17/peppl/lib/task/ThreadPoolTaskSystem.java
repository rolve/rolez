package ch.trick17.peppl.lib.task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

public final class ThreadPoolTaskSystem extends TaskSystem {
    
    private final int maxThreads;
    private volatile boolean initialized;
    private final List<Worker> workers;
    
    public ThreadPoolTaskSystem() {
        this(Runtime.getRuntime().availableProcessors());
    }
    
    public ThreadPoolTaskSystem(final int maxThreads) {
        assert maxThreads > 0;
        this.maxThreads = maxThreads;
        workers = new ArrayList<>(maxThreads);
    }
    
    private void initializeWorkers() {
        for(int i = 0; i < getMaxThreads(); i++)
            workers.add(new Worker());
        initialized = true;
    }
    
    public int getMaxThreads() {
        return maxThreads;
    }
    
    @Override
    protected void start(final Task<?> task) {
        if(!initialized)
            synchronized(this) {
                if(!initialized)
                    initializeWorkers();
            }
        
        /* Try to find an idle worker */
        /* IMPROVE: Do not always try the same worker first */
        boolean found = false;
        for(final Worker worker : workers) {
            found = worker.tryExecute(task);
            if(found)
                break;
        }
        
        /* Else run directly */
        if(!found)
            task.run();
    }
    
    private static class Worker extends Thread {
        
        final AtomicReference<Task<?>> currentTask = new AtomicReference<Task<?>>();
        
        public Worker() {
            setDaemon(true);
        }
        
        boolean tryExecute(final Task<?> task) {
            if(currentTask.compareAndSet(null, task)) {
                if(!isAlive())
                    start();
                else
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
                }
            }
        }
    }
    
    @Override
    public String toString() {
        return super.toString() + "[" + getMaxThreads() + "]";
    }
}

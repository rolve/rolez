package ch.trick17.peppl.lib;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskSystem {
    
    private final int numThreads;
    private final ExecutorService executor;
    private final AtomicInteger taskCount = new AtomicInteger(0);
    
    public TaskSystem() {
        this(Runtime.getRuntime().availableProcessors());
    }
    
    public TaskSystem(final int numThreads) {
        this.numThreads = numThreads;
        executor = Executors.newFixedThreadPool(numThreads);
    }
    
    public int getNumThreads() {
        return numThreads;
    }
    
    public <V> Future<V> runTask(final Task<V> task) {
        assert !executor.isShutdown();
        assert task.system == null;
        
        task.system = this;
        taskCount.incrementAndGet();
        return executor.submit(task);
    }
    
    void finishedTask(final Task<?> task) {
        assert task.system == this;
        
        if(taskCount.decrementAndGet() == 0)
            executor.shutdown();
    }
    
    /*
     * Global default task system
     */
    
    private static final TaskSystem defaultSystem = new TaskSystem();
    
    public static TaskSystem get() {
        return defaultSystem;
    }
}

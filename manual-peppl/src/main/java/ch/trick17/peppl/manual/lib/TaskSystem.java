package ch.trick17.peppl.manual.lib;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskSystem {
    
    private static final int NUM_THREADS = Runtime.getRuntime()
            .availableProcessors();
    
    private static ExecutorService executor = Executors
            .newFixedThreadPool(getNumThreads());
    
    private static final AtomicInteger taskCount = new AtomicInteger(0);
    
    public static int getNumThreads() {
        return NUM_THREADS;
    }
    
    public static <V> Future<V> runTask(final Task<V> task) {
        taskCount.incrementAndGet();
        return executor.submit(task);
    }
    
    static void finishedTask(final Task<?> task) {
        if(taskCount.decrementAndGet() == 0)
            executor.shutdown();
    }
}

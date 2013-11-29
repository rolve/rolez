package ch.trick17.peppl.manual.lib;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TaskSystem {
    
    private static final int NUM_THREADS = Runtime.getRuntime()
            .availableProcessors();
    
    private static ExecutorService executor = Executors
            .newFixedThreadPool(getNumThreads());
    
    public static <V> Future<V> runTask(final Task<V> task) {
        return executor.submit(task);
    }
    
    public static int getNumThreads() {
        return NUM_THREADS;
    }
}

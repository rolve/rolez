package ch.trick17.peppl.manual.lib;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TaskSystem {
    
    private static ExecutorService executor = Executors.newCachedThreadPool();
    
    public static <V> Future<V> runTask(final Task<V> task) {
        return executor.submit(task);
    }
}

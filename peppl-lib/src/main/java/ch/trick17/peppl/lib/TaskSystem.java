package ch.trick17.peppl.lib;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class TaskSystem {
    
    public <V> Future<V> runTask(final Callable<V> task) {
        final FutureTask<V> futureTask = new FutureTask<>(task);
        new Thread(futureTask).start();
        return futureTask;
    }
    
    /*
     * Global default task system
     */
    
    private static final TaskSystem defaultSystem = new TaskSystem();
    
    public static TaskSystem get() {
        return defaultSystem;
    }
}

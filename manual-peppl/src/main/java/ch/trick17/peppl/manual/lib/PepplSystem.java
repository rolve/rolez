package ch.trick17.peppl.manual.lib;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PepplSystem {
    
    private static ExecutorService executor = Executors.newCachedThreadPool();
    
    public static <V> Future<V> runTask(final Task<V> task) {
        return executor.submit(task);
    }
    
    public static void pass(final Object o) {
        // TODO
    }
    
    public static void share(final Object o) {
        // TODO
    }
    
    public static void usePassed(final Object o) {
        // TODO
    }
    
    public static void useShared(final Object o) {
        // TODO
    }
    
    public static void release(final Object o) {
        // TODO
    }
    
    public static void guardRead(final Object o) {
        // TODO
    }
    
    public static void guardReadWrite(final Object o) {
        // TODO
    }
    
}

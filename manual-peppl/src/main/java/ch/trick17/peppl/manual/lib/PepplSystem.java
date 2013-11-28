package ch.trick17.peppl.manual.lib;

import java.util.concurrent.Future;

public class PepplSystem {
    
    public static void guardReadWrite(final Object target) {
        // TODO
    }
    
    public static <V> Future<V> runTask(final Task<V> task) {
        // TODO
        return null;
    }
    
    public static void runVoidTask(final Task<Void> task) {
        // TODO
    }
    
    public static void pass(final Task<?> src, final Task<?> dest,
            final Object o) {
        // TODO
    }
    
    public static void share(final Task<?> src, final Task<?> dest,
            final Object o) {
        // TODO
    }
}

package ch.trick17.peppl.lib;

import java.util.concurrent.Callable;

public abstract class Task<V> implements Callable<V> {
    
    TaskSystem system = null;
    
    @Override
    public final V call() {
        try {
            return compute();
        } finally {
            system.finishedTask(this);
        }
    }
    
    abstract protected V compute();
}

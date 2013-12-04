package ch.trick17.peppl.lib;

import java.util.concurrent.Callable;

public abstract class Task<V> implements Callable<V> {
    
    TaskSystem system = null;
    
    @Override
    public final V call() {
        final V result = compute();
        system.finishedTask(this);
        return result;
    }
    
    abstract protected V compute();
}

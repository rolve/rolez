package ch.trick17.peppl.lib;

import java.util.concurrent.Callable;

public abstract class Task<V> implements Callable<V> {
    
    @Override
    public final V call() {
        final V result = compute();
        
        TaskSystem.finishedTask(this);
        return result;
    }
    
    abstract protected V compute();
}

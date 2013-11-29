package ch.trick17.peppl.manual.lib;

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

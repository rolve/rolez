package ch.trick17.rolez;

import java.util.concurrent.Callable;

public abstract class RunnableCallable implements Callable<Void>, Runnable {
    
    public Void call() {
        run();
        return null;
    }
}

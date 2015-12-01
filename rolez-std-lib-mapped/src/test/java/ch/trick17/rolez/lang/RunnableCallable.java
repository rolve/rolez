package ch.trick17.rolez.lang;

import java.util.concurrent.Callable;

public abstract class RunnableCallable implements Callable<Void>, Runnable {
    
    public Void call() {
        run();
        return null;
    }
}

package ch.trick17.rolez.lang.task;

public class SingleThreadTaskSystem extends TaskSystem {
    
    @Override
    protected void doStart(final Task<?> task) {
        task.run();
    }
}

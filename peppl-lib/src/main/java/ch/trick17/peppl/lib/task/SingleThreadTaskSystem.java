package ch.trick17.peppl.lib.task;

public class SingleThreadTaskSystem extends TaskSystem {
    
    @Override
    protected void start(final Task<?> task) {
        task.run();
    }
}

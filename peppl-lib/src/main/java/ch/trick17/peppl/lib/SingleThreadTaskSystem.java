package ch.trick17.peppl.lib;

public class SingleThreadTaskSystem extends TaskSystem {
    
    @Override
    protected void doRun(final Task<?> task) {
        task.run();
    }
}

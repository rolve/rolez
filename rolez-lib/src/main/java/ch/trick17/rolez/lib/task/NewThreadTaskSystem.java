package ch.trick17.rolez.lib.task;

/**
 * A simple {@link TaskSystem} that runs each task in a new thread.
 * 
 * @author Michael Faes
 */
public final class NewThreadTaskSystem extends TaskSystem {
    
    @Override
    protected void start(final Task<?> task) {
        new Thread(task).start();
    }
}

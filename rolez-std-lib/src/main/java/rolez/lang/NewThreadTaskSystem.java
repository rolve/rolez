package rolez.lang;

/**
 * A simple {@link TaskSystem} that runs each task in a new thread.
 * 
 * @author Michael Faes
 */
public final class NewThreadTaskSystem extends TaskSystem {
    
    @Override
    protected void doStart(final Task<?> task) {
        new Thread(task).start();
    }
}

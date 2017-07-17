package rolez.checked.lang;

public class SingleThreadTaskSystem extends TaskSystem {
    
    @Override
    protected void doStart(final Task<?> task) {
        task.run();
    }
}

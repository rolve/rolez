package rolez.internal;

import java.util.ArrayList;

import rolez.lang.Task;

/**
 * An {@link ArrayList} of {@link Task}s, with some added methods to make code generation easier.
 * Used to collect and finally join all tasks that are started in a method.
 */
public class Tasks extends ArrayList<Task<?>> {
    
    public <V> Task<V> addInline(Task<V> task) {
        add(task);
        return task;
    }
    
    public void joinAll() {
        for(Task<?> task : this)
            task.get();
    }
}

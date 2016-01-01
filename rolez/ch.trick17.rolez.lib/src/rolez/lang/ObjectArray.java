package rolez.lang;

import static java.util.Arrays.asList;

public class ObjectArray<T> extends Guarded {
    
    public final T[] data;
    
    public ObjectArray(int length) {
        // Note that the following is unsafe if the data array is assigned to a
        // variable with a concrete (non-object) array type, e.g., String[].
        @SuppressWarnings("unchecked") T[] array = (T[]) new Object[length];
        this.data = array;
    }
    
    /**
     * This constructor is used for wrapping the args array in the main method.
     */
    public ObjectArray(T[] data) {
        this.data = data;
    }
    
    @Override
    protected Iterable<?> guardedRefs() {
        return asList(data);
    }
}

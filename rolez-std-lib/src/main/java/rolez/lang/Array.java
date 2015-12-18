package rolez.lang;

/**
 * This class represents Java's implicit array class in the Rolez compiler.
 * 
 * @author Michael Faes
 */
public class Array<T> {
    
    public final int length;
    private final Object[] array;
    
    public Array(final int length) {
        this.length = length;
        this.array = new Object[length];
    }
    
    @SuppressWarnings("unchecked")
    public T get(final int i) {
        return (T) array[i];
    }
    
    public void set(final int i, final T element) {
        array[i] = element;
    }
}

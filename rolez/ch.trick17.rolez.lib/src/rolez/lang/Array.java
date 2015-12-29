package rolez.lang;

/**
 * This class represents Java's implicit array class in the Rolez compiler, but
 * is not actually used in Rolez programs.
 * 
 * @author Michael Faes
 */
@SuppressWarnings("unused")
public class Array<T> {
    
    public final int length = 0;
    
    public Array(final int length) {}
    
    public native T get(final int i);
    
    public native void set(final int i, final T element);
}

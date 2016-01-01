package rolez.lang;

/**
 * This class represents the Java array class in the Rolez compiler, but is not
 * actually used in Rolez programs. Instead the classes {@link ObjectArray},
 * {@link IntArray}, {@link DoubleArray}, {@link CharArray} , and
 * {@link BooleanArray} are used.
 * 
 * @author Michael Faes
 */
@SuppressWarnings("unused")
public class Array<T> extends Guarded {
    
    public final int length = 0;
    
    public Array(final int length) {}
    
    public native T get(final int i);
    
    public native void set(final int i, final T element);
}
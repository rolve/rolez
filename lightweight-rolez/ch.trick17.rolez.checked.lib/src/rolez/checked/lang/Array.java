package rolez.checked.lang;

/**
 * This class represents the interface of the <code>Array</code> class in the Rolez compiler, but is
 * not actually used in Rolez programs. Instead, the {@link CheckedArray} class is used.
 * 
 * @author Michael Faes
 */
public class Array<T> extends Slice<T> {
    
    public final int length = 0;
    
    public Array(int length) {}
    
    @Override
    public native CheckedArray<T[]> clone();
}

package rolez.lang;

/**
 * This class represents the interface of the <code>Array</code> class in the Rolez compiler, but is
 * not actually used in Rolez programs. Instead, the {@link GuardedArray} class is used.
 * 
 * @author Michael Faes
 */
@SuppressWarnings("unused")
public class Array<T> extends Slice<T> {
    
    public final int length = 0;
    
    public Array(int length) {}
}

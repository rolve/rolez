package rolez.lang;

/**
 * This class represents the interface of the <code>Vector</code> class in the Rolez compiler, but
 * is not actually used in Rolez programs. Instead, Java arrays are used directly.
 * 
 * @author Michael Faes
 */
public class Vector<T> {
    
    public final int length = 0;
    
    public native T get(int i);
}

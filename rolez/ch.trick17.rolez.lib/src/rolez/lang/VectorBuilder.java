package rolez.lang;

/**
 * This class represents the interface of the <code>VectorBuilder</code> class in the Rolez
 * compiler, but is not actually used in Rolez programs. Instead, {@link GuardedVectorBuilder} is
 * used.
 * 
 * @author Michael Faes
 */
public class VectorBuilder<T> extends Guarded {
    
    public VectorBuilder(int length) {}
    
    public native T get(int index);
    
    public native VectorBuilder<T> set(int index, T component);
    
    public native Vector<T> build();
}

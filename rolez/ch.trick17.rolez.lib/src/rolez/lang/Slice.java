package rolez.lang;

/**
 * This class represents the interface of the slice class in the Rolez compiler, but is not actually
 * used in Rolez programs. Instead the {@link GuardedSlice} class is used.
 * 
 * @author Michael Faes
 */
public class Slice<T> extends Guarded {
    
    public native T get(int i);
    
    public native void set(int i, T component);
    
    public native Slice<T> slice(SliceRange sliceRange);
    
    public native Slice<T> slice(int begin, int end, int step);
    
    public native Array<Slice<T>> partition(Partitioner p, int n);
}

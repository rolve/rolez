package rolez.lang;

/**
 * This class represents the interface of the slice class in the Rolez compiler, but is not actually
 * used in Rolez programs. Instead the {@link GuardedSlice} class is used.
 * 
 * @author Michael Faes
 */
public class Slice<T> extends Guarded {
    
    public final SliceRange range = null;
    
    public native T get(int i);
    
    public native void set(int i, T component);
    
    public native GuardedSlice<T[]> slice(SliceRange sliceRange);
    
    public native GuardedSlice<T[]> slice(int begin, int end, int step);
    
    public native GuardedArray<GuardedSlice<T[]>[]> partition(Partitioner p, int n);
}

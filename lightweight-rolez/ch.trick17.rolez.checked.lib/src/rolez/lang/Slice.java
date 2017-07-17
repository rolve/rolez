package rolez.lang;

/**
 * This class represents the interface of the <code>Slice</code> class in the Rolez compiler, but is
 * not actually used in Rolez programs. Instead the {@link CheckedSlice} class is used.
 * 
 * @author Michael Faes
 */
public class Slice<T> extends Checked {
    
    public final SliceRange range = null;
    
    public native int arrayLength();
    
    public native T get(int i);
    
    public native void set(int i, T component);
    
    @Safe
    public native CheckedSlice<T[]> slice(SliceRange sliceRange);
    
    @Safe
    public native CheckedSlice<T[]> slice(int begin, int end, int step);
    
    @Safe
    public native CheckedSlice<T[]> slice(int begin, int end);
    
    @Safe
    public native CheckedArray<CheckedSlice<T[]>[]> partition(Partitioner p, int n);
}

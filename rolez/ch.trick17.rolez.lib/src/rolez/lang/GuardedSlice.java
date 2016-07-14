package rolez.lang;

import static java.util.Collections.emptyList;
import static java.util.Collections.newSetFromMap;

import java.util.AbstractList;
import java.util.Collection;
import java.util.RandomAccess;
import java.util.Set;
import java.util.WeakHashMap;

public class GuardedSlice<A> extends Guarded {
    
    public final A data;
    public final SliceRange range;
    
    /* References to all slices that can at least one index in common. When a slice changes it role,
     * all overlapping slices change their role too. */
    final Set<GuardedSlice<A>> overlappingSlices = newSetFromMap(
            new WeakHashMap<GuardedSlice<A>, java.lang.Boolean>());
            
    GuardedSlice(A array, SliceRange range) {
        this.data = array;
        this.range = range;
    }
    
    @SuppressWarnings("unchecked")
    public <T> T get(int index) {
        checkIndex(index);
        return (T) ((Object[]) data)[index];
    }
    
    public void set(int index, Object component) {
        checkIndex(index);
        ((Object[]) data)[index] = component;
    }
    
    public int getInt(int index) {
        checkIndex(index);
        return ((int[]) data)[index];
    }
    
    public void setInt(int index, int component) {
        checkIndex(index);
        ((int[]) data)[index] = component;
    }
    
    public double getDouble(int index) {
        checkIndex(index);
        return ((double[]) data)[index];
    }
    
    public void setDouble(int index, double component) {
        checkIndex(index);
        ((double[]) data)[index] = component;
    }
    
    public boolean getBoolean(int index) {
        checkIndex(index);
        return ((boolean[]) data)[index];
    }
    
    public void setBoolean(int index, boolean component) {
        checkIndex(index);
        ((boolean[]) data)[index] = component;
    }
    
    public char getChar(int index) {
        checkIndex(index);
        return ((char[]) data)[index];
    }
    
    public void setChar(int index, char component) {
        checkIndex(index);
        ((char[]) data)[index] = component;
    }
    
    private void checkIndex(int index) {
        // IMPROVE: Suppress check for arrays? (only applies if array is statically a slice)
        if(!range.contains(index))
            throw new SliceIndexOutOfBoundsException(index);
    }
    
    public GuardedSlice<A> slice(SliceRange sliceRange) {
        if(!range.covers(sliceRange))
            throw new IllegalArgumentException("Given range: " + sliceRange
                    + " is not covered by this slice's range: " + range);
                    
        GuardedSlice<A> slice = new GuardedSlice<>(data, sliceRange);
        /* Make sure the slice has a guard (guards are normally created when the role of an object
         * changes, but objects with views can change their effective role without being passed or
         * shared themselves, so they need a guard from the start) */
        slice.getGuard();
        
        if(!slice.range.isEmpty()) {
            /* Make sure the new slice is not added while existing slices are being processed */
            synchronized(viewLock()) {
                /* New slice can only overlap with overlapping slices of this slice */
                for(GuardedSlice<A> other : overlappingSlices)
                    if(!slice.range.intersectWith(other.range).isEmpty()) {
                        slice.overlappingSlices.add(other);
                        other.overlappingSlices.add(slice);
                    }
                overlappingSlices.add(slice);
                slice.overlappingSlices.add(this);
            }
        }
        return slice;
    }
    
    public final GuardedSlice<A> slice(int begin, int end, int step) {
        return slice(new SliceRange(begin, end, step));
    }
    
    // TODO: Replace return type with some final or even immutable array class
    @SuppressWarnings("unchecked")
    public final GuardedArray<GuardedSlice<A>[]> partition(Partitioner p, int n) {
        final GuardedArray<SliceRange[]> ranges = p.partition(range, n);
        GuardedSlice<A>[] slices = new GuardedSlice[n];
        for(int i = 0; i < ranges.data.length; i++)
            slices[i] = slice(ranges.data[i]);
        return new GuardedArray<>(slices);
    }
    
    @Override
    protected final Iterable<?> guardedRefs() {
        if(data instanceof Object[])
            return new SliceList();
        else
            return emptyList();
    }
    
    @Override
    protected final Collection<? extends Guarded> views() {
        return overlappingSlices;
    }
    
    @Override
    protected Object viewLock() {
        return data; // The data array is the same for all slices, so it can act as the view lock.
                     // However, care must be taken to not expose the array to code that may lock it.
    }
    
    private final class SliceList extends AbstractList<Object> implements RandomAccess {
        @Override
        public Object get(final int index) {
            return ((Object[]) data)[range.begin + index * range.step];
        }
        
        @Override
        public Object set(int index, Object element) {
            final int i = range.begin + index * range.step;
            final Object previous = ((Object[]) data)[i];
            ((Object[]) data)[i] = element;
            return previous;
        }
        
        @Override
        public int size() {
            return range.size();
        }
    }
    
    @Override
    public final String toString() {
        final String result = "(" + range.begin + ":" + range.end + "%" + range.step + ")";
        // TODO: Append content
        return result;
    }
}

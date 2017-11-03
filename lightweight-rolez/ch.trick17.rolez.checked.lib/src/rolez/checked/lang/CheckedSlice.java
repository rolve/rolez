package rolez.checked.lang;

import static java.util.Collections.emptyList;
import static java.util.Collections.newSetFromMap;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Collection;
import java.util.RandomAccess;
import java.util.Set;
import java.util.WeakHashMap;

import rolez.checked.lang.annotation.Read;
import rolez.checked.lang.annotation.Write;

public class CheckedSlice<A> extends Checked {
    
    protected final A data;
    protected final SliceRange range;
    
    /* References to all slices that can at least one index in common. When a slice changes it role,
     * all overlapping slices change their role too. */
    final Set<CheckedSlice<A>> overlappingSlices = newSetFromMap(
            new WeakHashMap<CheckedSlice<A>, java.lang.Boolean>());
    
    CheckedSlice(A array, SliceRange range) {
        /* Slices can have views and therefore are initialized eagerly */
        super(true);
        this.data = array;
        this.range = range;
    }
    
    @Read
    public int arrayLength() {
        return Array.getLength(data);
    }
    
    @Read
    public A getUncheckedArrayRead() {
    	return this.data;
    }

    @Write
    public A getUncheckedArrayWrite() {
    	return this.data;
    }
    
    @Read
    public SliceRange getSliceRange() {
    	return this.range;
    }
    
    @Read
    @SuppressWarnings("unchecked")
    public <T> T get(int index) {
        checkIndex(index);
        return (T) ((Object[]) data)[index];
    }

    @Write
    public void set(int index, Object component) {
        checkIndex(index);
        ((Object[]) data)[index] = component;
    }

    @Read
    public double getDouble(int index) {
        checkIndex(index);
        return ((double[]) data)[index];
    }

    @Write
    public void setDouble(int index, double component) {
        checkIndex(index);
        ((double[]) data)[index] = component;
    }

    @Read
    public long getLong(int index) {
        checkIndex(index);
        return ((long[]) data)[index];
    }

    @Write
    public void setLong(int index, long component) {
        checkIndex(index);
        ((long[]) data)[index] = component;
    }

    @Read
    public int getInt(int index) {
        checkIndex(index);
        return ((int[]) data)[index];
    }

    @Write
    public void setInt(int index, int component) {
        checkIndex(index);
        ((int[]) data)[index] = component;
    }

    @Read
    public short getShort(int index) {
        checkIndex(index);
        return ((short[]) data)[index];
    }

    @Write
    public void setShort(int index, short component) {
        checkIndex(index);
        ((short[]) data)[index] = component;
    }

    @Read
    public byte getByte(int index) {
        checkIndex(index);
        return ((byte[]) data)[index];
    }

    @Write
    public void setByte(int index, byte component) {
        checkIndex(index);
        ((byte[]) data)[index] = component;
    }

    @Read
    public boolean getBoolean(int index) {
        checkIndex(index);
        return ((boolean[]) data)[index];
    }

    @Write
    public void setBoolean(int index, boolean component) {
        checkIndex(index);
        ((boolean[]) data)[index] = component;
    }

    @Read
    public char getChar(int index) {
        checkIndex(index);
        return ((char[]) data)[index];
    }
    
    @Write
    public void setChar(int index, char component) {
        checkIndex(index);
        ((char[]) data)[index] = component;
    }
    
    private void checkIndex(int index) {
        // IMPROVE: Suppress check for arrays? (only applies if array is statically a slice)
        if(!range.contains(index))
            throw new SliceIndexOutOfBoundsException(index);
    }

    @Read
    public CheckedSlice<A> slice(SliceRange sliceRange) {
        if(!range.covers(sliceRange))
            throw new IllegalArgumentException("Given range: " + sliceRange
                    + " is not covered by this slice's range: " + range);
        
        CheckedSlice<A> slice = new CheckedSlice<>(data, sliceRange);
        if(!slice.range.isEmpty()) {
            /* Make sure the new slice is not added while existing slices are being processed */
            synchronized(viewLock()) {
                /* New slice can only overlap with overlapping slices of this slice */
                for(CheckedSlice<A> other : overlappingSlices)
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
    
    @Read
    public final CheckedSlice<A> slice(int begin, int end, int step) {
        return slice(new SliceRange(begin, end, step));
    }
    
    @Read
    public final CheckedSlice<A> slice(int begin, int end) {
        return slice(begin, end, 1);
    }
    
    // TODO: Replace return type with some final or even immutable array class
    @SuppressWarnings("unchecked")
    public final CheckedArray<CheckedSlice<A>[]> partition(Partitioner p, int n) {
        SliceRange[] ranges = p.partition(range, n);
        CheckedSlice<A>[] slices = new CheckedSlice[n];
        for(int i = 0; i < ranges.length; i++)
            slices[i] = slice(ranges[i]);
        return new CheckedArray<>(slices);
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

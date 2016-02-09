package rolez.lang;

import static java.util.Collections.emptyList;
import static java.util.Collections.newSetFromMap;

import java.util.AbstractList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.RandomAccess;
import java.util.Set;

public class GuardedSlice<A> extends Guarded {
    
    public final A data;
    public final SliceRange range;
    
    /* References to all (direct) subslices. Guarding a slice must also consider subslices, which
     * may currently be "owned" by a different task. */
    final Set<GuardedSlice<A>> subslices = newSetFromMap(
            new IdentityHashMap<GuardedSlice<A>, Boolean>());
            
    // TODO: Find a way to "free" unused subslices again
    
    GuardedSlice(A array, SliceRange range) {
        this.data = array;
        this.range = range;
    }
    
    public GuardedSlice<A> slice(final SliceRange sliceRange) {
        if(!range.covers(sliceRange))
            throw new IllegalArgumentException("Given range: " + sliceRange
                    + " is not covered by this slice's range: " + range);
                    
        final GuardedSlice<A> slice = new GuardedSlice<>(data, sliceRange);
        
        /* Make sure the new slice is not added while existing slices are being processed */
        synchronized(viewLock()) {
            getGuard().initializeViewGuard(slice.getGuard());
            addSubslice(slice);
        }
        return slice;
    }
    
    public final GuardedSlice<A> slice(final int begin, final int end, final int step) {
        return slice(new SliceRange(begin, end, step));
    }
    
    // TODO: Replace return type with some final or even immutable array class
    public final GuardedArray<GuardedSlice<A>[]> partition(final Partitioner p, final int n) {
        final GuardedArray<SliceRange[]> ranges = p.partition(range, n);
        GuardedSlice<A>[] slices = new GuardedSlice[n];
        for(int i = 0; i < ranges.data.length; i++)
            slices[i] = slice(ranges.data[i]);
        return new GuardedArray<>(slices);
    }
    
    private void addSubslice(final GuardedSlice<A> slice) {
        // IMPROVE: Better way to handle empty slices?
        if(slice.range.isEmpty()) {
            subslices.add(slice);
            return;
        }
        
        /* If an existing subslice covers the new one completely, delegate all the work to this one
         * and be done */
        for(final GuardedSlice<A> s : subslices) {
            if(s.range.covers(slice.range)) {
                s.addSubslice(slice);
                return;
            }
        }
        
        /* Otherwise, check which existing subslices intersect with (and specifically, are covered
         * by) the new one */
        final Iterator<GuardedSlice<A>> i = subslices.iterator();
        while(i.hasNext()) {
            final GuardedSlice<A> subslice = i.next();
            if(slice.range.covers(subslice.range)) {
                /* Put the new slice between "this" and the subslice */
                i.remove();
                slice.subslices.add(subslice);
            }
            else {
                final SliceRange overlap = subslice.range.intersectWith(slice.range);
                if(!overlap.isEmpty()) {
                    final GuardedSlice<A> overlapSlice = new GuardedSlice<>(data, overlap);
                    subslice.getGuard().initializeViewGuard(overlapSlice.getGuard());
                    slice.subslices.add(overlapSlice);
                    subslice.addSubslice(overlapSlice);
                }
            }
        }
        
        subslices.add(slice);
    }
    
    @Override
    protected final Iterable<?> guardedRefs() {
        if(data instanceof Object[])
            return new SliceList();
        else
            return emptyList();
    }
    
    @Override
    protected final Iterable<? extends Guarded> views() {
        return subslices;
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
        public Object set(final int index, final Object element) {
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

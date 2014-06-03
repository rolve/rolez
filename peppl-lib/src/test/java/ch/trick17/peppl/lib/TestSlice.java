package ch.trick17.peppl.lib;

import static java.util.Collections.newSetFromMap;

import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import ch.trick17.peppl.lib.SliceRange;
import ch.trick17.peppl.lib.guard.BaseSlice;
import ch.trick17.peppl.lib.guard.IntArray;
import ch.trick17.peppl.lib.guard.Slice;

/**
 * A slice of an {@link IntArray}. See {@link BaseSlice} for more information.
 * 
 * @author Michael Faes
 * @see Slice
 */
public class TestSlice {
    
    final AtomicInteger sharedCount = new AtomicInteger(0);
    
    public final SliceRange range;
    public final int[] data;
    
    final Set<TestSlice> subslices =
            newSetFromMap(new WeakHashMap<TestSlice, Boolean>());
    
    TestSlice(final SliceRange range, final int[] data) {
        this.range = range;
        this.data = data;
    }
    
    final TestSlice createSlice(final SliceRange sliceRange) {
        return new TestSlice(sliceRange, data);
    }
    
    public final TestSlice slice(final SliceRange sliceRange) {
        if(!range.covers(sliceRange))
            throw new IllegalArgumentException("Given range: " + sliceRange
                    + " is not covered by this slice's range: " + range);
        
        final TestSlice slice = createSlice(sliceRange);
        addSubslice(slice);
        
        return slice;
    }
    
    void addSubslice(final TestSlice slice) {
        
        /* Otherwise, check which existing subslices intersect with (and
         * specifically, are covered by) the new one. */
        final Iterator<TestSlice> i = subslices.iterator();
        while(i.hasNext()) {
            final TestSlice subslice = i.next();
            if(slice.range.covers(subslice.range)) {
                /* Put the new slice between "this" and the subslice. First,
                 * remove old links */
                i.remove();
                /* Add new links */
                slice.subslices.add(subslice);
            }
            else {
                final SliceRange overlap =
                        subslice.range.intersectWith(slice.range);
                if(!overlap.isEmpty()) {
                    final TestSlice overlapSlice = createSlice(overlap);
                    slice.subslices.add(overlapSlice);
                    subslice.addSubslice(overlapSlice);
                }
            }
        }
        
        subslices.add(slice);
    }
    
    public final TestSlice slice(final int begin, final int end, final int step) {
        return slice(new SliceRange(begin, end, step));
    }
    
    final void processViews(final Op op, final Set<TestSlice> processed) {
        // Same as processViewsRecursive, except "this" is not processed
        for(final TestSlice view : subslices)
            if(view != null)
                view.processViewsRecursive(op, processed);
    }
    
    void processViewsRecursive(final Op op, final Set<TestSlice> processed) {
        if(processed.add(this)) {
            for(final TestSlice view : subslices)
                if(view != null)
                    view.processViewsRecursive(op, processed);
            
            op.process(this);
        }
    }
    
    interface Op {
        void process(TestSlice slice);
    }
}

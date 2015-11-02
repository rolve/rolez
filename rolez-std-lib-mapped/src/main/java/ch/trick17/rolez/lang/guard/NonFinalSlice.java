package ch.trick17.rolez.lang.guard;

import static java.util.Collections.newSetFromMap;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Set;

import ch.trick17.rolez.lang.SliceRange;

abstract class NonFinalSlice<S extends NonFinalSlice<S>> extends BaseSlice<S> {
    
    /* References to all (direct) subslices. Guarding a slice must also consider
     * subslices, which may currently be "owned" by a different task. */
    final Set<S> subslices = newSetFromMap(new IdentityHashMap<S, Boolean>());
    
    // TODO: Find a way to "free" unused subslices again
    
    NonFinalSlice(final SliceRange range) {
        super(range);
    }
    
    @Override
    public final S slice(final SliceRange sliceRange) {
        if(!range.covers(sliceRange))
            throw new IllegalArgumentException("Given range: " + sliceRange + " is not covered by this slice's range: "
                    + range);
                    
        final S slice = createSlice(sliceRange);
        
        /* Make sure the new slice is not added while existing slices are being
         * processed. */
        synchronized(viewLock) {
            getGuard().initializeViewGuard(slice.getGuard());
            addSubslice(slice);
        }
        return slice;
    }
    
    void addSubslice(final S slice) {
        // IMPROVE: Better way to handle empty slices?
        if(slice.range.isEmpty()) {
            subslices.add(slice);
            return;
        }
        
        /* If an existing subslice covers the new one completely, delegate all
         * the work to this one and be done. */
        for(final NonFinalSlice<S> s : subslices) {
            if(s.range.covers(slice.range)) {
                s.addSubslice(slice);
                return;
            }
        }
        
        /* Otherwise, check which existing subslices intersect with (and
         * specifically, are covered by) the new one. */
        final Iterator<S> i = subslices.iterator();
        while(i.hasNext()) {
            final S subslice = i.next();
            if(slice.range.covers(subslice.range)) {
                /* Put the new slice between "this" and the subslice. First,
                 * remove old links */
                i.remove();
                /* Add new links */
                slice.subslices.add(subslice);
            }
            else {
                final SliceRange overlap = subslice.range.intersectWith(slice.range);
                if(!overlap.isEmpty()) {
                    final S overlapSlice = createSlice(overlap);
                    slice.subslices.add(overlapSlice);
                    subslice.addSubslice(overlapSlice);
                }
            }
        }
        
        subslices.add(slice);
    }
    
    abstract S createSlice(SliceRange sliceRange);
    
    @Override
    protected final Iterable<S> views() {
        return subslices;
    }
}

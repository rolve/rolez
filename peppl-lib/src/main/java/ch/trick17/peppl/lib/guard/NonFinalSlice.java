package ch.trick17.peppl.lib.guard;

import static java.util.Collections.newSetFromMap;

import java.util.IdentityHashMap;
import java.util.Set;
import java.util.WeakHashMap;

import ch.trick17.peppl.lib.SliceRange;

abstract class NonFinalSlice<S extends NonFinalSlice<S>> extends BaseSlice<S> {
    
    /* References to all (direct) subslices. Guarding a slice must also consider
     * subslices, which may currently be "owned" by a different task. References
     * to subslices are weak, i.e., they don't prevent subslices to be
     * garbage-collected if they are not used anymore. However, see below. */
    private final Set<S> subslices =
            newSetFromMap(new WeakHashMap<S, Boolean>());
    
    /* References to superslices are kept to prevent them from being
     * garbage-collected as long as they have referenced subslices. This
     * prevents slices from getting disconnected from their parents's parents,
     * which would prevent proper guarding. */
    private final Set<NonFinalSlice<?>> superslices =
            newSetFromMap(new IdentityHashMap<NonFinalSlice<?>, Boolean>());
    
    NonFinalSlice(final SliceRange range) {
        super(range);
    }
    
    @Override
    public final S slice(final SliceRange sliceRange) {
        if(!range.covers(sliceRange))
            throw new IllegalArgumentException("Given range: " + sliceRange
                    + " is not covered by this slice's range: " + range);
        
        final S slice = createSlice(sliceRange);
        
        /* Make sure the new slice is not added while existing slices are being
         * processed. */
        synchronized(viewLock) {
            getGuard().initializeViewGuard(slice.getGuard());
            registerSlice(slice);
        }
        return slice;
    }
    
    private void registerSlice(final S slice) {
        // TODO: How to handle empty slice best?
        for(final NonFinalSlice<S> subslice : subslices) {
            if(subslice.range.covers(slice.range)) {
                subslice.registerSlice(slice);
                return;
            }
        }
        
        subslices.add(slice);
        ((NonFinalSlice<S>) slice).superslices.add(this);
    }
    
    abstract S createSlice(SliceRange sliceRange);
    
    @Override
    Iterable<? extends Guarded> views() {
        return subslices;
    }
}

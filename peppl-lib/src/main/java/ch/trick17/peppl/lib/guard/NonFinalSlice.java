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
        assert sliceRange.begin >= range.begin;
        assert sliceRange.end <= range.end;
        assert sliceRange.step >= range.step;
        
        final S slice = createSlice(sliceRange);
        getGuard().initializeViewGuard(slice.getGuard());
        
        subslices.add(slice);
        ((NonFinalSlice<S>) slice).superslices.add(this);
        return slice;
    }
    
    abstract S createSlice(SliceRange sliceRange);
    
    @Override
    Iterable<? extends Guarded> views() {
        return subslices;
    }
}

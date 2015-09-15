package ch.trick17.rolez.lib.guard;

import static java.util.Collections.emptySet;

import ch.trick17.rolez.lib.SliceRange;

/**
 * A slice of a {@link LongArray}. See {@link BaseSlice} for more information.
 * 
 * @author Michael Faes
 * @see Slice
 */
public class LongSlice extends NonFinalSlice<LongSlice> {
    
    public final long[] data;
    
    LongSlice(final SliceRange range, final long[] data) {
        super(range);
        this.data = data;
    }
    
    @Override
    final LongSlice createSlice(final SliceRange sliceRange) {
        return new LongSlice(sliceRange, data);
    }
    
    @Override
    final Iterable<? extends Guarded> guardedRefs() {
        return emptySet();
    }
}

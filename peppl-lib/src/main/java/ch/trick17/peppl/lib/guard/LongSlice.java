package ch.trick17.peppl.lib.guard;

import java.util.Collections;

import ch.trick17.peppl.lib.SliceRange;

/**
 * A slice of a {@link LongArray}. See {@link BaseSlice} for more information.
 * 
 * @author Michael Faes
 * @see Slice
 */
public class LongSlice extends BaseSlice<LongSlice> {
    
    public final long[] data;
    
    LongSlice(final SliceRange range, final long[] data) {
        super(range);
        assert range.end <= data.length;
        this.data = data;
    }
    
    @Override
    final LongSlice createSlice(final SliceRange sliceRange) {
        return new LongSlice(sliceRange, data);
    }
    
    @Override
    final Iterable<? extends Guarded> guardedRefs() {
        return Collections.unmodifiableSet(subslices);
    }
}

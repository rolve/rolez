package ch.trick17.peppl.lib.guard;

import java.util.Collections;

import ch.trick17.peppl.lib.SliceRange;

/**
 * A slice of an {@link IntArray}. See {@link BaseSlice} for more information.
 * 
 * @author Michael Faes
 * @see Slice
 */
public class IntSlice extends BaseSlice<IntSlice> {
    
    public final int[] data;
    
    IntSlice(final SliceRange range, final int[] data) {
        super(range);
        assert range.end <= data.length;
        this.data = data;
    }
    
    @Override
    final IntSlice createSlice(final SliceRange sliceRange) {
        return new IntSlice(sliceRange, data);
    }
    
    @Override
    final Iterable<? extends Guarded> guardedRefs() {
        return Collections.unmodifiableSet(subslices);
    }
}

package ch.trick17.peppl.lib.guard;

import java.util.Collections;

import ch.trick17.peppl.lib.SliceRange;

/**
 * A slice of a {@link DoubleArray}. See {@link BaseSlice} for more information.
 * 
 * @author Michael Faes
 * @see Slice
 */
public class DoubleSlice extends BaseSlice<DoubleSlice> {
    
    public final double[] data;
    
    DoubleSlice(final SliceRange range, final double[] data) {
        super(range);
        assert range.end <= data.length;
        this.data = data;
    }
    
    @Override
    final DoubleSlice createSlice(final SliceRange sliceRange) {
        return new DoubleSlice(sliceRange, data);
    }
    
    @Override
    final Iterable<? extends Guarded> guardedRefs() {
        return Collections.unmodifiableSet(subslices);
    }
}

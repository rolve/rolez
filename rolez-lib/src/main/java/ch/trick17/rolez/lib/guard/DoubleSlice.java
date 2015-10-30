package ch.trick17.rolez.lib.guard;

import static java.util.Collections.emptySet;

import ch.trick17.rolez.lib.SliceRange;

/**
 * A slice of a {@link DoubleArray}. See {@link BaseSlice} for more information.
 * 
 * @author Michael Faes
 * @see Slice
 */
public class DoubleSlice extends NonFinalSlice<DoubleSlice> {
    
    public final double[] data;
    
    DoubleSlice(final SliceRange range, final double[] data) {
        super(range);
        this.data = data;
    }
    
    @Override
    final DoubleSlice createSlice(final SliceRange sliceRange) {
        return new DoubleSlice(sliceRange, data);
    }
    
    @Override
    protected final Iterable<? extends Guarded> guardedRefs() {
        return emptySet();
    }
}

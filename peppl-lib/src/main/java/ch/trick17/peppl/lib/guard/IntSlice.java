package ch.trick17.peppl.lib.guard;

import java.util.Collections;

/**
 * A slice of an {@link IntArray}. See {@link BaseSlice} for more information.
 * 
 * @author Michael Faes
 * @see Slice
 */
public class IntSlice extends BaseSlice<IntSlice> {
    
    public final int[] data;
    
    IntSlice(final int[] data, final int beginIndex, final int endIndex,
            final int stepSize) {
        super(beginIndex, endIndex, stepSize);
        assert endIndex <= data.length;
        this.data = data;
    }
    
    @Override
    final IntSlice createSlice(final int beginIndex, final int endIndex,
            final int stepSize) {
        return new IntSlice(data, beginIndex, endIndex, stepSize);
    }
    
    @Override
    final Iterable<? extends Guarded> guardedRefs() {
        return Collections.unmodifiableSet(subslices);
    }
}

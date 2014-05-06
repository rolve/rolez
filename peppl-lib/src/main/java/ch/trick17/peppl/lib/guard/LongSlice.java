package ch.trick17.peppl.lib.guard;

import java.util.Collections;

/**
 * A slice of a {@link LongArray}. See {@link BaseSlice} for more information.
 * 
 * @author Michael Faes
 * @see Slice
 */
public class LongSlice extends BaseSlice<LongSlice> {
    
    public final long[] data;
    
    LongSlice(final long[] data, final int beginIndex, final int endIndex,
            final int stepSize) {
        super(beginIndex, endIndex, stepSize);
        assert endIndex <= data.length;
        this.data = data;
    }
    
    @Override
    final LongSlice createSlice(final int beginIndex, final int endIndex,
            final int stepSize) {
        return new LongSlice(data, beginIndex, endIndex, stepSize);
    }
    
    @Override
    final Iterable<? extends Guarded> guardedRefs() {
        return Collections.unmodifiableSet(subslices);
    }
}

package ch.trick17.peppl.lib.guard;

import java.util.Collections;

public class LongSlice extends BaseSlice<LongSlice> {
    
    public final long[] data;
    
    LongSlice(final long[] data, final int beginIndex, final int endIndex) {
        super(beginIndex, endIndex);
        assert endIndex <= data.length;
        this.data = data;
    }
    
    @Override
    final LongSlice createSlice(final int beginIndex, final int endIndex) {
        return new LongSlice(data, beginIndex, endIndex);
    }
    
    @Override
    final Iterable<? extends Guarded> guardedRefs() {
        return Collections.unmodifiableSet(subslices);
    }
}

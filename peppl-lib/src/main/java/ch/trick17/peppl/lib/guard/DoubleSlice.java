package ch.trick17.peppl.lib.guard;

import java.util.Collections;

public class DoubleSlice extends AbstractSlice<DoubleSlice> {
    
    public final double[] data;
    
    DoubleSlice(final double[] data, final int beginIndex, final int endIndex) {
        super(beginIndex, endIndex);
        assert endIndex <= data.length;
        this.data = data;
    }
    
    @Override
    final DoubleSlice createSlice(final int beginIndex, final int endIndex) {
        return new DoubleSlice(data, beginIndex, endIndex);
    }
    
    @Override
    final Iterable<? extends Guarded> guardedRefs() {
        return Collections.unmodifiableSet(subslices);
    }
}

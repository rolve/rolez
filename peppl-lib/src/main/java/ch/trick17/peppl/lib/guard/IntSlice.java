package ch.trick17.peppl.lib.guard;

import java.util.Collections;

public class IntSlice extends AbstractSlice<IntSlice> {
    
    public final int[] data;
    
    IntSlice(final int[] data, final int beginIndex, final int endIndex) {
        super(beginIndex, endIndex);
        assert endIndex <= data.length;
        this.data = data;
    }
    
    @Override
    final IntSlice createSlice(final int beginIndex, final int endIndex) {
        return new IntSlice(data, beginIndex, endIndex);
    }
    
    @Override
    final Iterable<? extends Guarded> guardedRefs() {
        return Collections.unmodifiableSet(subslices);
    }
}

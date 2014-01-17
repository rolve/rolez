package ch.trick17.peppl.lib.guard;

import java.util.Collections;

public class IntSlice extends AbstractSlice<IntSlice> {
    
    public final int[] data;
    
    IntSlice(final int[] data, final int beginIndex, final int endIndex) {
        super(beginIndex, endIndex);
        assert beginIndex >= 0 && beginIndex <= data.length;
        assert endIndex > beginIndex && endIndex <= data.length;
        this.data = data;
    }
    
    @Override
    final IntSlice createSlice(final int beginIndex, final int endIndex) {
        return new IntSlice(data, beginIndex, endIndex);
    }
    
    @Override
    final Iterable<? extends Guarded> allRefs() {
        return Collections.unmodifiableSet(subslices);
    }
}

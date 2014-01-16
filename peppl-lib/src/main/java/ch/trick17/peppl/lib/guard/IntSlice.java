package ch.trick17.peppl.lib.guard;

import java.util.Collections;

public class IntSlice extends Guarded {
    
    public final int[] data;
    public final int begin;
    public final int end;
    
    IntSlice(final int[] data, final int beginIndex, final int endIndex) {
        assert beginIndex >= 0 && beginIndex <= data.length;
        assert endIndex > beginIndex && endIndex <= data.length;
        this.data = data;
        this.begin = beginIndex;
        this.end = endIndex;
    }
    
    public final IntSlice slice(final int beginIndex, final int endIndex) {
        assert beginIndex >= begin;
        assert endIndex <= end;
        return new IntSlice(data, beginIndex, endIndex);
    }
    
    @Override
    final Iterable<? extends Guarded> allRefs() {
        return Collections.emptyList();
    }
}

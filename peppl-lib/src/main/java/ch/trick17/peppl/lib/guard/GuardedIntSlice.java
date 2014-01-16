package ch.trick17.peppl.lib.guard;

import java.util.Collections;

public final class GuardedIntSlice extends GuardedIntArray {
    
    public final int begin;
    public final int end;
    
    GuardedIntSlice(final int[] data, final int beginIndex, final int endIndex) {
        super(data);
        assert beginIndex >= 0 && beginIndex <= data.length;
        assert endIndex > beginIndex && endIndex <= data.length;
        this.begin = beginIndex;
        this.end = endIndex;
    }
    
    @Override
    public GuardedIntSlice slice(final int beginIndex, final int endIndex) {
        assert beginIndex >= begin;
        assert endIndex <= end;
        return super.slice(beginIndex, endIndex);
    }
    
    @Override
    Iterable<? extends Guarded> allRefs() {
        return Collections.emptyList();
    }
}

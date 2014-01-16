package ch.trick17.peppl.lib.guard;

import java.util.AbstractList;

public final class GuardedSlice<E extends Guarded> extends GuardedArray<E> {
    
    public final int begin;
    public final int end;
    
    GuardedSlice(final E[] data, final int beginIndex, final int endIndex) {
        super(data);
        assert beginIndex >= 0 && beginIndex <= data.length;
        assert endIndex > beginIndex && endIndex <= data.length;
        this.begin = beginIndex;
        this.end = endIndex;
    }
    
    @Override
    public GuardedSlice<E> slice(final int beginIndex, final int endIndex) {
        assert beginIndex >= begin;
        assert endIndex <= end;
        return super.slice(beginIndex, endIndex);
    }
    
    @Override
    Iterable<? extends Guarded> allRefs() {
        return new AbstractList<Guarded>() {
            @Override
            public Guarded get(final int index) {
                return data[begin + index];
            }
            
            @Override
            public int size() {
                return end - begin;
            }
        };
    }
}

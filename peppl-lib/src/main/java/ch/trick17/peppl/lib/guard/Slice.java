package ch.trick17.peppl.lib.guard;

import java.util.AbstractList;

public class Slice<E extends Guarded> extends Guarded {
    
    public final E[] data;
    public final int begin;
    public final int end;
    
    Slice(final E[] data, final int beginIndex, final int endIndex) {
        assert beginIndex >= 0 && beginIndex <= data.length;
        assert endIndex > beginIndex && endIndex <= data.length;
        this.data = data;
        this.begin = beginIndex;
        this.end = endIndex;
    }
    
    public final Slice<E> slice(final int beginIndex, final int endIndex) {
        assert beginIndex >= begin;
        assert endIndex <= end;
        return new Slice<E>(data, beginIndex, endIndex);
    }
    
    @Override
    final Iterable<? extends Guarded> allRefs() {
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

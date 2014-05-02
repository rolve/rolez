package ch.trick17.peppl.lib.guard;

import static java.util.Collections.newSetFromMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

abstract class BaseSlice<S extends BaseSlice<?>> extends Guarded {
    
    public final int begin;
    public final int end;
    final Set<S> subslices = newSetFromMap(new WeakHashMap<S, Boolean>());
    
    public BaseSlice(final int beginIndex, final int endIndex) {
        assert beginIndex >= 0;
        assert endIndex >= beginIndex;
        begin = beginIndex;
        end = endIndex;
    }
    
    public final int length() {
        return end - begin;
    }
    
    public final S slice(final int beginIndex, final int endIndex) {
        assert beginIndex >= begin;
        assert endIndex <= end;
        
        final S slice = createSlice(beginIndex, endIndex);
        subslices.add(slice);
        return slice;
    }
    
    public final List<S> partition(final int n) {
        final int baseSize = length() / n;
        final int largeSlices = length() % n;
        
        final ArrayList<S> slices = new ArrayList<>(n);
        int beginIndex = begin;
        for(int i = 0; i < n; i++) {
            final int endIndex = beginIndex + baseSize
                    + (i < largeSlices ? 1 : 0);
            slices.add(slice(beginIndex, endIndex));
            beginIndex = endIndex;
        }
        assert slices.size() == n;
        assert beginIndex == end;
        return slices;
    }
    
    abstract S createSlice(int beginIndex, int endIndex);
    
    @Override
    public void guardRead() {
        super.guardRead();
        for(final S slice : subslices)
            slice.guardRead();
    }
    
    @Override
    public void guardReadWrite() {
        super.guardReadWrite();
        for(final S slice : subslices)
            slice.guardReadWrite();
    }
}

package ch.trick17.peppl.lib.guard;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

abstract class AbstractSlice<S extends AbstractSlice<?>> extends Guarded {
    
    public final int begin;
    public final int end;
    final Set<S> subslices = Collections
            .newSetFromMap(new WeakHashMap<S, Boolean>());
    
    public AbstractSlice(final int beginIndex, final int endIndex) {
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

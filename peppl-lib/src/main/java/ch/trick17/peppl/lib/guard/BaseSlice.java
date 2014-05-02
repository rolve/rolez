package ch.trick17.peppl.lib.guard;

import static java.util.Collections.newSetFromMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import ch.trick17.peppl.lib.task.Task;

/**
 * Base class for all {@link Guarded} slices, e.g. {@link Slice} (for reference
 * types) or {@link IntSlice}. A slice represents a contiguous part of an array,
 * e.g. {@link Array} (for reference types) or {@link IntArray}. Note that an
 * array is just a special case of a slice, namely a slice that covers the whole
 * array.
 * <p>
 * <code>Slice</code> and <code>Array</code> classes in the
 * {@link ch.trick17.peppl.lib.guard} package are {@link Guarded}, i.e., they
 * are mutable objects that can be passed to and shared with other {@link Task}
 * s. Like with other guarded objects, read and write access to members (i.e.
 * elements) of guarded slices has to be guarded explicitly by the client code.
 * That is why slices expose the underlying array directly as a public field.
 * <p>
 * A slice can be created from existing slices (and therefore from arrays) using
 * the {@link #slice(int, int)} method. Slices created that way "belong" to the
 * slice they were created from, in terms of {@linkplain #share() sharing},
 * {@linkplain #pass() passing} and of course reading and writing.
 * 
 * @author Michael Faes
 * @param <S>
 *            The concrete slice type.
 */
abstract class BaseSlice<S extends BaseSlice<S>> extends Guarded {
    
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

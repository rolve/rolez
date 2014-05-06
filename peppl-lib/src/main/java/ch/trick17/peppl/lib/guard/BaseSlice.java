package ch.trick17.peppl.lib.guard;

import static java.util.Collections.newSetFromMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import ch.trick17.peppl.lib.Partitioner;
import ch.trick17.peppl.lib.Partitioner.SliceDef;
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
    public final int step;
    
    final Set<S> subslices = newSetFromMap(new WeakHashMap<S, Boolean>());
    
    public BaseSlice(final int beginIndex, final int endIndex,
            final int stepSize) {
        assert beginIndex >= 0;
        assert endIndex >= beginIndex;
        assert stepSize > 0;
        begin = beginIndex;
        end = endIndex;
        step = stepSize;
    }
    
    public final int size() {
        final int length = end - begin;
        return (length - 1) / step + 1;
    }
    
    public final S slice(final int beginIndex, final int endIndex) {
        return slice(beginIndex, endIndex, 1);
    }
    
    public final S slice(final int beginIndex, final int endIndex,
            final int stepSize) {
        assert beginIndex >= begin;
        assert endIndex <= end;
        assert stepSize >= step;
        
        final S slice = createSlice(beginIndex, endIndex, stepSize);
        subslices.add(slice);
        return slice;
    }
    
    public final List<S> partition(final Partitioner p, final int n) {
        final List<SliceDef> defs =
                p.partition(new SliceDef(begin, end, step), n);
        final List<S> slices = new ArrayList<>(n);
        for(final SliceDef def : defs)
            slices.add(slice(def.begin, def.end, def.step));
        return slices;
    }
    
    abstract S createSlice(int beginIndex, int endIndex, int stepSize);
    
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

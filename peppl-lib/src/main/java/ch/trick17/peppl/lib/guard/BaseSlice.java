package ch.trick17.peppl.lib.guard;

import static java.util.Collections.newSetFromMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import ch.trick17.peppl.lib.Partitioner;
import ch.trick17.peppl.lib.SliceRange;
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
    
    public final SliceRange range;
    final Set<S> subslices = newSetFromMap(new WeakHashMap<S, Boolean>());
    
    BaseSlice(final SliceRange range) {
        this.range = range;
    }
    
    public final int size() {
        return range.size();
    }
    
    public final S slice(final int begin, final int end, final int step) {
        return slice(new SliceRange(begin, end, step));
    }
    
    public final S slice(final SliceRange sliceRange) {
        assert sliceRange.begin >= range.begin;
        assert sliceRange.end <= range.end;
        assert sliceRange.step >= range.step;
        
        final S slice = createSlice(sliceRange);
        subslices.add(slice);
        return slice;
    }
    
    public final List<S> partition(final Partitioner p, final int n) {
        final List<SliceRange> ranges = p.partition(range, n);
        final List<S> slices = new ArrayList<>(n);
        for(final SliceRange r : ranges)
            slices.add(slice(r));
        return slices;
    }
    
    abstract S createSlice(SliceRange sliceRange);
    
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

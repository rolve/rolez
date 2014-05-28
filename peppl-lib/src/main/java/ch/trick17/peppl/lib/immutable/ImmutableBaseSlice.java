package ch.trick17.peppl.lib.immutable;

import java.util.ArrayList;
import java.util.List;

import ch.trick17.peppl.lib.Partitioner;
import ch.trick17.peppl.lib.SliceRange;
import ch.trick17.peppl.lib.task.Task;

/**
 * Base class for all {@link Immutable} slices, e.g. {@link ImmutableSlice} (for
 * reference types) or {@link ImmutableIntSlice}. A slice represents a
 * contiguous part of an array, e.g. {@link ImmutableArray} (for reference
 * types) or {@link ImmutableIntArray}. Note that an array is just a special
 * case of a slice, namely a slice that covers the whole array.
 * <p>
 * <code>Slice</code> and <code>Array</code> classes in the
 * {@link ch.trick17.peppl.lib.immutable} package are {@link Immutable}, i.e.,
 * they are immutable objects that can be shared with other {@link Task}s. For
 * efficiency reasons, immutable slices expose the underlying array directly as
 * a public field. It is the client's code responsibility to not break the
 * semantics of the class by mutating the array.
 * 
 * @author Michael Faes
 * @param <S>
 *            The concrete slice type.
 */
abstract class ImmutableBaseSlice<S extends ImmutableBaseSlice<S>> extends
        Immutable {
    
    public final SliceRange range;
    
    ImmutableBaseSlice(final SliceRange range) {
        this.range = range;
    }
    
    public final S slice(final SliceRange sliceRange) {
        if(!range.covers(sliceRange))
            throw new IllegalArgumentException("Given range: " + sliceRange
                    + " is not covered by this slice's range: " + range);
        
        return createSlice(sliceRange);
    }
    
    public final S slice(final int begin, final int end, final int step) {
        return slice(new SliceRange(begin, end, step));
    }
    
    public final List<S> partition(final Partitioner p, final int n) {
        final List<SliceRange> ranges = p.partition(range, n);
        final List<S> slices = new ArrayList<>(n);
        for(final SliceRange r : ranges)
            slices.add(slice(r));
        return slices;
    }
    
    abstract S createSlice(SliceRange sliceRange);
}

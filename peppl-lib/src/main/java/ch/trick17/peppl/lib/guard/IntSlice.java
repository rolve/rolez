package ch.trick17.peppl.lib.guard;

import static java.util.Collections.emptySet;
import ch.trick17.peppl.lib.SliceRange;

/**
 * A slice of an {@link IntArray}. See {@link BaseSlice} for more information.
 * 
 * @author Michael Faes
 * @see Slice
 */
public class IntSlice extends NonFinalSlice<IntSlice> {
    
    public final int[] data;
    
    IntSlice(final SliceRange range, final int[] data) {
        super(range);
        this.data = data;
    }
    
    @Override
    final IntSlice createSlice(final SliceRange sliceRange) {
        return new IntSlice(sliceRange, data);
    }
    
    @Override
    final Iterable<? extends Guarded> guardedRefs() {
        return emptySet();
    }
}

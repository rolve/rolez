package ch.trick17.rolez.lang.guard;

import static java.util.Collections.emptySet;

import java.util.AbstractList;
import java.util.RandomAccess;

import ch.trick17.rolez.lang.SliceRange;

/**
 * A slice of a {@link FinalArray}. See {@link BaseSlice} for more information.
 * 
 * @author Michael Faes
 * @param <E>
 *            The type of elements in this array slice. Must be a subtype of
 *            {@link Guarded}.
 * @see Slice
 * @see IntSlice
 * @see LongSlice
 * @see DoubleSlice
 */
public class FinalSlice<E extends Guarded> extends BaseSlice<FinalSlice<E>> {
    
    public final E[] data;
    private final SliceList listImpl = new SliceList();
    
    FinalSlice(final SliceRange range, final E[] data) {
        super(range);
        this.data = data;
    }
    
    @Override
    public final FinalSlice<E> slice(final SliceRange sliceRange) {
        if(!range.covers(sliceRange))
            throw new IllegalArgumentException("Given range: " + sliceRange + " is not covered by this slice's range: "
                    + range);
                    
        return new FinalSlice<>(sliceRange, data);
    }
    
    @Override
    protected final Iterable<? extends Guarded> guardedRefs() {
        return listImpl;
    }
    
    @Override
    protected final Iterable<? extends Guarded> views() {
        return emptySet();
    }
    
    private final class SliceList extends AbstractList<E> implements RandomAccess {
        @Override
        public E get(final int index) {
            return data[range.begin + index * range.step];
        }
        
        @Override
        public int size() {
            return range.size();
        }
    }
}

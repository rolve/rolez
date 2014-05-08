package ch.trick17.peppl.lib.guard;

import java.util.AbstractList;
import java.util.RandomAccess;

import ch.trick17.peppl.lib.SliceRange;

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
        assert range.end <= data.length;
        this.data = data;
    }
    
    @Override
    public final FinalSlice<E> slice(final SliceRange sliceRange) {
        assert sliceRange.begin >= range.begin;
        assert sliceRange.end <= range.end;
        assert sliceRange.step >= range.step;
        return createSlice(sliceRange);
    }
    
    @Override
    final FinalSlice<E> createSlice(final SliceRange sliceRange) {
        return new FinalSlice<>(sliceRange, data);
    }
    
    @Override
    final Iterable<? extends Guarded> guardedRefs() {
        return listImpl;
    }
    
    @Override
    public final String toString() {
        return listImpl.toString();
    }
    
    private final class SliceList extends AbstractList<E> implements
            RandomAccess {
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

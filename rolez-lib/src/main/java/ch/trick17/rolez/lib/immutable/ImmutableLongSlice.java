package ch.trick17.rolez.lib.immutable;

import ch.trick17.rolez.lib.SliceRange;

/**
 * A slice of an {@link ImmutableLongArray}. See {@link ImmutableBaseSlice} for
 * more information.
 * 
 * @author Michael Faes
 * @see ImmutableSlice
 */
public class ImmutableLongSlice extends ImmutableBaseSlice<ImmutableLongSlice> {
    
    public final long[] data;
    
    ImmutableLongSlice(final SliceRange range, final long[] data) {
        super(range);
        assert range.end <= data.length;
        this.data = data;
    }
    
    @Override
    final ImmutableLongSlice createSlice(final SliceRange sliceRange) {
        return new ImmutableLongSlice(sliceRange, data);
    }
}

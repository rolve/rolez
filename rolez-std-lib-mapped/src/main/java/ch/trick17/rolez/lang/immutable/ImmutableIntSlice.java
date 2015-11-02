package ch.trick17.rolez.lang.immutable;

import ch.trick17.rolez.lang.SliceRange;

/**
 * A slice of an {@link ImmutableIntArray}. See {@link ImmutableBaseSlice} for
 * more information.
 * 
 * @author Michael Faes
 * @see ImmutableSlice
 */
public class ImmutableIntSlice extends ImmutableBaseSlice<ImmutableIntSlice> {
    
    public final int[] data;
    
    ImmutableIntSlice(final SliceRange range, final int[] data) {
        super(range);
        assert range.end <= data.length;
        this.data = data;
    }
    
    @Override
    final ImmutableIntSlice createSlice(final SliceRange sliceRange) {
        return new ImmutableIntSlice(sliceRange, data);
    }
}

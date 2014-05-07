package ch.trick17.peppl.lib.immutable;

import ch.trick17.peppl.lib.SliceRange;

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

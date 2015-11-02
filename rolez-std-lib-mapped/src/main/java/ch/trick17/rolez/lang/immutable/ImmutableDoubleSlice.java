package ch.trick17.rolez.lang.immutable;

import ch.trick17.rolez.lang.SliceRange;

/**
 * A slice of an {@link ImmutableDoubleArray}. See {@link ImmutableBaseSlice}
 * for more information.
 * 
 * @author Michael Faes
 * @see ImmutableSlice
 */
public class ImmutableDoubleSlice extends
        ImmutableBaseSlice<ImmutableDoubleSlice> {
    
    public final double[] data;
    
    ImmutableDoubleSlice(final SliceRange range, final double[] data) {
        super(range);
        assert range.end <= data.length;
        this.data = data;
    }
    
    @Override
    final ImmutableDoubleSlice createSlice(final SliceRange sliceRange) {
        return new ImmutableDoubleSlice(sliceRange, data);
    }
}

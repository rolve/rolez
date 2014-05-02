package ch.trick17.peppl.lib.immutable;

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
    
    ImmutableDoubleSlice(final double[] data, final int beginIndex,
            final int endIndex) {
        super(beginIndex, endIndex);
        assert endIndex <= data.length;
        this.data = data;
    }
    
    @Override
    final ImmutableDoubleSlice createSlice(final int beginIndex,
            final int endIndex) {
        return new ImmutableDoubleSlice(data, beginIndex, endIndex);
    }
}

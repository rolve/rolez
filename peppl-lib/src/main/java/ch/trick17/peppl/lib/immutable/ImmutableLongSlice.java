package ch.trick17.peppl.lib.immutable;

/**
 * A slice of an {@link ImmutableLongArray}. See {@link ImmutableBaseSlice} for
 * more information.
 * 
 * @author Michael Faes
 * @see ImmutableSlice
 */
public class ImmutableLongSlice extends ImmutableBaseSlice<ImmutableLongSlice> {
    
    public final long[] data;
    
    ImmutableLongSlice(final long[] data, final int beginIndex,
            final int endIndex) {
        super(beginIndex, endIndex);
        assert endIndex <= data.length;
        this.data = data;
    }
    
    @Override
    final ImmutableLongSlice createSlice(final int beginIndex,
            final int endIndex) {
        return new ImmutableLongSlice(data, beginIndex, endIndex);
    }
}

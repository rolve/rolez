package ch.trick17.peppl.lib.immutable;

/**
 * A slice of an {@link ImmutableIntArray}. See {@link ImmutableBaseSlice} for
 * more information.
 * 
 * @author Michael Faes
 * @see ImmutableSlice
 */
public class ImmutableIntSlice extends ImmutableBaseSlice<ImmutableIntSlice> {
    
    public final int[] data;
    
    ImmutableIntSlice(final int[] data, final int beginIndex, final int endIndex) {
        super(beginIndex, endIndex);
        assert endIndex <= data.length;
        this.data = data;
    }
    
    @Override
    final ImmutableIntSlice createSlice(final int beginIndex, final int endIndex) {
        return new ImmutableIntSlice(data, beginIndex, endIndex);
    }
}

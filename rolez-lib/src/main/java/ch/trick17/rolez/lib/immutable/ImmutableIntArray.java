package ch.trick17.rolez.lib.immutable;

import java.util.Arrays;

import ch.trick17.rolez.lib.SliceRange;

/**
 * An {@link Immutable} array with <code>int</code> elements. See
 * {@link ImmutableBaseSlice} for more information.
 * 
 * @author Michael Faes
 * @see ImmutableArray
 */
public class ImmutableIntArray extends ImmutableIntSlice {
    
    @SafeVarargs
    public ImmutableIntArray(final int... data) {
        super(SliceRange.forArray(data), data);
    }
    
    @Override
    public String toString() {
        return Arrays.toString(data);
    }
}

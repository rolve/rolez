package ch.trick17.rolez.lang.immutable;

import java.util.Arrays;

import ch.trick17.rolez.lang.SliceRange;

/**
 * An {@link Immutable} array with <code>long</code> elements. See
 * {@link ImmutableBaseSlice} for more information.
 * 
 * @author Michael Faes
 * @see ImmutableArray
 */
public class ImmutableLongArray extends ImmutableLongSlice {
    
    @SafeVarargs
    public ImmutableLongArray(final long... data) {
        super(SliceRange.forArray(data), data);
    }
    
    public ImmutableLongArray(final int length) {
        this(new long[length]);
    }
    
    @Override
    public String toString() {
        return Arrays.toString(data);
    }
}

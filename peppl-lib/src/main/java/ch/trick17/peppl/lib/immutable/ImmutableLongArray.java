package ch.trick17.peppl.lib.immutable;

import java.util.Arrays;

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
        super(data, 0, data.length);
    }
    
    public ImmutableLongArray(final int length) {
        super(new long[length], 0, length);
    }
    
    @Override
    public String toString() {
        return Arrays.toString(data);
    }
}

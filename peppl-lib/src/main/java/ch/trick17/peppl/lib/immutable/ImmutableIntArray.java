package ch.trick17.peppl.lib.immutable;

import java.util.Arrays;

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
        super(data, 0, data.length);
    }
    
    public ImmutableIntArray(final int length) {
        super(new int[length], 0, length);
    }
    
    @Override
    public String toString() {
        return Arrays.toString(data);
    }
}

package ch.trick17.peppl.lib.immutable;

import java.util.Arrays;

/**
 * An {@link Immutable} array with <code>double</code> elements. See
 * {@link ImmutableBaseSlice} for more information.
 * 
 * @author Michael Faes
 * @see ImmutableArray
 */
public class ImmutableDoubleArray extends ImmutableDoubleSlice {
    
    @SafeVarargs
    public ImmutableDoubleArray(final double... data) {
        super(data, 0, data.length);
    }
    
    public ImmutableDoubleArray(final int length) {
        super(new double[length], 0, length);
    }
    
    @Override
    public String toString() {
        return Arrays.toString(data);
    }
}

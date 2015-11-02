package ch.trick17.rolez.lang.immutable;

import java.util.Arrays;

import ch.trick17.rolez.lang.SliceRange;

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
        super(SliceRange.forArray(data), data);
    }
    
    public ImmutableDoubleArray(final int length) {
        this(new double[length]);
    }
    
    @Override
    public String toString() {
        return Arrays.toString(data);
    }
}

package ch.trick17.rolez.lang.guard;

import ch.trick17.rolez.lang.SliceRange;

/**
 * A {@link Guarded} array with <code>double</code> elements. See
 * {@link BaseSlice} for more information.
 * 
 * @author Michael Faes
 */
public class DoubleArray extends DoubleSlice {
    
    @SafeVarargs
    public DoubleArray(final double... data) {
        super(SliceRange.forArray(data), data);
    }
}
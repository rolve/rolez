package ch.trick17.peppl.lib.guard;

import java.util.Arrays;

import ch.trick17.peppl.lib.SliceRange;

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
    
    public DoubleArray(final int length) {
        this(new double[length]);
    }
    
    @Override
    public String toString() {
        return Arrays.toString(data);
    }
}

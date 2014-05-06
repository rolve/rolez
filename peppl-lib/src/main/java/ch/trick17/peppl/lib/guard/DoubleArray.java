package ch.trick17.peppl.lib.guard;

import java.util.Arrays;

/**
 * A {@link Guarded} array with <code>double</code> elements. See
 * {@link BaseSlice} for more information.
 * 
 * @author Michael Faes
 */
public class DoubleArray extends DoubleSlice {
    
    @SafeVarargs
    public DoubleArray(final double... data) {
        super(data, 0, data.length, 1);
    }
    
    public DoubleArray(final int length) {
        super(new double[length], 0, length, 1);
    }
    
    @Override
    public String toString() {
        return Arrays.toString(data);
    }
}

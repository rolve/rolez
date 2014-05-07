package ch.trick17.peppl.lib.guard;

import java.util.Arrays;

import ch.trick17.peppl.lib.SliceRange;

/**
 * A {@link Guarded} array with <code>int</code> elements. See {@link BaseSlice}
 * for more information.
 * 
 * @author Michael Faes
 */
public class IntArray extends IntSlice {
    
    @SafeVarargs
    public IntArray(final int... data) {
        super(SliceRange.forArray(data), data);
    }
    
    @Override
    public String toString() {
        return Arrays.toString(data);
    }
}

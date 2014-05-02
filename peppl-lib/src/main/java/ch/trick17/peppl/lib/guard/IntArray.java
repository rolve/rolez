package ch.trick17.peppl.lib.guard;

import java.util.Arrays;

/**
 * A {@link Guarded} array with <code>int</code> elements. See {@link BaseSlice}
 * for more information.
 * 
 * @author Michael Faes
 */
public class IntArray extends IntSlice {
    
    @SafeVarargs
    public IntArray(final int... data) {
        super(data, 0, data.length);
    }
    
    public IntArray(final int length) {
        super(new int[length], 0, length);
    }
    
    @Override
    public String toString() {
        return Arrays.toString(data);
    }
}

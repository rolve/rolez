package ch.trick17.peppl.lib.guard;

import java.util.Arrays;

/**
 * A {@link Guarded} array with <code>long</code> elements. See
 * {@link BaseSlice} for more information.
 * 
 * @author Michael Faes
 */
public class LongArray extends LongSlice {
    
    @SafeVarargs
    public LongArray(final long... data) {
        super(data, 0, data.length, 1);
    }
    
    public LongArray(final int length) {
        super(new long[length], 0, length, 1);
    }
    
    @Override
    public String toString() {
        return Arrays.toString(data);
    }
}

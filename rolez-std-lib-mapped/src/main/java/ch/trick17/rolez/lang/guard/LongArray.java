package ch.trick17.rolez.lang.guard;

import ch.trick17.rolez.lang.SliceRange;

/**
 * A {@link Guarded} array with <code>long</code> elements. See
 * {@link BaseSlice} for more information.
 * 
 * @author Michael Faes
 */
public class LongArray extends LongSlice {
    
    @SafeVarargs
    public LongArray(final long... data) {
        super(SliceRange.forArray(data), data);
    }
    
    public LongArray(final int length) {
        this(new long[length]);
    }
}

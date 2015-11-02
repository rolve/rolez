package ch.trick17.rolez.lang.guard;

import ch.trick17.rolez.lang.SliceRange;

/**
 * A {@link Guarded} array with reference type elements. See {@link BaseSlice}
 * for more information.
 * 
 * @author Michael Faes
 * @param <E>
 *            The type of elements in this array. Must be a subtype of
 *            {@link Guarded}.
 * @see FinalArray
 * @see IntArray
 * @see LongArray
 * @see DoubleArray
 */
public final class Array<E extends Guarded> extends Slice<E> {
    
    @SafeVarargs
    public Array(final E... data) {
        super(SliceRange.forArray(data), data);
    }
}

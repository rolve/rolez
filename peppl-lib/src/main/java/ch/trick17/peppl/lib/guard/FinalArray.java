package ch.trick17.peppl.lib.guard;

import ch.trick17.peppl.lib.SliceRange;
import ch.trick17.peppl.lib.immutable.Immutable;
import ch.trick17.peppl.lib.immutable.ImmutableArray;

/**
 * A {@link Guarded} array with reference type elements that cannot be changed.
 * Note that this is different from an {@link ImmutableArray}. While an
 * immutable array may only contain {@link Immutable} elements (meaning the
 * array is <em>transitively</em> immutable), a final array may contain
 * references to mutable objects, but these references may not be changed. This
 * guarantee may have a positive impact on the guarding performance of such
 * arrays, in particular if subslices are involved.
 * <p>
 * See {@link BaseSlice} for more information about arrays and slices in
 * general.
 * 
 * @author Michael Faes
 * @param <E>
 *            The type of elements in this array. Must be a subtype of
 *            {@link Guarded}.
 * @see Array
 * @see IntArray
 * @see LongArray
 * @see DoubleArray
 */
public final class FinalArray<E extends Guarded> extends FinalSlice<E> {
    
    @SafeVarargs
    public FinalArray(final E... data) {
        super(SliceRange.forArray(data), data);
    }
}

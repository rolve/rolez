package ch.trick17.peppl.lib.immutable;

import java.util.Arrays;

import ch.trick17.peppl.lib.SliceRange;

/**
 * An {@link Immutable} array with reference type elements. See
 * {@link ImmutableBaseSlice} for more information.
 * 
 * @author Michael Faes
 * @param <E>
 *            The type of elements in this array slice. Must be an
 *            {@linkplain Immutable#isImmutable(Class) immutable} type. (Note
 *            that this is checked at runtime, when the slice is created.)
 * @see ImmutableIntArray
 * @see ImmutableLongArray
 * @see ImmutableDoubleArray
 */
public final class ImmutableArray<E> extends ImmutableSlice<E> {
    
    @SafeVarargs
    public ImmutableArray(final E... data) {
        super(SliceRange.forArray(data), data);
    }
    
    @Override
    public String toString() {
        return Arrays.toString(data);
    }
}

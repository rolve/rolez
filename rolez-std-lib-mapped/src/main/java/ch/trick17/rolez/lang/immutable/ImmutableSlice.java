package ch.trick17.rolez.lang.immutable;

import ch.trick17.rolez.lang.SliceRange;

/**
 * A slice of a {@link Immutable} array with reference type elements (i.e. an
 * {@link ImmutableArray}). See {@link ImmutableBaseSlice} for more information.
 * 
 * @author Michael Faes
 * @param <E>
 *            The type of elements in this array slice. Must be an
 *            {@linkplain Immutable#isImmutable(Class) immutable} type. (Note
 *            that this is checked at runtime, when the slice is created.)
 * @see ImmutableIntSlice
 * @see ImmutableLongSlice
 * @see ImmutableDoubleSlice
 */
public class ImmutableSlice<E> extends ImmutableBaseSlice<ImmutableSlice<E>> {
    
    public final E[] data;
    
    ImmutableSlice(final SliceRange range, final E[] data) {
        super(range);
        if(!isImmutable(data.getClass().getComponentType()))
            throw new AssertionError(
                    "Cannot create an ImmutableArray with a mutable element type: "
                            + data.getClass().getComponentType().getName());
        assert range.end <= data.length;
        this.data = data;
    }
    
    @Override
    final ImmutableSlice<E> createSlice(final SliceRange sliceRange) {
        return new ImmutableSlice<>(sliceRange, data);
    }
}

package rolez.lang;

/**
 * Thrown to indicate that a {@link Slice} has been accessed with an illegal index. The index is not
 * {@linkplain SliceRange#contains(int) contained} in the slice's {@link SliceRange}.
 *
 * @author Michael Faes
 */
public class SliceIndexOutOfBoundsException extends IndexOutOfBoundsException {
    
    public SliceIndexOutOfBoundsException(int index) {
        super("Array index out of range: " + index);
    }
}

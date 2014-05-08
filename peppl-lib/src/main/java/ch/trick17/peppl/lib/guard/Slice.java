package ch.trick17.peppl.lib.guard;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.RandomAccess;

import ch.trick17.peppl.lib.SliceRange;

/**
 * A slice of a {@link Guarded} array with reference type elements (i.e. an
 * {@link Array}). See {@link BaseSlice} for more information.
 * 
 * @author Michael Faes
 * @param <E>
 *            The type of elements in this array slice. Must be a subtype of
 *            {@link Guarded}.
 * @see IntSlice
 * @see LongSlice
 * @see DoubleSlice
 */
public class Slice<E extends Guarded> extends NonFinalSlice<Slice<E>> {
    
    public final E[] data;
    private final SliceList listImpl = new SliceList();
    
    Slice(final SliceRange range, final E[] data) {
        super(range);
        assert range.end <= data.length;
        this.data = data;
    }
    
    @Override
    final Slice<E> createSlice(final SliceRange sliceRange) {
        return new Slice<>(sliceRange, data);
    }
    
    @Override
    final Iterable<? extends Guarded> guardedRefs() {
        return new Iterable<Guarded>() {
            public Iterator<Guarded> iterator() {
                final Iterator<E> dataIter = listImpl.iterator();
                final Iterator<Slice<E>> sliceIter = subslices.iterator();
                return new Iterator<Guarded>() {
                    
                    public boolean hasNext() {
                        return dataIter.hasNext() || sliceIter.hasNext();
                    }
                    
                    public Guarded next() {
                        if(dataIter.hasNext())
                            return dataIter.next();
                        else
                            return sliceIter.next();
                    }
                    
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }
    
    @Override
    public final String toString() {
        return listImpl.toString();
    }
    
    private final class SliceList extends AbstractList<E> implements
            RandomAccess {
        @Override
        public E get(final int index) {
            return data[range.begin + index * range.step];
        }
        
        @Override
        public E set(final int index, final E element) {
            final int i = range.begin + index * range.step;
            final E previous = data[i];
            data[i] = element;
            return previous;
        }
        
        @Override
        public int size() {
            return range.size();
        }
    }
}

package ch.trick17.peppl.lib.guard;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;

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
public class Slice<E extends Guarded> extends BaseSlice<Slice<E>> implements
        List<E>, RandomAccess {
    
    public final E[] data;
    private final SliceList listImpl = new SliceList();
    
    Slice(final E[] data, final int beginIndex, final int endIndex) {
        super(beginIndex, endIndex);
        assert endIndex <= data.length;
        this.data = data;
    }
    
    @Override
    final Slice<E> createSlice(final int beginIndex, final int endIndex) {
        return new Slice<>(data, beginIndex, endIndex);
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
    
    public int size() {
        return listImpl.size();
    }
    
    public boolean isEmpty() {
        return listImpl.isEmpty();
    }
    
    public boolean contains(final Object o) {
        return listImpl.contains(o);
    }
    
    public Iterator<E> iterator() {
        return listImpl.iterator();
    }
    
    public Object[] toArray() {
        return listImpl.toArray();
    }
    
    public <T> T[] toArray(final T[] a) {
        return listImpl.toArray(a);
    }
    
    public boolean add(final E e) {
        return listImpl.add(e);
    }
    
    public boolean remove(final Object o) {
        return listImpl.remove(o);
    }
    
    public boolean containsAll(final Collection<?> c) {
        return listImpl.containsAll(c);
    }
    
    public boolean addAll(final Collection<? extends E> c) {
        return listImpl.addAll(c);
    }
    
    public boolean addAll(final int index, final Collection<? extends E> c) {
        return listImpl.addAll(index, c);
    }
    
    public boolean removeAll(final Collection<?> c) {
        return listImpl.removeAll(c);
    }
    
    public boolean retainAll(final Collection<?> c) {
        return listImpl.retainAll(c);
    }
    
    public void clear() {
        listImpl.clear();
    }
    
    @Override
    public boolean equals(final Object o) {
        return listImpl.equals(o);
    }
    
    @Override
    public int hashCode() {
        return listImpl.hashCode();
    }
    
    public E get(final int index) {
        return listImpl.get(index);
    }
    
    public E set(final int index, final E element) {
        return listImpl.set(index, element);
    }
    
    public void add(final int index, final E element) {
        listImpl.add(index, element);
    }
    
    public E remove(final int index) {
        return listImpl.remove(index);
    }
    
    public int indexOf(final Object o) {
        return listImpl.indexOf(o);
    }
    
    public int lastIndexOf(final Object o) {
        return listImpl.lastIndexOf(o);
    }
    
    public ListIterator<E> listIterator() {
        return listImpl.listIterator();
    }
    
    public ListIterator<E> listIterator(final int index) {
        return listImpl.listIterator(index);
    }
    
    public List<E> subList(final int fromIndex, final int toIndex) {
        return listImpl.subList(fromIndex, toIndex);
    }
    
    @Override
    public String toString() {
        return listImpl.toString();
    }
    
    private final class SliceList extends AbstractList<E> implements
            RandomAccess {
        @Override
        public E get(final int index) {
            return data[begin + index];
        }
        
        @Override
        public E set(final int index, final E element) {
            final int i = begin + index;
            final E previous = data[i];
            data[i] = element;
            return previous;
        }
        
        @Override
        public int size() {
            return end - begin;
        }
    }
}

package ch.trick17.peppl.lib.immutable;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;

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
public class ImmutableSlice<E> extends ImmutableBaseSlice<ImmutableSlice<E>>
        implements List<E>, RandomAccess {
    
    public final E[] data;
    private final SliceList listImpl = new SliceList();
    
    ImmutableSlice(final E[] data, final int beginIndex, final int endIndex) {
        super(beginIndex, endIndex);
        if(!isImmutable(data.getClass().getComponentType()))
            throw new AssertionError(
                    "Cannot create an ImmutableArray with a mutable element type: "
                            + data.getClass().getComponentType().getName());
        assert endIndex <= data.length;
        this.data = data;
    }
    
    @Override
    final ImmutableSlice<E> createSlice(final int beginIndex, final int endIndex) {
        return new ImmutableSlice<>(data, beginIndex, endIndex);
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
        public int size() {
            return end - begin;
        }
    }
}

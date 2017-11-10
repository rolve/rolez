package rolez.checked.util;

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import rolez.checked.lang.Checked;

/**
 * Basically a stripped-down version of {@link java.util.ArrayList}, but guarded.
 */
public class ArrayList<E> extends Checked implements Iterable<E> {
    
    private static final int DEFAULT_CAPACITY = 10;
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
    private static final Object[] EMPTY_ELEMENTDATA = {};
    
    private Object[] elementData;
    private int size;
    private int modCount = 0;
    
    public ArrayList() {
        elementData = EMPTY_ELEMENTDATA;
    }
    
    public int size() {
        return size;
    }
    
    public boolean isEmpty() {
        return size == 0;
    }
    
    public E get(int index) {
        rangeCheck(index);
        return elementData(index);
    }
    
    public E set(int index, E element) {
        rangeCheck(index);
        
        E oldValue = elementData(index);
        elementData[index] = element;
        return oldValue;
    }
    
    public boolean add(E e) {
        ensureCapacityInternal(size + 1); // Increments modCount!!
        elementData[size++] = e;
        return true;
    }
    
    public void add(int index, E element) {
        rangeCheckForAdd(index);
        
        ensureCapacityInternal(size + 1); // Increments modCount!!
        System.arraycopy(elementData, index, elementData, index + 1,
                size - index);
        elementData[index] = element;
        size++;
    }
    
    public boolean remove(Object o) {
        if(o == null) {
            for(int index = 0; index < size; index++)
                if(elementData[index] == null) {
                    fastRemove(index);
                    return true;
                }
        }
        else {
            for(int index = 0; index < size; index++)
                if(o.equals(elementData[index])) {
                    fastRemove(index);
                    return true;
                }
        }
        return false;
    }
    
    public E remove(int index) {
        rangeCheck(index);
        
        modCount++;
        E oldValue = elementData(index);
        
        int numMoved = size - index - 1;
        if(numMoved > 0)
            System.arraycopy(elementData, index + 1, elementData, index, numMoved);
        elementData[--size] = null; // clear to let GC do its work
        
        return oldValue;
    }
    
    public void clear() {
        modCount++;
        // clear to let GC do its work
        for(int i = 0; i < size; i++)
            elementData[i] = null;
        size = 0;
    }
    
    public boolean addAll(ArrayList<E> list) {
        Object[] a = list.toArray();
        int numNew = a.length;
        ensureCapacityInternal(size + numNew); // Increments modCount
        System.arraycopy(a, 0, elementData, size, numNew);
        size += numNew;
        return numNew != 0;
    }
    
    private Object[] toArray() {
        return Arrays.copyOf(elementData, size);
    }
    
    public Iterator<E> iterator() {
        return new Itr();
    }
    
    /* Implementation */
    
    @SuppressWarnings("unchecked")
    private E elementData(int index) {
        return (E) elementData[index];
    }
    
    private void rangeCheck(int index) {
        if(index >= size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }
    
    private void rangeCheckForAdd(int index) {
        if(index > size || index < 0)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }
    
    private String outOfBoundsMsg(int index) {
        return "Index: " + index + ", Size: " + size;
    }
    
    private void ensureCapacityInternal(int minCapacity) {
        int actualMinCapacity = minCapacity;
        if(elementData == EMPTY_ELEMENTDATA)
            actualMinCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);
        
        modCount++;
        // overflow-conscious code
        if(actualMinCapacity - elementData.length > 0)
            grow(actualMinCapacity);
    }
    
    private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = elementData.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if(newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if(newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        
        // minCapacity is usually close to size, so this is a win:
        elementData = Arrays.copyOf(elementData, newCapacity);
    }
    
    private static int hugeCapacity(int minCapacity) {
        if(minCapacity < 0) // overflow
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
    }
    
    private void fastRemove(int index) {
        modCount++;
        int numMoved = size - index - 1;
        if(numMoved > 0)
            System.arraycopy(elementData, index + 1, elementData, index, numMoved);
        elementData[--size] = null; // clear to let GC do its work
    }
    
    private class Itr implements Iterator<E> {
        int cursor; // index of next element to return
        int lastRet = -1; // index of last element returned; -1 if no such
        int expectedModCount = modCount;
        
        public boolean hasNext() {
            return cursor != size;
        }
        
        @SuppressWarnings("unchecked")
        public E next() {
            checkForComodification();
            int i = cursor;
            if(i >= size)
                throw new NoSuchElementException();
            Object[] data = ArrayList.this.elementData;
            if(i >= data.length)
                throw new ConcurrentModificationException();
            cursor = i + 1;
            return (E) data[lastRet = i];
        }
        
        public void remove() {
            if(lastRet < 0)
                throw new IllegalStateException();
            checkForComodification();
            
            try {
                ArrayList.this.remove(lastRet);
                cursor = lastRet;
                lastRet = -1;
                expectedModCount = modCount;
            } catch(IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }
        
        private void checkForComodification() {
            if(modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }
    
    @Override
    protected Iterable<?> guardedRefs() {
        return this;
    }
}

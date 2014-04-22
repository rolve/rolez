package ch.trick17.peppl.lib.guard;

import java.util.AbstractList;
import java.util.Iterator;

public class Slice<E extends Guarded> extends AbstractSlice<Slice<E>> {
    
    public final E[] data;
    
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
    final Iterable<? extends Guarded> allRefs() {
        return new Iterable<Guarded>() {
            public Iterator<Guarded> iterator() {
                return new Iterator<Guarded>() {
                    Iterator<E> dataIter = new AbstractList<E>() {
                        @Override
                        public E get(final int index) {
                            return data[begin + index];
                        }
                        
                        @Override
                        public int size() {
                            return end - begin;
                        }
                    }.iterator();
                    Iterator<Slice<E>> sliceIter = subslices.iterator();
                    
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
}

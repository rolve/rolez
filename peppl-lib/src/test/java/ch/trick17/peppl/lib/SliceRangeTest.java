package ch.trick17.peppl.lib;

import static org.junit.Assert.assertEquals;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class SliceRangeTest {
    
    static final int[] SIZES = {0, 1, 2, 4, 5, 8, 49};
    static final int[] BEGINS = {0, 1, 2, 5};
    static final int[] STEPS = {1, 2, 3, 4, 5};
    
    @Parameters(name = "{0}, {1}")
    public static List<Object[]> parameters() {
        final ArrayList<SliceRange> ranges = new ArrayList<SliceRange>();
        for(final int begin : BEGINS)
            for(final int size : SIZES)
                for(final int step : STEPS)
                    ranges.add(new SliceRange(begin, begin + size, step));
        
        final List<Object[]> params = new ArrayList<Object[]>();
        for(int i = 0; i < ranges.size(); i++)
            for(int j = i; j < ranges.size(); j++)
                params.add(new Object[]{ranges.get(i), ranges.get(j)});
        
        return params;
    }
    
    private final SliceRange range1;
    private final SliceRange range2;
    
    public SliceRangeTest(final SliceRange range1, final SliceRange range2) {
        this.range1 = range1;
        this.range2 = range2;
    }
    
    @Test
    public void testCoversAndSize() {
        final SliceRangeSet range1Set = new SliceRangeSet(range1);
        final SliceRangeSet range2Set = new SliceRangeSet(range2);
        
        assertEquals(range1Set.containsAll(range2Set), range1.covers(range2));
        assertEquals(range2Set.containsAll(range1Set), range2.covers(range1));
        
        assertEquals(range1Set.size(), range1.size());
    }
    
    static class SliceRangeSet extends AbstractSet<Integer> {
        
        private final SliceRange base;
        
        public SliceRangeSet(final SliceRange base) {
            this.base = base;
        }
        
        @Override
        public Iterator<Integer> iterator() {
            return new Iterator<Integer>() {
                int i = 0;
                
                private int index() {
                    return base.begin + i * base.step;
                }
                
                public boolean hasNext() {
                    return index() < base.end;
                }
                
                public Integer next() {
                    final int next = index();
                    i++;
                    return next;
                }
                
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
        
        @Override
        public int size() {
            int size = 0;
            for(final Iterator<?> i = iterator(); i.hasNext(); i.next())
                size++;
            return size;
        }
    }
}

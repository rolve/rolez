package ch.trick17.peppl.lib.guard;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class BaseSliceTest {
    
    @Test
    public void testPartition() {
        TestSlice slice = new TestSlice(0, 100);
        List<TestSlice> slices = slice.partition(8);
        assertEquals(8, slices.size());
        assertCover(slice, slices);
        assertBalanced(slices);
        
        slice = new TestSlice(2, 4);
        slices = slice.partition(4);
        assertEquals(4, slices.size());
        assertCover(slice, slices);
        assertBalanced(slices);
        
        slice = new TestSlice(0, 0);
        slices = slice.partition(2);
        assertEquals(2, slices.size());
        assertCover(slice, slices);
        assertBalanced(slices);
    }
    
    private static void assertCover(final BaseSlice<?> original,
            final List<? extends BaseSlice<?>> slices) {
        assertEquals(original.begin, slices.get(0).begin);
        
        for(int i = 1; i < slices.size(); i++)
            assertEquals(slices.get(i - 1).end, slices.get(i).begin);
        
        assertEquals(original.end, slices.get(slices.size() - 1).end);
    }
    
    private static void assertBalanced(final List<TestSlice> slices) {
        int min = Integer.MAX_VALUE;
        int max = 0;
        for(final TestSlice slice : slices) {
            final int length = slice.length();
            if(length < min)
                min = length;
            if(length > max)
                max = length;
        }
        assertTrue(max - min <= 1);
    }
    
    static class TestSlice extends BaseSlice<TestSlice> {
        
        public TestSlice(final int beginIndex, final int endIndex) {
            super(beginIndex, endIndex);
        }
        
        @Override
        TestSlice createSlice(final int beginIndex, final int endIndex) {
            return new TestSlice(beginIndex, endIndex);
        }
        
        @Override
        Iterable<? extends Guarded> guardedRefs() {
            return emptyList();
        }
    }
}

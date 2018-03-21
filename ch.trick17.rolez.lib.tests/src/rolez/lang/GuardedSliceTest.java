package rolez.lang;

import static java.lang.Math.min;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class GuardedSliceTest {
    
    private final int length;
    private final List<SliceRange> ranges;
    
    @Parameters(name = "{0}, {1}")
    public static List<Object[]> parameters() {
        final List<Object[]> params = new ArrayList<>();
        
        /* Non-overlapping */
        for(final int lengths : asList(1, 2, 4, 5, 10, 20)) {
            final List<SliceRange> ranges = new ArrayList<>();
            for(int i = 0; i < 20; i += lengths)
                ranges.add(new SliceRange(i, i + lengths, 1));
                
            params.add(new Object[]{20, ranges});
        }
        
        /* Non-overlapping striped */
        for(final int steps : asList(2, 3, 4, 5, 10, 19, 20)) {
            final List<SliceRange> ranges = new ArrayList<>();
            for(int i = 0; i < steps; i++)
                ranges.add(new SliceRange(i, 20, steps));
                
            params.add(new Object[]{20, ranges});
        }
        
        /* Overlapping by one */
        for(final int lengths : asList(1, 2, 4, 5, 10)) {
            final List<SliceRange> ranges = new ArrayList<>();
            for(int i = 0; i < 20; i += lengths)
                ranges.add(new SliceRange(i, min(20, i + lengths + 1), 1));
                
            params.add(new Object[]{20, ranges});
        }
        
        /* Overlapping striped */
        final List<SliceRange> ranges = new ArrayList<>();
        for(final int step : asList(1, 2, 3, 4, 5, 10, 11, 19, 20))
            ranges.add(new SliceRange(20 - step, 20, step));
            
        params.add(new Object[]{20, ranges});
        
        return params;
    }
    
    public GuardedSliceTest(final int length, final List<SliceRange> ranges) {
        this.length = length;
        this.ranges = ranges;
    }
    
    @BeforeClass
    public static void setup() {
        Task.registerNewRootTask();
    }
    
    @AfterClass
    public static void tearDown() {
        Task.unregisterRootTask();
    }
    
    @Test
    public void testSlice() {
        final GuardedArray<?> array = new GuardedArray<>(new Guarded[length]);
        
        final List<GuardedSlice<?>> slices = new ArrayList<>();
        slices.add(array);
        for(final SliceRange range : ranges) {
            final GuardedSlice<?> slice = array.slice(range);
            assertEquals(range, slice.range);
            slices.add(slice);
        }
        
        assertSoundSlicesCondition(array, slices);
    }
    
    private static void assertSoundSlicesCondition(GuardedArray<?> array,
            List<GuardedSlice<?>> slices) {
        for(int index = array.range.begin; index < array.range.end; index += array.range.step) {
            /* Find all slices that contain the given index */
            final List<GuardedSlice<?>> containing = new ArrayList<>();
            for(final GuardedSlice<?> s : slices)
                if(contains(s.range, index))
                    containing.add(s);
                    
            /* For each pair of such slices, assert that they reference each other */
            for(int i = 0; i < containing.size(); i++)
                for(int j = i + 1; j < containing.size(); j++) {
                    assertTrue(containing.get(i).overlappingSlices.contains(containing.get(j)));
                    assertTrue(containing.get(j).overlappingSlices.contains(containing.get(i)));
                }
        }
    }
    
    private static boolean contains(final SliceRange range, final int index) {
        if(range.begin > index)
            return false;
        if(range.end <= index)
            return false;
        if((index - range.begin) % range.step != 0)
            return false;
        return true;
    }
}

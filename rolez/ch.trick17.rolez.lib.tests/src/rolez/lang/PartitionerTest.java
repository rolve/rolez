package rolez.lang;

import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class PartitionerTest {
    
    protected void assertCover(final SliceRange original, final Collection<SliceRange> slices) {
        final Set<Integer> indices = new HashSet<Integer>();
        
        for(final SliceRange slice : slices)
            for(int i = slice.begin; i < slice.end; i += slice.step)
                assertTrue(indices.add(i));
                
        for(int i = original.begin; i < original.end; i++)
            assertTrue(indices.contains(i));
    }

    protected void assertBalanced(final Collection<SliceRange> slices) {
        int min = Integer.MAX_VALUE;
        int max = 0;
        for(final SliceRange slice : slices) {
            final int length = slice.size();
            if(length < min)
                min = length;
            if(length > max)
                max = length;
        }
        assertTrue(max - min <= 1);
    }
}

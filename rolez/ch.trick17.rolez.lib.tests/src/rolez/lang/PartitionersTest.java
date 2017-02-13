package rolez.lang;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class PartitionersTest {
    
    static final int[] INTERESTING_SIZES = {2, 4, 5, 7, 8, 20, 49, 97};
    static final int[] INTERESTING_NS = {1, 2, 3, 4, 5, 7, 8};
    static final List<List<? extends Partitioner>> INTERESTING_MODES = new ArrayList<List<? extends Partitioner>>(4) {
        {
            add(asList(ContiguousPartitioner.INSTANCE, ContiguousPartitioner.INSTANCE));
            add(asList(StripedPartitioner.INSTANCE, StripedPartitioner.INSTANCE));
            add(asList(ContiguousPartitioner.INSTANCE, StripedPartitioner.INSTANCE));
            add(asList(StripedPartitioner.INSTANCE, ContiguousPartitioner.INSTANCE));
        }
    };
    
    static final int[] SPECIAL_SIZES = {0, 1};
    static final int[] SOME_NS = {2, 4, 7};
    
    @Parameters(name = "size={0}, ns={1},{2}, modes={3}")
    public static List<Object[]> parameters() {
        Task.registerNewRootTask(); // required for the partitioners' toString() method (yeah...)
        
        final List<Object[]> params = new ArrayList<Object[]>();
        
        /* All combinations of sizes, ns and modes */
        for(final int size : INTERESTING_SIZES)
            for(final int n1 : INTERESTING_NS)
                for(final int n2 : INTERESTING_NS)
                    for(final List<? extends Partitioner> modes : INTERESTING_MODES)
                        params.add(new Object[]{size, n1, n2, modes});
                        
        /* Combinations of special sizes, some ns and some modes */
        for(final int size : SPECIAL_SIZES)
            for(final int n2 : SOME_NS)
                for(final List<? extends Partitioner> modes : INTERESTING_MODES)
                    params.add(new Object[]{size, 1, n2, modes});
                    
        return params;
    }
    
    @AfterClass
    public static void unregisterRootTask() {
        Task.unregisterRootTask();
    }
    
    private final int size;
    private final int n1;
    private final int n2;
    private final List<Partitioner> modes;
    
    public PartitionersTest(final int size, final int n1, final int n2,
            final List<Partitioner> modes) {
        this.size = size;
        this.n1 = n1;
        this.n2 = n2;
        this.modes = modes;
    }
    
    @Test
    public void testPartition() {
        final SliceRange orig = new SliceRange(0, size, 1);
        
        final Collection<SliceRange> slices1 = asList(modes.get(0).partition(orig, n1).data);
        assertEquals(n1, slices1.size());
        assertCover(orig, slices1);
        assertBalanced(slices1);
        
        final List<SliceRange> slices2 = new ArrayList<>(n1 * n2);
        for(final SliceRange s1 : slices1)
            slices2.addAll(asList(modes.get(1).partition(s1, n2).data));
        assertEquals(n1 * n2, slices2.size());
        assertCover(orig, slices2);
        assertBalanced(slices2);
    }
    
    private static void assertCover(final SliceRange original,
            final Collection<SliceRange> slices) {
        final Set<Integer> indices = new HashSet<Integer>();
        
        for(final SliceRange slice : slices)
            for(int i = slice.begin; i < slice.end; i += slice.step)
                assertTrue(indices.add(i));
                
        for(int i = original.begin; i < original.end; i++)
            assertTrue(indices.contains(i));
    }
    
    private static void assertBalanced(final Collection<SliceRange> slices) {
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

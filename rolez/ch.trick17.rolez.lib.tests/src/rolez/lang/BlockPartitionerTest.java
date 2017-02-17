package rolez.lang;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import static rolez.lang.Task.currentTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class BlockPartitionerTest extends PartitionerTest {
    
    static final int[] SIZES = {2, 4, 5, 8, 20, 32, 300, 600000};
    static final int[] BLOCK_SIZES = {1, 2, 3, 4, 8};
    static final int[] NS = {1, 2, 3, 4, 5, 7, 8};
    
    @Parameters(name = "size={0}, blockSize= {1}, n={2}")
    public static List<Object[]> parameters() {
        final List<Object[]> params = new ArrayList<Object[]>();
        
        /* All combinations of block sizes, sizes, and ns */
        for(int size : SIZES)
            for(int blockSize : BLOCK_SIZES)
                for(int n : NS)
                    params.add(new Object[]{size, blockSize, n});
            
        return params;
    }
    
    @BeforeClass
    public static void registerNewRootTask() {
        Task.registerNewRootTask();
    }
    
    @AfterClass
    public static void unregisterRootTask() {
        Task.unregisterRootTask();
    }
    
    private final int size;
    private final int blockSize;
    private final int n;
    
    public BlockPartitionerTest(int size, int blockSize, int n) {
        this.size = size;
        this.blockSize = blockSize;
        this.n = n;
    }
    
    @Test
    public void testPartition() {
        assumeTrue(size % blockSize == 0);
        
        SliceRange orig = new SliceRange(0, size, 1);
        
        BlockPartitioner partitioner = new BlockPartitioner(blockSize, currentTask().idBits());
        Collection<SliceRange> slices = asList(partitioner.partition(orig, n));
        assertEquals(n, slices.size());
        assertCover(orig, slices);
        assertBlockSizes(slices);
        assertBalancedBlocks(slices);
    }
    
    private void assertBlockSizes(final Collection<SliceRange> slices) {
        for(SliceRange range : slices)
            assertTrue(range.size() % blockSize == 0);
    }
    
    private void assertBalancedBlocks(final Collection<SliceRange> slices) {
        int min = Integer.MAX_VALUE;
        int max = 0;
        for(SliceRange slice : slices) {
            int length = slice.size();
            if(length < min)
                min = length;
            if(length > max)
                max = length;
        }
        assertTrue(max - min <= blockSize);
    }
}

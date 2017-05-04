package rolez.lang;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

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
public class ContiguousStripedPartitionersTest extends PartitionerTest {
    
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
    
    @Parameters(name = "size={0}, ns={1},{2}, modes={4}")
    public static List<Object[]> parameters() {
        Task.registerNewRootTask(); // required for the partitioners' toString() method (yeah...)
        
        final List<Object[]> params = new ArrayList<>();
        
        /* All combinations of sizes, ns and modes */
        for(final int size : INTERESTING_SIZES)
            for(final int n1 : INTERESTING_NS)
                for(final int n2 : INTERESTING_NS)
                    for(final List<? extends Partitioner> modes : INTERESTING_MODES)
                        params.add(new Object[]{size, n1, n2, modes, modes.toString()});
                        
        /* Combinations of special sizes, some ns and some modes */
        for(final int size : SPECIAL_SIZES)
            for(final int n2 : SOME_NS)
                for(final List<? extends Partitioner> modes : INTERESTING_MODES)
                    params.add(new Object[]{size, 1, n2, modes, modes.toString()});
                
        Task.unregisterRootTask(); // unregister again, since other tests (that register a new root
                                   // task themselves) may run first
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
    private final int n1;
    private final int n2;
    private final List<Partitioner> modes;
    
    public ContiguousStripedPartitionersTest(final int size, final int n1, final int n2,
            final List<Partitioner> modes, @SuppressWarnings("unused") String modesString) {
        this.size = size;
        this.n1 = n1;
        this.n2 = n2;
        this.modes = modes;
    }
    
    @Test
    public void testPartition() {
        final SliceRange orig = new SliceRange(0, size, 1);
        
        final Collection<SliceRange> slices1 = asList(modes.get(0).partition(orig, n1));
        assertEquals(n1, slices1.size());
        assertCover(orig, slices1);
        assertBalanced(slices1);
        
        final List<SliceRange> slices2 = new ArrayList<>(n1 * n2);
        for(final SliceRange s1 : slices1)
            slices2.addAll(asList(modes.get(1).partition(s1, n2)));
        assertEquals(n1 * n2, slices2.size());
        assertCover(orig, slices2);
        assertBalanced(slices2);
    }
}

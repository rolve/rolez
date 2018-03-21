package rolez.lang;

import static java.util.Collections.newSetFromMap;
import static org.junit.Assert.assertTrue;
import static rolez.lang.Eager.collectAndCheck;
import static rolez.lang.Eager.collectAndCheckGuarded;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import rolez.lang.Eager.ConcurrentInterferenceException;

public class EagerTest {
    
    /** public static Set<Guarded>[] collectAndCheck(Object[][] args, long idBits){
     * 
     * @param args in form passedT1, sharedT1, passedT2, sharedT2, ... 
     * returns the collected sets as follows:
     * 0: passedT1
     * 1: passedReachable T1
     * 2: sharedReachable T1
     * 3: passedT2
     * 4: passedReachable T2
     * 5: sharedReachable T2
     * ...
     * @return sets collected for potentially 
     */
    
    @Rule
    public final ExpectedException exception = ExpectedException.none();
    
    @BeforeClass
    public static void setup() {
        Task.registerNewRootTask();
    }
    
    @AfterClass
    public static void tearDown() {
        Task.unregisterRootTask();
    }
    
    @Test
    public void testInterferenceException() {
        GuardedArray<?> arr0 = GuardedArray.wrap(new Object[] {});
        GuardedArray<?> arr1 = GuardedArray.wrap(new Object[] {arr0});
        
        exception.expect(ConcurrentInterferenceException.class);
        collectAndCheck(new Object[][]{
                new Object[] {arr0},
                new Object[] {},
                new Object[] {},
                new Object[] {arr1},
            } , 1L);    
    }
    
    @Test
    public void testInterferenceExceptionGuarded() {
        GuardedArray<?> arr0 = GuardedArray.wrap(new Object[] {});
        GuardedArray<?> arr1 = GuardedArray.wrap(new Object[] {arr0});
        
        exception.expect(ConcurrentInterferenceException.class);
        collectAndCheckGuarded(new Object[][]{
                new Object[] {arr0},
                new Object[] {},
                new Object[] {},
                new Object[] {arr1},
            } , 1L);    
    }
    
    @Test
    public void testPassedAndSharedOverlap() {
        GuardedArray<?> arr0 = GuardedArray.wrap(new Object[] {});
        GuardedArray<?> arr1 = GuardedArray.wrap(new Object[] {arr0});
        
        exception.expect(ConcurrentInterferenceException.class);
        collectAndCheck(new Object[][]{
                new Object[] {arr0},
                new Object[] {arr1},
                new Object[] {},
                new Object[] {arr1},
            } , 1L);    
    }
    
    @Test
    public void testSliceInterferenceException() {
        GuardedArray<?> arr0 = GuardedArray.wrap(new Object[] {});
        GuardedArray<?> arr1 = GuardedArray.wrap(new Object[] {arr0});
        GuardedSlice<Object> slice = GuardedArray.wrap(new Object[] {0, arr0, 2, 3, 4, 5, arr1, 7, 8, 9 });
        
        GuardedSlice<Object> part1 = slice.slice(0,  3);
        GuardedSlice<Object> part2 = slice.slice(4, 10);
        
        
        exception.expect(ConcurrentInterferenceException.class);
        collectAndCheck(new Object[][]{
                new Object[] {part1},
                new Object[] {},
                new Object[] {},
                new Object[] {part2},
            } , 1L);    
    }
    
    @Test
    public void testSliceIterferenceOk() {
        GuardedArray<?> arr0 = GuardedArray.wrap(new Object[] {});
        GuardedArray<?> arr1 = GuardedArray.wrap(new Object[] {arr0});
        GuardedSlice<Object> slice = GuardedArray.wrap(new Object[] {0, arr0, 2, 3, 4, 5, arr1, 7, 8, 9 });
        
        GuardedSlice<Object> part1 = slice.slice(0,  3);
        GuardedSlice<Object> part2 = slice.slice(4, 10);
        GuardedSlice<Object> middle = slice.slice(3, 4);
        
        GuardedArray<?> otherObj = GuardedArray.wrap(new Object[] {});

        collectAndCheck(new Object[][]{
                new Object[] {middle},
                new Object[] {part1},
                new Object[] {otherObj},
                new Object[] {part2},
            } , 1L);
        

        collectAndCheckGuarded(new Object[][]{
                new Object[] {middle},
                new Object[] {part1},
                new Object[] {otherObj},
                new Object[] {part2},
            } , 1L);
    }
    
    @Test
    public void testReturnedSets() {
        GuardedArray<?> arr0 = GuardedArray.wrap(new Object[] {});
        GuardedArray<?> arr1 = GuardedArray.wrap(new Object[] {arr0});
        GuardedArray<?> arr2 = GuardedArray.wrap(new Object[] {});
        
        GuardedSlice<Object> slice = GuardedArray.wrap(new Object[] {0, arr0, 2, arr2, 4, 5, arr1, 7, 8, 9 });
        
        GuardedSlice<Object> part1 = slice.slice(0,  3);
        GuardedSlice<Object> middle = slice.slice(3, 4);
        GuardedSlice<Object> part2 = slice.slice(4, 10);
        
        GuardedArray<?> otherObj = GuardedArray.wrap(new Object[] {});
        
        Set<Guarded>[] returned = collectAndCheck(new Object[][]{
                new Object[] {middle},
                new Object[] {part1},
                new Object[] {otherObj},
                new Object[] {part2},
            } , 1L);
        
        // middle is the only object directly passed to T1
        assertSetContents(returned[0], middle);
        // middle and arr2 are both passed to T1
        assertSetContents(returned[1], middle, arr2);
        // part1 and arr2 are both shared to T1
        assertSetContents(returned[2], part1, arr0);
        
        assertSetContents(returned[3], otherObj);
        assertSetContents(returned[4], otherObj);
        assertSetContents(returned[5], part2, arr1, arr0);
        
        returned = collectAndCheckGuarded(new Object[][]{
                new Object[] {middle},
                new Object[] {part1},
                new Object[] {otherObj},
                new Object[] {part2},
            } , 1L);
        
        assertSetContents(returned[0], middle);
        assertSetContents(returned[1], middle, arr2);
        assertSetContents(returned[2], part1, arr0);
        
        assertSetContents(returned[3], otherObj);
        assertSetContents(returned[4], otherObj);
        assertSetContents(returned[5], part2, arr1, arr0);
    }
    
    /**
     * assert that the set contains exactly the elements specified
     * @param set
     * @param contents
     */
    private void assertSetContents(Set<?> set, Object... contents) {
        Set<Object> builtSet = newSetFromMap(new IdentityHashMap<Object, java.lang.Boolean>());
        builtSet.addAll(Arrays.asList(contents));
        assertTrue(set.equals(builtSet));
    }
}

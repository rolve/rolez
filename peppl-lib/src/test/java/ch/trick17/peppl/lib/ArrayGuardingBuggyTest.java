package ch.trick17.peppl.lib;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ch.trick17.peppl.lib.SomeClasses.Int;
import ch.trick17.peppl.lib.guard.Array;
import ch.trick17.peppl.lib.guard.IntArray;
import ch.trick17.peppl.lib.guard.IntSlice;
import ch.trick17.peppl.lib.guard.Slice;
import ch.trick17.peppl.lib.task.Task;
import ch.trick17.peppl.lib.task.TaskSystem;
import ch.trick17.simplejpf.test.JpfTest;

public class ArrayGuardingBuggyTest extends JpfTest {
    
    @Test
    public void testShareOverlappingSliceModify() {
        if(verifyNoPropertyViolation()) {
            final Array<Int> a = new Array<>(new Int[4]);
            for(int i = 0; i < a.data.length; i++)
                a.data[i] = new Int(i);
            
            final Slice<Int> slice1 = a.slice(0, 3, 1);
            final Slice<Int> slice2 = a.slice(1, 4, 1);
            slice1.share();
            final Task<Void> task = TaskSystem.getDefault().run(new Runnable() {
                public void run() {
                    assertEquals(1, slice1.data[1].value);
                    slice1.releaseShared();
                }
            });
            
            slice2.guardReadWrite();
            slice2.data[1] = new Int(100);
            
            task.get();
        }
    }
    
    @Test
    public void testShareOverlappingPrimitiveSlice() {
        if(verifyNoPropertyViolation()) {
            final IntArray a = new IntArray(0, 1, 2);
            
            final IntSlice slice1 = a.slice(0, 2, 1);
            final IntSlice slice2 = a.slice(1, 3, 1);
            slice1.share();
            final Task<Void> task = TaskSystem.getDefault().run(new Runnable() {
                public void run() {
                    assertEquals(1, slice1.data[1]);
                    slice1.releaseShared();
                }
            });
            
            slice2.guardReadWrite();
            slice2.data[1] = 2;
            task.get();
        }
    }
}

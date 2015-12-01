package ch.trick17.rolez.lang;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ch.trick17.rolez.lang.SomeClasses.Int;
import ch.trick17.rolez.lang.SomeClasses.Ref;
import ch.trick17.rolez.lang.guard.Array;
import ch.trick17.rolez.lang.guard.FinalArray;
import ch.trick17.rolez.lang.guard.FinalSlice;
import ch.trick17.rolez.lang.guard.IntArray;
import ch.trick17.rolez.lang.guard.IntSlice;
import ch.trick17.rolez.lang.guard.Slice;
import ch.trick17.rolez.lang.task.NewThreadTaskSystem;
import ch.trick17.rolez.lang.task.SingleThreadTaskSystem;
import ch.trick17.rolez.lang.task.Task;
import ch.trick17.rolez.lang.task.TaskSystem;
import ch.trick17.rolez.lang.task.ThreadPoolTaskSystem;

@RunWith(Parameterized.class)
public class ArrayGuardingTest extends GuardingTest {
    
    @Parameters(name = "{0}, {1}")
    public static List<?> taskSystems() {
        return Arrays.asList(new Object[][]{
                {new NewThreadTaskSystem(), VerifyMode.CORRECTNESS},
                {new ThreadPoolTaskSystem(), VerifyMode.CORRECTNESS},
                {new SingleThreadTaskSystem(), VerifyMode.CORRECTNESS},
                {new NewThreadTaskSystem(), VerifyMode.PARALLELISM},
                {new ThreadPoolTaskSystem(3), VerifyMode.PARALLELISM}});
    }
    
    public ArrayGuardingTest(final TaskSystem s, final VerifyMode mode) {
        super(s, mode);
    }
    
    @Test
    public void testShareArray() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final Array<Int> a = new Array<>(new Int[3]);
            for(int i = 0; i < a.data.length; i++)
                a.data[i] = new Int(i);
            
            a.share();
            final Task<Void> task = s.start(new RunnableCallable() {
                public void run() {
                    assertEquals(2, a.data[2].value);
                    a.releaseShared();
                }
            });
            
            a.guardRead();
            a.data[2].guardReadWrite();
            a.data[2].value = 1;
            
            task.get();
        }
    }
    
    @Test
    public void testShareFinalArray() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final FinalArray<Int> a = new FinalArray<>(new Int[3]);
            // Initialization may be done right after array creation
            for(int i = 0; i < a.data.length; i++)
                a.data[i] = new Int(i);
            
            a.share();
            final Task<Void> task = s.start(new RunnableCallable() {
                public void run() {
                    assertEquals(2, a.data[2].value);
                    a.releaseShared();
                }
            });
            
            // No guard required for reading element
            a.data[2].guardReadWrite();
            a.data[2].value = 1;
            
            task.get();
        }
    }
    
    @Test
    public void testSharePrimitiveArray() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final IntArray a = new IntArray(new int[]{0});
            
            a.share();
            final Task<Void> task = s.start(new RunnableCallable() {
                public void run() {
                    assertEquals(0, a.data[0]);
                    a.releaseShared();
                }
            });
            
            a.guardReadWrite();
            a.data[0] = 1;
            task.get();
        }
    }
    
    @Test
    public void testPassArray() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final Array<Int> a = new Array<>(new Int[3]);
            for(int i = 0; i < a.data.length; i++)
                a.data[i] = new Int(i);
            
            a.pass();
            final Task<Void> task = s.start(new RunnableCallable() {
                public void run() {
                    a.registerNewOwner();
                    for(int i = 0; i < a.data.length; i++)
                        a.data[i].value++;
                    a.releasePassed();
                }
            });
            
            a.guardRead();
            for(int i = 0; i < a.data.length; i++) {
                a.data[i].guardRead();
                assertEquals(i + 1, a.data[i].value);
            }
            
            task.get();
        }
    }
    
    @Test
    public void testPassPrimitiveArray() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final IntArray a = new IntArray(0, 1, 2);
            
            a.pass();
            final Task<Void> task = s.start(new RunnableCallable() {
                public void run() {
                    a.registerNewOwner();
                    for(int i = 0; i < a.data.length; i++)
                        a.data[i]++;
                    a.releasePassed();
                }
            });
            
            a.guardRead();
            for(int i = 0; i < a.data.length; i++)
                assertEquals(i + 1, a.data[i]);
            task.get();
        }
    }
    
    @Test
    public void testShareArrayElement() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final Int i = new Int();
            final Array<Int> a = new Array<>(i);
            
            i.share();
            final Task<Void> task1 = s.start(new RunnableCallable() {
                public void run() {
                    assertEquals(0, i.value);
                    i.releaseShared();
                }
            });
            
            a.share();
            final Task<Void> task2 = s.start(new RunnableCallable() {
                public void run() {
                    assertEquals(0, a.data[0].value);
                    a.releaseShared();
                }
            });
            
            i.share();
            final Task<Void> task3 = s.start(new RunnableCallable() {
                public void run() {
                    assertEquals(0, i.value);
                    i.releaseShared();
                }
            });
            
            i.guardReadWrite();
            i.value = 1;
            
            task1.get();
            task2.get();
            task3.get();
        }
    }
    
    @Test
    public void testPassArrayElement() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final Int i = new Int();
            final Array<Int> a = new Array<>(i);
            
            i.pass();
            final Task<Void> task1 = s.start(new RunnableCallable() {
                public void run() {
                    i.registerNewOwner();
                    i.value++;
                    i.releasePassed();
                }
            });
            
            a.pass();
            final Task<Void> task2 = s.start(new RunnableCallable() {
                public void run() {
                    a.registerNewOwner();
                    a.data[0].value++;
                    a.releasePassed();
                }
            });
            
            i.pass();
            final Task<Void> task3 = s.start(new RunnableCallable() {
                public void run() {
                    i.registerNewOwner();
                    i.value++;
                    i.releasePassed();
                }
            });
            
            i.guardRead();
            assertEquals(3, i.value);
            task1.get();
            task2.get();
            task3.get();
        }
    }
    
    @Test
    public void testPassArrayElementNestedModify() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final Int i = new Int();
            final Array<Int> a = new Array<>(i);
            
            a.pass();
            final Task<Void> task = s.start(new RunnableCallable() {
                public void run() {
                    a.registerNewOwner();
                    a.data[0] = new Int();
                    
                    final Int i2 = a.data[0];
                    i2.value++;
                    i2.pass();
                    final Task<Void> task2 = s.start(new RunnableCallable() {
                        public void run() {
                            i2.registerNewOwner();
                            i2.value++;
                            i2.releasePassed();
                        }
                    });
                    
                    i2.guardReadWrite();
                    assertEquals(2, i2.value);
                    i2.value++;
                    
                    a.releasePassed();
                    task2.get();
                }
            });
            
            a.guardRead();
            a.data[0].guardRead();
            assertEquals(3, a.data[0].value);
            task.get();
        }
    }
    
    @Test
    public void testShareSlice() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final Array<Int> a = new Array<>(new Int[4]);
            for(int i = 0; i < a.data.length; i++)
                a.data[i] = new Int(i);
            
            final Slice<Int> slice = a.slice(0, 2, 1);
            slice.share();
            final Task<Void> task = s.start(new RunnableCallable() {
                public void run() {
                    assertEquals(1, slice.data[1].value);
                    slice.releaseShared();
                }
            });
            
            a.guardRead();
            a.data[1].guardReadWrite();
            a.data[1].value = 0;
            
            task.get();
        }
    }
    
    @Test
    public void testShareSliceModify() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final Array<Int> a = new Array<>(new Int[4]);
            for(int i = 0; i < a.data.length; i++)
                a.data[i] = new Int(i);
            
            final Slice<Int> slice = a.slice(0, 2, 1);
            slice.share();
            final Task<Void> task = s.start(new RunnableCallable() {
                public void run() {
                    assertEquals(1, slice.data[1].value);
                    slice.releaseShared();
                }
            });
            
            a.guardReadWrite();
            a.data[1] = new Int(100);
            
            task.get();
        }
    }
    
    @Test
    public void testSliceSharedArray() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final Array<Int> a = new Array<>(new Int[4]);
            for(int i = 0; i < a.data.length; i++)
                a.data[i] = new Int(i);
            
            a.share();
            final Task<Void> task = s.start(new RunnableCallable() {
                public void run() {
                    final Slice<Int> slice = a.slice(0, 2, 1);
                    a.releaseShared();
                    /* Use slice again to prevent incidental correct behavior
                     * due to garbage collection */
                    slice.toString();
                }
            });
            
            task.get();
        }
    }
    
    @Test
    public void testSliceModifySharedArray() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final Array<Int> a = new Array<>(new Int[4]);
            for(int i = 0; i < a.data.length; i++)
                a.data[i] = new Int(i);
            
            a.share();
            final Task<Void> task = s.start(new RunnableCallable() {
                public void run() {
                    assertEquals(1, a.data[1].value);
                    a.releaseShared();
                }
            });
            
            final Slice<Int> slice = a.slice(0, 2, 1);
            slice.guardReadWrite();
            slice.data[1] = new Int(0);
            
            task.get();
        }
    }
    
    // FIXME: Fix guarding of overlapping slices:
    
    @Test
    public void testShareOverlappingSliceModify() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final Array<Int> a = new Array<>(new Int[4]);
            for(int i = 0; i < a.data.length; i++)
                a.data[i] = new Int(i);
            
            final Slice<Int> slice1 = a.slice(0, 3, 1);
            final Slice<Int> slice2 = a.slice(1, 4, 1);
            slice1.share();
            final Task<Void> task = s.start(new RunnableCallable() {
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
    
    // FIXME: Test guarding of striped slices
    
    @Test
    public void testShareFinalSlice() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final FinalArray<Int> a = new FinalArray<>(new Int[3]);
            // Initialization may be done right after array creation
            for(int i = 0; i < a.data.length; i++)
                a.data[i] = new Int(i);
            
            final FinalSlice<Int> slice = a.slice(0, 2, 1);
            slice.share();
            final Task<Void> task = s.start(new RunnableCallable() {
                public void run() {
                    assertEquals(1, slice.data[1].value);
                    slice.releaseShared();
                }
            });
            
            // No read required for reading element
            a.data[1].guardReadWrite();
            a.data[1].value = 0;
            
            task.get();
        }
    }
    
    @Test
    public void testSharePrimitiveSlice() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final IntArray a = new IntArray(0, 1);
            
            final IntSlice slice = a.slice(0, 1, 1);
            slice.share();
            final Task<Void> task = s.start(new RunnableCallable() {
                public void run() {
                    assertEquals(0, slice.data[0]);
                    slice.releaseShared();
                }
            });
            
            a.guardReadWrite();
            a.data[0] = 1;
            task.get();
        }
    }
    
    @Test
    public void testShareReferencedPrimitiveSlice() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final IntArray a = new IntArray(0, 1, 2, 3);
            
            final IntSlice slice = a.slice(0, 2, 1);
            final Ref<IntSlice> ref = new Ref<>(slice);
            ref.share();
            final Task<Void> task = s.start(new RunnableCallable() {
                public void run() {
                    final IntSlice slice2 = ref.o.slice(1, 2, 1);
                    assertEquals(1, slice2.data[1]);
                    ref.releaseShared();
                    slice2.toString();
                }
            });
            
            a.guardReadWrite();
            a.data[1] = 0;
            
            task.get();
        }
    }
    
    @Test
    public void testShareOverlappingPrimitiveSlice() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final IntArray a = new IntArray(0, 1, 2);
            
            final IntSlice slice1 = a.slice(0, 2, 1);
            final IntSlice slice2 = a.slice(1, 3, 1);
            slice1.share();
            final Task<Void> task = s.start(new RunnableCallable() {
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
    
    @Test
    public void testShareSubslice() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final Array<Int> a = new Array<>(new Int[4]);
            for(int i = 0; i < a.data.length; i++)
                a.data[i] = new Int(i);
            
            Slice<Int> slice = a.slice(0, 3, 1);
            final Slice<Int> subslice = slice.slice(0, 2, 1);
            
            slice = null; // slice is not referenced anymore!
            System.gc();
            
            subslice.share();
            final Task<Void> task = s.start(new RunnableCallable() {
                public void run() {
                    assertEquals(0, subslice.data[0].value);
                    subslice.releaseShared();
                }
            });
            
            a.guardReadWrite();
            a.data[0] = new Int(1);
            
            task.get();
        }
    }
    
    @Test
    public void testPassSlice() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final Array<Int> a = new Array<>(new Int[10]);
            for(int i = 0; i < a.data.length; i++)
                a.data[i] = new Int(i);
            
            final Slice<Int> slice = a.slice(0, 5, 1);
            slice.pass();
            final Task<Void> task = s.start(new RunnableCallable() {
                public void run() {
                    slice.registerNewOwner();
                    for(int i = slice.range.begin; i < slice.range.end; i++)
                        slice.data[i].value++;
                    slice.releasePassed();
                }
            });
            
            a.guardRead();
            for(int i = 0; i < slice.range.end; i++) {
                a.data[i].guardRead();
                assertEquals(i + 1, a.data[i].value);
            }
            for(int i = slice.range.end; i < a.data.length; i++) {
                a.data[i].guardRead();
                assertEquals(i, a.data[i].value);
            }
            task.get();
        }
    }
    
    @Test
    public void testPassFinalSlice() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final FinalArray<Int> a = new FinalArray<>(new Int[6]);
            for(int i = 0; i < a.data.length; i++)
                a.data[i] = new Int(i);
            
            final FinalSlice<Int> slice = a.slice(0, a.range.size() / 2, 1);
            slice.pass();
            final Task<Void> task = s.start(new RunnableCallable() {
                public void run() {
                    slice.registerNewOwner();
                    for(int i = slice.range.begin; i < slice.range.end; i++)
                        slice.data[i].value++;
                    // IMPROVE: Find a way to release array elements separately
                    // and earlier to allow more parallelism
                    slice.releasePassed();
                }
            });
            
            for(int i = 0; i < slice.range.end; i++) {
                a.data[i].guardRead();
                assertEquals(i + 1, a.data[i].value);
            }
            for(int i = slice.range.end; i < a.data.length; i++) {
                a.data[i].guardRead();
                assertEquals(i, a.data[i].value);
            }
            task.get();
        }
    }
    
    @Test
    public void testPassPrimitiveSlice() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final IntArray a = new IntArray(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
            
            final IntSlice slice = a.slice(0, 5, 1);
            slice.pass();
            final Task<Void> task = s.start(new RunnableCallable() {
                public void run() {
                    slice.registerNewOwner();
                    for(int i = slice.range.begin; i < slice.range.end; i++)
                        slice.data[i]++;
                    slice.releasePassed();
                }
            });
            
            a.guardRead();
            for(int i = 0; i < slice.range.end; i++)
                assertEquals(i + 1, a.data[i]);
            for(int i = slice.range.end; i < a.data.length; i++)
                assertEquals(i, a.data[i]);
            task.get();
        }
    }
    
    @Test
    public void testPassReferencedPrimitiveSlice() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final IntArray a = new IntArray(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
            
            final IntSlice slice = a.slice(0, 5, 1);
            final Ref<IntSlice> ref = new Ref<>(slice);
            ref.pass();
            final Task<Void> task = s.start(new RunnableCallable() {
                public void run() {
                    ref.registerNewOwner();
                    final IntSlice slice2 = ref.o.slice(0, 5, 1);
                    for(int i = slice2.range.begin; i < slice2.range.end; i++)
                        slice2.data[i]++;
                    ref.releasePassed();
                    slice2.toString();
                }
            });
            
            a.guardRead();
            for(int i = 0; i < slice.range.end; i++)
                assertEquals(i + 1, a.data[i]);
            for(int i = slice.range.end; i < a.data.length; i++)
                assertEquals(i, a.data[i]);
            task.get();
        }
    }
    
    @Test
    public void testShareSliceMultiple() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final Int i = new Int();
            final Array<Int> a = new Array<>(i);
            final Slice<Int> slice = a.slice(0, 1, 1);
            
            slice.share();
            final Task<Void> task1 = s.start(new RunnableCallable() {
                public void run() {
                    assertEquals(0, slice.data[0].value);
                    slice.releaseShared();
                }
            });
            
            a.share();
            final Task<Void> task2 = s.start(new RunnableCallable() {
                public void run() {
                    assertEquals(0, a.data[0].value);
                    a.releaseShared();
                }
            });
            
            slice.share();
            final Task<Void> task3 = s.start(new RunnableCallable() {
                public void run() {
                    assertEquals(0, slice.data[0].value);
                    slice.releaseShared();
                }
            });
            
            i.guardReadWrite();
            i.value = 1;
            
            task1.get();
            task2.get();
            task3.get();
        }
    }
    
    @Test
    public void testPassSliceMultiple() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final Int i = new Int();
            final Array<Int> a = new Array<>(i);
            final Slice<Int> slice = a.slice(0, 1, 1);
            
            slice.pass();
            final Task<Void> task1 = s.start(new RunnableCallable() {
                public void run() {
                    slice.registerNewOwner();
                    slice.data[0].value++;
                    slice.releasePassed();
                }
            });
            
            a.pass();
            final Task<Void> task2 = s.start(new RunnableCallable() {
                public void run() {
                    a.registerNewOwner();
                    a.data[0].value++;
                    a.releasePassed();
                }
            });
            
            slice.pass();
            final Task<Void> task3 = s.start(new RunnableCallable() {
                public void run() {
                    slice.registerNewOwner();
                    slice.data[0].value++;
                    slice.releasePassed();
                }
            });
            
            i.guardRead();
            assertEquals(3, i.value);
            task1.get();
            task2.get();
            task3.get();
        }
    }
    
    @Test
    public void testPassDifferentSlices() {
        if(verify(mode)) {
            final Array<Int> a = new Array<>(new Int[10]);
            for(int i = 0; i < a.data.length; i++)
                a.data[i] = new Int(i);
            
            final Slice<Int> slice1 = a.slice(0, 5, 1);
            final Slice<Int> slice2 =
                    a.slice(slice1.range.end, a.data.length, 1);
            
            slice1.pass();
            final Task<Void> task1 = s.start(new RunnableCallable() {
                public void run() {
                    slice1.registerNewOwner();
                    for(int i = slice1.range.begin; i < slice1.range.end; i++)
                        slice1.data[i].value++;
                    region(0);
                    slice1.releasePassed();
                }
            });
            
            slice2.pass();
            final Task<Void> task2 = s.start(new RunnableCallable() {
                public void run() {
                    slice2.registerNewOwner();
                    for(int i = slice2.range.begin; i < slice2.range.end; i++)
                        slice2.data[i].value++;
                    region(1);
                    slice2.releasePassed();
                }
            });
            region(2);
            
            a.guardRead();
            for(int i = 0; i < a.data.length; i++) {
                a.data[i].guardRead();
                assertEquals(i + 1, a.data[i].value);
            }
            task1.get();
            task2.get();
        }
    }
    
    @Test
    public void testPassSubsliceNested() {
        /* IMPROVE: Allow {0, 2} by releasing slices independent of subslices
         * that are still owned by other threads. */
        if(verify(mode, new int[][]{{1, 2}, {0, 2}})) {
            final Array<Int> a = new Array<>(new Int[3]);
            for(int i = 0; i < a.data.length; i++)
                a.data[i] = new Int(i);
            
            final Slice<Int> slice1 = a.slice(0, 2, 1);
            slice1.pass();
            final Task<Void> task = s.start(new RunnableCallable() {
                public void run() {
                    slice1.registerNewOwner();
                    
                    final Slice<Int> slice2 = slice1.slice(0, 1, 1);
                    slice2.pass();
                    final Task<Void> task2 = s.start(new RunnableCallable() {
                        public void run() {
                            slice2.registerNewOwner();
                            slice2.data[0].value++;
                            region(0);
                            slice2.releasePassed();
                        }
                    });
                    
                    slice1.data[1].value++;
                    region(1);
                    
                    slice1.releasePassed();
                    region(2);
                    task2.get();
                }
            });
            
            a.data[2].value++;
            region(3);
            
            a.guardRead();
            for(int i = 0; i < a.data.length; i++) {
                a.data[i].guardRead();
                assertEquals(i + 1, a.data[i].value);
            }
            task.get();
        }
    }
    
    @Test
    public void testPassPrimitiveSubsliceNested() {
        /* IMPROVE: Allow {0, 2} by releasing slices independent of subslices
         * that are still owned by other threads. */
        if(verify(mode, new int[][]{{1, 2}, {0, 2}})) {
            final IntArray a = new IntArray(0, 1, 2);
            
            final IntSlice slice1 = a.slice(0, 2, 1);
            slice1.pass();
            final Task<Void> task = s.start(new RunnableCallable() {
                public void run() {
                    slice1.registerNewOwner();
                    
                    final IntSlice slice2 = slice1.slice(0, 1, 1);
                    slice2.pass();
                    final Task<Void> task2 = s.start(new RunnableCallable() {
                        public void run() {
                            slice2.registerNewOwner();
                            slice2.data[0]++;
                            region(0);
                            slice2.releasePassed();
                        }
                    });
                    
                    slice1.data[1]++;
                    region(1);
                    
                    slice1.releasePassed();
                    region(2);
                    task2.get();
                }
            });
            
            a.data[2]++;
            region(3);
            
            a.guardRead();
            for(int i = 0; i < a.data.length; i++)
                assertEquals(i + 1, a.data[i]);
            task.get();
        }
    }
}

package rolez.lang;

import static org.junit.Assert.assertEquals;
import static rolez.lang.Guarded.guardReadOnly;
import static rolez.lang.Guarded.guardReadWrite;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import rolez.lang.SomeClasses.Int;
import rolez.lang.SomeClasses.Ref;

@RunWith(Parameterized.class)
public class ArrayGuardingTest extends GuardingTest {
    
    @Parameters(name = "{0}, {1}")
    public static List<?> taskSystems() {
        return Arrays.asList(new Object[][]{{new NewThreadTaskSystem(), VerifyMode.CORRECTNESS}, {
                new ThreadPoolTaskSystem(), VerifyMode.CORRECTNESS}, {new SingleThreadTaskSystem(),
                        VerifyMode.CORRECTNESS}, {new NewThreadTaskSystem(),
                                VerifyMode.PARALLELISM}, {new ThreadPoolTaskSystem(3),
                                        VerifyMode.PARALLELISM}});
    }
    
    public ArrayGuardingTest(final TaskSystem s, final VerifyMode mode) {
        super(s, mode);
    }
    
    @Test
    public void testShareArray() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final GuardedArray<Int[]> a = new GuardedArray<>(new Int[3]);
            for(int i = 0; i < a.data.length; i++)
                a.data[i] = new Int(i);
                
            a.share();
            final Task<Void> task = s.start(new RunnableCallable() {
                public void run() {
                    assertEquals(2, a.data[2].value);
                    a.releaseShared();
                }
            });
            
            guardReadOnly(a);
            guardReadWrite(a.data[2]);
            a.data[2].value = 1;
            
            task.get();
        }
    }
    
    @Test
    public void testSharePrimitiveArray() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final GuardedArray<int[]> a = new GuardedArray<>(new int[]{0});
            
            a.share();
            final Task<Void> task = s.start(new RunnableCallable() {
                public void run() {
                    assertEquals(0, a.data[0]);
                    a.releaseShared();
                }
            });
            
            guardReadWrite(a);
            a.data[0] = 1;
            task.get();
        }
    }
    
    @Test
    public void testPassArray() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final GuardedArray<Int[]> a = new GuardedArray<>(new Int[3]);
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
            
            guardReadOnly(a);
            for(int i = 0; i < a.data.length; i++) {
                guardReadOnly(a.data[i]);
                assertEquals(i + 1, a.data[i].value);
            }
            
            task.get();
        }
    }
    
    @Test
    public void testPassPrimitiveArray() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final GuardedArray<int[]> a = new GuardedArray<>(new int[]{0, 1, 2});
            
            a.pass();
            final Task<Void> task = s.start(new RunnableCallable() {
                public void run() {
                    a.registerNewOwner();
                    for(int i = 0; i < a.data.length; i++)
                        a.data[i]++;
                    a.releasePassed();
                }
            });
            
            guardReadOnly(a);
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
            final GuardedArray<Int[]> a = new GuardedArray<>(new Int[]{i});
            
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
            
            guardReadWrite(i);
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
            final GuardedArray<Int[]> a = new GuardedArray<>(new Int[]{i});
            
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
            
            guardReadOnly(i);
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
            final GuardedArray<Int[]> a = new GuardedArray<>(new Int[]{i});
            
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
                    
                    guardReadWrite(i2);
                    assertEquals(2, i2.value);
                    i2.value++;
                    
                    a.releasePassed();
                    task2.get();
                }
            });
            
            guardReadOnly(a);
            guardReadOnly(a.data[0]);
            assertEquals(3, a.data[0].value);
            task.get();
        }
    }
    
    @Test
    public void testShareSlice() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final GuardedArray<Int[]> a = new GuardedArray<>(new Int[4]);
            for(int i = 0; i < a.data.length; i++)
                a.data[i] = new Int(i);
                
            final GuardedSlice<Int[]> slice = a.slice(0, 2, 1);
            slice.share();
            final Task<Void> task = s.start(new RunnableCallable() {
                public void run() {
                    assertEquals(1, slice.data[1].value);
                    slice.releaseShared();
                }
            });
            
            guardReadOnly(a);
            guardReadWrite(a.data[1]);
            a.data[1].value = 0;
            
            task.get();
        }
    }
    
    @Test
    public void testShareSliceModify() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final GuardedArray<Int[]> a = new GuardedArray<>(new Int[4]);
            for(int i = 0; i < a.data.length; i++)
                a.data[i] = new Int(i);
                
            final GuardedSlice<Int[]> slice = a.slice(0, 2, 1);
            slice.share();
            final Task<Void> task = s.start(new RunnableCallable() {
                public void run() {
                    assertEquals(1, slice.data[1].value);
                    slice.releaseShared();
                }
            });
            
            guardReadWrite(a);
            a.data[1] = new Int(100);
            
            task.get();
        }
    }
    
    @Test
    public void testSliceSharedArray() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final GuardedArray<Int[]> a = new GuardedArray<>(new Int[4]);
            for(int i = 0; i < a.data.length; i++)
                a.data[i] = new Int(i);
                
            a.share();
            final Task<Void> task = s.start(new RunnableCallable() {
                public void run() {
                    final GuardedSlice<Int[]> slice = a.slice(0, 2, 1);
                    a.releaseShared();
                    /* Use slice again to prevent incidental correct behavior due to garbage
                     * collection */
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
            final GuardedArray<Int[]> a = new GuardedArray<>(new Int[4]);
            for(int i = 0; i < a.data.length; i++)
                a.data[i] = new Int(i);
                
            a.share();
            final Task<Void> task = s.start(new RunnableCallable() {
                public void run() {
                    assertEquals(1, a.data[1].value);
                    a.releaseShared();
                }
            });
            
            final GuardedSlice<Int[]> slice = a.slice(0, 2, 1);
            guardReadWrite(slice);
            slice.data[1] = new Int(0);
            
            task.get();
        }
    }
    
    // FIXME: Fix guarding of overlapping slices:
    
    @Test
    public void testShareOverlappingSliceModify() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final GuardedArray<Int[]> a = new GuardedArray<>(new Int[4]);
            for(int i = 0; i < a.data.length; i++)
                a.data[i] = new Int(i);
                
            final GuardedSlice<Int[]> slice1 = a.slice(0, 3, 1);
            final GuardedSlice<Int[]> slice2 = a.slice(1, 4, 1);
            slice1.share();
            final Task<Void> task = s.start(new RunnableCallable() {
                public void run() {
                    assertEquals(1, slice1.data[1].value);
                    slice1.releaseShared();
                }
            });
            
            guardReadWrite(slice2);
            slice2.data[1] = new Int(100);
            
            task.get();
        }
    }
    
    // FIXME: Test guarding of striped slices
    
    @Test
    public void testSharePrimitiveSlice() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final GuardedArray<int[]> a = new GuardedArray<>(new int[]{0, 1});
            
            final GuardedSlice<int[]> slice = a.slice(0, 1, 1);
            slice.share();
            final Task<Void> task = s.start(new RunnableCallable() {
                public void run() {
                    assertEquals(0, slice.data[0]);
                    slice.releaseShared();
                }
            });
            
            guardReadWrite(a);
            a.data[0] = 1;
            task.get();
        }
    }
    
    @Test
    public void testShareReferencedPrimitiveSlice() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final GuardedArray<int[]> a = new GuardedArray<>(new int[]{0, 1, 2, 3});
            
            final GuardedSlice<int[]> slice = a.slice(0, 2, 1);
            final Ref<GuardedSlice<int[]>> ref = new Ref<>(slice);
            ref.share();
            final Task<Void> task = s.start(new RunnableCallable() {
                public void run() {
                    final GuardedSlice<int[]> slice2 = ref.o.slice(1, 2, 1);
                    assertEquals(1, slice2.data[1]);
                    ref.releaseShared();
                }
            });
            
            guardReadWrite(a);
            a.data[1] = 0;
            
            task.get();
        }
    }
    
    @Test
    public void testShareOverlappingPrimitiveSlice() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final GuardedArray<int[]> a = new GuardedArray<>(new int[]{0, 1, 2});
            
            final GuardedSlice<int[]> slice1 = a.slice(0, 2, 1);
            final GuardedSlice<int[]> slice2 = a.slice(1, 3, 1);
            slice1.share();
            final Task<Void> task = s.start(new RunnableCallable() {
                public void run() {
                    assertEquals(1, slice1.data[1]);
                    slice1.releaseShared();
                }
            });
            
            guardReadWrite(slice2);
            slice2.data[1] = 2;
            task.get();
        }
    }
    
    @Test
    public void testShareSubslice() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final GuardedArray<Int[]> a = new GuardedArray<>(new Int[4]);
            for(int i = 0; i < a.data.length; i++)
                a.data[i] = new Int(i);
                
            GuardedSlice<Int[]> slice = a.slice(0, 3, 1);
            final GuardedSlice<Int[]> subslice = slice.slice(0, 2, 1);
            
            slice = null; // slice is not referenced anymore!
            java.lang.System.gc();
            
            subslice.share();
            final Task<Void> task = s.start(new RunnableCallable() {
                public void run() {
                    assertEquals(0, subslice.data[0].value);
                    subslice.releaseShared();
                }
            });
            
            guardReadWrite(a);
            a.data[0] = new Int(1);
            
            task.get();
        }
    }
    
    @Test
    public void testPassSlice() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final GuardedArray<Int[]> a = new GuardedArray<>(new Int[10]);
            for(int i = 0; i < a.data.length; i++)
                a.data[i] = new Int(i);
                
            final GuardedSlice<Int[]> slice = a.slice(0, 5, 1);
            slice.pass();
            final Task<Void> task = s.start(new RunnableCallable() {
                public void run() {
                    slice.registerNewOwner();
                    for(int i = slice.range.begin; i < slice.range.end; i++)
                        slice.data[i].value++;
                    slice.releasePassed();
                }
            });
            
            guardReadOnly(a);
            for(int i = 0; i < slice.range.end; i++) {
                guardReadOnly(a.data[i]);
                assertEquals(i + 1, a.data[i].value);
            }
            for(int i = slice.range.end; i < a.data.length; i++) {
                guardReadOnly(a.data[i]);
                assertEquals(i, a.data[i].value);
            }
            task.get();
        }
    }
    
    @Test
    public void testPassPrimitiveSlice() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final GuardedArray<int[]> a = new GuardedArray<>(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8,
                    9});
                    
            final GuardedSlice<int[]> slice = a.slice(0, 5, 1);
            slice.pass();
            final Task<Void> task = s.start(new RunnableCallable() {
                public void run() {
                    slice.registerNewOwner();
                    for(int i = slice.range.begin; i < slice.range.end; i++)
                        slice.data[i]++;
                    slice.releasePassed();
                }
            });
            
            guardReadOnly(a);
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
            final GuardedArray<int[]> a = new GuardedArray<>(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8,
                    9});
                    
            final GuardedSlice<int[]> slice = a.slice(0, 5, 1);
            final Ref<GuardedSlice<int[]>> ref = new Ref<>(slice);
            ref.pass();
            final Task<Void> task = s.start(new RunnableCallable() {
                public void run() {
                    ref.registerNewOwner();
                    final GuardedSlice<int[]> slice2 = ref.o.slice(0, 5, 1);
                    for(int i = slice2.range.begin; i < slice2.range.end; i++)
                        slice2.data[i]++;
                    ref.releasePassed();
                    slice2.toString();
                }
            });
            
            guardReadOnly(a);
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
            final GuardedArray<Int[]> a = new GuardedArray<>(new Int[]{i});
            final GuardedSlice<Int[]> slice = a.slice(0, 1, 1);
            
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
            
            guardReadWrite(i);
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
            final GuardedArray<Int[]> a = new GuardedArray<>(new Int[]{i});
            final GuardedSlice<Int[]> slice = a.slice(0, 1, 1);
            
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
            
            guardReadOnly(i);
            assertEquals(3, i.value);
            task1.get();
            task2.get();
            task3.get();
        }
    }
    
    @Test
    public void testPassDifferentSlices() {
        if(verify(mode)) {
            final GuardedArray<Int[]> a = new GuardedArray<>(new Int[10]);
            for(int i = 0; i < a.data.length; i++)
                a.data[i] = new Int(i);
                
            final GuardedSlice<Int[]> slice1 = a.slice(0, 5, 1);
            final GuardedSlice<Int[]> slice2 = a.slice(slice1.range.end, a.data.length, 1);
            
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
            
            guardReadOnly(a);
            for(int i = 0; i < a.data.length; i++) {
                guardReadOnly(a.data[i]);
                assertEquals(i + 1, a.data[i].value);
            }
            task1.get();
            task2.get();
        }
    }
    
    @Test
    public void testPassSubsliceNested() {
        /* IMPROVE: Allow {0, 2} by releasing slices independent of subslices that are still owned
         * by other threads. */
        if(verify(mode, new int[][]{{1, 2}, {0, 2}})) {
            final GuardedArray<Int[]> a = new GuardedArray<>(new Int[3]);
            for(int i = 0; i < a.data.length; i++)
                a.data[i] = new Int(i);
                
            final GuardedSlice<Int[]> slice1 = a.slice(0, 2, 1);
            slice1.pass();
            final Task<Void> task = s.start(new RunnableCallable() {
                public void run() {
                    slice1.registerNewOwner();
                    
                    final GuardedSlice<Int[]> slice2 = slice1.slice(0, 1, 1);
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
            
            guardReadOnly(a);
            for(int i = 0; i < a.data.length; i++) {
                guardReadOnly(a.data[i]);
                assertEquals(i + 1, a.data[i].value);
            }
            task.get();
        }
    }
    
    @Test
    public void testPassPrimitiveSubsliceNested() {
        /* IMPROVE: Allow {0, 2} by releasing slices independent of subslices that are still owned
         * by other threads. */
        if(verify(mode, new int[][]{{1, 2}, {0, 2}})) {
            final GuardedArray<int[]> a = new GuardedArray<>(new int[]{0, 1, 2});
            
            final GuardedSlice<int[]> slice1 = a.slice(0, 2, 1);
            slice1.pass();
            final Task<Void> task = s.start(new RunnableCallable() {
                public void run() {
                    slice1.registerNewOwner();
                    
                    final GuardedSlice<int[]> slice2 = slice1.slice(0, 1, 1);
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
            
            guardReadOnly(a);
            for(int i = 0; i < a.data.length; i++)
                assertEquals(i + 1, a.data[i]);
            task.get();
        }
    }
    
    @Test
    public void testSlicePureArray() {
        if(verify(mode, new int[][]{{0, 2}, {1, 2}})) {
            final GuardedArray<int[]> a = new GuardedArray<>(new int[]{0, 1});
            
            a.pass();
            Task<Void> task = s.start(new RunnableCallable() {
                public void run() {
                    a.registerNewOwner();
                    a.data[0]++;
                    region(0);
                    a.releasePassed();
                }
            });
            
            GuardedSlice<int[]> slice = a.slice(0, 1, 1); // Slicing never blocks
            region(1);
            
            guardReadOnly(slice); // A slice created from a dynamically pure array is also pure, i.e., this may block
            assertEquals(1, slice.data[0]);
            region(2);
            
            task.get();
        }
    }
    
    @Test
    public void testSliceAndPassInParallel() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final GuardedArray<int[]> a = new GuardedArray<>(new int[]{0});
            
            a.pass();
            Task<Void> task = s.start(new RunnableCallable() {
                public void run() {
                    a.registerNewOwner();
                    
                    a.pass();
                    s.start(new RunnableCallable() {
                        public void run() {
                            a.registerNewOwner();
                            a.releasePassed();
                        }
                    });
                    
                    a.releasePassed();
                }
            });
            
            a.slice(0, 1, 1);
            task.get();
        }
    }
    
    @Test
    public void testSliceAndShareInParallel() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final GuardedArray<int[]> a = new GuardedArray<>(new int[]{0});
            
            a.share();
            Task<Void> task = s.start(new RunnableCallable() {
                public void run() {
                    a.share();
                    s.start(new RunnableCallable() {
                        public void run() {
                            assertEquals(0, a.data[0]);
                            a.releaseShared();
                        }
                    });
                    
                    a.releaseShared();
                }
            });
            
            GuardedSlice<int[]> slice = a.slice(0, 1, 1);
            guardReadWrite(slice);
            slice.data[0]++;
            task.get();
        }
    }
    
    @Test
    public void testSliceInParallel() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final GuardedArray<int[]> a = new GuardedArray<>(new int[]{0, 1, 2});
            
            a.pass();
            Task<Void> task = s.start(new RunnableCallable() {
                public void run() {
                    a.registerNewOwner();
                    
                    final GuardedSlice<int[]> slice = a.slice(1, 3, 1); // Slicing in parallel
                    slice.pass();
                    s.start(new RunnableCallable() {
                        public void run() {
                            slice.registerNewOwner();
                            slice.data[1]++;
                            slice.releasePassed();
                        }
                    });
                    
                    a.releasePassed();
                }
            });
            
            GuardedSlice<int[]> slice = a.slice(0, 2, 1); // Slicing in parallel
            guardReadOnly(slice);
            assertEquals(2, slice.data[1]);
            task.get();
        }
    }
}

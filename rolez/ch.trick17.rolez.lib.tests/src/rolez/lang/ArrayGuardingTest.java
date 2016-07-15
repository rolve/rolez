package rolez.lang;

import static ch.trick17.simplejpf.test.JpfParallelismTest.VerifyMode.CORRECTNESS;
import static ch.trick17.simplejpf.test.JpfParallelismTest.VerifyMode.PARALLELISM;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static rolez.lang.Guarded.guardReadOnly;
import static rolez.lang.Guarded.guardReadWrite;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import rolez.lang.SomeClasses.Int;
import rolez.lang.SomeClasses.Ref;

@RunWith(Parameterized.class)
public class ArrayGuardingTest extends TaskBasedJpfTest {
    
    @Parameters(name = "{0}, {1}")
    public static List<?> taskSystems() {
        return asList(new Object[][]{
                {new NewThreadTaskSystem(), CORRECTNESS},
                {new ThreadPoolTaskSystem(), CORRECTNESS},
                {new SingleThreadTaskSystem(), CORRECTNESS},
                {new NewThreadTaskSystem(), PARALLELISM},
                {new ThreadPoolTaskSystem(3), PARALLELISM}
        });
    }
    
    public ArrayGuardingTest(final TaskSystem s, final VerifyMode mode) {
        super(s, mode);
    }
    
    @Test
    public void testShareArray() {
        verifyTask(new RunnableCallable() {
            public void run() {
                final GuardedArray<Int[]> a = new GuardedArray<>(new Int[3]);
                for(int i = 0; i < a.data.length; i++)
                    a.data[i] = new Int(i);
                
                Task<?> task = new Task<>(new RunnableCallable() {
                    public void run() {
                        assertEquals(2, a.data[2].value);
                        a.releaseShared();
                    }
                });
                a.share(task);
                s.start(task);
                
                guardReadWrite(guardReadOnly(a).data[2]).value = 1;
            }
        });
    }
    
    @Test
    public void testPassArray() {
        verifyTask(new RunnableCallable() {
            public void run() {
                final GuardedArray<Int[]> a = new GuardedArray<>(new Int[3]);
                for(int i = 0; i < a.data.length; i++)
                    a.data[i] = new Int(i);
                
                Task<Void> task = new Task<>(new RunnableCallable() {
                    public void run() {
                        a.completePass();
                        for(int i = 0; i < a.data.length; i++)
                            a.data[i].value++;
                        a.releasePassed();
                    }
                });
                a.pass(task);
                s.start(task);
                
                guardReadOnly(a);
                for(int i = 0; i < a.data.length; i++)
                    assertEquals(i + 1, guardReadOnly(a.data[i]).value);
            }
        });
    }
    
    @Test
    public void testSharePrimitiveArray() {
        verifyTask(new RunnableCallable() {
            public void run() {
                final GuardedArray<int[]> a = new GuardedArray<>(new int[]{0});
                
                Task<Void> task = new Task<>(new RunnableCallable() {
                    public void run() {
                        assertEquals(0, a.data[0]);
                        a.releaseShared();
                    }
                });
                a.share(task);
                s.start(task);
                
                guardReadWrite(a).data[0] = 1;
            }
        });
    }
    
    @Test
    public void testPassPrimitiveArray() {
        verifyTask(new RunnableCallable() {
            public void run() {
                final GuardedArray<int[]> a = new GuardedArray<>(new int[]{0, 1, 2});
                
                Task<Void> task = new Task<>(new RunnableCallable() {
                    public void run() {
                        a.completePass();
                        for(int i = 0; i < a.data.length; i++)
                            a.data[i]++;
                        a.releasePassed();
                    }
                });
                a.pass(task);
                s.start(task);
                
                guardReadOnly(a);
                for(int i = 0; i < a.data.length; i++)
                    assertEquals(i + 1, a.data[i]);
            }
        });
    }
    
    @Test
    public void testShareArrayElement() {
        verifyTask(new RunnableCallable() {
            public void run() {
                final Int i = new Int();
                final GuardedArray<Int[]> a = new GuardedArray<>(new Int[]{i});
                
                Task<Void> task1 = new Task<>(new RunnableCallable() {
                    public void run() {
                        assertEquals(0, i.value);
                        i.releaseShared();
                    }
                });
                i.share(task1);
                s.start(task1);
                
                Task<Void> task2 = new Task<>(new RunnableCallable() {
                    public void run() {
                        assertEquals(0, a.data[0].value);
                        a.releaseShared();
                    }
                });
                a.share(task2);
                s.start(task2);
                
                Task<Void> task3 = new Task<>(new RunnableCallable() {
                    public void run() {
                        assertEquals(0, i.value);
                        i.releaseShared();
                    }
                });
                i.share(task3);
                s.start(task3);
                
                guardReadWrite(i).value = 1;
            }
        });
    }
    
    @Test
    public void testPassArrayElement() {
        verifyTask(new RunnableCallable() {
            public void run() {
                final Int i = new Int();
                final GuardedArray<Int[]> a = new GuardedArray<>(new Int[]{i});
                
                Task<Void> task1 = new Task<>(new RunnableCallable() {
                    public void run() {
                        i.completePass();
                        i.value++;
                        i.releasePassed();
                    }
                });
                i.pass(task1);
                s.start(task1);
                
                Task<Void> task2 = new Task<>(new RunnableCallable() {
                    public void run() {
                        a.completePass();
                        a.data[0].value++;
                        a.releasePassed();
                    }
                });
                a.pass(task2);
                s.start(task2);
                
                Task<Void> task3 = new Task<>(new RunnableCallable() {
                    public void run() {
                        i.completePass();
                        i.value++;
                        i.releasePassed();
                    }
                });
                i.pass(task3);
                s.start(task3);
                
                assertEquals(3, guardReadOnly(i).value);
            }
        });
    }
    
    @Test
    public void testPassArrayElementNestedModify() {
        verifyTask(new RunnableCallable() {
            public void run() {
                final Int i = new Int();
                final GuardedArray<Int[]> a = new GuardedArray<>(new Int[]{i});
                
                Task<Void> task1 = new Task<>(new RunnableCallable() {
                    public void run() {
                        a.completePass();
                        a.data[0] = new Int();
                        
                        final Int i2 = a.data[0];
                        i2.value++;
                        Task<Void> task2 = new Task<>(new RunnableCallable() {
                            public void run() {
                                i2.completePass();
                                i2.value++;
                                i2.releasePassed();
                            }
                        });
                        i2.pass(task2);
                        s.start(task2);
                        
                        assertEquals(2, guardReadWrite(i2).value);
                        i2.value++;
                        
                        a.releasePassed();
                    }
                });
                a.pass(task1);
                s.start(task1);
                
                assertEquals(3, guardReadOnly(guardReadOnly(a).data[0]).value);
            }
        });
    }
    
    @Test
    public void testShareSlice() {
        verifyTask(new RunnableCallable() {
            public void run() {
                final GuardedArray<Int[]> a = new GuardedArray<>(new Int[4]);
                for(int i = 0; i < a.data.length; i++)
                    a.data[i] = new Int(i);
                
                final GuardedSlice<Int[]> slice = a.slice(0, 2, 1);
                Task<Void> task = new Task<>(new RunnableCallable() {
                    public void run() {
                        guardReadOnly(slice); // Not necessary, but could happen
                        assertEquals(1, slice.data[1].value);
                        slice.releaseShared();
                    }
                });
                slice.share(task);
                s.start(task);
                
                guardReadWrite(guardReadOnly(a).data[1]).value = 0;
            }
        });
    }
    
    @Test
    public void testPassSlice() {
        verifyTask(new RunnableCallable() {
            public void run() {
                final GuardedArray<Int[]> a = new GuardedArray<>(new Int[10]);
                for(int i = 0; i < a.data.length; i++)
                    a.data[i] = new Int(i);
                
                final GuardedSlice<Int[]> slice = a.slice(0, 5, 1);
                Task<Void> task = new Task<>(new RunnableCallable() {
                    public void run() {
                        slice.completePass();
                        guardReadWrite(slice); // Not necessary, but could happen
                        for(int i = slice.range.begin; i < slice.range.end; i++)
                            slice.data[i].value++;
                        slice.releasePassed();
                    }
                });
                slice.pass(task);
                s.start(task);
                
                guardReadOnly(a);
                for(int i = 0; i < slice.range.end; i++)
                    assertEquals(i + 1, guardReadOnly(a.data[i]).value);
                for(int i = slice.range.end; i < a.data.length; i++)
                    assertEquals(i, guardReadOnly(a.data[i]).value);
            }
        });
    }
    
    @Test
    public void testPassSliceModifyInParent() {
        verifyTask(new RunnableCallable() {
            public void run() {
                final GuardedArray<int[]> a = new GuardedArray<>(new int[4]);
                for(int i = 0; i < a.data.length; i++)
                    a.data[i] = i;
                
                final GuardedSlice<int[]> slice = a.slice(0, 3, 1);
                Task<Void> task = new Task<>(new RunnableCallable() {
                    public void run() {
                        slice.completePass();
                        assertEquals(0, slice.data[0]);
                        slice.releasePassed();
                    }
                });
                slice.pass(task);
                s.start(task);
                
                guardReadWrite(a).data[0] = 1;
            }
        });
    }
    
    @Test
    public void testSharePrimitiveSlice() {
        verifyTask(new RunnableCallable() {
            public void run() {
                final GuardedArray<int[]> a = new GuardedArray<>(new int[]{0, 1});
                
                final GuardedSlice<int[]> slice = a.slice(0, 1, 1);
                Task<Void> task = new Task<>(new RunnableCallable() {
                    public void run() {
                        assertEquals(0, slice.data[0]);
                        slice.releaseShared();
                    }
                });
                slice.share(task);
                s.start(task);
                
                guardReadWrite(a).data[0] = 1;
            }
        });
    }
    
    @Test
    public void testPassPrimitiveSlice() {
        verifyTask(new RunnableCallable() {
            public void run() {
                GuardedArray<int[]> a = new GuardedArray<>(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
                
                final GuardedSlice<int[]> slice = a.slice(0, 5, 1);
                Task<Void> task = new Task<>(new RunnableCallable() {
                    public void run() {
                        slice.completePass();
                        for(int i = slice.range.begin; i < slice.range.end; i++)
                            slice.data[i]++;
                        slice.releasePassed();
                    }
                });
                slice.pass(task);
                s.start(task);
                
                guardReadOnly(a);
                for(int i = 0; i < slice.range.end; i++)
                    assertEquals(i + 1, a.data[i]);
                for(int i = slice.range.end; i < a.data.length; i++)
                    assertEquals(i, a.data[i]);
            }
        });
    }
    
    @Test
    public void testShareSliceModify() {
        verifyTask(new RunnableCallable() {
            public void run() {
                final GuardedArray<Int[]> a = new GuardedArray<>(new Int[4]);
                for(int i = 0; i < a.data.length; i++)
                    a.data[i] = new Int(i);
                
                final GuardedSlice<Int[]> slice = a.slice(0, 2, 1);
                Task<Void> task = new Task<>(new RunnableCallable() {
                    public void run() {
                        assertEquals(1, slice.data[1].value);
                        slice.releaseShared();
                    }
                });
                slice.share(task);
                s.start(task);
                
                guardReadWrite(a).data[1] = new Int(100);
            }
        });
    }
    
    @Test
    public void testSliceSharedArray() {
        verifyTask(new RunnableCallable() {
            public void run() {
                final GuardedArray<Int[]> a = new GuardedArray<>(new Int[4]);
                for(int i = 0; i < a.data.length; i++)
                    a.data[i] = new Int(i);
                
                Task<Void> task = new Task<>(new RunnableCallable() {
                    public void run() {
                        final GuardedSlice<Int[]> slice = a.slice(0, 2, 1);
                        assertEquals(0, slice.data[0].value);
                        a.releaseShared();
                        /* Use slice again to prevent incidental garbage collection */
                        slice.toString();
                    }
                });
                a.share(task);
                s.start(task);
                
                guardReadWrite(a).data[0] = new Int(1);
            }
        });
    }
    
    @Test
    public void testSliceAndShareSharedSlice() {
        verifyTask(new RunnableCallable() {
            public void run() {
                final GuardedArray<Int[]> a = new GuardedArray<>(new Int[4]);
                for(int i = 0; i < a.data.length; i++)
                    a.data[i] = new Int(i);
                
                final GuardedSlice<Int[]> slice1 = a.slice(0, 3, 1);
                Task<Void> task1 = new Task<>(new RunnableCallable() {
                    public void run() {
                        final GuardedSlice<Int[]> slice2 = slice1.slice(0, 2, 1);
                        
                        Task<Void> task2 = new Task<>(new RunnableCallable() {
                            public void run() {
                                assertEquals(0, slice2.data[0].value);
                                slice2.releaseShared();
                            }
                        });
                        slice2.share(task2);
                        s.start(task2);
                        
                        slice1.releaseShared();
                    }
                });
                slice1.share(task1);
                s.start(task1);
                
                guardReadWrite(a).data[0] = new Int(1);
            }
        });
    }
    
    @Test
    public void testSliceModifySharedArray() {
        verifyTask(new RunnableCallable() {
            public void run() {
                final GuardedArray<Int[]> a = new GuardedArray<>(new Int[4]);
                for(int i = 0; i < a.data.length; i++)
                    a.data[i] = new Int(i);
                
                Task<Void> task = new Task<>(new RunnableCallable() {
                    public void run() {
                        assertEquals(1, a.data[1].value);
                        a.releaseShared();
                    }
                });
                a.share(task);
                s.start(task);
                
                GuardedSlice<Int[]> slice = a.slice(0, 2, 1);
                guardReadWrite(slice).data[1] = new Int(0);
            }
        });
    }
    
    @Test
    public void testSliceSubslice() {
        verifyTask(new RunnableCallable() {
            public void run() {
                final GuardedArray<int[]> a = new GuardedArray<>(new int[]{0});
                final GuardedSlice<int[]> slice1 = a.slice(0, 1, 1);
                
                Task<Void> task = new Task<>(new RunnableCallable() {
                    public void run() {
                        slice1.completePass();
                        slice1.data[0]++;
                        slice1.releasePassed();
                    }
                });
                slice1.pass(task);
                s.start(task);
                
                GuardedSlice<int[]> slice2 = a.slice(0, 1, 1);
                assertEquals(1, guardReadOnly(slice2).data[0]);
            }
        });
    }
    
    @Test
    public void testShareOverlappingSliceModify() {
        verifyTask(new RunnableCallable() {
            public void run() {
                final GuardedArray<Int[]> a = new GuardedArray<>(new Int[4]);
                for(int i = 0; i < a.data.length; i++)
                    a.data[i] = new Int(i);
                
                final GuardedSlice<Int[]> slice1 = a.slice(0, 3, 1);
                final GuardedSlice<Int[]> slice2 = a.slice(1, 4, 1);
                Task<Void> task = new Task<>(new RunnableCallable() {
                    public void run() {
                        assertEquals(1, slice1.data[1].value);
                        slice1.releaseShared();
                    }
                });
                slice1.share(task);
                s.start(task);
                
                guardReadWrite(slice2).data[1] = new Int(100);
                
            }
        });
    }
    
    // FIXME: Test guarding of striped slices
    
    @Test
    public void testShareReferencedPrimitiveSlice() {
        verifyTask(new RunnableCallable() {
            public void run() {
                final GuardedArray<int[]> a = new GuardedArray<>(new int[]{0, 1, 2, 3});
                
                final GuardedSlice<int[]> slice = a.slice(0, 2, 1);
                final Ref<GuardedSlice<int[]>> ref = new Ref<>(slice);
                Task<Void> task = new Task<>(new RunnableCallable() {
                    public void run() {
                        final GuardedSlice<int[]> slice2 = ref.o.slice(1, 2, 1);
                        assertEquals(1, slice2.data[1]);
                        ref.releaseShared();
                    }
                });
                ref.share(task);
                s.start(task);
                
                guardReadWrite(a).data[1] = 0;
            }
        });
    }
    
    @Test
    public void testShareOverlappingPrimitiveSlice() {
        verifyTask(new RunnableCallable() {
            public void run() {
                final GuardedArray<int[]> a = new GuardedArray<>(new int[]{0, 1, 2});
                
                final GuardedSlice<int[]> slice1 = a.slice(0, 2, 1);
                final GuardedSlice<int[]> slice2 = a.slice(1, 3, 1);
                Task<Void> task = new Task<>(new RunnableCallable() {
                    public void run() {
                        assertEquals(1, slice1.data[1]);
                        slice1.releaseShared();
                    }
                });
                slice1.share(task);
                s.start(task);
                
                guardReadWrite(slice2).data[1] = 2;
            }
        });
    }
    
    @Test
    public void testShareSubslice() {
        verifyTask(new RunnableCallable() {
            public void run() {
                final GuardedArray<Int[]> a = new GuardedArray<>(new Int[4]);
                for(int i = 0; i < a.data.length; i++)
                    a.data[i] = new Int(i);
                
                GuardedSlice<Int[]> slice = a.slice(0, 3, 1);
                final GuardedSlice<Int[]> subslice = slice.slice(0, 2, 1);
                
                slice = null; // slice is not referenced anymore!
                java.lang.System.gc();
                
                Task<Void> task = new Task<>(new RunnableCallable() {
                    public void run() {
                        assertEquals(0, subslice.data[0].value);
                        subslice.releaseShared();
                    }
                });
                subslice.share(task);
                s.start(task);
                
                guardReadWrite(a).data[0] = new Int(1);
            }
        });
    }
    
    @Test
    public void testPassReferencedPrimitiveSlice() {
        verifyTask(new RunnableCallable() {
            public void run() {
                GuardedArray<int[]> a = new GuardedArray<>(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
                
                final GuardedSlice<int[]> slice = a.slice(0, 5, 1);
                final Ref<GuardedSlice<int[]>> ref = new Ref<>(slice);
                Task<Void> task = new Task<>(new RunnableCallable() {
                    public void run() {
                        ref.completePass();
                        final GuardedSlice<int[]> slice2 = ref.o.slice(0, 5, 1);
                        for(int i = slice2.range.begin; i < slice2.range.end; i++)
                            slice2.data[i]++;
                        ref.releasePassed();
                        slice2.toString();
                    }
                });
                ref.pass(task);
                s.start(task);
                
                guardReadOnly(a);
                for(int i = 0; i < slice.range.end; i++)
                    assertEquals(i + 1, a.data[i]);
                for(int i = slice.range.end; i < a.data.length; i++)
                    assertEquals(i, a.data[i]);
            }
        });
    }
    
    @Test
    public void testShareSliceMultiple() {
        verifyTask(new RunnableCallable() {
            public void run() {
                final Int i = new Int();
                final GuardedArray<Int[]> a = new GuardedArray<>(new Int[]{i});
                final GuardedSlice<Int[]> slice = a.slice(0, 1, 1);
                
                Task<Void> task1 = new Task<>(new RunnableCallable() {
                    public void run() {
                        assertEquals(0, slice.data[0].value);
                        slice.releaseShared();
                    }
                });
                slice.share(task1);
                s.start(task1);
                
                Task<Void> task2 = new Task<>(new RunnableCallable() {
                    public void run() {
                        assertEquals(0, a.data[0].value);
                        a.releaseShared();
                    }
                });
                a.share(task2);
                s.start(task2);
                
                Task<Void> task3 = new Task<>(new RunnableCallable() {
                    public void run() {
                        assertEquals(0, slice.data[0].value);
                        slice.releaseShared();
                    }
                });
                slice.share(task3);
                s.start(task3);
                
                guardReadWrite(i).value = 1;
            }
        });
    }
    
    @Test
    public void testPassSliceMultiple() {
        verifyTask(new RunnableCallable() {
            public void run() {
                final Int i = new Int();
                final GuardedArray<Int[]> a = new GuardedArray<>(new Int[]{i});
                final GuardedSlice<Int[]> slice = a.slice(0, 1, 1);
                
                Task<Void> task1 = new Task<>(new RunnableCallable() {
                    public void run() {
                        slice.completePass();
                        slice.data[0].value++;
                        slice.releasePassed();
                    }
                });
                slice.pass(task1);
                s.start(task1);
                
                Task<Void> task2 = new Task<>(new RunnableCallable() {
                    public void run() {
                        a.completePass();
                        a.data[0].value++;
                        a.releasePassed();
                    }
                });
                a.pass(task2);
                s.start(task2);
                
                Task<Void> task3 = new Task<>(new RunnableCallable() {
                    public void run() {
                        slice.completePass();
                        slice.data[0].value++;
                        slice.releasePassed();
                    }
                });
                slice.pass(task3);
                s.start(task3);
                
                assertEquals(3, guardReadOnly(i).value);
            }
        });
    }
    
    @Test
    public void testPassDifferentSlices() {
        verifyTask(new RunnableCallable() {
            public void run() {
                final GuardedArray<Int[]> a = new GuardedArray<>(new Int[10]);
                for(int i = 0; i < a.data.length; i++)
                    a.data[i] = new Int(i);
                
                final GuardedSlice<Int[]> slice1 = a.slice(0, 5, 1);
                final GuardedSlice<Int[]> slice2 = a.slice(slice1.range.end, a.data.length, 1);
                
                Task<Void> task1 = new Task<>(new RunnableCallable() {
                    public void run() {
                        slice1.completePass();
                        for(int i = slice1.range.begin; i < slice1.range.end; i++)
                            slice1.data[i].value++;
                        region(0);
                        slice1.releasePassed();
                    }
                });
                slice1.pass(task1);
                s.start(task1);
                
                Task<Void> task2 = new Task<>(new RunnableCallable() {
                    public void run() {
                        slice2.completePass();
                        for(int i = slice2.range.begin; i < slice2.range.end; i++)
                            slice2.data[i].value++;
                        region(1);
                        slice2.releasePassed();
                    }
                });
                slice2.pass(task2);
                s.start(task2);
                region(2);
                
                guardReadOnly(a);
                for(int i = 0; i < a.data.length; i++)
                    assertEquals(i + 1, guardReadOnly(a.data[i]).value);
            }
        });
    }
    
    @Test
    public void testPassSubsliceNested() {
        /* IMPROVE: Allow {0, 2} by releasing slices independent of subslices that are still owned
         * by other threads. */
        verifyTask(new int[][]{{1, 2}, {0, 2}}, new RunnableCallable() {
            public void run() {
                final GuardedArray<Int[]> a = new GuardedArray<>(new Int[3]);
                for(int i = 0; i < a.data.length; i++)
                    a.data[i] = new Int(i);
                
                final GuardedSlice<Int[]> slice1 = a.slice(0, 2, 1);
                Task<Void> task1 = new Task<>(new RunnableCallable() {
                    public void run() {
                        slice1.completePass();
                        
                        final GuardedSlice<Int[]> slice2 = slice1.slice(0, 1, 1);
                        Task<Void> task2 = new Task<>(new RunnableCallable() {
                            public void run() {
                                slice2.completePass();
                                slice2.data[0].value++;
                                region(0);
                                slice2.releasePassed();
                            }
                        });
                        slice2.pass(task2);
                        s.start(task2);
                        
                        slice1.data[1].value++;
                        region(1);
                        
                        slice1.releasePassed();
                        region(2);
                    }
                });
                slice1.pass(task1);
                s.start(task1);
                
                a.data[2].value++;
                region(3);
                
                guardReadOnly(a);
                for(int i = 0; i < a.data.length; i++)
                    assertEquals(i + 1, guardReadOnly(a.data[i]).value);
            }
        });
        
    }
    
    @Test
    public void testPassPrimitiveSubsliceNested() {
        /* IMPROVE: Allow {0, 2} by releasing slices independent of subslices that are still owned
         * by other threads. */
        verifyTask(new int[][]{{1, 2}, {0, 2}}, new RunnableCallable() {
            public void run() {
                final GuardedArray<int[]> a = new GuardedArray<>(new int[]{0, 1, 2});
                
                final GuardedSlice<int[]> slice1 = a.slice(0, 2, 1);
                Task<Void> task1 = new Task<>(new RunnableCallable() {
                    public void run() {
                        slice1.completePass();
                        
                        final GuardedSlice<int[]> slice2 = slice1.slice(0, 1, 1);
                        Task<Void> task2 = new Task<>(new RunnableCallable() {
                            public void run() {
                                slice2.completePass();
                                slice2.data[0]++;
                                region(0);
                                slice2.releasePassed();
                            }
                        });
                        slice2.pass(task2);
                        s.start(task2);
                        
                        slice1.data[1]++;
                        region(1);
                        
                        slice1.releasePassed();
                        region(2);
                    }
                });
                slice1.pass(task1);
                s.start(task1);
                
                a.data[2]++;
                region(3);
                
                guardReadOnly(a);
                for(int i = 0; i < a.data.length; i++)
                    assertEquals(i + 1, a.data[i]);
            }
        });
        
    }
    
    @Test
    public void testSlicePureArray() {
        verifyTask(new int[][]{{0, 2}, {1, 2}}, new RunnableCallable() {
            public void run() {
                final GuardedArray<int[]> a = new GuardedArray<>(new int[]{0, 1});
                
                Task<Void> task = new Task<>(new RunnableCallable() {
                    public void run() {
                        a.completePass();
                        a.data[0]++;
                        region(0);
                        a.releasePassed();
                    }
                });
                a.pass(task);
                s.start(task);
                
                GuardedSlice<int[]> slice = a.slice(0, 1, 1); // Slicing never blocks
                region(1);
                
                assertEquals(1, guardReadOnly(slice).data[0]);
                region(2);
            }
        });
    }
    
    @Test
    public void testSliceAndPassInParallel() {
        verifyTask(new RunnableCallable() {
            public void run() {
                final GuardedArray<int[]> a = new GuardedArray<>(new int[]{0});
                
                Task<Void> task1 = new Task<>(new RunnableCallable() {
                    public void run() {
                        a.completePass();
                        
                        Task<Void> task2 = new Task<>(new RunnableCallable() {
                            public void run() {
                                a.completePass();
                                a.releasePassed();
                            }
                        });
                        a.pass(task2);
                        s.start(task2);
                        
                        a.releasePassed();
                    }
                });
                a.pass(task1);
                s.start(task1);
                
                a.slice(0, 1, 1);
            }
        });
    }
    
    @Test
    public void testSliceAndShareInParallel() {
        verifyTask(new RunnableCallable() {
            public void run() {
                final GuardedArray<int[]> a = new GuardedArray<>(new int[]{0});
                
                Task<Void> task1 = new Task<>(new RunnableCallable() {
                    public void run() {
                        Task<Void> task2 = new Task<>(new RunnableCallable() {
                            public void run() {
                                assertEquals(0, a.data[0]);
                                a.releaseShared();
                            }
                        });
                        a.share(task2);
                        s.start(task2);
                        
                        a.releaseShared();
                    }
                });
                a.share(task1);
                s.start(task1);
                
                GuardedSlice<int[]> slice = a.slice(0, 1, 1);
                guardReadWrite(slice).data[0]++;
            }
        });
    }
    
    @Test
    public void testSliceAndShareSubsliceInParallel() {
        verifyTask(new RunnableCallable() {
            public void run() {
                final GuardedArray<int[]> a = new GuardedArray<>(new int[]{0});
                final GuardedSlice<int[]> slice1 = a.slice(0, 1, 1);
                
                Task<Void> task1 = new Task<>(new RunnableCallable() {
                    public void run() {
                        Task<Void> task2 = new Task<>(new RunnableCallable() {
                            public void run() {
                                assertEquals(0, slice1.data[0]);
                                slice1.releaseShared();
                            }
                        });
                        slice1.share(task2);
                        s.start(task2);
                        
                        slice1.releaseShared();
                    }
                });
                slice1.share(task1);
                s.start(task1);
                
                GuardedSlice<int[]> slice2 = a.slice(0, 1, 1);
                guardReadWrite(slice2).data[0]++;
            }
        });
    }
    
    @Test
    public void testSliceInParallel() {
        verifyTask(new RunnableCallable() {
            public void run() {
                final GuardedArray<int[]> a = new GuardedArray<>(new int[]{0, 1, 2});
                
                Task<Void> task1 = new Task<>(new RunnableCallable() {
                    public void run() {
                        a.completePass();
                        
                        final GuardedSlice<int[]> slice = a.slice(1, 3, 1); // Slicing in parallel
                        Task<Void> task2 = new Task<>(new RunnableCallable() {
                            public void run() {
                                slice.completePass();
                                slice.data[1]++;
                                slice.releasePassed();
                            }
                        });
                        slice.pass(task2);
                        s.start(task2);
                        
                        a.releasePassed();
                    }
                });
                a.pass(task1);
                s.start(task1);
                
                GuardedSlice<int[]> slice = a.slice(0, 2, 1); // Slicing in parallel
                assertEquals(2, guardReadOnly(slice).data[1]);
            }
        });
    }
    
    // TODO: Test objects/slices referenced twice in a tree and superslice referenced from subslice
}

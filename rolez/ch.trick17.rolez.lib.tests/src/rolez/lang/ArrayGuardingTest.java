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
        verifyTask(new Runnable() {
            public void run() {
                final GuardedArray<Int[]> a = new GuardedArray<>(new Int[3]);
                for(int i = 0; i < a.data.length; i++)
                    a.data[i] = new Int(i);
                
                Task<?> task = new Task<Void>() {
                    @Override
                    public Void runRolez() {
                        assertEquals(2, a.data[2].value);
                        a.releaseShared();
                        return null;
                    }
                };
                a.share(task);
                s.start(task);
                
                guardReadWrite(guardReadOnly(a).data[2]).value = 1;
            }
        });
    }
    
    @Test
    public void testPassArray() {
        verifyTask(new Runnable() {
            public void run() {
                final GuardedArray<Int[]> a = new GuardedArray<>(new Int[3]);
                for(int i = 0; i < a.data.length; i++)
                    a.data[i] = new Int(i);
                
                Task<?> task = new Task<Void>() {
                    @Override
                    protected Void runRolez() {
                        a.completePass();
                        for(int i = 0; i < a.data.length; i++)
                            a.data[i].value++;
                        a.releasePassed();
                        return null;
                    }
                };
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
        verifyTask(new Runnable() {
            public void run() {
                final GuardedArray<int[]> a = new GuardedArray<>(new int[]{0});
                
                Task<?> task = new Task<Void>() {
                    @Override
                    protected Void runRolez() {
                        assertEquals(0, a.data[0]);
                        a.releaseShared();
                        return null;
                    }
                };
                a.share(task);
                s.start(task);
                
                guardReadWrite(a).data[0] = 1;
            }
        });
    }
    
    @Test
    public void testPassPrimitiveArray() {
        verifyTask(new Runnable() {
            public void run() {
                final GuardedArray<int[]> a = new GuardedArray<>(new int[]{0, 1, 2});
                
                Task<?> task = new Task<Void>() {
                    @Override
                    protected Void runRolez() {
                        a.completePass();
                        for(int i = 0; i < a.data.length; i++)
                            a.data[i]++;
                        a.releasePassed();
                        return null;
                    }
                };
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
        verifyTask(new Runnable() {
            public void run() {
                final Int i = new Int();
                final GuardedArray<Int[]> a = new GuardedArray<>(new Int[]{i});
                
                Task<?> task1 = new Task<Void>() {
                    @Override
                    protected Void runRolez() {
                        assertEquals(0, i.value);
                        i.releaseShared();
                        return null;
                    }
                };
                i.share(task1);
                s.start(task1);
                
                Task<?> task2 = new Task<Void>() {
                    @Override
                    protected Void runRolez() {
                        assertEquals(0, a.data[0].value);
                        a.releaseShared();
                        return null;
                    }
                };
                a.share(task2);
                s.start(task2);
                
                Task<?> task3 = new Task<Void>() {
                    @Override
                    protected Void runRolez() {
                        assertEquals(0, i.value);
                        i.releaseShared();
                        return null;
                    }
                };
                i.share(task3);
                s.start(task3);
                
                guardReadWrite(i).value = 1;
            }
        });
    }
    
    @Test
    public void testPassArrayElement() {
        verifyTask(new Runnable() {
            public void run() {
                final Int i = new Int();
                final GuardedArray<Int[]> a = new GuardedArray<>(new Int[]{i});
                
                Task<?> task1 = new Task<Void>() {
                    @Override
                    protected Void runRolez() {
                        i.completePass();
                        i.value++;
                        i.releasePassed();
                        return null;
                    }
                };
                i.pass(task1);
                s.start(task1);
                
                Task<?> task2 = new Task<Void>() {
                    @Override
                    protected Void runRolez() {
                        a.completePass();
                        a.data[0].value++;
                        a.releasePassed();
                        return null;
                    }
                };
                a.pass(task2);
                s.start(task2);
                
                Task<?> task3 = new Task<Void>() {
                    @Override
                    protected Void runRolez() {
                        i.completePass();
                        i.value++;
                        i.releasePassed();
                        return null;
                    }
                };
                i.pass(task3);
                s.start(task3);
                
                assertEquals(3, guardReadOnly(i).value);
            }
        });
    }
    
    @Test
    public void testPassArrayElementNestedModify() {
        verifyTask(new Runnable() {
            public void run() {
                final Int i = new Int();
                final GuardedArray<Int[]> a = new GuardedArray<>(new Int[]{i});
                
                Task<?> task1 = new Task<Void>() {
                    @Override
                    protected Void runRolez() {
                        a.completePass();
                        a.data[0] = new Int();
                        
                        final Int i2 = a.data[0];
                        i2.value++;
                        Task<?> task2 = new Task<Void>() {
                            @Override
                            protected Void runRolez() {
                                i2.completePass();
                                i2.value++;
                                i2.releasePassed();
                                return null;
                            }
                        };
                        i2.pass(task2);
                        s.start(task2);
                        
                        assertEquals(2, guardReadWrite(i2).value);
                        i2.value++;
                        
                        a.releasePassed();
                        return null;
                    }
                };
                a.pass(task1);
                s.start(task1);
                
                assertEquals(3, guardReadOnly(guardReadOnly(a).data[0]).value);
            }
        });
    }
    
    @Test
    public void testShareSlice() {
        verifyTask(new Runnable() {
            public void run() {
                final GuardedArray<Int[]> a = new GuardedArray<>(new Int[4]);
                for(int i = 0; i < a.data.length; i++)
                    a.data[i] = new Int(i);
                
                final GuardedSlice<Int[]> slice = a.slice(0, 2, 1);
                Task<?> task = new Task<Void>() {
                    @Override
                    protected Void runRolez() {
                        guardReadOnly(slice); // Not necessary, but could happen
                        assertEquals(1, slice.data[1].value);
                        slice.releaseShared();
                        return null;
                    }
                };
                slice.share(task);
                s.start(task);
                
                guardReadWrite(guardReadOnly(a).data[1]).value = 0;
            }
        });
    }
    
    @Test
    public void testPassSlice() {
        verifyTask(new Runnable() {
            public void run() {
                final GuardedArray<Int[]> a = new GuardedArray<>(new Int[10]);
                for(int i = 0; i < a.data.length; i++)
                    a.data[i] = new Int(i);
                
                final GuardedSlice<Int[]> slice = a.slice(0, 5, 1);
                Task<?> task = new Task<Void>() {
                    @Override
                    protected Void runRolez() {
                        slice.completePass();
                        guardReadWrite(slice); // Not necessary, but could happen
                        for(int i = slice.range.begin; i < slice.range.end; i++)
                            slice.data[i].value++;
                        slice.releasePassed();
                        return null;
                    }
                };
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
        verifyTask(new Runnable() {
            public void run() {
                final GuardedArray<int[]> a = new GuardedArray<>(new int[4]);
                for(int i = 0; i < a.data.length; i++)
                    a.data[i] = i;
                
                final GuardedSlice<int[]> slice = a.slice(0, 3, 1);
                Task<?> task = new Task<Void>() {
                    @Override
                    protected Void runRolez() {
                        slice.completePass();
                        assertEquals(0, slice.data[0]);
                        slice.releasePassed();
                        return null;
                    }
                };
                slice.pass(task);
                s.start(task);
                
                guardReadWrite(a).data[0] = 1;
            }
        });
    }
    
    @Test
    public void testSharePrimitiveSlice() {
        verifyTask(new Runnable() {
            public void run() {
                final GuardedArray<int[]> a = new GuardedArray<>(new int[]{0, 1});
                
                final GuardedSlice<int[]> slice = a.slice(0, 1, 1);
                Task<?> task = new Task<Void>() {
                    @Override
                    protected Void runRolez() {
                        assertEquals(0, slice.data[0]);
                        slice.releaseShared();
                        return null;
                    }
                };
                slice.share(task);
                s.start(task);
                
                guardReadWrite(a).data[0] = 1;
            }
        });
    }
    
    @Test
    public void testPassPrimitiveSlice() {
        verifyTask(new Runnable() {
            public void run() {
                GuardedArray<int[]> a = new GuardedArray<>(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
                
                final GuardedSlice<int[]> slice = a.slice(0, 5, 1);
                Task<?> task = new Task<Void>() {
                    @Override
                    protected Void runRolez() {
                        slice.completePass();
                        for(int i = slice.range.begin; i < slice.range.end; i++)
                            slice.data[i]++;
                        slice.releasePassed();
                        return null;
                    }
                };
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
        verifyTask(new Runnable() {
            public void run() {
                final GuardedArray<Int[]> a = new GuardedArray<>(new Int[4]);
                for(int i = 0; i < a.data.length; i++)
                    a.data[i] = new Int(i);
                
                final GuardedSlice<Int[]> slice = a.slice(0, 2, 1);
                Task<?> task = new Task<Void>() {
                    @Override
                    protected Void runRolez() {
                        assertEquals(1, slice.data[1].value);
                        slice.releaseShared();
                        return null;
                    }
                };
                slice.share(task);
                s.start(task);
                
                guardReadWrite(a).data[1] = new Int(100);
            }
        });
    }
    
    @Test
    public void testSliceSharedArray() {
        verifyTask(new Runnable() {
            public void run() {
                final GuardedArray<Int[]> a = new GuardedArray<>(new Int[4]);
                for(int i = 0; i < a.data.length; i++)
                    a.data[i] = new Int(i);
                
                Task<?> task = new Task<Void>() {
                    @Override
                    protected Void runRolez() {
                        final GuardedSlice<Int[]> slice = a.slice(0, 2, 1);
                        assertEquals(0, slice.data[0].value);
                        a.releaseShared();
                        /* Use slice again to prevent incidental garbage collection */
                        slice.toString();
                        return null;
                    }
                };
                a.share(task);
                s.start(task);
                
                guardReadWrite(a).data[0] = new Int(1);
            }
        });
    }
    
    @Test
    public void testSliceAndShareSharedSlice() {
        verifyTask(new Runnable() {
            public void run() {
                final GuardedArray<Int[]> a = new GuardedArray<>(new Int[4]);
                for(int i = 0; i < a.data.length; i++)
                    a.data[i] = new Int(i);
                
                final GuardedSlice<Int[]> slice1 = a.slice(0, 3, 1);
                Task<?> task1 = new Task<Void>() {
                    @Override
                    protected Void runRolez() {
                        final GuardedSlice<Int[]> slice2 = slice1.slice(0, 2, 1);
                        
                        Task<?> task2 = new Task<Void>() {
                            @Override
                            protected Void runRolez() {
                                assertEquals(0, slice2.data[0].value);
                                slice2.releaseShared();
                                return null;
                            }
                        };
                        slice2.share(task2);
                        s.start(task2);
                        
                        slice1.releaseShared();
                        return null;
                    }
                };
                slice1.share(task1);
                s.start(task1);
                
                guardReadWrite(a).data[0] = new Int(1);
            }
        });
    }
    
    @Test
    public void testSliceModifySharedArray() {
        verifyTask(new Runnable() {
            public void run() {
                final GuardedArray<Int[]> a = new GuardedArray<>(new Int[4]);
                for(int i = 0; i < a.data.length; i++)
                    a.data[i] = new Int(i);
                
                Task<?> task = new Task<Void>() {
                    @Override
                    protected Void runRolez() {
                        assertEquals(1, a.data[1].value);
                        a.releaseShared();
                        return null;
                    }
                };
                a.share(task);
                s.start(task);
                
                GuardedSlice<Int[]> slice = a.slice(0, 2, 1);
                guardReadWrite(slice).data[1] = new Int(0);
            }
        });
    }
    
    @Test
    public void testSliceSubslice() {
        verifyTask(new Runnable() {
            public void run() {
                final GuardedArray<int[]> a = new GuardedArray<>(new int[]{0});
                final GuardedSlice<int[]> slice1 = a.slice(0, 1, 1);
                
                Task<?> task = new Task<Void>() {
                    @Override
                    protected Void runRolez() {
                        slice1.completePass();
                        slice1.data[0]++;
                        slice1.releasePassed();
                        return null;
                    }
                };
                slice1.pass(task);
                s.start(task);
                
                GuardedSlice<int[]> slice2 = a.slice(0, 1, 1);
                assertEquals(1, guardReadOnly(slice2).data[0]);
            }
        });
    }
    
    @Test
    public void testShareOverlappingSliceModify() {
        verifyTask(new Runnable() {
            public void run() {
                final GuardedArray<Int[]> a = new GuardedArray<>(new Int[4]);
                for(int i = 0; i < a.data.length; i++)
                    a.data[i] = new Int(i);
                
                final GuardedSlice<Int[]> slice1 = a.slice(0, 3, 1);
                final GuardedSlice<Int[]> slice2 = a.slice(1, 4, 1);
                Task<?> task = new Task<Void>() {
                    @Override
                    protected Void runRolez() {
                        assertEquals(1, slice1.data[1].value);
                        slice1.releaseShared();
                        return null;
                    }
                };
                slice1.share(task);
                s.start(task);
                
                guardReadWrite(slice2).data[1] = new Int(100);
                
            }
        });
    }
    
    // FIXME: Test guarding of striped slices
    
    @Test
    public void testShareReferencedPrimitiveSlice() {
        verifyTask(new Runnable() {
            public void run() {
                final GuardedArray<int[]> a = new GuardedArray<>(new int[]{0, 1, 2, 3});
                
                final GuardedSlice<int[]> slice = a.slice(0, 2, 1);
                final Ref<GuardedSlice<int[]>> ref = new Ref<>(slice);
                Task<?> task = new Task<Void>() {
                    @Override
                    protected Void runRolez() {
                        final GuardedSlice<int[]> slice2 = ref.o.slice(1, 2, 1);
                        assertEquals(1, slice2.data[1]);
                        ref.releaseShared();
                        return null;
                    }
                };
                ref.share(task);
                s.start(task);
                
                guardReadWrite(a).data[1] = 0;
            }
        });
    }
    
    @Test
    public void testShareOverlappingPrimitiveSlice() {
        verifyTask(new Runnable() {
            public void run() {
                final GuardedArray<int[]> a = new GuardedArray<>(new int[]{0, 1, 2});
                
                final GuardedSlice<int[]> slice1 = a.slice(0, 2, 1);
                final GuardedSlice<int[]> slice2 = a.slice(1, 3, 1);
                Task<?> task = new Task<Void>() {
                    @Override
                    protected Void runRolez() {
                        assertEquals(1, slice1.data[1]);
                        slice1.releaseShared();
                        return null;
                    }
                };
                slice1.share(task);
                s.start(task);
                
                guardReadWrite(slice2).data[1] = 2;
            }
        });
    }
    
    @Test
    public void testShareSubslice() {
        verifyTask(new Runnable() {
            public void run() {
                final GuardedArray<Int[]> a = new GuardedArray<>(new Int[4]);
                for(int i = 0; i < a.data.length; i++)
                    a.data[i] = new Int(i);
                
                GuardedSlice<Int[]> slice = a.slice(0, 3, 1);
                final GuardedSlice<Int[]> subslice = slice.slice(0, 2, 1);
                
                slice = null; // slice is not referenced anymore!
                java.lang.System.gc();
                
                Task<?> task = new Task<Void>() {
                    @Override
                    protected Void runRolez() {
                        assertEquals(0, subslice.data[0].value);
                        subslice.releaseShared();
                        return null;
                    }
                };
                subslice.share(task);
                s.start(task);
                
                guardReadWrite(a).data[0] = new Int(1);
            }
        });
    }
    
    @Test
    public void testPassReferencedPrimitiveSlice() {
        verifyTask(new Runnable() {
            public void run() {
                GuardedArray<int[]> a = new GuardedArray<>(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
                
                final GuardedSlice<int[]> slice = a.slice(0, 5, 1);
                final Ref<GuardedSlice<int[]>> ref = new Ref<>(slice);
                Task<?> task = new Task<Void>() {
                    @Override
                    protected Void runRolez() {
                        ref.completePass();
                        final GuardedSlice<int[]> slice2 = ref.o.slice(0, 5, 1);
                        for(int i = slice2.range.begin; i < slice2.range.end; i++)
                            slice2.data[i]++;
                        ref.releasePassed();
                        slice2.toString();
                        return null;
                    }
                };
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
        verifyTask(new Runnable() {
            public void run() {
                final Int i = new Int();
                final GuardedArray<Int[]> a = new GuardedArray<>(new Int[]{i});
                final GuardedSlice<Int[]> slice = a.slice(0, 1, 1);
                
                Task<?> task1 = new Task<Void>() {
                    @Override
                    protected Void runRolez() {
                        assertEquals(0, slice.data[0].value);
                        slice.releaseShared();
                        return null;
                    }
                };
                slice.share(task1);
                s.start(task1);
                
                Task<?> task2 = new Task<Void>() {
                    @Override
                    protected Void runRolez() {
                        assertEquals(0, a.data[0].value);
                        a.releaseShared();
                        return null;
                    }
                };
                a.share(task2);
                s.start(task2);
                
                Task<?> task3 = new Task<Void>() {
                    @Override
                    protected Void runRolez() {
                        assertEquals(0, slice.data[0].value);
                        slice.releaseShared();
                        return null;
                    }
                };
                slice.share(task3);
                s.start(task3);
                
                guardReadWrite(i).value = 1;
            }
        });
    }
    
    @Test
    public void testPassSliceMultiple() {
        verifyTask(new Runnable() {
            public void run() {
                final Int i = new Int();
                final GuardedArray<Int[]> a = new GuardedArray<>(new Int[]{i});
                final GuardedSlice<Int[]> slice = a.slice(0, 1, 1);
                
                Task<?> task1 = new Task<Void>() {
                    @Override
                    protected Void runRolez() {
                        slice.completePass();
                        slice.data[0].value++;
                        slice.releasePassed();
                        return null;
                    }
                };
                slice.pass(task1);
                s.start(task1);
                
                Task<?> task2 = new Task<Void>() {
                    @Override
                    protected Void runRolez() {
                        a.completePass();
                        a.data[0].value++;
                        a.releasePassed();
                        return null;
                    }
                };
                a.pass(task2);
                s.start(task2);
                
                Task<?> task3 = new Task<Void>() {
                    @Override
                    protected Void runRolez() {
                        slice.completePass();
                        slice.data[0].value++;
                        slice.releasePassed();
                        return null;
                    }
                };
                slice.pass(task3);
                s.start(task3);
                
                assertEquals(3, guardReadOnly(i).value);
            }
        });
    }
    
    @Test
    public void testPassDifferentSlices() {
        verifyTask(new Runnable() {
            public void run() {
                final GuardedArray<Int[]> a = new GuardedArray<>(new Int[10]);
                for(int i = 0; i < a.data.length; i++)
                    a.data[i] = new Int(i);
                
                final GuardedSlice<Int[]> slice1 = a.slice(0, 5, 1);
                final GuardedSlice<Int[]> slice2 = a.slice(slice1.range.end, a.data.length, 1);
                
                Task<?> task1 = new Task<Void>() {
                    @Override
                    protected Void runRolez() {
                        slice1.completePass();
                        for(int i = slice1.range.begin; i < slice1.range.end; i++)
                            slice1.data[i].value++;
                        region(0);
                        slice1.releasePassed();
                        return null;
                    }
                };
                slice1.pass(task1);
                s.start(task1);
                
                Task<?> task2 = new Task<Void>() {
                    @Override
                    protected Void runRolez() {
                        slice2.completePass();
                        for(int i = slice2.range.begin; i < slice2.range.end; i++)
                            slice2.data[i].value++;
                        region(1);
                        slice2.releasePassed();
                        return null;
                    }
                };
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
        verifyTask(new int[][]{{1, 2}, {0, 2}}, new Runnable() {
            public void run() {
                final GuardedArray<Int[]> a = new GuardedArray<>(new Int[3]);
                for(int i = 0; i < a.data.length; i++)
                    a.data[i] = new Int(i);
                
                final GuardedSlice<Int[]> slice1 = a.slice(0, 2, 1);
                Task<?> task1 = new Task<Void>() {
                    @Override
                    protected Void runRolez() {
                        slice1.completePass();
                        
                        final GuardedSlice<Int[]> slice2 = slice1.slice(0, 1, 1);
                        Task<?> task2 = new Task<Void>() {
                            @Override
                            protected Void runRolez() {
                                slice2.completePass();
                                slice2.data[0].value++;
                                region(0);
                                slice2.releasePassed();
                                return null;
                            }
                        };
                        slice2.pass(task2);
                        s.start(task2);
                        
                        slice1.data[1].value++;
                        region(1);
                        
                        slice1.releasePassed();
                        region(2);
                        return null;
                    }
                };
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
        verifyTask(new int[][]{{1, 2}, {0, 2}}, new Runnable() {
            public void run() {
                final GuardedArray<int[]> a = new GuardedArray<>(new int[]{0, 1, 2});
                
                final GuardedSlice<int[]> slice1 = a.slice(0, 2, 1);
                Task<?> task1 = new Task<Void>() {
                    @Override
                    protected Void runRolez() {
                        slice1.completePass();
                        
                        final GuardedSlice<int[]> slice2 = slice1.slice(0, 1, 1);
                        Task<?> task2 = new Task<Void>() {
                            @Override
                            protected Void runRolez() {
                                slice2.completePass();
                                slice2.data[0]++;
                                region(0);
                                slice2.releasePassed();
                                return null;
                            }
                        };
                        slice2.pass(task2);
                        s.start(task2);
                        
                        slice1.data[1]++;
                        region(1);
                        
                        slice1.releasePassed();
                        region(2);
                        return null;
                    }
                };
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
        verifyTask(new int[][]{{0, 2}, {1, 2}}, new Runnable() {
            public void run() {
                final GuardedArray<int[]> a = new GuardedArray<>(new int[]{0, 1});
                
                Task<?> task = new Task<Void>() {
                    @Override
                    protected Void runRolez() {
                        a.completePass();
                        a.data[0]++;
                        region(0);
                        a.releasePassed();
                        return null;
                    }
                };
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
        verifyTask(new Runnable() {
            public void run() {
                final GuardedArray<int[]> a = new GuardedArray<>(new int[]{0});
                
                Task<?> task1 = new Task<Void>() {
                    @Override
                    protected Void runRolez() {
                        a.completePass();
                        
                        Task<?> task2 = new Task<Void>() {
                            @Override
                            protected Void runRolez() {
                                a.completePass();
                                a.releasePassed();
                                return null;
                            }
                        };
                        a.pass(task2);
                        s.start(task2);
                        
                        a.releasePassed();
                        return null;
                    }
                };
                a.pass(task1);
                s.start(task1);
                
                a.slice(0, 1, 1);
            }
        });
    }
    
    @Test
    public void testSliceAndShareInParallel() {
        verifyTask(new Runnable() {
            public void run() {
                final GuardedArray<int[]> a = new GuardedArray<>(new int[]{0});
                
                Task<?> task1 = new Task<Void>() {
                    @Override
                    protected Void runRolez() {
                        Task<?> task2 = new Task<Void>() {
                            @Override
                            protected Void runRolez() {
                                assertEquals(0, a.data[0]);
                                a.releaseShared();
                                return null;
                            }
                        };
                        a.share(task2);
                        s.start(task2);
                        
                        a.releaseShared();
                        return null;
                    }
                };
                a.share(task1);
                s.start(task1);
                
                GuardedSlice<int[]> slice = a.slice(0, 1, 1);
                guardReadWrite(slice).data[0]++;
            }
        });
    }
    
    @Test
    public void testSliceAndShareSubsliceInParallel() {
        verifyTask(new Runnable() {
            public void run() {
                final GuardedArray<int[]> a = new GuardedArray<>(new int[]{0});
                final GuardedSlice<int[]> slice1 = a.slice(0, 1, 1);
                
                Task<?> task1 = new Task<Void>() {
                    @Override
                    protected Void runRolez() {
                        Task<?> task2 = new Task<Void>() {
                            @Override
                            protected Void runRolez() {
                                assertEquals(0, slice1.data[0]);
                                slice1.releaseShared();
                                return null;
                            }
                        };
                        slice1.share(task2);
                        s.start(task2);
                        
                        slice1.releaseShared();
                        return null;
                    }
                };
                slice1.share(task1);
                s.start(task1);
                
                GuardedSlice<int[]> slice2 = a.slice(0, 1, 1);
                guardReadWrite(slice2).data[0]++;
            }
        });
    }
    
    @Test
    public void testSliceInParallel() {
        verifyTask(new Runnable() {
            public void run() {
                final GuardedArray<int[]> a = new GuardedArray<>(new int[]{0, 1, 2});
                
                Task<?> task1 = new Task<Void>() {
                    @Override
                    protected Void runRolez() {
                        a.completePass();
                        
                        final GuardedSlice<int[]> slice = a.slice(1, 3, 1); // Slicing in parallel
                        Task<?> task2 = new Task<Void>() {
                            @Override
                            protected Void runRolez() {
                                slice.completePass();
                                slice.data[1]++;
                                slice.releasePassed();
                                return null;
                            }
                        };
                        slice.pass(task2);
                        s.start(task2);
                        
                        a.releasePassed();
                        return null;
                    }
                };
                a.pass(task1);
                s.start(task1);
                
                GuardedSlice<int[]> slice = a.slice(0, 2, 1); // Slicing in parallel
                assertEquals(2, guardReadOnly(slice).data[1]);
            }
        });
    }
    
    // TODO: Test objects/slices referenced twice in a tree and superslice referenced from subslice
}

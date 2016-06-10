package rolez.lang;

import static ch.trick17.simplejpf.test.JpfParallelismTest.VerifyMode.CORRECTNESS;
import static ch.trick17.simplejpf.test.JpfParallelismTest.VerifyMode.PARALLELISM;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static rolez.lang.Guarded.guardReadOnly;
import static rolez.lang.Guarded.guardReadWrite;

import java.util.List;

import org.junit.Ignore;
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
                    
                a.share();
                s.start(new RunnableCallable() {
                    public void run() {
                        assertEquals(2, a.data[2].value);
                        a.releaseShared();
                    }
                });
                
                guardReadWrite(guardReadOnly(a).data[2]).value = 1;
            }
        });
    }
    
    @Test
    public void testSharePrimitiveArray() {
        verifyTask(new RunnableCallable() {
            public void run() {
                final GuardedArray<int[]> a = new GuardedArray<>(new int[]{0});
                
                a.share();
                s.start(new RunnableCallable() {
                    public void run() {
                        assertEquals(0, a.data[0]);
                        a.releaseShared();
                    }
                });
                
                guardReadWrite(a).data[0] = 1;
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
                    
                a.pass();
                s.start(new RunnableCallable() {
                    public void run() {
                        a.registerNewOwner();
                        for(int i = 0; i < a.data.length; i++)
                            a.data[i].value++;
                        a.releasePassed();
                    }
                });
                
                guardReadOnly(a);
                for(int i = 0; i < a.data.length; i++)
                    assertEquals(i + 1, guardReadOnly(a.data[i]).value);
            }
        });
    }
    
    @Test
    public void testPassPrimitiveArray() {
        verifyTask(new RunnableCallable() {
            public void run() {
                final GuardedArray<int[]> a = new GuardedArray<>(new int[]{0, 1, 2});
                
                a.pass();
                s.start(new RunnableCallable() {
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
            }
        });
    }
    
    @Test
    public void testShareArrayElement() {
        verifyTask(new RunnableCallable() {
            public void run() {
                final Int i = new Int();
                final GuardedArray<Int[]> a = new GuardedArray<>(new Int[]{i});
                
                i.share();
                s.start(new RunnableCallable() {
                    public void run() {
                        assertEquals(0, i.value);
                        i.releaseShared();
                    }
                });
                
                a.share();
                s.start(new RunnableCallable() {
                    public void run() {
                        assertEquals(0, a.data[0].value);
                        a.releaseShared();
                    }
                });
                
                i.share();
                s.start(new RunnableCallable() {
                    public void run() {
                        assertEquals(0, i.value);
                        i.releaseShared();
                    }
                });
                
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
                
                i.pass();
                s.start(new RunnableCallable() {
                    public void run() {
                        i.registerNewOwner();
                        i.value++;
                        i.releasePassed();
                    }
                });
                
                a.pass();
                s.start(new RunnableCallable() {
                    public void run() {
                        a.registerNewOwner();
                        a.data[0].value++;
                        a.releasePassed();
                    }
                });
                
                i.pass();
                s.start(new RunnableCallable() {
                    public void run() {
                        i.registerNewOwner();
                        i.value++;
                        i.releasePassed();
                    }
                });
                
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
                
                a.pass();
                s.start(new RunnableCallable() {
                    public void run() {
                        a.registerNewOwner();
                        a.data[0] = new Int();
                        
                        final Int i2 = a.data[0];
                        i2.value++;
                        i2.pass();
                        s.start(new RunnableCallable() {
                            public void run() {
                                i2.registerNewOwner();
                                i2.value++;
                                i2.releasePassed();
                            }
                        });
                        
                        assertEquals(2, guardReadWrite(i2).value);
                        i2.value++;
                        
                        a.releasePassed();
                    }
                });
                
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
                slice.share();
                s.start(new RunnableCallable() {
                    public void run() {
                        guardReadOnly(slice); // Not necessary, but could happen
                        assertEquals(1, slice.data[1].value);
                        slice.releaseShared();
                    }
                });
                
                guardReadWrite(guardReadOnly(a).data[1]).value = 0;
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
                slice.share();
                s.start(new RunnableCallable() {
                    public void run() {
                        assertEquals(1, slice.data[1].value);
                        slice.releaseShared();
                    }
                });
                
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
                    
                a.share();
                s.start(new RunnableCallable() {
                    public void run() {
                        final GuardedSlice<Int[]> slice = a.slice(0, 2, 1);
                        a.releaseShared();
                        /* Use slice again to prevent incidental correct behavior due to garbage
                         * collection */
                        slice.toString();
                    }
                });
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
                    
                a.share();
                s.start(new RunnableCallable() {
                    public void run() {
                        assertEquals(1, a.data[1].value);
                        a.releaseShared();
                    }
                });
                
                final GuardedSlice<Int[]> slice = a.slice(0, 2, 1);
                guardReadWrite(slice).data[1] = new Int(0);
            }
        });
    }
    
    @Test
    @Ignore // FIXME
    public void testSliceSubslice() {
        verifyTask(new RunnableCallable() {
            public void run() {
                final GuardedArray<int[]> a = new GuardedArray<>(new int[]{0});
                final GuardedSlice<int[]> slice1 = a.slice(0, 1, 1);
                
                slice1.pass();
                s.start(new RunnableCallable() {
                    public void run() {
                        slice1.registerNewOwner();
                        slice1.data[0]++;
                        slice1.releasePassed();
                    }
                });
                
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
                slice1.share();
                s.start(new RunnableCallable() {
                    public void run() {
                        assertEquals(1, slice1.data[1].value);
                        slice1.releaseShared();
                    }
                });
                
                guardReadWrite(slice2).data[1] = new Int(100);
                
            }
        });
    }
    
    // FIXME: Test guarding of striped slices
    
    @Test
    public void testSharePrimitiveSlice() {
        verifyTask(new RunnableCallable() {
            public void run() {
                final GuardedArray<int[]> a = new GuardedArray<>(new int[]{0, 1});
                
                final GuardedSlice<int[]> slice = a.slice(0, 1, 1);
                slice.share();
                s.start(new RunnableCallable() {
                    public void run() {
                        assertEquals(0, slice.data[0]);
                        slice.releaseShared();
                    }
                });
                
                guardReadWrite(a).data[0] = 1;
            }
        });
    }
    
    @Test
    public void testShareReferencedPrimitiveSlice() {
        verifyTask(new RunnableCallable() {
            public void run() {
                final GuardedArray<int[]> a = new GuardedArray<>(new int[]{0, 1, 2, 3});
                
                final GuardedSlice<int[]> slice = a.slice(0, 2, 1);
                final Ref<GuardedSlice<int[]>> ref = new Ref<>(slice);
                ref.share();
                s.start(new RunnableCallable() {
                    public void run() {
                        final GuardedSlice<int[]> slice2 = ref.o.slice(1, 2, 1);
                        assertEquals(1, slice2.data[1]);
                        ref.releaseShared();
                    }
                });
                
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
                slice1.share();
                s.start(new RunnableCallable() {
                    public void run() {
                        assertEquals(1, slice1.data[1]);
                        slice1.releaseShared();
                    }
                });
                
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
                
                subslice.share();
                s.start(new RunnableCallable() {
                    public void run() {
                        assertEquals(0, subslice.data[0].value);
                        subslice.releaseShared();
                    }
                });
                
                guardReadWrite(a).data[0] = new Int(1);
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
                slice.pass();
                s.start(new RunnableCallable() {
                    public void run() {
                        slice.registerNewOwner();
                        guardReadWrite(slice); // Not necessary, but could happen
                        for(int i = slice.range.begin; i < slice.range.end; i++)
                            slice.data[i].value++;
                        slice.releasePassed();
                    }
                });
                
                guardReadOnly(a);
                for(int i = 0; i < slice.range.end; i++)
                    assertEquals(i + 1, guardReadOnly(a.data[i]).value);
                for(int i = slice.range.end; i < a.data.length; i++)
                    assertEquals(i, guardReadOnly(a.data[i]).value);
            }
        });
    }
    
    @Test
    public void testPassPrimitiveSlice() {
        verifyTask(new RunnableCallable() {
            public void run() {
                GuardedArray<int[]> a = new GuardedArray<>(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
                
                final GuardedSlice<int[]> slice = a.slice(0, 5, 1);
                slice.pass();
                s.start(new RunnableCallable() {
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
                ref.pass();
                s.start(new RunnableCallable() {
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
                
                slice.share();
                s.start(new RunnableCallable() {
                    public void run() {
                        assertEquals(0, slice.data[0].value);
                        slice.releaseShared();
                    }
                });
                
                a.share();
                s.start(new RunnableCallable() {
                    public void run() {
                        assertEquals(0, a.data[0].value);
                        a.releaseShared();
                    }
                });
                
                slice.share();
                s.start(new RunnableCallable() {
                    public void run() {
                        assertEquals(0, slice.data[0].value);
                        slice.releaseShared();
                    }
                });
                
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
                
                slice.pass();
                s.start(new RunnableCallable() {
                    public void run() {
                        slice.registerNewOwner();
                        slice.data[0].value++;
                        slice.releasePassed();
                    }
                });
                
                a.pass();
                s.start(new RunnableCallable() {
                    public void run() {
                        a.registerNewOwner();
                        a.data[0].value++;
                        a.releasePassed();
                    }
                });
                
                slice.pass();
                s.start(new RunnableCallable() {
                    public void run() {
                        slice.registerNewOwner();
                        slice.data[0].value++;
                        slice.releasePassed();
                    }
                });
                
                assertEquals(3, guardReadOnly(i).value);
            }
        });
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
            s.start(new RunnableCallable() {
                public void run() {
                    slice1.registerNewOwner();
                    for(int i = slice1.range.begin; i < slice1.range.end; i++)
                        slice1.data[i].value++;
                    region(0);
                    slice1.releasePassed();
                }
            });
            
            slice2.pass();
            s.start(new RunnableCallable() {
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
            for(int i = 0; i < a.data.length; i++)
                assertEquals(i + 1, guardReadOnly(a.data[i]).value);
        }
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
                slice1.pass();
                s.start(new RunnableCallable() {
                    public void run() {
                        slice1.registerNewOwner();
                        
                        final GuardedSlice<Int[]> slice2 = slice1.slice(0, 1, 1);
                        slice2.pass();
                        s.start(new RunnableCallable() {
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
                    }
                });
                
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
                slice1.pass();
                s.start(new RunnableCallable() {
                    public void run() {
                        slice1.registerNewOwner();
                        
                        final GuardedSlice<int[]> slice2 = slice1.slice(0, 1, 1);
                        slice2.pass();
                        s.start(new RunnableCallable() {
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
                    }
                });
                
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
                
                a.pass();
                s.start(new RunnableCallable() {
                    public void run() {
                        a.registerNewOwner();
                        a.data[0]++;
                        region(0);
                        a.releasePassed();
                    }
                });
                
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
                
                a.pass();
                s.start(new RunnableCallable() {
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
            }
        });
    }
    
    @Test
    public void testSliceAndShareInParallel() {
        verifyTask(new RunnableCallable() {
            public void run() {
                final GuardedArray<int[]> a = new GuardedArray<>(new int[]{0});
                
                a.share();
                s.start(new RunnableCallable() {
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
                guardReadWrite(slice).data[0]++;
            }
        });
    }
    
    @Test
    @Ignore // FIXME
    public void testSliceAndShareSubsliceInParallel() {
        verifyTask(new RunnableCallable() {
            public void run() {
                final GuardedArray<int[]> a = new GuardedArray<>(new int[]{0});
                final GuardedSlice<int[]> slice1 = a.slice(0, 1, 1);
                
                slice1.share();
                s.start(new RunnableCallable() {
                    public void run() {
                        slice1.share();
                        s.start(new RunnableCallable() {
                            public void run() {
                                assertEquals(0, slice1.data[0]);
                                slice1.releaseShared();
                            }
                        });
                        
                        slice1.releaseShared();
                    }
                });
                
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
                
                a.pass();
                s.start(new RunnableCallable() {
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
                assertEquals(2, guardReadOnly(slice).data[1]);
            }
        });
    }
    
    // TODO: Test objects/slices referenced twice in a tree and superslice referenced from subslice
}

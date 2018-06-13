package rolez.lang;

import static ch.trick17.simplejpf.test.JpfParallelismTest.VerifyMode.CORRECTNESS;
import static ch.trick17.simplejpf.test.JpfParallelismTest.VerifyMode.PARALLELISM;
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
import rolez.lang.SomeClasses.Node;
import rolez.lang.SomeClasses.Ref;

@RunWith(Parameterized.class)
public class ObjectGuardingTest extends TaskBasedJpfTest {
    
    @Parameters(name = "{0}, {1}")
    public static List<?> taskSystems() {
        return Arrays.asList(new Object[][]{
                {new NewThreadTaskSystem(), CORRECTNESS},
                {new SingleThreadTaskSystem(), CORRECTNESS},
                {new NewThreadTaskSystem(), PARALLELISM}
        });
    }
    
    public ObjectGuardingTest(TaskSystem s, VerifyMode mode) {
        super(s, mode);
    }
    
    @Test
    public void testShare() {
        verifyTask(new int[][]{{2, 3}, {0, 3}}, new Runnable() {
            public void run() {
                final Int i = new Int();
                
                Task<?> task = new Task<Void>(new Object[]{}, new Object[]{i}) {
                    @Override
                    protected Void runRolez() {
                        region(0);
                        assertEquals(0, i.value);
                        return null;
                    }
                };
                s.start(task);
                region(2);
                
                guardReadWrite(i).value = 1;
                region(3);
            }
        });
    }
    
    @Test
    public void testShareReadFirst() {
        assumeVerifyCorrectness();
        verifyTask(new Runnable() {
            public void run() {
                final Int i = new Int();
                
                Task<?> task = new Task<Void>(new Object[]{}, new Object[]{i}) {
                    @Override
                    protected Void runRolez() {
                        assertEquals(0, i.value);
                        return null;
                    }
                };
                s.start(task);
                
                int v = guardReadOnly(i).value + 1;
                guardReadWrite(i).value = v;
            }
        });
    }
    
    @Test
    public void testShareMissingGuard() {
        assumeMultithreaded();
        verifyTaskAssertionError(new Runnable() {
            public void run() {
                final Int i = new Int();
                
                Task<?> task = new Task<Void>(new Object[]{}, new Object[]{i}) {
                    @Override
                    protected Void runRolez() {
                        assertEquals(0, i.value);
                        return null;
                    }
                };
                s.start(task);
                
                // A missing guard causes non-determinism
                i.value = 1;
            }
        });
    }
    
    @Test
    public void testShareMultiple() {
        verifyTask(new int[][]{{4, 5}, {0, 5}, {2, 5}}, new Runnable() {
            public void run() {
                final Int i = new Int();
                
                Task<?> task1 = new Task<Void>(new Object[]{}, new Object[]{i}) {
                    @Override
                    protected Void runRolez() {
                        region(0);
                        assertEquals(0, i.value);
                        return null;
                    }
                };
                s.start(task1);
                
                Task<?> task2 = new Task<Void>(new Object[]{}, new Object[]{i}) {
                    @Override
                    protected Void runRolez() {
                        region(2);
                        assertEquals(0, i.value);
                        return null;
                    }
                };
                s.start(task2);
                region(4);
                
                guardReadWrite(i).value = 1;
                region(5);
            }
        });
    }
    
    @Test
    public void testPass() {
        verifyTask(new int[][]{{0, 3}, {2, 3}}, new Runnable() {
            public void run() {
                final Int i = new Int();
                
                Task<?> task = new Task<Void>(new Object[]{i}, new Object[]{}) {
                    @Override
                    protected Void runRolez() {
                        region(0);
                        i.value++;
                        return null;
                    }
                };
                s.start(task);
                region(2);
                
                assertEquals(1, guardReadOnly(i).value);
                region(3);
            }
        });
    }
    
    @Test
    public void testPassMissingGuard() {
        assumeMultithreaded();
        verifyTaskAssertionError(new Runnable() {
            public void run() {
                final Int i = new Int();
                
                Task<?> task = new Task<Void>(new Object[]{i}, new Object[]{}) {
                    @Override
                    protected Void runRolez() {
                        i.value = 1;
                        return null;
                    }
                };
                s.start(task);
                
                // A missing guard causes non-determinism
                assertEquals(1, i.value);
            }
        });
    }
    
    @Test
    public void testPassMultiple() {
        /* IMPROVE: Allow {0, 4} in parallel by passing not-yet-available data to tasks (so far,
         * pass() is blocking) */
        verifyTask(new int[][]{{0, 2, 5}, {4, 5}, {0, 4}}, new Runnable() {
            public void run() {
                final Int i = new Int();
                
                Task<?> task1 = new Task<Void>(new Object[]{i}, new Object[]{}) {
                    @Override
                    protected Void runRolez() {
                        region(0);
                        i.value++;
                        return null;
                    }
                };
                s.start(task1);
                
                Task<?> task2 = new Task<Void>(new Object[]{i}, new Object[]{}) {
                    @Override
                    protected Void runRolez() {
                        region(2);
                        i.value++;
                        return null;
                    }
                };
                s.start(task2);
                region(4);
                
                assertEquals(2, guardReadOnly(i).value);
                
                region(5);
            }
        });
    }
    
    @Test
    public void testPassNested() {
        verifyTask(new int[][]{{4, 5}, {2, 5}, {0, 5}}, new Runnable() {
            public void run() {
                final Int i = new Int();
                
                Task<?> task1 = new Task<Void>(new Object[]{i}, new Object[]{}) {
                    @Override
                    protected Void runRolez() {
                        i.value++;
                        
                        Task<?> task2 = new Task<Void>(new Object[]{i}, new Object[]{}) {
                            @Override
                            protected Void runRolez() {
                                i.value++;
                                region(0);
                                return null;
                            }
                        };
                        s.start(task2);
                        region(2);
                        
                        assertEquals(2, guardReadWrite(i).value);
                        i.value++;
                        return null;
                    }
                };
                s.start(task1);
                region(4);
                
                assertEquals(3, guardReadOnly(i).value);
                
                region(5);
            }
        });
    }
    
    @Test
    public void testPassNestedWithoutGuarding() {
        assumeVerifyCorrectness();
        verifyTask(new Runnable() {
            public void run() {
                final Int i = new Int();
                
                Task<?> task1 = new Task<Void>(new Object[]{i}, new Object[]{}) {
                    @Override
                    protected Void runRolez() {
                        Task<?> task2 = new Task<Void>(new Object[]{i}, new Object[]{}) {
                            @Override
                            protected Void runRolez() {
                                return null;
                            }
                        };
                        s.start(task2);
                        
                        return null;
                    }
                };
                s.start(task1);
            }
        });
    }
    
    @Test
    public void testDoublePass() {
        assumeVerifyCorrectness();
        verifyTask(new Runnable() {
            public void run() {
                final Int i = new Int();
                final Int j = i;
                
                Task<?> task = new Task<Void>(new Object[]{i, j}, new Object[]{}) {
                    @Override
                    protected Void runRolez() {
                        guardReadWrite(i).value = 42;
                        return null;
                    }
                };
                s.start(task);
                
                assertEquals(42, guardReadOnly(i).value);
            }
        });
    }
    
    @Test
    public void testPassShare() {
        verifyTask(new Runnable() {
            public void run() {
                final Int i = new Int();
                
                Task<?> task1 = new Task<Void>(new Object[]{i}, new Object[]{}) {
                    @Override
                    protected Void runRolez() {
                        i.value++;
                        return null;
                    }
                };
                s.start(task1);
                
                Task<?> task2 = new Task<Void>(new Object[]{}, new Object[]{i}) {
                    @Override
                    protected Void runRolez() {
                        assertEquals(1, i.value);
                        region(1);
                        return null;
                    }
                };
                s.start(task2);
                region(2);
                
                guardReadWrite(i).value++;
            }
        });
    }
    
    @Test
    public void testShareGroup() {
        verifyTask(new int[][]{{2, 3}, {0, 3}}, new Runnable() {
            public void run() {
                final Int i = new Int();
                final Ref<Int> r = new Ref<>(i);
                
                Task<?> task = new Task<Void>(new Object[]{}, new Object[]{r}) {
                    @Override
                    protected Void runRolez() {
                        assertEquals(0, r.o.value);
                        region(0);
                        return null;
                    }
                };
                s.start(task);
                region(2);
                
                guardReadWrite(i).value = 1;
                region(3);
            }
        });
    }
    
    @Test
    public void testShareGroupMultiple() {
        assumeVerifyCorrectness();
        verifyTask(new Runnable() {
            public void run() {
                final Int i = new Int();
                final Ref<Int> r = new Ref<>(i);
                
                for(int k = 0; k < 2; k++) {
                    Task<?> task = new Task<Void>(new Object[]{}, new Object[]{r}) {
                        @Override
                        protected Void runRolez() {
                            assertEquals(0, r.o.value);
                            return null;
                        }
                    };
                    s.start(task);
                }
                
                guardReadWrite(i).value = 1;
            }
        });
    }
    
    @Test
    public void testPassGroup() {
        verifyTask(new int[][]{{2, 3}, {0, 3}}, new Runnable() {
            public void run() {
                final Int i = new Int();
                final Ref<Int> r = new Ref<>(i);
                
                Task<?> task = new Task<Void>(new Object[]{r}, new Object[]{}) {
                    @Override
                    protected Void runRolez() {
                        r.o.value++;
                        region(0);
                        return null;
                    }
                };
                s.start(task);
                region(2);
                
                assertEquals(1, guardReadOnly(i).value);
                region(3);
            }
        });
    }
    
    @Test
    public void testPassGroupMultiple() {
        assumeVerifyCorrectness();
        verifyTask(new Runnable() {
            public void run() {
                final Int i = new Int();
                final Ref<Int> r = new Ref<>(i);
                
                for(int k = 0; k < 2; k++) {
                    Task<?> task = new Task<Void>(new Object[]{r}, new Object[]{}) {
                        @Override
                        protected Void runRolez() {
                            r.o.value++;
                            return null;
                        }
                    };
                    s.start(task);
                }
                
                assertEquals(2, guardReadOnly(i).value);
            }
        });
    }
    
    @Test
    public void testPassGroupNested() {
        assumeVerifyCorrectness();
        verifyTask(new Runnable() {
            public void run() {
                final Int i = new Int();
                final Ref<Int> r = new Ref<>(i);
                
                Task<?> task1 = new Task<Void>(new Object[]{r}, new Object[]{}) {
                    @Override
                    protected Void runRolez() {
                        final Int i2 = r.o;
                        i2.value++;
                        
                        Task<?> task2 = new Task<Void>(new Object[]{r}, new Object[]{}) {
                            @Override
                            protected Void runRolez() {
                                r.o.value++;
                                return null;
                            }
                        };
                        s.start(task2);
                        
                        assertEquals(2, guardReadWrite(i2).value);
                        i2.value++;
                        return null;
                    }
                };
                s.start(task1);
                
                assertEquals(3, guardReadOnly(i).value);
            }
        });
    }
    
    @Test
    public void testPassShareGroup() {
        verifyTask(new Runnable() {
            public void run() {
                final Int i = new Int();
                final Ref<Int> r = new Ref<>(i);
                
                Task<?> task1 = new Task<Void>(new Object[]{r}, new Object[]{}) {
                    @Override
                    protected Void runRolez() {
                        r.o.value++;
                        return null;
                    }
                };
                s.start(task1);
                
                Task<?> task2 = new Task<Void>(new Object[]{}, new Object[]{r}) {
                    @Override
                    protected Void runRolez() {
                        assertEquals(1, r.o.value);
                        region(1);
                        return null;
                    }
                };
                s.start(task2);
                region(2);
                
                guardReadWrite(i).value++;
            }
        });
    }
    
    @Test
    public void testShareSubgroupMultiple() {
        verifyTask(new int[][]{{0, 4}, {1, 4}, {2, 4}, {3, 4}}, new Runnable() {
            public void run() {
                final Int i = new Int();
                final Ref<Int> r = new Ref<>(i);
                
                Task<?> task1 = new Task<Void>(new Object[]{}, new Object[]{i}) {
                    @Override
                    protected Void runRolez() {
                        assertEquals(0, i.value);
                        region(0);
                        return null;
                    }
                };
                s.start(task1);
                
                Task<?> task2 = new Task<Void>(new Object[]{}, new Object[]{r}) {
                    @Override
                    protected Void runRolez() {
                        assertEquals(0, r.o.value);
                        region(1);
                        return null;
                    }
                };
                s.start(task2);
                
                Task<?> task3 = new Task<Void>(new Object[]{}, new Object[]{i}) {
                    @Override
                    protected Void runRolez() {
                        assertEquals(0, i.value);
                        region(2);
                        return null;
                    }
                };
                s.start(task3);
                region(3);
                
                guardReadWrite(i).value = 1;
                region(4);
            }
        });
    }
    
    @Test
    public void testPassSubgroup() {
        /* IMPROVE: Allow all regions by passing not-yet-available data */
        verifyTask(new int[][]{{0, 1, 2}, {0, 3}, {1, 3}}, new Runnable() {
            public void run() {
                final Int i = new Int();
                final Ref<Int> r = new Ref<>(i);
                
                Task<?> task1 = new Task<Void>(new Object[]{i}, new Object[]{}) {
                    @Override
                    protected Void runRolez() {
                        region(0);
                        i.value++;
                        return null;
                    }
                };
                s.start(task1);
                
                Task<?> task2 = new Task<Void>(new Object[]{r}, new Object[]{}) {
                    @Override
                    protected Void runRolez() {
                        region(1);
                        r.o.value++;
                        return null;
                    }
                };
                s.start(task2);
                
                Task<?> task3 = new Task<Void>(new Object[]{i}, new Object[]{}) {
                    @Override
                    protected Void runRolez() {
                        region(2);
                        i.value++;
                        return null;
                    }
                };
                s.start(task3);
                region(3);
                
                assertEquals(3, guardReadOnly(i).value);
            }
        });
    }
    
    @Test
    public void testPassSubgroupNested() {
        verifyTask(new Runnable() {
            public void run() {
                final Int i = new Int();
                final Ref<Int> r = new Ref<>(i);
                
                Task<?> task1 = new Task<Void>(new Object[]{r}, new Object[]{}) {
                    @Override
                    protected Void runRolez() {
                        final Int i2 = r.o;
                        i2.value++;
                        
                        Task<?> task2 = new Task<Void>(new Object[]{i2}, new Object[]{}) {
                            @Override
                            protected Void runRolez() {
                                region(0);
                                i2.value++;
                                return null;
                            }
                        };
                        s.start(task2);
                        region(2);
                        
                        return null;
                    }
                };
                s.start(task1);
                region(4);
                
                assertEquals(2, guardReadOnly(i).value);
            }
        });
    }
    
    @Test
    public void testPassShareSubgroup() {
        /* IMPROVE: Allow {0, 2, 3} by sharing not-yet-available data? */
        verifyTask(new int[][]{{0, 2}, {0, 3}}, new Runnable() {
            public void run() {
                final Int i = new Int();
                final Ref<Int> r = new Ref<>(i);
                
                Task<?> task1 = new Task<Void>(new Object[]{r}, new Object[]{}) {
                    @Override
                    protected Void runRolez() {
                        r.o.value++;
                        region(0);
                        return null;
                    }
                };
                s.start(task1);
                
                Task<?> task2 = new Task<Void>(new Object[]{}, new Object[]{i}) {
                    @Override
                    protected Void runRolez() {
                        region(2);
                        assertEquals(1, i.value);
                        return null;
                    }
                };
                s.start(task2);
                region(3);
                
                guardReadWrite(i).value++;
            }
        });
    }
    
    @Test
    public void testShareGroupModify() {
        assumeVerifyCorrectness();
        verifyTask(new Runnable() {
            public void run() {
                final Int i = new Int();
                final Ref<Int> r = new Ref<>(i);
                
                Task<?> task = new Task<Void>(new Object[]{}, new Object[]{r}) {
                    @Override
                    protected Void runRolez() {
                        assertEquals(0, i.value);
                        return null;
                    }
                };
                s.start(task);
                
                guardReadWrite(r).o = new Int(10);
            }
        });
    }
    
    @Test
    public void testPassGroupModify() {
        assumeVerifyCorrectness();
        verifyTask(new Runnable() {
            public void run() {
                final Int i = new Int();
                final Ref<Int> r = new Ref<>(i);
                
                Task<?> task1 = new Task<Void>(new Object[]{r}, new Object[]{}) {
                    @Override
                    protected Void runRolez() {
                        r.o = new Int();
                        r.o.value = 10;
                        return null;
                    }
                };
                s.start(task1);
                
                assertEquals(0, guardReadOnly(i).value);
                assertEquals(10, guardReadOnly(guardReadOnly(r).o).value);
            }
        });
    }
    
    @Test
    public void testPassSubgroupNestedModify() {
        assumeVerifyCorrectness();
        verifyTask(new Runnable() {
            public void run() {
                final Int i = new Int();
                final Ref<Int> r = new Ref<>(i);
                
                Task<?> task1 = new Task<Void>(new Object[]{r}, new Object[]{}) {
                    @Override
                    protected Void runRolez() {
                        r.o = new Int();
                        
                        final Int i2 = r.o;
                        i2.value++;
                        Task<?> task2 = new Task<Void>(new Object[]{i2}, new Object[]{}) {
                            @Override
                            protected Void runRolez() {
                                i2.value++;
                                return null;
                            }
                        };
                        s.start(task2);
                        return null;
                    }
                };
                s.start(task1);
                
                assertEquals(2, guardReadOnly(guardReadOnly(r).o).value);
            }
        });
    }
    
    @Test
    public void testShareCycleModify() {
        assumeVerifyCorrectness();
        verifyTask(new Runnable() {
            public void run() {
                final Node n1 = new Node();
                final Node n2 = new Node(n1);
                n1.next = n2;
                
                Task<?> task = new Task<Void>(new Object[]{}, new Object[]{n1}) {
                    @Override
                    protected Void runRolez() {
                        assertEquals(0, n1.next.data);
                        return null;
                    }
                };
                s.start(task);
                
                guardReadWrite(n1).next = new Node(10);
            }
        });
    }
    
    @Test
    public void testReturn() {
        assumeVerifyCorrectness();
        verifyTask(new Runnable() {
            public void run() {
                Task<Int> task1 = new Task<Int>(new Object[]{}, new Object[]{}) {
                    @Override
                    protected Int runRolez() {
                        final Int result = new Int();
                        
                        Task<?> task2 = new Task<Void>(new Object[]{result}, new Object[]{}) {
                            @Override
                            protected Void runRolez() {
                                guardReadWrite(result).value = 3;
                                return null;
                            }
                        };
                        s.start(task2);
                        
                        return result;
                    }
                };
                s.start(task1);
                Int i = task1.get();
                
                assertEquals(3, guardReadOnly(i).value);
            }
        });
    }
    
    @Test
    public void testReturnUninitialized() {
        assumeVerifyCorrectness();
        verifyTask(new Runnable() {
            public void run() {
                Task<Int> task = new Task<Int>(new Object[]{}, new Object[]{}) {
                    @Override
                    protected Int runRolez() {
                        return new Int(3);
                    }
                };
                s.start(task);
                Int i = task.get();
                
                assertEquals(3, guardReadOnly(i).value);
            }
        });
    }
    
    @Test
    public void testReturnUninitializedContainer() {
        assumeVerifyCorrectness();
        verifyTask(new Runnable() {
            public void run() {
                Task<Ref<Int>> task1 = new Task<Ref<Int>>(new Object[]{}, new Object[]{}) {
                    @Override
                    protected Ref<Int> runRolez() {
                        final Int i = new Int();

                        Task<?> task2 = new Task<Void>(new Object[]{i}, new Object[]{}) {
                            @Override
                            protected Void runRolez() {
                                guardReadWrite(i).value = 3;
                                return null;
                            }
                        };
                        s.start(task2);
                        
                        return new Ref<>(i);
                    }
                };
                s.start(task1);
                Ref<Int> ref = task1.get();
                
                assertEquals(3, guardReadOnly(guardReadOnly(ref).o).value);
            }
        });
    }
}

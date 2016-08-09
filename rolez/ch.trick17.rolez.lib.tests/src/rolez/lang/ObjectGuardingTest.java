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
                {new ThreadPoolTaskSystem(), CORRECTNESS},
                {new SingleThreadTaskSystem(), CORRECTNESS},
                {new ThreadPoolTaskSystem(3), PARALLELISM}
        });
    }
    
    public ObjectGuardingTest(TaskSystem s, VerifyMode mode) {
        super(s, mode);
    }
    
    @Test
    public void testShare() {
        verifyTask(new int[][]{{0, 1}, {2, 3}, {0, 3}}, new RunnableCallable() {
            public void run() {
                final Int i = new Int();
                
                Task<?> task = new Task<>(new RunnableCallable() {
                    public void run() {
                        region(0);
                        assertEquals(0, i.value);
                        i.releaseShared();
                        region(1);
                    }
                });
                i.share(task);
                s.start(task);
                region(2);
                
                guardReadWrite(i).value = 1;
                region(3);
            }
        });
    }
    
    @Test
    public void testShareMissingGuard() {
        assumeMultithreaded();
        verifyTaskAssertionError(new RunnableCallable() {
            public void run() {
                final Int i = new Int();
                
                Task<?> task = new Task<>(new RunnableCallable() {
                    public void run() {
                        assertEquals(0, i.value);
                        i.releaseShared();
                    }
                });
                i.share(task);
                s.start(task);
                
                // A missing guard causes non-determinism
                i.value = 1;
            }
        });
    }
    
    @Test
    public void testShareMissingRelease() {
        verifyTaskDeadlock(new RunnableCallable() {
            public void run() {
                final Int i = new Int();
                
                Task<?> task = new Task<>(new RunnableCallable() {
                    public void run() {
                        assertEquals(0, i.value);
                        // A missing release causes a deadlock
                    }
                });
                i.share(task);
                s.start(task);
                
                guardReadWrite(i).value = 1;
            }
        });
    }
    
    @Test
    public void testShareMultiple() {
        verifyTask(new int[][]{{0, 1}, {2, 3}, {4, 5}, {0, 5}, {2, 5}, {0, 3 /* or: 1, 2 */}},
                new RunnableCallable() {
                    public void run() {
                        final Int i = new Int();
                        
                        Task<?> task1 = new Task<>(new RunnableCallable() {
                            public void run() {
                                region(0);
                                assertEquals(0, i.value);
                                i.releaseShared();
                                region(1);
                            }
                        });
                        i.share(task1);
                        s.start(task1);
                        
                        Task<?> task2 = new Task<>(new RunnableCallable() {
                            public void run() {
                                region(2);
                                assertEquals(0, i.value);
                                i.releaseShared();
                                region(3);
                            }
                        });
                        i.share(task2);
                        s.start(task2);
                        region(4);
                        
                        guardReadWrite(i).value = 1;
                        region(5);
                    }
                });
    }
    
    @Test
    public void testShareMultipleMissingRelease() {
        verifyTaskDeadlock(new RunnableCallable() {
            public void run() {
                final Int i = new Int();
                
                for(int k = 0; k < 3; k++) {
                    final int theK = k;
                    Task<?> task = new Task<>(new RunnableCallable() {
                        public void run() {
                            assertEquals(0, i.value);
                            
                            // A single missing release causes a deadlock:
                            if(theK != 0)
                                i.releaseShared();
                        }
                    });
                    i.share(task);
                    s.start(task);
                }
                
                guardReadWrite(i).value = 1;
            }
        });
    }
    
    @Test
    public void testPass() {
        verifyTask(new int[][]{{0, 1}, {0, 3}, {2, 3}}, new RunnableCallable() {
            public void run() {
                final Int i = new Int();
                
                Task<?> task = new Task<>(new RunnableCallable() {
                    public void run() {
                        i.completePass();
                        region(0);
                        i.value++;
                        i.releasePassed();
                        region(1);
                    }
                });
                i.pass(task);
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
        verifyTaskAssertionError(new RunnableCallable() {
            public void run() {
                final Int i = new Int();
                
                Task<?> task = new Task<>(new RunnableCallable() {
                    public void run() {
                        i.completePass();
                        i.value = 1;
                        i.releasePassed();
                    }
                });
                i.pass(task);
                s.start(task);
                
                // A missing guard causes non-determinism
                assertEquals(1, i.value);
            }
        });
    }
    
    @Test
    public void testPassMissingRelease() {
        assumeMultithreaded();
        verifyTaskDeadlock(new RunnableCallable() {
            public void run() {
                final Int i = new Int();
                
                Task<?> task = new Task<>(new RunnableCallable() {
                    public void run() {
                        i.completePass();
                        i.value = 1;
                        // A missing release causes a deadlock
                    }
                });
                i.pass(task);
                s.start(task);
                
                assertEquals(1, guardReadOnly(i).value);
            }
        });
    }
    
    @Test
    public void testPassMultiple() {
        /* IMPROVE: Allow {0, 4} in parallel by passing not-yet-available data to tasks (so far,
         * pass() is blocking) */
        verifyTask(new int[][]{{0, 1}, {0, 2, 3}, {0, 2, 5}, {4, 5}, {0, 4}},
                new RunnableCallable() {
                    public void run() {
                        final Int i = new Int();
                        
                        Task<?> task1 = new Task<>(new RunnableCallable() {
                            public void run() {
                                i.completePass();
                                region(0);
                                i.value++;
                                i.releasePassed();
                                region(1);
                            }
                        });
                        i.pass(task1);
                        s.start(task1);
                        
                        Task<?> task2 = new Task<>(new RunnableCallable() {
                            public void run() {
                                i.completePass();
                                region(2);
                                i.value++;
                                i.releasePassed();
                                region(3);
                            }
                        });
                        i.pass(task2);
                        s.start(task2);
                        region(4);
                        
                        assertEquals(2, guardReadOnly(i).value);
                        
                        region(5);
                    }
                });
    }
    
    @Test
    public void testPassMultipleMissingRelease() {
        assumeMultithreaded();
        verifyTaskDeadlock(new RunnableCallable() {
            public void run() {
                final Int i = new Int();
                
                final int taskCount = 2;
                for(int k = 0; k < taskCount; k++) {
                    final int theK = k;
                    Task<?> task = new Task<>(new RunnableCallable() {
                        public void run() {
                            i.completePass();
                            i.value++;
                            
                            // A single missing release causes a deadlock:
                            if(theK != 0)
                                i.releasePassed();
                        }
                    });
                    i.pass(task);
                    s.start(task);
                }
                
                assertEquals(taskCount, guardReadOnly(i).value);
            }
        });
    }
    
    @Test
    public void testPassNested() {
        verifyTask(new int[][]{{0, 1}, {2, 3}, {4, 5}, {0, 3}, {2, 5}, {0, 5}},
                new RunnableCallable() {
                    public void run() {
                        final Int i = new Int();
                        
                        Task<?> task1 = new Task<>(new RunnableCallable() {
                            public void run() {
                                i.completePass();
                                i.value++;
                                
                                Task<?> task2 = new Task<>(new RunnableCallable() {
                                    public void run() {
                                        i.completePass();
                                        i.value++;
                                        region(0);
                                        i.releasePassed();
                                        region(1);
                                    }
                                });
                                i.pass(task2);
                                s.start(task2);
                                region(2);
                                
                                assertEquals(2, guardReadWrite(i).value);
                                i.value++;
                                
                                i.releasePassed();
                                region(3);
                            }
                        });
                        i.pass(task1);
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
        verifyTask(new RunnableCallable() {
            public void run() {
                final Int i = new Int();
                
                Task<?> task1 = new Task<>(new RunnableCallable() {
                    public void run() {
                        i.completePass();
                        Task<?> task2 = new Task<>(new RunnableCallable() {
                            public void run() {
                                i.completePass();
                                i.releasePassed();
                            }
                        });
                        i.pass(task2);
                        s.start(task2);
                        
                        i.releasePassed();
                    }
                });
                i.pass(task1);
                s.start(task1);
            }
        });
    }
    
    @Test
    public void testPassNestedMissingRelease() {
        assumeMultithreaded();
        verifyTaskDeadlock(new RunnableCallable() {
            public void run() {
                final Int i = new Int();
                
                Task<?> task1 = new Task<>(new RunnableCallable() {
                    public void run() {
                        i.completePass();
                        i.value++;
                        
                        Task<?> task2 = new Task<>(new RunnableCallable() {
                            public void run() {
                                i.completePass();
                                i.value++;
                                // A missed release causes deadlock
                            }
                        });
                        i.pass(task2);
                        s.start(task2);
                        
                        i.releasePassed();
                    }
                });
                i.pass(task1);
                s.start(task1);
                
                assertEquals(3, guardReadOnly(i).value);
            }
        });
    }
    
    @Test
    public void testPassShare() {
        verifyTask(new RunnableCallable() {
            public void run() {
                final Int i = new Int();
                
                Task<?> task1 = new Task<>(new RunnableCallable() {
                    public void run() {
                        i.completePass();
                        i.value++;
                        i.releasePassed();
                        region(0);
                    }
                });
                i.pass(task1);
                s.start(task1);
                
                Task<?> task2 = new Task<>(new RunnableCallable() {
                    public void run() {
                        assertEquals(1, i.value);
                        region(1);
                        i.releaseShared();
                    }
                });
                i.share(task2);
                s.start(task2);
                region(2);
                
                guardReadWrite(i).value++;
            }
        });
    }
    
    @Test
    public void testShareGroup() {
        verifyTask(new int[][]{{0, 1}, {2, 3}, {0, 3}}, new RunnableCallable() {
            public void run() {
                final Int i = new Int();
                final Ref<Int> r = new Ref<>(i);
                
                Task<?> task = new Task<>(new RunnableCallable() {
                    public void run() {
                        assertEquals(0, r.o.value);
                        region(0);
                        r.releaseShared();
                        region(1);
                    }
                });
                r.share(task);
                s.start(task);
                region(2);
                
                guardReadWrite(i).value = 1;
                region(3);
            }
        });
    }
    
    @Test
    public void testShareGroupMultiple() {
        verifyTask(new RunnableCallable() {
            public void run() {
                final Int i = new Int();
                final Ref<Int> r = new Ref<>(i);
                
                for(int k = 0; k < 2; k++) {
                    final int theK = k;
                    Task<?> task = new Task<>(new RunnableCallable() {
                        public void run() {
                            assertEquals(0, r.o.value);
                            r.releaseShared();
                            region(theK);
                        }
                    });
                    r.share(task);
                    s.start(task);
                }
                
                guardReadWrite(i).value = 1;
                region(2);
            }
        });
    }
    
    @Test
    public void testPassGroup() {
        verifyTask(new int[][]{{0, 1}, {2, 3}, {0, 3}}, new RunnableCallable() {
            public void run() {
                final Int i = new Int();
                final Ref<Int> r = new Ref<>(i);
                
                Task<?> task = new Task<>(new RunnableCallable() {
                    public void run() {
                        r.completePass();
                        r.o.value++;
                        region(0);
                        r.releasePassed();
                        region(1);
                    }
                });
                r.pass(task);
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
        verifyTask(new RunnableCallable() {
            public void run() {
                final Int i = new Int();
                final Ref<Int> r = new Ref<>(i);
                
                for(int k = 0; k < 2; k++) {
                    Task<?> task = new Task<>(new RunnableCallable() {
                        public void run() {
                            r.completePass();
                            r.o.value++;
                            r.releasePassed();
                        }
                    });
                    r.pass(task);
                    s.start(task);
                }
                
                assertEquals(2, guardReadOnly(i).value);
            }
        });
    }
    
    @Test
    public void testPassGroupNested() {
        assumeVerifyCorrectness();
        verifyTask(new RunnableCallable() {
            public void run() {
                final Int i = new Int();
                final Ref<Int> r = new Ref<>(i);
                
                Task<?> task1 = new Task<>(new RunnableCallable() {
                    public void run() {
                        r.completePass();
                        final Int i2 = r.o;
                        i2.value++;
                        
                        Task<?> task2 = new Task<>(new RunnableCallable() {
                            public void run() {
                                r.completePass();
                                r.o.value++;
                                r.releasePassed();
                            }
                        });
                        r.pass(task2);
                        s.start(task2);
                        
                        assertEquals(2, guardReadWrite(i2).value);
                        i2.value++;
                        
                        r.releasePassed();
                    }
                });
                r.pass(task1);
                s.start(task1);
                
                assertEquals(3, guardReadOnly(i).value);
            }
        });
    }
    
    @Test
    public void testPassShareGroup() {
        verifyTask(new RunnableCallable() {
            public void run() {
                final Int i = new Int();
                final Ref<Int> r = new Ref<>(i);
                
                Task<?> task1 = new Task<>(new RunnableCallable() {
                    public void run() {
                        r.completePass();
                        r.o.value++;
                        r.releasePassed();
                        region(0);
                    }
                });
                r.pass(task1);
                s.start(task1);
                
                Task<?> task2 = new Task<>(new RunnableCallable() {
                    public void run() {
                        assertEquals(1, r.o.value);
                        region(1);
                        r.releaseShared();
                    }
                });
                r.share(task2);
                s.start(task2);
                region(2);
                
                guardReadWrite(i).value++;
            }
        });
    }
    
    @Test
    public void testShareSubgroupMultiple() {
        verifyTask(new int[][]{{0, 4}, {1, 4}, {2, 4}, {3, 4}}, new RunnableCallable() {
            public void run() {
                final Int i = new Int();
                final Ref<Int> r = new Ref<>(i);
                
                Task<?> task1 = new Task<>(new RunnableCallable() {
                    public void run() {
                        assertEquals(0, i.value);
                        region(0);
                        i.releaseShared();
                    }
                });
                i.share(task1);
                s.start(task1);
                
                Task<?> task2 = new Task<>(new RunnableCallable() {
                    public void run() {
                        assertEquals(0, r.o.value);
                        region(1);
                        r.releaseShared();
                    }
                });
                r.share(task2);
                s.start(task2);
                
                Task<?> task3 = new Task<>(new RunnableCallable() {
                    public void run() {
                        assertEquals(0, i.value);
                        region(2);
                        i.releaseShared();
                    }
                });
                i.share(task3);
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
        verifyTask(new int[][]{{0, 1, 2}, {0, 3}, {1, 3}}, new RunnableCallable() {
            public void run() {
                final Int i = new Int();
                final Ref<Int> r = new Ref<>(i);
                
                Task<?> task1 = new Task<>(new RunnableCallable() {
                    public void run() {
                        i.completePass();
                        region(0);
                        i.value++;
                        i.releasePassed();
                    }
                });
                i.pass(task1);
                s.start(task1);
                
                Task<?> task2 = new Task<>(new RunnableCallable() {
                    public void run() {
                        r.completePass();
                        region(1);
                        r.o.value++;
                        r.releasePassed();
                    }
                });
                r.pass(task2);
                s.start(task2);
                
                Task<?> task3 = new Task<>(new RunnableCallable() {
                    public void run() {
                        i.completePass();
                        region(2);
                        i.value++;
                        i.releasePassed();
                    }
                });
                i.pass(task3);
                s.start(task3);
                region(3);
                
                assertEquals(3, guardReadOnly(i).value);
            }
        });
    }
    
    @Test
    public void testPassSubgroupNested() {
        /* IMPROVE: Allow {0, 3} by releasing objects independent of reachable objects that are
         * still owned by other threads. */
        verifyTask(new int[][]{{0, 1}, {2, 3}, {0, 3}}, new RunnableCallable() {
            public void run() {
                final Int i = new Int();
                final Ref<Int> r = new Ref<>(i);
                
                Task<?> task1 = new Task<>(new RunnableCallable() {
                    public void run() {
                        r.completePass();
                        final Int i2 = r.o;
                        i2.value++;
                        
                        Task<?> task2 = new Task<>(new RunnableCallable() {
                            public void run() {
                                i2.completePass();
                                region(0);
                                i2.value++;
                                i2.releasePassed();
                                region(1);
                            }
                        });
                        i2.pass(task2);
                        s.start(task2);
                        region(2);
                        
                        r.releasePassed();
                        region(3);
                    }
                });
                r.pass(task1);
                s.start(task1);
                region(4);
                
                assertEquals(2, guardReadOnly(i).value);
            }
        });
    }
    
    @Test
    public void testPassShareSubgroup() {
        /* IMPROVE: Allow {0, 2, 3} by sharing not-yet-available data? */
        verifyTask(new int[][]{{0, 1}, {0, 2}, {0, 3}}, new RunnableCallable() {
            public void run() {
                final Int i = new Int();
                final Ref<Int> r = new Ref<>(i);
                
                Task<?> task1 = new Task<>(new RunnableCallable() {
                    public void run() {
                        r.completePass();
                        r.o.value++;
                        region(0);
                        r.releasePassed();
                        region(1);
                    }
                });
                r.pass(task1);
                s.start(task1);
                
                Task<?> task2 = new Task<>(new RunnableCallable() {
                    public void run() {
                        region(2);
                        assertEquals(1, i.value);
                        i.releaseShared();
                    }
                });
                i.share(task2);
                s.start(task2);
                region(3);
                
                guardReadWrite(i).value++;
            }
        });
    }
    
    @Test
    public void testShareGroupModify() {
        assumeVerifyCorrectness();
        verifyTask(new RunnableCallable() {
            public void run() {
                final Int i = new Int();
                final Ref<Int> r = new Ref<>(i);
                
                Task<?> task = new Task<>(new RunnableCallable() {
                    public void run() {
                        assertEquals(0, i.value);
                        r.releaseShared();
                    }
                });
                r.share(task);
                s.start(task);
                
                guardReadWrite(r).o = new Int(10);
            }
        });
    }
    
    @Test
    public void testPassGroupModify() {
        assumeVerifyCorrectness();
        verifyTask(new RunnableCallable() {
            public void run() {
                final Int i = new Int();
                final Ref<Int> r = new Ref<>(i);
                
                Task<?> task1 = new Task<>(new RunnableCallable() {
                    public void run() {
                        r.completePass();
                        r.o = new Int();
                        r.o.value = 10;
                        r.releasePassed();
                    }
                });
                r.pass(task1);
                s.start(task1);
                
                assertEquals(0, guardReadOnly(i).value);
                assertEquals(10, guardReadOnly(guardReadOnly(r).o).value);
            }
        });
    }
    
    @Test
    public void testPassSubgroupNestedModify() {
        assumeVerifyCorrectness();
        verifyTask(new RunnableCallable() {
            public void run() {
                final Int i = new Int();
                final Ref<Int> r = new Ref<>(i);
                
                Task<?> task1 = new Task<>(new RunnableCallable() {
                    public void run() {
                        r.completePass();
                        r.o = new Int();
                        
                        final Int i2 = r.o;
                        i2.value++;
                        Task<?> task2 = new Task<>(new RunnableCallable() {
                            public void run() {
                                i2.completePass();
                                i2.value++;
                                i2.releasePassed();
                            }
                        });
                        i2.pass(task2);
                        s.start(task2);
                        r.releasePassed();
                    }
                });
                r.pass(task1);
                s.start(task1);
                
                assertEquals(2, guardReadOnly(guardReadOnly(r).o).value);
            }
        });
    }
    
    @Test
    public void testShareCycleModify() {
        assumeVerifyCorrectness();
        verifyTask(new RunnableCallable() {
            public void run() {
                final Node n1 = new Node();
                final Node n2 = new Node(n1);
                n1.next = n2;
                
                Task<?> task = new Task<>(new RunnableCallable() {
                    public void run() {
                        assertEquals(0, n1.next.data);
                        n1.releaseShared();
                    }
                });
                n1.share(task);
                s.start(task);
                
                guardReadWrite(n1).next = new Node(10);
            }
        });
    }
}

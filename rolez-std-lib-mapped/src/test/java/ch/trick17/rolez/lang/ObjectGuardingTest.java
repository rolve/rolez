package ch.trick17.rolez.lang;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ch.trick17.rolez.lang.SomeClasses.Int;
import ch.trick17.rolez.lang.SomeClasses.Node;
import ch.trick17.rolez.lang.SomeClasses.Ref;
import ch.trick17.rolez.lang.task.NewThreadTaskSystem;
import ch.trick17.rolez.lang.task.SingleThreadTaskSystem;
import ch.trick17.rolez.lang.task.Task;
import ch.trick17.rolez.lang.task.TaskSystem;
import ch.trick17.rolez.lang.task.ThreadPoolTaskSystem;

@RunWith(Parameterized.class)
public class ObjectGuardingTest extends GuardingTest {
    
    @Parameters(name = "{0}, {1}")
    public static List<?> taskSystems() {
        return Arrays.asList(new Object[][]{
                {new NewThreadTaskSystem(), VerifyMode.CORRECTNESS},
                {new ThreadPoolTaskSystem(), VerifyMode.CORRECTNESS},
                {new SingleThreadTaskSystem(), VerifyMode.CORRECTNESS},
                {new NewThreadTaskSystem(), VerifyMode.PARALLELISM},
                {new ThreadPoolTaskSystem(3), VerifyMode.PARALLELISM}});
    }
    
    public ObjectGuardingTest(final TaskSystem s, final VerifyMode mode) {
        super(s, mode);
    }
    
    @Test
    public void testShare() {
        verify(new int[][]{{0, 1}, {2, 3}, {0, 3}}, new Runnable() {
            public void run() {
                final Int i = new Int();
                
                i.share();
                s.run(new Runnable() {
                    public void run() {
                        region(0);
                        assertEquals(0, i.value);
                        i.releaseShared();
                        region(1);
                    }
                });
                region(2);
                
                i.guardReadWrite();
                i.value = 1;
                region(3);
            }
        });
    }
    
    @Test
    public void testShareMissingGuard() {
        assumeVerifyCorrectness();
        assumeMultithreaded();
        if(verifyAssertionError()) {
            final Int i = new Int();
            
            i.share();
            final Task<Void> task = s.run(new Runnable() {
                public void run() {
                    assertEquals(0, i.value);
                    i.releaseShared();
                }
            });
            
            // A missing guard causes non-determinism
            i.value = 1;
            task.get();
        }
    }
    
    @Test
    public void testShareMissingRelease() {
        assumeVerifyCorrectness();
        if(verifyDeadlock()) {
            final Int i = new Int();
            
            i.share();
            final Task<Void> task = s.run(new Runnable() {
                public void run() {
                    assertEquals(0, i.value);
                    // A missing release causes a deadlock
                }
            });
            
            i.guardReadWrite();
            i.value = 1;
            task.get();
        }
    }
    
    @Test
    public void testShareMultiple() {
        if(verify(mode, new int[][]{{0, 1}, {2, 3}, {4, 5}, {0, 5}, {2, 5},
                {0, 3 /* or: 1, 2 */}})) {
            final Int i = new Int();
            
            i.share();
            final Task<?> task1 = s.run(new Runnable() {
                public void run() {
                    region(0);
                    assertEquals(0, i.value);
                    i.releaseShared();
                    region(1);
                }
            });
            
            i.share();
            final Task<?> task2 = s.run(new Runnable() {
                public void run() {
                    region(2);
                    assertEquals(0, i.value);
                    i.releaseShared();
                    region(3);
                }
            });
            region(4);
            
            i.guardReadWrite();
            i.value = 1;
            region(5);
            
            task1.get();
            task2.get();
        }
    }
    
    @Test
    public void testShareMultipleMissingRelease() {
        assumeVerifyCorrectness();
        if(verifyDeadlock()) {
            final Int i = new Int();
            
            final Task<?>[] tasks = new Task<?>[3];
            for(int k = 0; k < 3; k++) {
                final int theI = k;
                
                i.share();
                tasks[k] = s.run(new Runnable() {
                    public void run() {
                        assertEquals(0, i.value);
                        
                        // A single missing release causes a deadlock:
                        if(theI != 0)
                            i.releaseShared();
                    }
                });
            }
            
            i.guardReadWrite();
            i.value = 1;
            for(final Task<?> task : tasks)
                task.get();
        }
    }
    
    @Test
    public void testPass() {
        if(verify(mode, new int[][]{{0, 1}, {0, 3}, {2, 3}})) {
            final Int i = new Int();
            
            i.pass();
            final Task<Void> task = s.run(new Runnable() {
                public void run() {
                    i.registerNewOwner();
                    region(0);
                    i.value++;
                    i.releasePassed();
                    region(1);
                }
            });
            region(2);
            
            i.guardRead();
            assertEquals(1, i.value);
            region(3);
            
            task.get();
        }
    }
    
    @Test
    public void testPassMissingGuard() {
        assumeVerifyCorrectness();
        assumeMultithreaded();
        if(verifyAssertionError()) {
            final Int i = new Int();
            
            i.pass();
            final Task<Void> task = s.run(new Runnable() {
                public void run() {
                    i.registerNewOwner();
                    i.value = 1;
                    i.releasePassed();
                }
            });
            
            // A missing guard causes non-determinism
            assertEquals(1, i.value);
            task.get();
        }
    }
    
    @Test
    public void testPassMissingRelease() {
        assumeVerifyCorrectness();
        assumeMultithreaded();
        if(verifyDeadlock()) {
            final Int i = new Int();
            
            i.pass();
            final Task<Void> task = s.run(new Runnable() {
                public void run() {
                    i.registerNewOwner();
                    i.value = 1;
                    // A missing release causes a deadlock
                }
            });
            
            i.guardRead();
            assertEquals(1, i.value);
            task.get();
        }
    }
    
    @Test
    public void testPassMultiple() {
        /* IMPROVE: Allow {0, 4} in parallel by passing not-yet-available data
         * to tasks (so far, pass() is blocking) */
        if(verify(mode, new int[][]{{0, 1}, {0, 2, 3}, {0, 2, 5}, {4, 5},
                {0, 4}})) {
            final Int i = new Int();
            
            i.pass();
            final Task<?> task1 = s.run(new Runnable() {
                public void run() {
                    i.registerNewOwner();
                    region(0);
                    i.value++;
                    i.releasePassed();
                    region(1);
                }
            });
            
            i.pass();
            final Task<?> task2 = s.run(new Runnable() {
                public void run() {
                    i.registerNewOwner();
                    region(2);
                    i.value++;
                    i.releasePassed();
                    region(3);
                }
            });
            region(4);
            
            i.guardRead();
            assertEquals(2, i.value);
            
            region(5);
            task1.get();
            task2.get();
        }
    }
    
    @Test
    public void testPassMultipleMissingRelease() {
        assumeVerifyCorrectness();
        assumeMultithreaded();
        if(verifyDeadlock()) {
            final Int i = new Int();
            
            final int taskCount = 2;
            final Task<?>[] tasks = new Task<?>[taskCount];
            for(int k = 0; k < taskCount; k++) {
                final int theK = k;
                i.pass();
                tasks[k] = s.run(new Runnable() {
                    public void run() {
                        i.registerNewOwner();
                        i.value++;
                        
                        // A single missing release causes a deadlock:
                        if(theK != 0)
                            i.releasePassed();
                    }
                });
            }
            
            i.guardRead();
            assertEquals(taskCount, i.value);
            for(final Task<?> task : tasks)
                task.get();
        }
    }
    
    @Test
    public void testPassNested() {
        if(verify(mode, new int[][]{{0, 1}, {2, 3}, {4, 5}, {0, 3}, {2, 5},
                {0, 5}})) {
            final Int i = new Int();
            
            i.pass();
            final Task<Void> task = s.run(new Runnable() {
                public void run() {
                    i.registerNewOwner();
                    i.value++;
                    
                    i.pass();
                    final Task<Void> task2 = s.run(new Runnable() {
                        public void run() {
                            i.registerNewOwner();
                            i.value++;
                            region(0);
                            i.releasePassed();
                            region(1);
                        }
                    });
                    region(2);
                    
                    i.guardReadWrite();
                    assertEquals(2, i.value);
                    i.value++;
                    
                    i.releasePassed();
                    region(3);
                    task2.get();
                }
            });
            region(4);
            
            i.guardRead();
            assertEquals(3, i.value);
            
            region(5);
            task.get();
        }
    }
    
    @Test
    public void testPassNestedMissingRelease() {
        assumeVerifyCorrectness();
        assumeMultithreaded();
        if(verifyDeadlock()) {
            final Int i = new Int();
            
            i.pass();
            final Task<Void> task = s.run(new Runnable() {
                public void run() {
                    i.registerNewOwner();
                    i.value++;
                    
                    i.pass();
                    final Task<Void> task2 = s.run(new Runnable() {
                        public void run() {
                            i.registerNewOwner();
                            i.value++;
                            // A missed release causes deadlock
                        }
                    });
                    
                    i.releasePassed();
                    task2.get();
                }
            });
            
            i.guardRead();
            assertEquals(3, i.value);
            task.get();
        }
    }
    
    @Test
    public void testPassShare() {
        if(verify(mode)) {
            final Int i = new Int();
            
            i.pass();
            final Task<Void> task = s.run(new Runnable() {
                public void run() {
                    i.registerNewOwner();
                    i.value++;
                    i.releasePassed();
                    region(0);
                }
            });
            
            i.share();
            final Task<Void> task2 = s.run(new Runnable() {
                public void run() {
                    assertEquals(1, i.value);
                    region(1);
                    i.releaseShared();
                }
            });
            region(2);
            
            i.guardReadWrite();
            i.value++;
            task.get();
            task2.get();
        }
    }
    
    @Test
    public void testShareGroup() {
        if(verify(mode, new int[][]{{0, 1}, {2, 3}, {0, 3}})) {
            final Int i = new Int();
            final Ref<Int> r = new Ref<>(i);
            
            r.share();
            final Task<Void> task = s.run(new Runnable() {
                public void run() {
                    assertEquals(0, r.o.value);
                    region(0);
                    r.releaseShared();
                    region(1);
                }
            });
            region(2);
            
            i.guardReadWrite();
            i.value = 1;
            region(3);
            
            task.get();
        }
    }
    
    @Test
    public void testShareGroupMultiple() {
        if(verify(mode)) {
            final Int i = new Int();
            final Ref<Int> r = new Ref<>(i);
            
            final Task<?>[] tasks = new Task<?>[2];
            for(int k = 0; k < 2; k++) {
                final int theK = k;
                r.share();
                tasks[k] = s.run(new Runnable() {
                    public void run() {
                        assertEquals(0, r.o.value);
                        r.releaseShared();
                        region(theK);
                    }
                });
            }
            
            i.guardReadWrite();
            i.value = 1;
            region(2);
            
            for(final Task<?> task : tasks)
                task.get();
        }
    }
    
    @Test
    public void testPassGroup() {
        if(verify(mode, new int[][]{{0, 1}, {2, 3}, {0, 3}})) {
            final Int i = new Int();
            final Ref<Int> r = new Ref<>(i);
            
            r.pass();
            final Task<Void> task = s.run(new Runnable() {
                public void run() {
                    r.registerNewOwner();
                    r.o.value++;
                    region(0);
                    r.releasePassed();
                    region(1);
                }
            });
            region(2);
            
            i.guardRead();
            assertEquals(1, i.value);
            region(3);
            
            task.get();
        }
    }
    
    @Test
    public void testPassGroupMultiple() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final Int i = new Int();
            final Ref<Int> r = new Ref<>(i);
            
            final Task<?>[] tasks = new Task<?>[2];
            for(int k = 0; k < 2; k++) {
                r.pass();
                tasks[k] = s.run(new Runnable() {
                    public void run() {
                        r.registerNewOwner();
                        r.o.value++;
                        r.releasePassed();
                    }
                });
            }
            
            i.guardRead();
            assertEquals(2, i.value);
            for(final Task<?> task : tasks)
                task.get();
        }
    }
    
    @Test
    public void testPassGroupNested() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final Int i = new Int();
            final Ref<Int> r = new Ref<>(i);
            
            r.pass();
            final Task<Void> task = s.run(new Runnable() {
                public void run() {
                    r.registerNewOwner();
                    final Int i2 = r.o;
                    i2.value++;
                    
                    r.pass();
                    final Task<Void> task2 = s.run(new Runnable() {
                        public void run() {
                            r.registerNewOwner();
                            r.o.value++;
                            r.releasePassed();
                        }
                    });
                    
                    i2.guardReadWrite();
                    assertEquals(2, i2.value);
                    i2.value++;
                    
                    r.releasePassed();
                    task2.get();
                }
            });
            
            i.guardRead();
            assertEquals(3, i.value);
            task.get();
        }
    }
    
    @Test
    public void testPassShareGroup() {
        if(verify(mode)) {
            final Int i = new Int();
            final Ref<Int> r = new Ref<>(i);
            
            r.pass();
            final Task<Void> task = s.run(new Runnable() {
                public void run() {
                    r.registerNewOwner();
                    r.o.value++;
                    r.releasePassed();
                    region(0);
                }
            });
            
            r.share();
            final Task<Void> task2 = s.run(new Runnable() {
                public void run() {
                    assertEquals(1, r.o.value);
                    region(1);
                    r.releaseShared();
                }
            });
            region(2);
            
            i.guardReadWrite();
            i.value++;
            task.get();
            task2.get();
        }
    }
    
    @Test
    public void testShareSubgroupMultiple() {
        if(verify(mode, new int[][]{{0, 4}, {1, 4}, {2, 4}, {3, 4}})) {
            final Int i = new Int();
            final Ref<Int> r = new Ref<>(i);
            
            i.share();
            final Task<Void> task1 = s.run(new Runnable() {
                public void run() {
                    assertEquals(0, i.value);
                    region(0);
                    i.releaseShared();
                }
            });
            
            r.share();
            final Task<Void> task2 = s.run(new Runnable() {
                public void run() {
                    assertEquals(0, r.o.value);
                    region(1);
                    r.releaseShared();
                }
            });
            
            i.share();
            final Task<Void> task3 = s.run(new Runnable() {
                public void run() {
                    assertEquals(0, i.value);
                    region(2);
                    i.releaseShared();
                }
            });
            region(3);
            
            i.guardReadWrite();
            i.value = 1;
            region(4);
            
            task1.get();
            task2.get();
            task3.get();
        }
    }
    
    @Test
    public void testPassSubgroup() {
        /* IMPROVE: Allow all regions by passing not-yet-available data */
        if(verify(mode, new int[][]{{0, 1, 2}, {0, 3}, {1, 3}})) {
            final Int i = new Int();
            final Ref<Int> r = new Ref<>(i);
            
            i.pass();
            final Task<Void> task1 = s.run(new Runnable() {
                public void run() {
                    i.registerNewOwner();
                    region(0);
                    i.value++;
                    i.releasePassed();
                }
            });
            
            r.pass();
            final Task<Void> task2 = s.run(new Runnable() {
                public void run() {
                    r.registerNewOwner();
                    region(1);
                    r.o.value++;
                    r.releasePassed();
                }
            });
            
            i.pass();
            final Task<Void> task3 = s.run(new Runnable() {
                public void run() {
                    i.registerNewOwner();
                    region(2);
                    i.value++;
                    i.releasePassed();
                }
            });
            region(3);
            
            i.guardRead();
            assertEquals(3, i.value);
            task1.get();
            task2.get();
            task3.get();
        }
    }
    
    @Test
    public void testPassSubgroupNested() {
        /* IMPROVE: Allow {0, 3} by releasing objects independent of reachable
         * objects that are still owned by other threads. */
        if(verify(mode, new int[][]{{0, 1}, {2, 3}, {0, 3}})) {
            final Int i = new Int();
            final Ref<Int> r = new Ref<>(i);
            
            r.pass();
            final Task<Void> task = s.run(new Runnable() {
                public void run() {
                    r.registerNewOwner();
                    final Int i2 = r.o;
                    i2.value++;
                    
                    i2.pass();
                    final Task<Void> task2 = s.run(new Runnable() {
                        public void run() {
                            i2.registerNewOwner();
                            region(0);
                            i2.value++;
                            i2.releasePassed();
                            region(1);
                        }
                    });
                    region(2);
                    
                    r.releasePassed();
                    region(3);
                    task2.get();
                }
            });
            region(4);
            
            i.guardRead();
            assertEquals(2, i.value);
            task.get();
        }
    }
    
    @Test
    public void testPassShareSubgroup() {
        /* IMPROVE: Allow {0, 2, 3} by sharing not-yet-available data? */
        if(verify(mode, new int[][]{{0, 1}, {0, 2}, {0, 3}})) {
            final Int i = new Int();
            final Ref<Int> r = new Ref<>(i);
            
            r.pass();
            final Task<Void> task = s.run(new Runnable() {
                public void run() {
                    r.registerNewOwner();
                    r.o.value++;
                    region(0);
                    r.releasePassed();
                    region(1);
                }
            });
            
            i.share();
            final Task<Void> task2 = s.run(new Runnable() {
                public void run() {
                    region(2);
                    assertEquals(1, i.value);
                    i.releaseShared();
                }
            });
            region(3);
            
            i.guardReadWrite();
            i.value++;
            task.get();
            task2.get();
        }
    }
    
    @Test
    public void testShareGroupModify() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final Int i = new Int();
            final Ref<Int> r = new Ref<>(i);
            
            r.share();
            final Task<Void> task = s.run(new Runnable() {
                public void run() {
                    assertEquals(0, i.value);
                    r.releaseShared();
                }
            });
            
            r.guardReadWrite();
            r.o = new Int(10);
            
            task.get();
        }
    }
    
    @Test
    public void testPassGroupModify() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final Int i = new Int();
            final Ref<Int> r = new Ref<>(i);
            
            r.pass();
            final Task<Void> task = s.run(new Runnable() {
                public void run() {
                    r.registerNewOwner();
                    r.o = new Int();
                    r.o.value = 10;
                    r.releasePassed();
                }
            });
            
            i.guardRead();
            assertEquals(0, i.value);
            
            r.guardRead();
            r.o.guardRead();
            assertEquals(10, r.o.value);
            task.get();
        }
    }
    
    @Test
    public void testPassSubgroupNestedModify() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final Int i = new Int();
            final Ref<Int> r = new Ref<>(i);
            
            r.pass();
            final Task<Void> task = s.run(new Runnable() {
                public void run() {
                    r.registerNewOwner();
                    r.o = new Int();
                    
                    final Int i2 = r.o;
                    i2.value++;
                    i2.pass();
                    final Task<Void> task2 = s.run(new Runnable() {
                        public void run() {
                            i2.registerNewOwner();
                            i2.value++;
                            i2.releasePassed();
                        }
                    });
                    
                    r.releasePassed();
                    task2.get();
                }
            });
            
            r.guardRead();
            r.o.guardRead();
            assertEquals(2, r.o.value);
            task.get();
        }
    }
    
    @Test
    public void testShareCycleModify() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final Node n1 = new Node();
            final Node n2 = new Node(n1);
            n1.next = n2;
            
            n1.share();
            final Task<Void> task = s.run(new Runnable() {
                public void run() {
                    assertEquals(0, n1.next.data);
                    n1.releaseShared();
                }
            });
            
            n1.guardReadWrite();
            n1.next = new Node(10);
            
            task.get();
        }
    }
}

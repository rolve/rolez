package ch.trick17.peppl.lib;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ch.trick17.peppl.lib.guard.Array;
import ch.trick17.peppl.lib.guard.IntArray;
import ch.trick17.peppl.lib.guard.IntSlice;
import ch.trick17.peppl.lib.guard.Slice;
import ch.trick17.peppl.lib.task.NewThreadTaskSystem;
import ch.trick17.peppl.lib.task.SingleThreadTaskSystem;
import ch.trick17.peppl.lib.task.Task;
import ch.trick17.peppl.lib.task.TaskSystem;
import ch.trick17.peppl.lib.task.ThreadPoolTaskSystem;
import ch.trick17.simplejpf.test.JpfParallelismTest;

@RunWith(Parameterized.class)
public class GuardTest extends JpfParallelismTest {
    
    @Parameters(name = "{0}, {1}")
    public static List<?> taskSystems() {
        return Arrays.asList(new Object[][]{
                {new NewThreadTaskSystem(), VerifyMode.CORRECTNESS},
                {new ThreadPoolTaskSystem(), VerifyMode.CORRECTNESS},
                {new SingleThreadTaskSystem(), VerifyMode.CORRECTNESS},
                {new NewThreadTaskSystem(), VerifyMode.PARALLELISM},
                {new ThreadPoolTaskSystem(3), VerifyMode.PARALLELISM}});
    }
    
    private final TaskSystem s;
    public final VerifyMode mode;
    
    public GuardTest(final TaskSystem s, final VerifyMode mode) {
        this.s = s;
        this.mode = mode;
    }
    
    @Before
    public void setJpfProps() {
        final Map<String, String> props = new HashMap<>();
        props.put("vm.por", "false");
        setJpfProperties(props);
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
            final IntContainer c = new IntContainer();
            
            c.share();
            final Task<Void> task = s.run(new Runnable() {
                public void run() {
                    assertEquals(0, c.i.value);
                    region(0);
                    c.releaseShared();
                    region(1);
                }
            });
            region(2);
            
            c.guardRead(); // To read i
            c.i.guardReadWrite();
            c.i.value = 1;
            region(3);
            
            task.get();
        }
    }
    
    @Test
    public void testShareGroupMultiple() {
        if(verify(mode)) {
            final Int i = new Int();
            final IntContainer c = new IntContainer(i);
            
            final Task<?>[] tasks = new Task<?>[2];
            for(int k = 0; k < 2; k++) {
                final int theK = k;
                c.share();
                tasks[k] = s.run(new Runnable() {
                    public void run() {
                        assertEquals(0, c.i.value);
                        c.releaseShared();
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
            final IntContainer c = new IntContainer(i);
            
            c.pass();
            final Task<Void> task = s.run(new Runnable() {
                public void run() {
                    c.registerNewOwner();
                    c.i.value++;
                    region(0);
                    c.releasePassed();
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
            final IntContainer c = new IntContainer(i);
            
            final Task<?>[] tasks = new Task<?>[2];
            for(int k = 0; k < 2; k++) {
                c.pass();
                tasks[k] = s.run(new Runnable() {
                    public void run() {
                        c.registerNewOwner();
                        c.i.value++;
                        c.releasePassed();
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
            final IntContainer c = new IntContainer(i);
            
            c.pass();
            final Task<Void> task = s.run(new Runnable() {
                public void run() {
                    c.registerNewOwner();
                    final Int i2 = c.i;
                    i2.value++;
                    
                    c.pass();
                    final Task<Void> task2 = s.run(new Runnable() {
                        public void run() {
                            c.registerNewOwner();
                            c.i.value++;
                            c.releasePassed();
                        }
                    });
                    
                    i2.guardReadWrite();
                    assertEquals(2, i2.value);
                    i2.value++;
                    
                    c.releasePassed();
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
            final IntContainer c = new IntContainer(i);
            
            c.pass();
            final Task<Void> task = s.run(new Runnable() {
                public void run() {
                    c.registerNewOwner();
                    c.i.value++;
                    c.releasePassed();
                    region(0);
                }
            });
            
            c.share();
            final Task<Void> task2 = s.run(new Runnable() {
                public void run() {
                    assertEquals(1, c.i.value);
                    region(1);
                    c.releaseShared();
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
            final IntContainer c = new IntContainer(i);
            
            i.share();
            final Task<Void> task1 = s.run(new Runnable() {
                public void run() {
                    assertEquals(0, i.value);
                    region(0);
                    i.releaseShared();
                }
            });
            
            c.share();
            final Task<Void> task2 = s.run(new Runnable() {
                public void run() {
                    assertEquals(0, c.i.value);
                    region(1);
                    c.releaseShared();
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
            final IntContainer c = new IntContainer(i);
            
            i.pass();
            final Task<Void> task1 = s.run(new Runnable() {
                public void run() {
                    i.registerNewOwner();
                    region(0);
                    i.value++;
                    i.releasePassed();
                }
            });
            
            c.pass();
            final Task<Void> task2 = s.run(new Runnable() {
                public void run() {
                    c.registerNewOwner();
                    region(1);
                    c.i.value++;
                    c.releasePassed();
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
            final IntContainer c = new IntContainer(i);
            
            c.pass();
            final Task<Void> task = s.run(new Runnable() {
                public void run() {
                    c.registerNewOwner();
                    final Int i2 = c.i;
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
                    
                    c.releasePassed();
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
            final IntContainer c = new IntContainer(i);
            
            c.pass();
            final Task<Void> task = s.run(new Runnable() {
                public void run() {
                    c.registerNewOwner();
                    c.i.value++;
                    region(0);
                    c.releasePassed();
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
    public void testPassGroupModify() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final Int i = new Int();
            final IntContainer c = new IntContainer(i);
            
            c.pass();
            final Task<Void> task = s.run(new Runnable() {
                public void run() {
                    c.registerNewOwner();
                    c.i = new Int();
                    c.i.value = 10;
                    c.releasePassed();
                }
            });
            
            i.guardRead();
            assertEquals(0, i.value);
            
            c.guardRead();
            c.i.guardRead();
            assertEquals(10, c.i.value);
            task.get();
        }
    }
    
    @Test
    public void testPassSubgroupNestedModify() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final Int i = new Int();
            final IntContainer c = new IntContainer(i);
            
            c.pass();
            final Task<Void> task = s.run(new Runnable() {
                public void run() {
                    c.registerNewOwner();
                    c.i = new Int();
                    
                    final Int i2 = c.i;
                    i2.value++;
                    i2.pass();
                    final Task<Void> task2 = s.run(new Runnable() {
                        public void run() {
                            i2.registerNewOwner();
                            i2.value++;
                            i2.releasePassed();
                        }
                    });
                    
                    c.releasePassed();
                    task2.get();
                }
            });
            
            c.guardRead();
            c.i.guardRead();
            assertEquals(2, c.i.value);
            task.get();
        }
    }
    
    @Test
    public void testShareArray() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final Array<Int> a = new Array<>(new Int[3]);
            for(int i = 0; i < a.data.length; i++)
                a.data[i] = new Int(i);
            
            a.share();
            final Task<Void> task = s.run(new Runnable() {
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
    public void testSharePrimitiveArray() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final IntArray a = new IntArray(new int[]{0});
            
            a.share();
            final Task<Void> task = s.run(new Runnable() {
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
            final Task<Void> task = s.run(new Runnable() {
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
            final Task<Void> task = s.run(new Runnable() {
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
            final Task<Void> task1 = s.run(new Runnable() {
                public void run() {
                    assertEquals(0, i.value);
                    i.releaseShared();
                }
            });
            
            a.share();
            final Task<Void> task2 = s.run(new Runnable() {
                public void run() {
                    assertEquals(0, a.data[0].value);
                    a.releaseShared();
                }
            });
            
            i.share();
            final Task<Void> task3 = s.run(new Runnable() {
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
            final Task<Void> task1 = s.run(new Runnable() {
                public void run() {
                    i.registerNewOwner();
                    i.value++;
                    i.releasePassed();
                }
            });
            
            a.pass();
            final Task<Void> task2 = s.run(new Runnable() {
                public void run() {
                    a.registerNewOwner();
                    a.data[0].value++;
                    a.releasePassed();
                }
            });
            
            i.pass();
            final Task<Void> task3 = s.run(new Runnable() {
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
            final Task<Void> task = s.run(new Runnable() {
                public void run() {
                    a.registerNewOwner();
                    a.data[0] = new Int();
                    
                    final Int i2 = a.data[0];
                    i2.value++;
                    i2.pass();
                    final Task<Void> task2 = s.run(new Runnable() {
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
            final Task<Void> task = s.run(new Runnable() {
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
            final Task<Void> task = s.run(new Runnable() {
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
    
    // FIXME: Test (and probably fix) guarding of overlapping slices
    // FIXME: Test and fix guarding of striped slices
    
    @Test
    public void testSharePrimitiveSlice() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final IntArray a = new IntArray(0, 1);
            
            final IntSlice slice = a.slice(0, 1, 1);
            slice.share();
            final Task<Void> task = s.run(new Runnable() {
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
    public void testPassSlice() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final Array<Int> a = new Array<>(new Int[10]);
            for(int i = 0; i < a.data.length; i++)
                a.data[i] = new Int(i);
            
            final Slice<Int> slice = a.slice(0, 5, 1);
            slice.pass();
            final Task<Void> task = s.run(new Runnable() {
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
    public void testPassPrimitiveSlice() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final IntArray a = new IntArray(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
            
            final IntSlice slice = a.slice(0, 5, 1);
            slice.pass();
            final Task<Void> task = s.run(new Runnable() {
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
    public void testShareSliceMultiple() {
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final Int i = new Int();
            final Array<Int> a = new Array<>(i);
            final Slice<Int> slice = a.slice(0, 1, 1);
            
            slice.share();
            final Task<Void> task1 = s.run(new Runnable() {
                public void run() {
                    assertEquals(0, slice.data[0].value);
                    slice.releaseShared();
                }
            });
            
            a.share();
            final Task<Void> task2 = s.run(new Runnable() {
                public void run() {
                    assertEquals(0, a.data[0].value);
                    a.releaseShared();
                }
            });
            
            slice.share();
            final Task<Void> task3 = s.run(new Runnable() {
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
            final Task<Void> task1 = s.run(new Runnable() {
                public void run() {
                    slice.registerNewOwner();
                    slice.data[0].value++;
                    slice.releasePassed();
                }
            });
            
            a.pass();
            final Task<Void> task2 = s.run(new Runnable() {
                public void run() {
                    a.registerNewOwner();
                    a.data[0].value++;
                    a.releasePassed();
                }
            });
            
            slice.pass();
            final Task<Void> task3 = s.run(new Runnable() {
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
            final Task<Void> task1 = s.run(new Runnable() {
                public void run() {
                    slice1.registerNewOwner();
                    for(int i = slice1.range.begin; i < slice1.range.end; i++)
                        slice1.data[i].value++;
                    region(0);
                    slice1.releasePassed();
                }
            });
            
            slice2.pass();
            final Task<Void> task2 = s.run(new Runnable() {
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
            final Task<Void> task = s.run(new Runnable() {
                public void run() {
                    slice1.registerNewOwner();
                    
                    final Slice<Int> slice2 = slice1.slice(0, 1, 1);
                    slice2.pass();
                    final Task<Void> task2 = s.run(new Runnable() {
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
            final Task<Void> task = s.run(new Runnable() {
                public void run() {
                    slice1.registerNewOwner();
                    
                    final IntSlice slice2 = slice1.slice(0, 1, 1);
                    slice2.pass();
                    final Task<Void> task2 = s.run(new Runnable() {
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
    
    // TODO: Change all tests to use implicit exception propagation using this
    // method:
    private void verify(final int[][] seqGroups, final Runnable test) {
        if(verify(mode, seqGroups)) {
            s.runDirectly(test);
        }
    }
    
    private void assumeMultithreaded() {
        if(s instanceof SingleThreadTaskSystem)
            throw new AssumptionViolatedException("not a multithreaded test");
    }
    
    private void assumeVerifyCorrectness() {
        if(mode != VerifyMode.CORRECTNESS)
            throw new AssumptionViolatedException(
                    "not verifying correctness properties");
    }
}

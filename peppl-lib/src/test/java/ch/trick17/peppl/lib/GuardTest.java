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

import ch.trick17.peppl.lib.task.NewThreadTaskSystem;
import ch.trick17.peppl.lib.task.SingleThreadTaskSystem;
import ch.trick17.peppl.lib.task.Task;
import ch.trick17.peppl.lib.task.TaskSystem;
import ch.trick17.simplejpf.test.JpfParallelismTest;

@RunWith(Parameterized.class)
public class GuardTest extends JpfParallelismTest {
    
    @Parameters(name = "{0}, {1}")
    public static List<?> taskSystems() {
        return Arrays.asList(new Object[][]{
                {new SingleThreadTaskSystem(), VerifyMode.CORRECTNESS},
                {new NewThreadTaskSystem(), VerifyMode.CORRECTNESS},
                {new NewThreadTaskSystem(), VerifyMode.PARALLELISM}});
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
        if(verify(mode, new int[][]{{0, 2}, {1, 2}, {1, 3}})) {
            final Int i = new Int();
            
            i.share();
            final Task<Void> task = s.run(new Runnable() {
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
            
            task.get();
        }
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
        if(verify(mode, new int[][]{{0, 2, 4}, {1, 3, 5}})) {
            final Int i = new Int();
            
            final Task<?>[] tasks = new Task<?>[2];
            for(int k = 0; k < 2; k++) {
                final int k2 = 2 * k;
                i.share();
                tasks[k] = s.run(new Runnable() {
                    public void run() {
                        region(k2);
                        assertEquals(0, i.value);
                        i.releaseShared();
                        region(k2 + 1);
                    }
                });
            }
            region(4);
            
            i.guardReadWrite();
            i.value = 1;
            region(5);
            
            for(final Task<?> task : tasks)
                task.get();
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
        if(verify(mode, new int[][]{{0, 2}, {1, 2}, {1, 3}})) {
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
         * to tasks (so far, pass() blocks) */
        if(verify(mode, new int[][]{{1, 3, 4}, {1, 2}, {2, 4}})) {
            final Int i = new Int();
            
            final Task<?>[] tasks = new Task<?>[2];
            for(int k = 0; k < 2; k++) {
                final int theK = k;
                i.pass();
                tasks[k] = s.run(new Runnable() {
                    public void run() {
                        i.registerNewOwner();
                        region(theK * 2);
                        i.value++;
                        i.releasePassed();
                        region(theK * 2 + 1);
                    }
                });
            }
            region(4);
            
            i.guardRead();
            assertEquals(2, i.value);
            for(final Task<?> task : tasks)
                task.get();
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
        if(verify(mode, new int[]{0, 1})) {
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
                        }
                    });
                    
                    i.guardReadWrite();
                    assertEquals(2, i.value);
                    i.value++;
                    
                    i.releasePassed();
                    task2.get();
                }
            });
            region(1);
            
            i.guardRead();
            assertEquals(3, i.value);
            
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
        if(verify(mode, new int[]{0, 1, 2})) {
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
        if(verify(mode, new int[][]{{0, 2}, {1, 2}, {1, 3}})) {
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
        if(verify(mode, new int[]{0, 1, 2})) {
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
        if(verify(mode, new int[][]{{0, 2}, {1, 2}, {1, 3}})) {
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
        if(verify(mode, new int[]{0, 1, 2})) {
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
    public void testShareSubgroup() {
        if(verify(mode, new int[][]{{0, 4}, {1, 5}})) {
            final Int i = new Int();
            final IntContainer c = new IntContainer(i);
            
            i.share();
            final Task<Void> task1 = s.run(new Runnable() {
                public void run() {
                    assertEquals(0, i.value);
                    region(0);
                    i.releaseShared();
                    region(1);
                }
            });
            
            c.share();
            final Task<Void> task2 = s.run(new Runnable() {
                public void run() {
                    assertEquals(0, c.i.value);
                    c.releaseShared();
                }
            });
            
            i.share();
            final Task<Void> task3 = s.run(new Runnable() {
                public void run() {
                    assertEquals(0, i.value);
                    i.releaseShared();
                }
            });
            region(4);
            
            i.guardReadWrite();
            i.value = 1;
            region(5);
            
            task1.get();
            task2.get();
            task3.get();
        }
    }
    
    @Test
    public void testPassSubgroup() {
        // TODO: Verify parallelism
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final Int i = new Int();
            final IntContainer c = new IntContainer(i);
            
            i.pass();
            final Task<Void> task1 = s.run(new Runnable() {
                public void run() {
                    i.registerNewOwner();
                    i.value++;
                    i.releasePassed();
                }
            });
            
            c.pass();
            final Task<Void> task2 = s.run(new Runnable() {
                public void run() {
                    c.registerNewOwner();
                    c.i.value++;
                    c.releasePassed();
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
    public void testPassSubgroupNested() {
        // TODO: Verify parallelism
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
    public void testPassShareSubgroup() {
        // TODO: Verify parallelism
        assumeVerifyCorrectness();
        if(verifyNoPropertyViolation()) {
            final Int i = new Int();
            final IntContainer c = new IntContainer(i);
            
            c.pass();
            final Task<Void> task = s.run(new Runnable() {
                public void run() {
                    c.registerNewOwner();
                    c.i.value++;
                    c.releasePassed();
                }
            });
            
            i.share();
            final Task<Void> task2 = s.run(new Runnable() {
                public void run() {
                    assertEquals(1, i.value);
                    i.releaseShared();
                }
            });
            
            i.guardReadWrite();
            i.value++;
            task.get();
            task2.get();
        }
    }
    
    @Test
    public void testPassGroupModify() {
        // TODO: Verify parallelism
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
        // TODO: Verify parallelism
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
                    
                    i2.guardReadWrite();
                    assertEquals(2, i2.value);
                    i2.value++;
                    
                    c.releasePassed();
                    task2.get();
                }
            });
            
            c.guardRead();
            c.i.guardRead();
            assertEquals(3, c.i.value);
            task.get();
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

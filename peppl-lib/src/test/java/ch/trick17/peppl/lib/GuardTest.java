package ch.trick17.peppl.lib;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import ch.trick17.simplejpf.JpfUnitTest;

@RunWith(Parameterized.class)
public class GuardTest extends JpfUnitTest {
    
    @Parameterized.Parameters(name = "{0}")
    public static List<?> taskSystems() {
        return Arrays.asList(new TaskSystem[][] {
                {new SingleThreadTaskSystem()}, {new NewThreadTaskSystem()}});
    }
    
    private final TaskSystem s;
    
    public GuardTest(final TaskSystem s) {
        this.s = s;
    }
    
    @Test
    public void testShare() {
        if(verifyNoPropertyViolation()) {
            final Int i = new Int();
            
            i.share();
            final Task<Void> task = s.run(new Runnable() {
                @Override
                public void run() {
                    assertEquals(0, i.value);
                    i.releaseShared();
                }
            });
            
            i.guardReadWrite();
            i.value = 1;
            task.get();
        }
    }
    
    @Test
    public void testShareMissingGuard() {
        if(multithreaded() && verifyAssertionError()) {
            final Int i = new Int();
            
            i.share();
            final Task<Void> task = s.run(new Runnable() {
                @Override
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
        if(verifyDeadlock()) {
            final Int i = new Int();
            
            i.share();
            final Task<Void> task = s.run(new Runnable() {
                @Override
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
        if(verifyNoPropertyViolation()) {
            final Int i = new Int();
            
            final Task<?>[] tasks = new Task<?>[3];
            for(int k = 0; k < 3; k++) {
                i.share();
                tasks[k] = s.run(new Runnable() {
                    @Override
                    public void run() {
                        assertEquals(0, i.value);
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
    public void testShareMultipleMissingRelease() {
        if(verifyDeadlock()) {
            final Int i = new Int();
            
            final Task<?>[] tasks = new Task<?>[3];
            for(int k = 0; k < 3; k++) {
                final int theI = k;
                
                i.share();
                tasks[k] = s.run(new Runnable() {
                    @Override
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
        if(verifyNoPropertyViolation()) {
            final Int i = new Int();
            
            i.pass();
            final Task<Void> task = s.run(new Runnable() {
                @Override
                public void run() {
                    i.registerNewOwner();
                    i.value++;
                    i.releasePassed();
                }
            });
            
            i.guardRead();
            assertEquals(1, i.value);
            task.get();
        }
    }
    
    @Test
    public void testPassMissingGuard() {
        if(multithreaded() && verifyAssertionError()) {
            final Int i = new Int();
            
            i.pass();
            final Task<Void> task = s.run(new Runnable() {
                @Override
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
        if(multithreaded() && verifyDeadlock()) {
            final Int i = new Int();
            
            i.pass();
            final Task<Void> task = s.run(new Runnable() {
                @Override
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
        if(verifyNoPropertyViolation()) {
            final Int i = new Int();
            
            final Task<?>[] tasks = new Task<?>[3];
            for(int k = 0; k < 3; k++) {
                i.pass();
                tasks[k] = s.run(new Runnable() {
                    @Override
                    public void run() {
                        i.registerNewOwner();
                        i.value++;
                        i.releasePassed();
                    }
                });
            }
            
            i.guardRead();
            assertEquals(3, i.value);
            for(final Task<?> task : tasks)
                task.get();
        }
    }
    
    @Test
    public void testPassMultipleMissingRelease() {
        if(multithreaded() && verifyDeadlock()) {
            final Int i = new Int();
            
            final Task<?>[] tasks = new Task<?>[3];
            for(int k = 0; k < 3; k++) {
                final int theK = k;
                i.pass();
                tasks[k] = s.run(new Runnable() {
                    @Override
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
            assertEquals(2, i.value);
            for(final Task<?> task : tasks)
                task.get();
        }
    }
    
    @Test
    public void testPassNested() {
        if(verifyNoPropertyViolation()) {
            final Int i = new Int();
            
            i.pass();
            final Task<Void> task = s.run(new Runnable() {
                @Override
                public void run() {
                    i.registerNewOwner();
                    i.value++;
                    
                    i.pass();
                    final Task<Void> task2 = s.run(new Runnable() {
                        @Override
                        public void run() {
                            i.registerNewOwner();
                            i.value++;
                            i.releasePassed();
                        }
                    });
                    
                    i.guardRead();
                    assertEquals(2, i.value);
                    
                    i.guardReadWrite(); // Not necessary here...
                    i.value++;
                    
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
    public void testPassNestedMissingRelease() {
        if(multithreaded() && verifyDeadlock()) {
            final Int i = new Int();
            
            i.pass();
            final Task<Void> task = s.run(new Runnable() {
                @Override
                public void run() {
                    i.registerNewOwner();
                    i.value++;
                    
                    i.pass();
                    final Task<Void> task2 = s.run(new Runnable() {
                        @Override
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
        if(verifyNoPropertyViolation()) {
            final Int i = new Int();
            
            i.pass();
            final Task<Void> task = s.run(new Runnable() {
                @Override
                public void run() {
                    i.registerNewOwner();
                    i.value++;
                    i.releasePassed();
                }
            });
            
            i.share();
            final Task<Void> task2 = s.run(new Runnable() {
                @Override
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
    public void testShareGroup() {
        if(verifyNoPropertyViolation()) {
            final IntContainer c = new IntContainer();
            
            c.share();
            final Task<Void> task = s.run(new Runnable() {
                @Override
                public void run() {
                    assertEquals(0, c.i.value);
                    c.releaseShared();
                }
            });
            
            c.guardRead(); // To read i
            c.i.guardReadWrite();
            c.i.value = 1;
            task.get();
        }
    }
    
    @Test
    public void testShareGroupMultiple() {
        if(verifyNoPropertyViolation()) {
            final Int i = new Int();
            final IntContainer c = new IntContainer(i);
            
            final Task<?>[] tasks = new Task<?>[3];
            for(int k = 0; k < 3; k++) {
                c.share();
                tasks[k] = s.run(new Runnable() {
                    @Override
                    public void run() {
                        assertEquals(0, c.i.value);
                        c.releaseShared();
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
    public void testPassGroup() {
        if(verifyNoPropertyViolation()) {
            final Int i = new Int();
            final IntContainer c = new IntContainer(i);
            
            c.pass();
            final Task<Void> task = s.run(new Runnable() {
                @Override
                public void run() {
                    c.registerNewOwner();
                    c.i.value++;
                    c.releasePassed();
                }
            });
            
            i.guardRead();
            assertEquals(1, i.value);
            task.get();
        }
    }
    
    @Test
    public void testPassGroupMultiple() {
        if(verifyNoPropertyViolation()) {
            final Int i = new Int();
            final IntContainer c = new IntContainer(i);
            
            final Task<?>[] tasks = new Task<?>[3];
            for(int k = 0; k < 3; k++) {
                c.pass();
                tasks[k] = s.run(new Runnable() {
                    @Override
                    public void run() {
                        c.registerNewOwner();
                        c.i.value++;
                        c.releasePassed();
                    }
                });
            }
            
            i.guardRead();
            assertEquals(3, i.value);
            for(final Task<?> task : tasks)
                task.get();
        }
    }
    
    @Test
    public void testPassGroupNested() {
        if(verifyNoPropertyViolation()) {
            final Int i = new Int();
            final IntContainer c = new IntContainer(i);
            
            c.pass();
            final Task<Void> task = s.run(new Runnable() {
                @Override
                public void run() {
                    c.registerNewOwner();
                    final Int i2 = c.i;
                    i2.value++;
                    
                    c.pass();
                    final Task<Void> task2 = s.run(new Runnable() {
                        @Override
                        public void run() {
                            c.registerNewOwner();
                            c.i.value++;
                            c.releasePassed();
                        }
                    });
                    
                    i2.guardRead();
                    assertEquals(2, i2.value);
                    
                    i2.guardReadWrite(); // Not necessary here...
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
        if(verifyNoPropertyViolation()) {
            final Int i = new Int();
            final IntContainer c = new IntContainer(i);
            
            c.pass();
            final Task<Void> task = s.run(new Runnable() {
                @Override
                public void run() {
                    c.registerNewOwner();
                    c.i.value++;
                    c.releasePassed();
                }
            });
            
            c.share();
            final Task<Void> task2 = s.run(new Runnable() {
                @Override
                public void run() {
                    assertEquals(1, c.i.value);
                    c.releaseShared();
                }
            });
            
            i.guardReadWrite();
            i.value++;
            task.get();
            task2.get();
        }
    }
    
    @Test
    public void testShareSubgroupMultiple() {
        if(verifyNoPropertyViolation()) {
            final Int i = new Int();
            final IntContainer c = new IntContainer(i);
            
            i.share();
            final Task<Void> task1 = s.run(new Runnable() {
                @Override
                public void run() {
                    assertEquals(0, i.value);
                    i.releaseShared();
                }
            });
            
            c.share();
            final Task<Void> task2 = s.run(new Runnable() {
                @Override
                public void run() {
                    assertEquals(0, c.i.value);
                    c.releaseShared();
                }
            });
            
            i.share();
            final Task<Void> task3 = s.run(new Runnable() {
                @Override
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
    public void testPassSubgroupMultiple() {
        if(verifyNoPropertyViolation()) {
            final Int i = new Int();
            final IntContainer c = new IntContainer(i);
            
            i.pass();
            final Task<Void> task1 = s.run(new Runnable() {
                @Override
                public void run() {
                    i.registerNewOwner();
                    i.value++;
                    i.releasePassed();
                }
            });
            
            c.pass();
            final Task<Void> task2 = s.run(new Runnable() {
                @Override
                public void run() {
                    c.registerNewOwner();
                    c.i.value++;
                    c.releasePassed();
                }
            });
            
            i.pass();
            final Task<Void> task3 = s.run(new Runnable() {
                @Override
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
        if(verifyNoPropertyViolation()) {
            final Int i = new Int();
            final IntContainer c = new IntContainer(i);
            
            c.pass();
            final Task<Void> task = s.run(new Runnable() {
                @Override
                public void run() {
                    c.registerNewOwner();
                    final Int i2 = c.i;
                    i2.value++;
                    
                    i2.pass();
                    final Task<Void> task2 = s.run(new Runnable() {
                        @Override
                        public void run() {
                            i2.registerNewOwner();
                            i2.value++;
                            i2.releasePassed();
                        }
                    });
                    
                    i2.guardRead();
                    assertEquals(2, i2.value);
                    
                    i2.guardReadWrite(); // Not necessary here...
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
        if(verifyNoPropertyViolation()) {
            final Int i = new Int();
            final IntContainer c = new IntContainer(i);
            
            c.pass();
            final Task<Void> task = s.run(new Runnable() {
                @Override
                public void run() {
                    c.registerNewOwner();
                    c.i.value++;
                    c.releasePassed();
                }
            });
            
            i.share();
            final Task<Void> task2 = s.run(new Runnable() {
                @Override
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
    
    private boolean multithreaded() {
        return !(s instanceof SingleThreadTaskSystem);
    }
    
    private static class Int extends PepplObject {
        int value;
    }
    
    private static class IntContainer extends PepplObject {
        Int i;
        
        public IntContainer() {
            i = new Int();
        }
        
        public IntContainer(final Int i) {
            this.i = i;
        }
    }
}

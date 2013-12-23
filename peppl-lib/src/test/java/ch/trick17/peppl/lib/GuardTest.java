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
            s.run(new Runnable() {
                @Override
                public void run() {
                    assertEquals(0, i.value);
                    i.releaseShared();
                }
            });
            
            i.guardReadWrite();
            i.value = 1;
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
            s.run(new Runnable() {
                @Override
                public void run() {
                    assertEquals(0, i.value);
                    
                    // A missing release causes a deadlock
                }
            });
            
            i.guardReadWrite();
            i.value = 1;
        }
    }
    
    @Test
    public void testShareMultiple() {
        if(verifyNoPropertyViolation()) {
            final Int i = new Int();
            
            for(int k = 0; k < 3; k++) {
                i.share();
                s.run(new Runnable() {
                    @Override
                    public void run() {
                        assertEquals(0, i.value);
                        i.releaseShared();
                    }
                });
            }
            
            i.guardReadWrite();
            i.value = 1;
        }
    }
    
    @Test
    public void testShareMultipleMissingRelease() {
        if(verifyDeadlock()) {
            final Int i = new Int();
            
            for(int k = 0; k < 3; k++) {
                final int theI = k;
                
                i.share();
                s.run(new Runnable() {
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
        }
    }
    
    @Test
    public void testPass() {
        if(verifyNoPropertyViolation()) {
            final Int i = new Int();
            
            i.pass();
            final Runnable task = new Runnable() {
                @Override
                public void run() {
                    i.registerNewOwner();
                    i.value++;
                    i.releasePassed();
                }
            };
            s.run(task);
            
            i.guardRead();
            assertEquals(1, i.value);
        }
    }
    
    @Test
    public void testPassMissingGuard() {
        if(multithreaded() && verifyAssertionError()) {
            final Int i = new Int();
            
            i.pass();
            final Runnable task = new Runnable() {
                @Override
                public void run() {
                    i.registerNewOwner();
                    i.value = 1;
                    i.releasePassed();
                }
            };
            s.run(task);
            
            // A missing guard causes non-determinism
            assertEquals(1, i.value);
        }
    }
    
    @Test
    public void testPassMissingRelease() {
        if(multithreaded() && verifyDeadlock()) {
            final Int i = new Int();
            
            i.pass();
            final Runnable task = new Runnable() {
                @Override
                public void run() {
                    i.registerNewOwner();
                    i.value = 1;
                    // A missing release causes a deadlock
                }
            };
            s.run(task);
            
            i.guardRead();
            assertEquals(1, i.value);
        }
    }
    
    @Test
    public void testPassMultiple() {
        if(verifyNoPropertyViolation()) {
            final Int i = new Int();
            
            for(int k = 0; k < 3; k++) {
                i.pass();
                s.run(new Runnable() {
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
        }
    }
    
    @Test
    public void testPassMultipleMissingRelease() {
        if(multithreaded() && verifyDeadlock()) {
            final Int i = new Int();
            
            for(int k = 0; k < 3; k++) {
                final int theK = k;
                i.pass();
                s.run(new Runnable() {
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
        }
    }
    
    @Test
    public void testPassNested() {
        if(verifyNoPropertyViolation()) {
            final Int i = new Int();
            
            i.pass();
            s.run(new Runnable() {
                @Override
                public void run() {
                    i.registerNewOwner();
                    i.value++;
                    
                    i.pass();
                    s.run(new Runnable() {
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
                }
            });
            
            i.guardRead();
            assertEquals(3, i.value);
        }
    }
    
    @Test
    public void testPassNestedMissingRelease() {
        if(multithreaded() && verifyDeadlock()) {
            final Int i = new Int();
            
            i.pass();
            s.run(new Runnable() {
                @Override
                public void run() {
                    i.registerNewOwner();
                    i.value++;
                    
                    i.pass();
                    s.run(new Runnable() {
                        @Override
                        public void run() {
                            i.registerNewOwner();
                            i.value++;
                            // A missed release causes deadlock
                        }
                    });
                    
                    i.releasePassed();
                }
            });
            
            i.guardRead();
            assertEquals(3, i.value);
        }
    }
    
    @Test
    public void testPassShare() {
        if(verifyNoPropertyViolation()) {
            final Int i = new Int();
            
            i.pass();
            s.run(new Runnable() {
                @Override
                public void run() {
                    i.registerNewOwner();
                    i.value++;
                    i.releasePassed();
                }
            });
            
            i.share();
            s.run(new Runnable() {
                @Override
                public void run() {
                    assertEquals(1, i.value);
                    i.releaseShared();
                }
            });
            
            i.guardReadWrite();
            i.value++;
        }
    }
    
    private boolean multithreaded() {
        return !(s instanceof SingleThreadTaskSystem);
    }
    
    private static class Int extends PepplObject {
        int value;
    }
}

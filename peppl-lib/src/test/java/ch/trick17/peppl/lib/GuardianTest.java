package ch.trick17.peppl.lib;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import ch.trick17.simplejpf.JpfUnitTest;

@RunWith(Parameterized.class)
public class GuardianTest extends JpfUnitTest {
    
    @Parameterized.Parameters(name = "{0}")
    public static List<?> taskSystems() {
        return Arrays.asList(new TaskSystem[][] {
                {new SingleThreadTaskSystem()}, {new NewThreadTaskSystem()}});
    }
    
    private final TaskSystem s;
    private Guardian guardian;
    
    public GuardianTest(final TaskSystem s) {
        this.s = s;
    }
    
    @Before
    public void setUp() {
        guardian = new Guardian();
    }
    
    @Test
    public void testShare() {
        if(verifyNoPropertyViolation()) {
            final Container c = new Container();
            
            guardian.share(c);
            s.run(new Runnable() {
                @Override
                public void run() {
                    assertEquals(0, c.value);
                    guardian.releaseShared(c);
                }
            });
            
            guardian.guardReadWrite(c);
            c.value = 1;
        }
    }
    
    @Test
    public void testShareMissingGuard() {
        if(multithreaded() && verifyAssertionError()) {
            final Container c = new Container();
            
            guardian.share(c);
            final Task<Void> task = s.run(new Runnable() {
                @Override
                public void run() {
                    assertEquals(0, c.value);
                    guardian.releaseShared(c);
                }
            });
            
            // A missing guard causes non-determinism:
            // guardian.guardReadWrite(c);
            c.value = 1;
            task.get();
        }
    }
    
    @Test
    public void testShareMissingRelease() {
        if(verifyDeadlock()) {
            final Container c = new Container();
            
            guardian.share(c);
            s.run(new Runnable() {
                @Override
                public void run() {
                    assertEquals(0, c.value);
                    
                    // A missing release causes a deadlock:
                    // guardian.releaseShared(c);
                }
            });
            
            guardian.guardReadWrite(c);
            c.value = 1;
        }
    }
    
    @Test
    public void testShareMultiple() {
        if(verifyNoPropertyViolation()) {
            final Container c = new Container();
            
            for(int i = 0; i < 3; i++) {
                guardian.share(c);
                s.run(new Runnable() {
                    @Override
                    public void run() {
                        assertEquals(0, c.value);
                        guardian.releaseShared(c);
                    }
                });
            }
            
            guardian.guardReadWrite(c);
            c.value = 1;
        }
    }
    
    @Test
    public void testShareMultipleMissingRelease() {
        if(verifyDeadlock()) {
            final Container c = new Container();
            
            for(int i = 0; i < 3; i++) {
                final int theI = i;
                
                guardian.share(c);
                s.run(new Runnable() {
                    @Override
                    public void run() {
                        assertEquals(0, c.value);
                        
                        // A single missing release causes a deadlock:
                        if(theI != 0)
                            guardian.releaseShared(c);
                    }
                });
            }
            
            guardian.guardReadWrite(c);
            c.value = 1;
        }
    }
    
    @Test
    public void testPass() {
        if(verifyNoPropertyViolation()) {
            final Container c = new Container();
            
            guardian.pass(c);
            final Runnable task = new Runnable() {
                @Override
                public void run() {
                    guardian.registerNewOwner(c);
                    c.value++;
                    guardian.releasePassed(c);
                }
            };
            s.run(task);
            
            guardian.guardRead(c);
            assertEquals(1, c.value);
        }
    }
    
    @Test
    public void testPassMissingGuard() {
        if(multithreaded() && verifyAssertionError()) {
            final Container c = new Container();
            
            guardian.pass(c);
            final Runnable task = new Runnable() {
                @Override
                public void run() {
                    guardian.registerNewOwner(c);
                    c.value = 1;
                    guardian.releasePassed(c);
                }
            };
            s.run(task);
            
            // A missing guard causes non-determinism:
            // guardian.guardRead(c);
            assertEquals(1, c.value);
        }
    }
    
    @Test
    public void testPassMissingRelease() {
        if(multithreaded() && verifyDeadlock()) {
            final Container c = new Container();
            
            guardian.pass(c);
            final Runnable task = new Runnable() {
                @Override
                public void run() {
                    guardian.registerNewOwner(c);
                    c.value = 1;
                    // A missing release causes a deadlock:
                    // guardian.releasePassed(c);
                }
            };
            s.run(task);
            
            guardian.guardRead(c);
            assertEquals(1, c.value);
        }
    }
    
    @Test
    public void testPassMultiple() {
        if(verifyNoPropertyViolation()) {
            final Container c = new Container();
            
            for(int i = 0; i < 3; i++) {
                guardian.pass(c);
                s.run(new Runnable() {
                    @Override
                    public void run() {
                        guardian.registerNewOwner(c);
                        c.value++;
                        guardian.releasePassed(c);
                    }
                });
            }
            
            guardian.guardRead(c);
            assertEquals(3, c.value);
        }
    }
    
    @Test
    public void testPassMultipleMissingRelease() {
        if(multithreaded() && verifyDeadlock()) {
            final Container c = new Container();
            
            for(int i = 0; i < 3; i++) {
                final int theI = i;
                guardian.pass(c);
                s.run(new Runnable() {
                    @Override
                    public void run() {
                        guardian.registerNewOwner(c);
                        c.value++;
                        
                        // A single missing release causes a deadlock:
                        if(theI != 0)
                            guardian.releasePassed(c);
                    }
                });
            }
            
            guardian.guardRead(c);
            assertEquals(2, c.value);
        }
    }
    
    @Test
    public void testPassNested() {
        if(verifyNoPropertyViolation()) {
            final Container c = new Container();
            
            guardian.pass(c);
            s.run(new Runnable() {
                @Override
                public void run() {
                    guardian.registerNewOwner(c);
                    c.value++;
                    
                    guardian.pass(c);
                    s.run(new Runnable() {
                        @Override
                        public void run() {
                            guardian.registerNewOwner(c);
                            c.value++;
                            guardian.releasePassed(c);
                        }
                    });
                    
                    guardian.guardRead(c);
                    assertEquals(2, c.value);
                    
                    guardian.guardReadWrite(c); // Not necessary here...
                    c.value++;
                    
                    guardian.releasePassed(c);
                }
            });
            
            guardian.guardRead(c);
            assertEquals(3, c.value);
        }
    }
    
    @Test
    public void testPassNestedMissingRelease() {
        if(multithreaded() && verifyDeadlock()) {
            final Container c = new Container();
            
            guardian.pass(c);
            s.run(new Runnable() {
                @Override
                public void run() {
                    guardian.registerNewOwner(c);
                    c.value++;
                    
                    guardian.pass(c);
                    s.run(new Runnable() {
                        @Override
                        public void run() {
                            guardian.registerNewOwner(c);
                            c.value++;
                            // A missed release causes deadlock:
                            // guardian.releasePassed(c);
                        }
                    });
                    
                    guardian.releasePassed(c);
                }
            });
            
            guardian.guardRead(c);
            assertEquals(3, c.value);
        }
    }
    
    @Test
    public void testPassShare() {
        if(verifyNoPropertyViolation()) {
            final Container c = new Container();
            
            guardian.pass(c);
            s.run(new Runnable() {
                @Override
                public void run() {
                    guardian.registerNewOwner(c);
                    c.value++;
                    guardian.releasePassed(c);
                }
            });
            
            guardian.share(c);
            s.run(new Runnable() {
                @Override
                public void run() {
                    assertEquals(1, c.value);
                    guardian.releaseShared(c);
                }
            });
            
            guardian.guardReadWrite(c);
            c.value++;
        }
    }
    
    private boolean multithreaded() {
        return !(s instanceof SingleThreadTaskSystem);
    }
    
    private static class Container extends PepplObject {
        int value;
    }
}

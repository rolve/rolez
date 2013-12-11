package ch.trick17.peppl.lib;

import java.util.concurrent.Callable;

import org.junit.Test;

import ch.trick17.peppl.lib.TaskSystem.Task;
import ch.trick17.simplejpf.JpfUnitTest;

public class GuardianTest extends JpfUnitTest {
    
    @Test
    public void testGuardReadWrite() {
        if(verifyNoPropertyViolation(args)) {
            final Guardian guardian = new Guardian();
            
            final Container c = new Container();
            
            guardian.share(c);
            start(new Runnable() {
                
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
    public void testGuardReadWriteFail() {
        if(verifyAssertionError(args)) {
            final Guardian guardian = new Guardian();
            
            final Container c = new Container();
            
            guardian.share(c);
            start(new Runnable() {
                
                @Override
                public void run() {
                    assertEquals(0, c.value);
                    guardian.releaseShared(c);
                }
            });
            
            // A missed guard causes non-determinism:
            // guardian.guardReadWrite(c);
            c.value = 1;
        }
    }
    
    @Test
    public void testGuardReadWriteDeadlock() {
        if(verifyDeadlock(args)) {
            final Guardian guardian = new Guardian();
            
            final Container c = new Container();
            
            guardian.share(c);
            start(new Runnable() {
                
                @Override
                public void run() {
                    assertEquals(0, c.value);
                    
                    // A missed release causes a deadlock:
                    // guardian.releaseShared(c);
                }
            });
            
            guardian.guardReadWrite(c);
            c.value = 1;
        }
    }
    
    @Test
    public void testGuardReadWriteMultiple() {
        if(verifyNoPropertyViolation(args)) {
            final Guardian guardian = new Guardian();
            
            final Container c = new Container();
            
            for(int i = 0; i < 3; i++) {
                guardian.share(c);
                start(new Runnable() {
                    
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
    public void testGuardReadWriteMultipleDeadlock() {
        if(verifyDeadlock(args)) {
            final Guardian guardian = new Guardian();
            
            final Container c = new Container();
            
            for(int i = 0; i < 3; i++) {
                final int theI = i;
                
                guardian.share(c);
                start(new Runnable() {
                    
                    @Override
                    public void run() {
                        assertEquals(0, c.value);
                        
                        // A single missed release causes a deadlock:
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
    public void testGuardReadWriteException() {
        if(verifyUnhandledException("java.lang.RuntimeException", args)) {
            final Guardian guardian = new Guardian();
            
            final Container c = new Container();
            
            guardian.share(c);
            final Task<Void> task = TaskSystem.get().runTask(
                    new Callable<Void>() {
                        @Override
                        public Void call() {
                            try {
                                doBadStuff();
                            } finally {
                                // Release needs to be in finally-block!
                                guardian.releaseShared(c);
                            }
                            return null;
                        }
                        
                        private void doBadStuff() {
                            throw new RuntimeException();
                        }
                    });
            
            guardian.guardReadWrite(c);
            c.value = 1;
            
            // Propagate thrown exception to original task (thread)
            task.get();
        }
    }
    
    @Test
    public void testGuardRead() {
        if(verifyNoPropertyViolation(args)) {
            final Guardian guardian = new Guardian();
            
            final Container c = new Container();
            
            guardian.pass(c);
            start(new Runnable() {
                
                @Override
                public void run() {
                    guardian.registerNewOwner(c);
                    c.value = 1;
                    guardian.releasePassed(c);
                }
            });
            
            guardian.guardRead(c);
            assertEquals(1, c.value);
        }
    }
    
    @Test
    public void testGuardReadFail() {
        if(verifyAssertionError(args)) {
            final Guardian guardian = new Guardian();
            
            final Container c = new Container();
            
            guardian.pass(c);
            start(new Runnable() {
                
                @Override
                public void run() {
                    guardian.registerNewOwner(c);
                    c.value = 1;
                    guardian.releasePassed(c);
                }
            });
            
            // A missed guard causes non-determinism:
            // guardian.guardRead(c);
            assertEquals(1, c.value);
        }
    }
    
    @Test
    public void testGuardReadDeadlock() {
        if(verifyDeadlock(args)) {
            final Guardian guardian = new Guardian();
            
            final Container c = new Container();
            
            guardian.pass(c);
            start(new Runnable() {
                
                @Override
                public void run() {
                    guardian.registerNewOwner(c);
                    c.value = 1;
                    
                    // A missed release causes a deadlock:
                    // guardian.releasePassed(c);
                }
            });
            
            guardian.guardRead(c);
            assertEquals(1, c.value);
        }
    }
    
    @Test
    public void testGuardReadMultiple() {
        if(verifyNoPropertyViolation(args)) {
            final Guardian guardian = new Guardian();
            
            final Container c = new Container();
            
            guardian.pass(c);
            start(new Runnable() {
                
                @Override
                public void run() {
                    guardian.registerNewOwner(c);
                    c.value++;
                    
                    guardian.pass(c);
                    start(new Runnable() {
                        
                        @Override
                        public void run() {
                            guardian.registerNewOwner(c);
                            c.value++;
                            guardian.releasePassed(c);
                        }
                    });
                    
                    guardian.guardRead(c);
                    assertEquals(2, c.value);
                    
                    guardian.guardReadWrite(c); // Not necessary in this case
                    c.value++;
                    
                    guardian.releasePassed(c);
                }
            });
            
            guardian.guardRead(c);
            assertEquals(3, c.value);
        }
    }
    
    @Test
    public void testGuardReadMultipleDeadlock() {
        if(verifyDeadlock(args)) {
            final Guardian guardian = new Guardian();
            
            final Container c = new Container();
            
            guardian.pass(c);
            start(new Runnable() {
                
                @Override
                public void run() {
                    guardian.registerNewOwner(c);
                    c.value++;
                    
                    guardian.pass(c);
                    start(new Runnable() {
                        
                        @Override
                        public void run() {
                            guardian.registerNewOwner(c);
                            c.value++;
                            // A missed release causes deadlock:
                            // guardian.releasePassed(c);
                        }
                    });
                    
                    // Explicit guard before release!
                    guardian.guardReadWrite(c);
                    guardian.releasePassed(c);
                }
            });
            
            guardian.guardRead(c);
            assertEquals(3, c.value);
        }
    }
    
    private static void start(final Runnable task) {
        new Thread(task).start();
    }
    
    private static class Container {
        int value = 0;
    }
}

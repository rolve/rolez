package ch.trick17.peppl.lib;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.junit.Test;

import ch.trick17.simplejpf.JpfUnitTest;

public class GuardianTest extends JpfUnitTest {
    
    @Test
    public void testGuardReadWrite() {
        if(verifyNoPropertyViolation()) {
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
    public void testGuardReadWriteFail() throws Throwable {
        if(verifyAssertionError()) {
            final Guardian guardian = new Guardian();
            
            final Container c = new Container();
            
            guardian.share(c);
            final Future<?> result = start(new Runnable() {
                @Override
                public void run() {
                    assertEquals(0, c.value);
                    guardian.releaseShared(c);
                }
            });
            
            // A missed guard causes non-determinism:
            // guardian.guardReadWrite(c);
            c.value = 1;
            try {
                result.get();
            } catch(final ExecutionException e) {
                throw e.getCause();
            }
        }
    }
    
    @Test
    public void testGuardReadWriteDeadlock() {
        if(verifyDeadlock()) {
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
        if(verifyNoPropertyViolation()) {
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
        if(verifyDeadlock()) {
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
    public void testGuardReadWriteException() throws Throwable {
        if(verifyUnhandledException("java.lang.RuntimeException", "Hello")) {
            final Guardian guardian = new Guardian();
            
            final Container c = new Container();
            
            guardian.share(c);
            final Future<?> result = start(new Runnable() {
                @Override
                public void run() {
                    try {
                        doBadStuff();
                    } finally {
                        // Release needs to be in finally-block!
                        guardian.releaseShared(c);
                    }
                }
                
                private void doBadStuff() {
                    throw new RuntimeException("Hello");
                }
            });
            
            guardian.guardReadWrite(c);
            c.value = 1;
            
            // Propagate thrown exception to original task (thread)
            try {
                result.get();
            } catch(final ExecutionException e) {
                throw e.getCause();
            }
        }
    }
    
    @Test
    public void testGuardRead() {
        if(verifyNoPropertyViolation()) {
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
        if(verifyAssertionError()) {
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
        if(verifyDeadlock()) {
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
        if(verifyNoPropertyViolation()) {
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
        if(verifyDeadlock()) {
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
    
    @Test
    public void testPassMultiple() {
        if(verifyNoPropertyViolation()) {
            final Guardian guardian = new Guardian();
            
            final Container c = new Container();
            
            guardian.pass(c);
            start(new Runnable() {
                @Override
                public void run() {
                    guardian.registerNewOwner(c);
                    c.value++;
                    guardian.releasePassed(c);
                }
            });
            
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
        }
    }
    
    @Test
    public void testShareMultiple() {
        if(verifyNoPropertyViolation()) {
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
            
            guardian.share(c);
            start(new Runnable() {
                @Override
                public void run() {
                    assertEquals(0, c.value);
                    guardian.releaseShared(c);
                }
            });
            
            guardian.guardReadWrite(c);
            c.value++;
        }
    }
    
    @Test
    public void testPassShare() {
        if(verifyNoPropertyViolation()) {
            final Guardian guardian = new Guardian();
            
            final Container c = new Container();
            
            guardian.pass(c);
            start(new Runnable() {
                @Override
                public void run() {
                    guardian.registerNewOwner(c);
                    c.value++;
                    guardian.releasePassed(c);
                }
            });
            
            guardian.share(c);
            start(new Runnable() {
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
    
    private static Future<?> start(final Runnable task) {
        final FutureTask<?> result = new FutureTask<>(task, null);
        new Thread(result).start();
        return result;
    }
    
    private static class Container extends PepplObject {
        int value = 0;
    }
}

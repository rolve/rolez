package ch.trick17.peppl.lib;

import org.junit.Test;

public class GuardianTest extends JpfUnitTest {
    
    @Test
    public void testGuardReadWrite() {
        if(verifyNoPropertyViolation(args)) {
            final Guardian guardian = new Guardian();
            
            final Container c = new Container();
            
            guardian.share(c);
            run(new Runnable() {
                
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
    
    @Test(expected = AssertionError.class)
    public void testGuardReadWriteFail() {
        if(verifyNoPropertyViolation(args)) {
            final Guardian guardian = new Guardian();
            
            final Container c = new Container();
            
            guardian.share(c);
            run(new Runnable() {
                
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
    
    @Test(expected = AssertionError.class)
    public void testGuardReadWriteDeadlock() {
        if(verifyNoPropertyViolation(args)) {
            final Guardian guardian = new Guardian();
            
            final Container c = new Container();
            
            guardian.share(c);
            run(new Runnable() {
                
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
                run(new Runnable() {
                    
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
    
    @Test(expected = AssertionError.class)
    public void testGuardReadWriteMultipleDeadlock() {
        if(verifyNoPropertyViolation(args)) {
            final Guardian guardian = new Guardian();
            
            final Container c = new Container();
            
            for(int i = 0; i < 3; i++) {
                final int theI = i;
                
                guardian.share(c);
                run(new Runnable() {
                    
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
    public void testGuardRead() {
        if(verifyNoPropertyViolation(args)) {
            final Guardian guardian = new Guardian();
            
            final Container c = new Container();
            
            guardian.pass(c);
            run(new Runnable() {
                
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
    
    @Test(expected = AssertionError.class)
    public void testGuardReadFail() {
        if(verifyNoPropertyViolation(args)) {
            final Guardian guardian = new Guardian();
            
            final Container c = new Container();
            
            guardian.pass(c);
            run(new Runnable() {
                
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
    
    @Test(expected = AssertionError.class)
    public void testGuardReadDeadlock() {
        if(verifyNoPropertyViolation(args)) {
            final Guardian guardian = new Guardian();
            
            final Container c = new Container();
            
            guardian.pass(c);
            run(new Runnable() {
                
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
            run(new Runnable() {
                
                @Override
                public void run() {
                    guardian.registerNewOwner(c);
                    c.value++;
                    
                    guardian.pass(c);
                    GuardianTest.run(new Runnable() {
                        
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
    
    @Test(expected = AssertionError.class)
    public void testGuardReadMultipleDeadlock() {
        if(verifyNoPropertyViolation(args)) {
            final Guardian guardian = new Guardian();
            
            final Container c = new Container();
            
            guardian.pass(c);
            run(new Runnable() {
                
                @Override
                public void run() {
                    guardian.registerNewOwner(c);
                    c.value++;
                    
                    guardian.pass(c);
                    GuardianTest.run(new Runnable() {
                        
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
    
    private static void run(final Runnable task) {
        new Thread(task).start();
    }
    
    private static class Container {
        
        int value = 0;
    }
}

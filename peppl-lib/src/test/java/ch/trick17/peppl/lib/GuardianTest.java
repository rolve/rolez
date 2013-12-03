package ch.trick17.peppl.lib;

import org.junit.Test;

public class GuardianTest extends JpfUnitTest {
    
    @Test
    public void testGuardReadWrite() {
        if(verifyNoPropertyViolation(args)) {
            final Guardian guardian = new Guardian(2);
            
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
            final Guardian guardian = new Guardian(3);
            
            final Container c = new Container();
            
            guardian.share(c);
            
            run(new Runnable() {
                
                @Override
                public void run() {
                    assertEquals(0, c.value);
                    guardian.releaseShared(c);
                }
            });
            
            // guardian.guardReadWrite(c);
            c.value = 1;
        }
    }
    
    private static void run(final Runnable task) {
        new Thread(task).start();
    }
    
    private static class Container {
        
        int value = 0;
    }
}

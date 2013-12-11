package ch.trick17.simplejpf;

import org.junit.Test;

/**
 * Tests the JPF setup for unit tests.
 * 
 * @author Michael Faes
 */
public class JpfUnitTestTest extends JpfUnitTest {
    
    @Test
    public void testAssertPass() {
        if(verifyNoPropertyViolation(args)) {
            assertTrue(true);
        }
    }
    
    @Test(expected = AssertionError.class)
    public void testAssertFail() {
        if(verifyNoPropertyViolation(args)) {
            assertTrue(false);
        }
    }
    
    @Test(expected = AssertionError.class)
    public void testAssertFailOtherThread() {
        if(verifyNoPropertyViolation(args)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    assertTrue(false);
                }
            }).start();
        }
    }
}

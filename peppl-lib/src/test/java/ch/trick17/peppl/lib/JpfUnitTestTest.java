package ch.trick17.peppl.lib;

import org.junit.Test;

/**
 * Tests the JPF setup for unit tests.
 * 
 * @author Michael Faes
 */
public class JpfUnitTestTest extends JpfUnitTest {
    
    @Test
    public void testPass() {
        if(verifyNoPropertyViolation(args)) {
            assertTrue(true);
        }
    }
    
    @Test(expected = AssertionError.class)
    public void testFail() {
        if(verifyNoPropertyViolation(args)) {
            assertTrue(false);
        }
    }
}

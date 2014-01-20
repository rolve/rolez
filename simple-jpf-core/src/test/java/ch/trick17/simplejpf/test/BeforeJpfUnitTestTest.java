package ch.trick17.simplejpf.test;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import ch.trick17.simplejpf.test.JpfTest;

public class BeforeJpfUnitTestTest extends JpfTest {
    
    private boolean ready = false;
    
    @Before
    public void setUp() {
        ready = true;
    }
    
    @Test
    public void testBefore() {
        if(verifyNoPropertyViolation()) {
            assertTrue(ready);
        }
    }
}

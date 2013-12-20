package ch.trick17.simplejpf;

import static org.junit.Assert.assertEquals;
import gov.nasa.jpf.vm.NoOutOfMemoryErrorProperty;

import java.util.ArrayList;
import java.util.concurrent.locks.LockSupport;

import org.junit.Test;

public class JpfUnitTestTest extends JpfUnitTest {
    
    @Test
    public void testAssertionError() {
        if(verifyAssertionError()) {
            assertEquals(3, 1 + 1);
        }
    }
    
    @Test
    public void testAssertionErrorDetails() {
        if(verifyAssertionError("expected:<3> but was:<2>")) {
            assertEquals(3, 1 + 1);
        }
    }
    
    @Test
    public void testDeadlock() {
        if(verifyDeadlock()) {
            LockSupport.park();
        }
    }
    
    @Test
    public void testNoPropertyViolation() {
        if(verifyNoPropertyViolation()) {
            assertEquals(2, 1 + 1);
        }
    }
    
    @Test
    public void testPropertyViolation() {
        if(verifyPropertyViolation(NoOutOfMemoryErrorProperty.class)) {
            final ArrayList<Object> list = new ArrayList<>();
            while(true)
                list.add(new int[1000000000]);
        }
    }
    
    @Test
    public void testUnhandledException() {
        if(verifyUnhandledException(RuntimeException.class.getName())) {
            throw new RuntimeException("Hello");
        }
    }
    
    @Test
    public void testUnhandledExceptionDetails() {
        if(verifyUnhandledException(RuntimeException.class.getName(), "Hello")) {
            throw new RuntimeException("Hello");
        }
    }
}

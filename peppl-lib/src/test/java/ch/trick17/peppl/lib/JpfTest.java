package ch.trick17.peppl.lib;

import gov.nasa.jpf.util.test.TestJPF;

import org.junit.Test;

public class JpfTest extends TestJPF {
    
    @Test
    public void test() {
        if(verifyNoPropertyViolation()) {
            System.out.println("Works?");
        }
    }
}

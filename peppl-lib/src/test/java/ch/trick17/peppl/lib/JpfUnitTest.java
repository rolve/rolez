package ch.trick17.peppl.lib;

import gov.nasa.jpf.util.test.TestJPF;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;

public class JpfUnitTest extends TestJPF {
    
    private static String[] args;
    
    @BeforeClass
    public static void setup() throws IOException {
        final Properties props = new Properties();
        props.load(JpfUnitTest.class.getResourceAsStream("jpf.properties"));
        props.setProperty("classpath", "lib/jpf-classes.jar"
                + File.pathSeparator + "target/test-classes");
        
        final ArrayList<String> argList = new ArrayList<>();
        for(final Entry<Object, Object> entry : props.entrySet())
            argList.add("+" + entry.getKey() + "=" + entry.getValue());
        
        args = argList.toArray(new String[argList.size()]);
    }
    
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

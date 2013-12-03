package ch.trick17.peppl.lib;

import gov.nasa.jpf.util.test.TestJPF;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Properties;

import org.junit.BeforeClass;

/**
 * Base class for unit tests that are run within JPF. Extends {@link TestJPF}
 * and sets up JPF in a (relatively) portable way.
 * <p>
 * Tests should be written like this:
 * 
 * <pre>
 * public void myTest() {
 *     if(verifyNoPropertyViolation(args)) {
 *         ...
 *     }
 * }
 * </pre>
 * 
 * @author Michael Faes
 */
public abstract class JpfUnitTest extends TestJPF {
    
    protected static String[] args;
    
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
}

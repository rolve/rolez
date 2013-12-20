package ch.trick17.simplejpf;

import static org.junit.Assert.fail;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.Error;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.Property;
import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.ExceptionInfo;
import gov.nasa.jpf.vm.NoUncaughtExceptionsProperty;
import gov.nasa.jpf.vm.NotDeadlockedProperty;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import javax.xml.bind.DatatypeConverter;

import org.junit.After;
import org.junit.Before;
import org.junit.runners.Parameterized;

/**
 * Base class for unit tests that are executed in {@link JPF}. Automatically
 * sets up JPF in a (relatively) portable way.
 * <p>
 * To be executed in JPF, tests should be written like this:
 * 
 * <pre>
 * public void myTest() {
 *     if(verifySomething()) { // e.g. verifyNoPropertyViolation()
 *         // actual test code
 *     }
 * }
 * </pre>
 * 
 * In contrast to {@link TestJPF}, this implementation fully supports
 * {@link Before} and {@link After} methods and, in addition,
 * {@link Parameterized} tests. This is achieved by fully serializing each test
 * instance and passing it to JPF for execution. The {@link TestJPF} class, in
 * contrast, creates a new test instance within JPF, which is oblivious to the
 * state of the original instance.
 * <p>
 * The consequence of this design is that tests must be {@link Serializable}.
 * 
 * @author Michael Faes
 */
public class JpfUnitTest implements Serializable {
    
    private static final Properties generalConfig;
    
    static {
        if(TestJPF.isJPFRun())
            generalConfig = null;
        else
            try {
                generalConfig = new Properties();
                generalConfig.load(JpfUnitTest.class
                        .getResourceAsStream("jpf.properties"));
                generalConfig.setProperty("classpath", System
                        .getProperty("java.class.path"));
            } catch(final IOException e) {
                throw new RuntimeException(e);
            }
    }
    
    /*
     * Verify methods. These are more or less copied from the TestJPF class
     */
    
    protected boolean verifyAssertionError() {
        return verifyUnhandledException("java.lang.AssertionError", null);
    }
    
    protected boolean verifyAssertionError(final String details) {
        return verifyUnhandledException("java.lang.AssertionError", details);
    }
    
    protected boolean verifyUnhandledException(final String className) {
        return verifyUnhandledException(className, null);
    }
    
    protected boolean verifyUnhandledException(final String className,
            final String details) {
        if(TestJPF.isJPFRun())
            return true;
        else {
            final Error error = runJpf().getLastError();
            if(error != null) {
                final Property errorProperty = error.getProperty();
                if(errorProperty instanceof NoUncaughtExceptionsProperty) {
                    final ExceptionInfo xi = ((NoUncaughtExceptionsProperty) errorProperty)
                            .getUncaughtExceptionInfo();
                    final String xn = xi.getExceptionClassname();
                    if(!xn.equals(className))
                        fail("JPF caught wrong exception: " + xn
                                + ", expected: " + className);
                    else if(details != null) {
                        final String gotDetails = xi.getDetails();
                        if(gotDetails == null)
                            fail("JPF caught the right exception but no details, expected: "
                                    + details);
                        else if(!gotDetails.endsWith(details))
                            fail("JPF caught the right exception but the details were wrong: "
                                    + gotDetails + ", expected: " + details);
                        // else: everything fine
                    }
                }
                else {
                    fail("JPF failed to catch exception, expected " + className);
                }
            }
            else {
                fail("JPF failed to catch exception, expected " + className);
            }
            return false;
        }
    }
    
    protected boolean verifyNoPropertyViolation() {
        if(TestJPF.isJPFRun())
            return true;
        else {
            final Error error = runJpf().getLastError();
            if(error != null)
                fail("JPF found unexpected errors: " + error.getDescription());
            return false;
        }
    }
    
    protected boolean verifyPropertyViolation(
            final Class<? extends Property> propertyCls) {
        if(TestJPF.isJPFRun())
            return true;
        else {
            final List<Error> errors = runJpf().getSearchErrors();
            if(errors != null)
                for(final Error e : errors)
                    if(propertyCls == e.getProperty().getClass())
                        return false;
            
            fail("JPF failed to detect error: " + propertyCls.getName());
            return false;
        }
    }
    
    protected boolean verifyDeadlock() {
        return verifyPropertyViolation(NotDeadlockedProperty.class);
    }
    
    /*
     * Implementation - JUnit part
     */
    
    private JPF runJpf() {
        final String serializedTest = serialize(this);
        final String methodName = getCaller();
        
        final Config config = new Config(propsToArgs(generalConfig));
        config.setTarget(JpfUnitTest.class.getName());
        config.setTargetEntry("runTest([Ljava/lang/String;)V");
        config.setTargetArgs(new String[] {serializedTest, methodName});
        
        final JPF jpf = new JPF(config);
        jpf.run();
        return jpf;
    }
    
    private static String getCaller() {
        final StackTraceElement[] trace = (new Throwable()).getStackTrace();
        for(final StackTraceElement e : trace)
            if(!e.getClassName().equals(JpfUnitTest.class.getName()))
                return e.getMethodName();
        throw new AssertionError("method not found");
    }
    
    private static String serialize(final Object o) {
        try {
            final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            new ObjectOutputStream(bytes).writeObject(o);
            return DatatypeConverter.printBase64Binary(bytes.toByteArray());
        } catch(final IOException e) {
            throw new AssertionError(e);
        }
    }
    
    private static String[] propsToArgs(final Properties props) {
        final ArrayList<String> args = new ArrayList<>();
        for(final Entry<Object, Object> entry : props.entrySet())
            args.add("+" + entry.getKey() + "=" + entry.getValue());
        return args.toArray(new String[args.size()]);
    }
    
    /*
     * Implementation - JPF part
     */
    
    /**
     * This is the entry point of the JPF execution. Deserializes the given test
     * instance and invokes the given method.
     * 
     * @param args
     *            The arguments for the test execution. The first argument is
     *            expected to be the serialized test instance and the second
     *            argument the name of the test method.
     * @throws Throwable
     *             Anything can happen...
     */
    static void runTest(final String args[]) throws Throwable {
        final String serializedTest = args[0];
        final String methodName = args[1];
        
        final Object testInstance = deserialize(serializedTest);
        final Class<?> testCls = testInstance.getClass();
        final Method method = testCls.getMethod(methodName);
        
        try {
            method.invoke(testInstance);
        } catch(final InvocationTargetException e) {
            throw e.getCause();
        }
    }
    
    private static Object deserialize(final String s) {
        try {
            final byte[] bytes = DatatypeConverter.parseBase64Binary(s);
            return new ObjectInputStream(new ByteArrayInputStream(bytes))
                    .readObject();
        } catch(final IOException | ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }
}

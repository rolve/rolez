package rolez.lang;

import static ch.trick17.simplejpf.test.JpfParallelismTest.VerifyMode.CORRECTNESS;
import static ch.trick17.simplejpf.test.JpfParallelismTest.VerifyMode.PARALLELISM;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static rolez.checked.lang.Checked.checkLegalWrite;
import static rolez.checked.lang.Checked.checkLegalRead;

import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import rolez.checked.lang.Role;
import rolez.lang.SomeCheckedClasses.*;

@RunWith(Parameterized.class)
public class ObjectCheckingTest extends TaskBasedJpfTest {

	@Parameters(name = "{0}, {1}")
    public static List<?> taskSystems() {
        return Arrays.asList(new Object[][]{
                {new NewThreadTaskSystem(), CORRECTNESS},
                {new ThreadPoolTaskSystem(), CORRECTNESS},
                {new SingleThreadTaskSystem(), CORRECTNESS},
                {new ThreadPoolTaskSystem(3), PARALLELISM}
        });
    }
    
    public ObjectCheckingTest(TaskSystem s, VerifyMode mode) {
        super(s, mode);
    }
    
    @Test
    public void testShare() {
        verifyTask(new int[][]{{2, 3}, {0, 3}}, new Runnable() {
            public void run() {
            	final A a = new A();
                
                Task<?> task = new Task<Void>(new Object[]{}, new Object[]{a}) {
                    @Override
                    protected Void runRolez() {
                        region(0);
                        assertEquals(0, a.value);
                        return null;
                    }
                };
                
                s.start(task);
                region(2);
                
                checkLegalWrite(a, Role.READWRITE).value = 1;
                region(3);
            }
        });
    }

    @Test
    public void testPass() {
    	verifyTask(new int[][]{{2, 3}, {0, 3}}, new Runnable() {
            public void run() {
            	final A a = new A();
                
                Task<?> task = new Task<Void>(new Object[]{a}, new Object[]{}) {
                    @Override
                    protected Void runRolez() {
                        region(0);
                        a.value = 1;
                        return null;
                    }
                };
                
                s.start(task);
                region(2);
                
                assertEquals(1,checkLegalRead(a, Role.READWRITE).value);
                region(3);
            }
        });
    }
    
    @Test
    public void testRef() {
        verifyTask(new int[][]{{2, 3}, {0, 3}}, new Runnable() {
            public void run() {
            	final A a = new A();
            	final B b = new B(a);
                
                Task<?> task = new Task<Void>(new Object[]{b}, new Object[]{}) {
                    @Override
                    protected Void runRolez() {
                        region(0);
                        b.a = new A();
                        return null;
                    }
                };
                
                s.start(task);
                region(2);
                
                assertNotEquals(a,checkLegalRead(b, Role.READWRITE).a);
                region(3);
            }
        });
    }
    
    
    // TODO: Find a way to test for the specific exception that will be thrown. 
    //		 This is not clean since the assertion error could be thrown by a 
    //		 real bug in the code...
    @Test(expected = AssertionError.class)
    public void testRuntimeException() {
        assumeVerifyCorrectness();
    	verifyTask(new Runnable() {
    		public void run() {
            	A a = new A();
                checkLegalWrite(a, Role.READONLY).value = 1;
            }
        });
    }
}

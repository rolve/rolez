package rolez.checked.lang;

import static ch.trick17.simplejpf.test.JpfParallelismTest.VerifyMode.CORRECTNESS;
import static ch.trick17.simplejpf.test.JpfParallelismTest.VerifyMode.PARALLELISM;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static rolez.checked.lang.Checked.checkLegalRead;
import static rolez.checked.lang.Checked.checkLegalWrite;
import static rolez.checked.lang.Guarded.guardReadOnly;
import static rolez.checked.lang.Guarded.guardReadWrite;

import java.util.Arrays;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import rolez.checked.lang.NewThreadTaskSystem;
import rolez.checked.lang.SingleThreadTaskSystem;
import rolez.checked.lang.Task;
import rolez.checked.lang.TaskSystem;
import rolez.checked.lang.ThreadPoolTaskSystem;
import rolez.checked.lang.SomeCheckedClasses.*;

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
                
                Task<?> task = new Task<Void>(new Object[]{}, new Object[]{a}, new Object[]{}) {
                    @Override
                    protected Void runRolez() {
                        region(0);
                        assertEquals(0, a.value);
                        return null;
                    }
                };
                
                s.start(task);
                region(2);
                
                checkLegalWrite(a).value = 1;
                region(3);
            }
        });
    }

    @Test
    public void testPass() {
    	verifyTask(new int[][]{{2, 3}, {0, 3}}, new Runnable() {
            public void run() {
            	final A a = new A();
                
                Task<?> task = new Task<Void>(new Object[]{a}, new Object[]{}, new Object[]{}) {
                    @Override
                    protected Void runRolez() {
                        region(0);
                        a.value = 1;
                        return null;
                    }
                };
                
                s.start(task);
                region(2);
                
                assertEquals(1,checkLegalRead(a).value);
                region(3);
            }
        });
    }
    
    @Test(expected = AssertionError.class)
    public void testPure() {
        assumeVerifyCorrectness();
    	verifyTask(new int[][]{{2, 3}, {0, 3}}, new Runnable() {
            public void run() {
            	final A a = new A();
                
                Task<?> task = new Task<Void>(new Object[]{}, new Object[]{}, new Object[]{a}) {
                    @Override
                    protected Void runRolez() {
                        region(0);
                        // This line is an illegal operation since it is not allowed to read non-final fields
                        // TODO: Try catch for runtime exception and try to propagate it outside
                        int i = checkLegalRead(a).value;
                        return null;
                    }
                };
                
                s.start(task);
                region(2);
                
                region(3);
            }
        });
    }
    
    @Test(expected = AssertionError.class)
    public void testPureRef() {
        assumeVerifyCorrectness();
    	verifyTask(new Runnable() {
            public void run() {
            	final A a = new A();
            	final B b = new B(a);
                
                Task<?> task = new Task<Void>(new Object[]{}, new Object[]{}, new Object[]{b}) {
                    @Override
                    protected Void runRolez() {
                        int i = checkLegalRead(b.a).value;
                        return null;
                    }
                };
                
                s.start(task);
            }
        });
    }  
    
    @Test
    public void testRef() {
        verifyTask(new int[][]{{2, 3}, {0, 3}}, new Runnable() {
            public void run() {
            	final A a = new A();
            	final B b = new B(a);
                
                Task<?> task = new Task<Void>(new Object[]{b}, new Object[]{}, new Object[]{}) {
                    @Override
                    protected Void runRolez() {
                        region(0);
                        b.a = new A();
                        return null;
                    }
                };
                
                s.start(task);
                region(2);
                
                assertNotEquals(a,checkLegalRead(b).a);
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
            	final A a = new A();
            	
            	Task<?> task = new Task<Void>(new Object[]{}, new Object[]{a}, new Object[]{}) {
                    @Override
                    protected Void runRolez() {
                        region(0);
                        checkLegalWrite(a).value = 1;
                        return null;
                    }
                };
                
                s.start(task);
            }
        });
    }
    
    @Test
    public void testReturn() {
    	verifyTask(new int[][]{{2, 3}, {0, 3}}, new Runnable() {
            public void run() {
            	final A a = new A();
                
                Task<B> task = new Task<B>(new Object[]{}, new Object[]{}, new Object[]{}) {
                    @Override
                    protected B runRolez() {
                        region(0);
                        B b = new B(a);
                        return b;
                    }
                };
                
                s.start(task);
                region(2);
                B b = task.get();
                
                // b has to become readwrite in the parent task
                checkLegalWrite(b).a = new A();
                region(3);
            }
        });
    }
    
    // TODO: Why does this result in "deadlock encountered" by jpf?
    @Ignore @Test
    public void testDeadlock() {
    	verifyTask(new int[][]{{2, 3}, {0, 3}}, new Runnable() {
            public void run() {
            	final A a = new A();
                
            	// Does only happen when objects are shared or shared pure
                Task<B> task = new Task<B>(new Object[]{}, new Object[]{a}, new Object[]{}) {
                    @Override
                    protected B runRolez() {
                        region(0);
                        B b = new B(a);
                        int i = guardReadOnly(a).value;
                        //int i = checkLegalRead(a).value;
                        return b;
                    }
                };
                
                s.start(task);
                region(2);
                B b = task.get();
                
                // b has to become readwrite in the parent task
                guardReadWrite(b).a = new A();
                //checkLegalWrite(b).a = new A();
                region(3);
            }
        });
    }
}

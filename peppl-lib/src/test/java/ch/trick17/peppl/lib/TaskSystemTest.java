package ch.trick17.peppl.lib;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.LockSupport;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import ch.trick17.peppl.lib.task.NewThreadTaskSystem;
import ch.trick17.peppl.lib.task.SingleThreadTaskSystem;
import ch.trick17.peppl.lib.task.Task;
import ch.trick17.peppl.lib.task.TaskSystem;
import ch.trick17.simplejpf.test.JpfTest;

@RunWith(Parameterized.class)
public class TaskSystemTest extends JpfTest {
    
    @Parameterized.Parameters(name = "{0}")
    public static List<?> taskSystems() {
        return Arrays.asList(new TaskSystem[][]{{new SingleThreadTaskSystem()},
                {new NewThreadTaskSystem()}});
    }
    
    private final TaskSystem system;
    
    public TaskSystemTest(final TaskSystem system) {
        this.system = system;
    }
    
    volatile boolean flag;
    
    @Test
    public void testRunTask() {
        if(verifyNoPropertyViolation()) {
            flag = false;
            final Thread original = Thread.currentThread();
            system.run(new Runnable() {
                public void run() {
                    flag = true;
                    LockSupport.unpark(original);
                }
            });
            while(!flag)
                LockSupport.park();
        }
    }
    
    @Test
    public void testRunTaskDeadlock() {
        if(verifyDeadlock()) {
            flag = false;
            final Thread original = Thread.currentThread();
            system.run(new Runnable() {
                public void run() {
                    // Not setting the flag will result in a deadlock:
                    // flag = true;
                    LockSupport.unpark(original);
                }
            });
            while(!flag)
                LockSupport.park();
        }
    }
    
    @Test
    public void testRunTaskMultiple() {
        if(verifyNoPropertyViolation()) {
            flag = false;
            final Thread original = Thread.currentThread();
            system.run(new Runnable() {
                public void run() {
                    system.run(new Runnable() {
                        public void run() {
                            system.run(new Runnable() {
                                public void run() {
                                    flag = true;
                                    LockSupport.unpark(original);
                                }
                            });
                        }
                    });
                }
            });
            
            while(!flag)
                LockSupport.park();
        }
    }
    
    @Test
    public void testRunTaskWaiting() {
        if(verifyNoPropertyViolation()) {
            system.run(new Runnable() {
                public void run() {
                    flag = false;
                    final Thread original = Thread.currentThread();
                    system.run(new Runnable() {
                        public void run() {
                            flag = true;
                            LockSupport.unpark(original);
                        }
                    });
                    while(!flag)
                        LockSupport.park();
                }
            });
        }
    }
    
    @Test
    public void testExceptionInTask() {
        if(verifyUnhandledException("java.lang.RuntimeException", "Hello")) {
            system.runDirectly(new Runnable() {
                public void run() {
                    system.run(new Runnable() {
                        public void run() {
                            throw new RuntimeException("Hello");
                        }
                    });
                }
            });
            /* Exception should be propagated automatically */
        }
    }
    
    @Test
    public void testReturnValue() throws Throwable {
        if(verifyNoPropertyViolation()) {
            final Task<Integer> task = system.run(new Callable<Integer>() {
                public Integer call() {
                    return 42;
                }
            });
            
            assertEquals(42, (int) task.get());
        }
    }
}

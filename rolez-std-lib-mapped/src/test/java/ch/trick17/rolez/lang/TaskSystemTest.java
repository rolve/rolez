package ch.trick17.rolez.lang;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.LockSupport;

import org.junit.Test;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import ch.trick17.rolez.lang.task.NewThreadTaskSystem;
import ch.trick17.rolez.lang.task.SingleThreadTaskSystem;
import ch.trick17.rolez.lang.task.Task;
import ch.trick17.rolez.lang.task.TaskSystem;
import ch.trick17.rolez.lang.task.ThreadPoolTaskSystem;
import ch.trick17.simplejpf.test.JpfParallelismTest;

@RunWith(Parameterized.class)
public class TaskSystemTest extends JpfParallelismTest {
    
    @Parameterized.Parameters(name = "{0}")
    public static List<?> taskSystems() {
        return Arrays.asList(new TaskSystem[][]{{new SingleThreadTaskSystem()},
                {new NewThreadTaskSystem()}, {new ThreadPoolTaskSystem()},
                {new ThreadPoolTaskSystem(1)}, {new ThreadPoolTaskSystem(2)}});
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
    
    @Test
    public void testNestedParallelism() {
        assumeMultithreaded();
        if(verifyParallelExcept()) {
            system.run(new Runnable() {
                public void run() {
                    /* Task 1 */
                    final Task<Void> task = system.run(new Runnable() {
                        public void run() {
                            /* Task 2 */
                            system.run(new Runnable() {
                                public void run() {
                                    /* Task 3 */
                                    region(0);
                                }
                            });
                            region(1);
                        }
                    });
                    /* Waiting for other tasks should not block worker threads
                     * of thread pools: */
                    task.get();
                }
            }).get();
        }
    }
    
    private void assumeMultithreaded() {
        if(system instanceof SingleThreadTaskSystem
                || (system instanceof ThreadPoolTaskSystem && ((ThreadPoolTaskSystem) system)
                        .getBaseSize() == 1))
            throw new AssumptionViolatedException("not a multithreaded test");
    }
}
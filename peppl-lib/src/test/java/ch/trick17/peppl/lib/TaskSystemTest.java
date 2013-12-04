package ch.trick17.peppl.lib;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.locks.LockSupport;

import org.junit.Test;

public class TaskSystemTest extends JpfUnitTest {
    
    volatile boolean flag;
    
    @Test
    public void testRunTask() {
        if(verifyNoPropertyViolation(args)) {
            final TaskSystem system = new TaskSystem();
            
            flag = false;
            final Thread original = Thread.currentThread();
            system.runTask(new Callable<Void>() {
                @Override
                public Void call() {
                    flag = true;
                    LockSupport.unpark(original);
                    return null;
                }
            });
            while(!flag)
                LockSupport.park();
        }
    }
    
    @Test
    public void testRunTaskDeadlock() {
        if(verifyDeadlock(args)) {
            final TaskSystem system = new TaskSystem();
            
            flag = false;
            final Thread original = Thread.currentThread();
            system.runTask(new Callable<Void>() {
                @Override
                public Void call() {
                    // Not setting the flag will result in a deadlock:
                    // flag = true;
                    LockSupport.unpark(original);
                    return null;
                }
            });
            while(!flag)
                LockSupport.park();
        }
    }
    
    @Test
    public void testRunTaskMultiple() {
        if(verifyNoPropertyViolation(args)) {
            final TaskSystem system = new TaskSystem();
            
            flag = false;
            final Thread original = Thread.currentThread();
            system.runTask(new Callable<Void>() {
                @Override
                public Void call() {
                    system.runTask(new Callable<Void>() {
                        @Override
                        public Void call() {
                            system.runTask(new Callable<Void>() {
                                @Override
                                public Void call() {
                                    flag = true;
                                    LockSupport.unpark(original);
                                    return null;
                                }
                            });
                            return null;
                        }
                    });
                    return null;
                }
            });
            
            while(!flag)
                LockSupport.park();
        }
    }
    
    @Test
    public void testRunTaskWaiting() {
        if(verifyNoPropertyViolation(args)) {
            // TaskSystem with only one thread
            final TaskSystem system = new TaskSystem();
            
            system.runTask(new Callable<Void>() {
                @Override
                public Void call() {
                    flag = false;
                    final Thread original = Thread.currentThread();
                    system.runTask(new Callable<Void>() {
                        @Override
                        public Void call() {
                            flag = true;
                            LockSupport.unpark(original);
                            return null;
                        }
                    });
                    while(!flag)
                        LockSupport.park();
                    return null;
                }
            });
        }
    }
    
    @Test
    public void testExceptionInTask() throws Throwable {
        if(verifyUnhandledExceptionDetails("java.lang.RuntimeException",
                "Hello", args)) {
            final TaskSystem system = new TaskSystem();
            final Future<Void> future = system.runTask(new Callable<Void>() {
                @Override
                public Void call() {
                    throw new RuntimeException("Hello");
                }
            });
            
            // Propagate thrown exception to original task (thread)
            try {
                while(true)
                    try {
                        future.get();
                    } catch(final InterruptedException e) {
                        // Ignore
                    }
            } catch(final ExecutionException e) {
                throw e.getCause();
            }
        }
    }
}

package ch.trick17.peppl.lib;

import java.util.concurrent.locks.LockSupport;

import org.junit.Test;

public class TaskSystemTest extends JpfUnitTest {
    
    @Test
    public void testExceptionInTask() {
        if(verifyUnhandledException("java.lang.RuntimeException", args)) {
            new TaskSystem().runTask(new Task<Void>() {
                @Override
                protected Void compute() {
                    throw new RuntimeException();
                }
            });
        }
    }
    
    volatile boolean flag;
    
    @Test
    public void testRunTask() {
        if(verifyNoPropertyViolation(args)) {
            final TaskSystem system = new TaskSystem(2);
            
            flag = false;
            final Thread original = Thread.currentThread();
            system.runTask(new Task<Void>() {
                @Override
                protected Void compute() {
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
            final TaskSystem system = new TaskSystem(2);
            
            flag = false;
            final Thread original = Thread.currentThread();
            system.runTask(new Task<Void>() {
                @Override
                protected Void compute() {
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
            final TaskSystem system = new TaskSystem(2);
            
            flag = false;
            final Thread original = Thread.currentThread();
            system.runTask(new Task<Void>() {
                @Override
                protected Void compute() {
                    system.runTask(new Task<Void>() {
                        @Override
                        protected Void compute() {
                            system.runTask(new Task<Void>() {
                                @Override
                                protected Void compute() {
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
            final TaskSystem system = new TaskSystem(1);
            
            system.runTask(new Task<Void>() {
                @Override
                protected Void compute() {
                    flag = false;
                    final Thread original = Thread.currentThread();
                    system.runTask(new Task<Void>() {
                        @Override
                        protected Void compute() {
                            flag = true;
                            LockSupport.unpark(original);
                            return null;
                        }
                    });
                    // Since there is only one thread in the task system, the
                    // child task is only executed when the current task is
                    // finished. Which obviously is a bad idea...
                    while(!flag)
                        LockSupport.park();
                    return null;
                }
            });
        }
    }
    
    @Test
    public void testAutomaticShutdown() {
        if(verifyAssertionError(args)) {
            final TaskSystem system = new TaskSystem(2);
            system.runTask(new Task<Void>() {
                @Override
                protected Void compute() {
                    return null;
                }
            });
            // This will cause an error as the last task of the system will
            // shut it down automatically:
            system.runTask(new Task<Void>() {
                @Override
                protected Void compute() {
                    return null;
                }
            });
        }
    }
    
    @Test
    public void testAutomaticShutdownMultiple() {
        if(verifyAssertionError(args)) {
            final TaskSystem system = new TaskSystem(2);
            system.runTask(new Task<Void>() {
                @Override
                protected Void compute() {
                    system.runTask(new Task<Void>() {
                        @Override
                        protected Void compute() {
                            system.runTask(new Task<Void>() {
                                @Override
                                protected Void compute() {
                                    system.runTask(new Task<Void>() {
                                        @Override
                                        protected Void compute() {
                                            return null;
                                        }
                                    });
                                    return null;
                                }
                            });
                            return null;
                        }
                    });
                    return null;
                }
            });
            // This will cause an error as the last task of the system will
            // shut it down automatically:
            system.runTask(new Task<Void>() {
                @Override
                protected Void compute() {
                    return null;
                }
            });
        }
    }
}

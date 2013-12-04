package ch.trick17.peppl.lib;

import org.junit.Test;

public class TaskSystemTest extends JpfUnitTest {
    
    @Test
    public void testExceptionInTask() {
        if(verifyNoPropertyViolation(args)) {
            new TaskSystem().runTask(new Task<Void>() {
                @Override
                protected Void compute() {
                    throw new RuntimeException();
                }
            });
        }
    }
}

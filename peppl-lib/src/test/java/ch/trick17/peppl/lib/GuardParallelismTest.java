package ch.trick17.peppl.lib;

import org.junit.Test;

import ch.trick17.peppl.lib.task.NewThreadTaskSystem;
import ch.trick17.peppl.lib.task.Task;
import ch.trick17.peppl.lib.task.TaskSystem;
import ch.trick17.simplejpf.test.JpfParallelismTest;

public class GuardParallelismTest extends JpfParallelismTest {
    
    private final TaskSystem s = new NewThreadTaskSystem();
    
    @Test
    public void parallelTest() {
        if(verifyParallelism()) {
            final Int i = new Int();
            
            i.share();
            final Task<Void> task = s.run(new Runnable() {
                @Override
                public void run() {
                    region(1);
                    i.releaseShared();
                    region(2);
                }
            });
            
            region(3);
            i.guardReadWrite();
            region(4);
            
            task.get();
        }
    }
}

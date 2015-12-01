package ch.trick17.rolez.lang;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.internal.AssumptionViolatedException;

import ch.trick17.rolez.lang.task.SingleThreadTaskSystem;
import ch.trick17.rolez.lang.task.TaskSystem;
import ch.trick17.simplejpf.test.JpfParallelismTest;

public class GuardingTest extends JpfParallelismTest {
    
    final TaskSystem s;
    final VerifyMode mode;
    
    public GuardingTest(final TaskSystem s, final VerifyMode mode) {
        this.s = s;
        this.mode = mode;
    }
    
    @Before
    public void setJpfProps() {
        final Map<String, String> props = new HashMap<>();
        props.put("vm.por", "false");
        setJpfProperties(props);
    }
    
    // TODO: Change all tests to use implicit exception propagation using this
    // method:
    void verify(final int[][] seqGroups, final RunnableCallable test) {
        if(verify(mode, seqGroups)) {
            s.run(test);
        }
    }
    
    void assumeMultithreaded() {
        if(s instanceof SingleThreadTaskSystem)
            throw new AssumptionViolatedException("not a multithreaded test");
    }
    
    void assumeVerifyCorrectness() {
        if(mode != VerifyMode.CORRECTNESS)
            throw new AssumptionViolatedException(
                    "not verifying correctness properties");
    }
}

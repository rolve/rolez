package rolez.lang;

import java.util.HashMap;
import java.util.Map;

import org.junit.internal.AssumptionViolatedException;

import ch.trick17.simplejpf.test.JpfParallelismTest;

public class TaskBasedJpfTest extends JpfParallelismTest {
    
    final TaskSystem s;
    final VerifyMode mode;
    
    public TaskBasedJpfTest(TaskSystem s, VerifyMode mode) {
        this.s = s;
        this.mode = mode;
    }
    
    @Override
    protected Map<String, Object> additionalJpfProperties() {
        final Map<String, Object> props = new HashMap<>();
        props.put("vm.por", "false");
        // props.put("report.console.property_violation", "error,snapshot,trace");
        // props.put("sourcepath", "src" + pathSeparator + "../ch.trick17.rolez.lib/src");
        return props;
    }
    
    void verifyTask(int[][] seqGroups, RunnableCallable task) {
        if(verify(mode, seqGroups))
            s.run(new Task<>(task));
    }
    
    void verifyTask(RunnableCallable task) {
        if(verify(mode))
            s.run(new Task<>(task));
    }
    
    void verifyTaskDeadlock(RunnableCallable task) {
        assumeVerifyCorrectness();
        if(verifyDeadlock())
            s.run(new Task<>(task));
    }
    
    void verifyTaskAssertionError(RunnableCallable task) {
        assumeVerifyCorrectness();
        if(verifyAssertionError())
            s.run(new Task<>(task));
    }
    
    /**
     * Skips the current test if this instance's task system is a {@link SingleThreadTaskSystem}.
     */
    void assumeMultithreaded() {
        if(s instanceof SingleThreadTaskSystem)
            throw new AssumptionViolatedException("not a multithreaded test");
    }
    
    /**
     * Skips the current test if this instance's
     * {@link ch.trick17.simplejpf.test.JpfParallelismTest.VerifyMode VerifyMode} is not
     * {@link ch.trick17.simplejpf.test.JpfParallelismTest.VerifyMode#CORRECTNESS CORRECTNESS}.
     */
    void assumeVerifyCorrectness() {
        if(mode != VerifyMode.CORRECTNESS)
            throw new AssumptionViolatedException("not verifying correctness properties");
    }
}

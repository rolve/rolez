package ch.trick17.peppl.manual;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ch.trick17.peppl.lib.task.NewThreadTaskSystem;
import ch.trick17.peppl.lib.task.SingleThreadTaskSystem;
import ch.trick17.peppl.lib.task.TaskSystem;
import ch.trick17.peppl.lib.task.ThreadPoolTaskSystem;
import ch.trick17.peppl.manual.quicksort.QuickSort;
import ch.trick17.peppl.manual.reduce.Reduce;
import ch.trick17.peppl.manual.simple.Simple;
import ch.trick17.peppl.manual.simplegroups.SimpleGroups;
import ch.trick17.simplejpf.test.JpfTest;

@RunWith(Parameterized.class)
public class ManualPepplTest extends JpfTest {
    
    @Parameters(name = "{0}")
    public static List<?> taskSystems() {
        return Arrays.asList(new Object[][]{{new SingleThreadTaskSystem()},
                {new NewThreadTaskSystem()}, {new ThreadPoolTaskSystem()}});
    }
    
    private final TaskSystem system;
    
    public ManualPepplTest(final TaskSystem system) {
        this.system = system;
    }
    
    @Before
    public void setJpfProps() {
        final Map<String, String> props = new HashMap<>();
        props.put("vm.por", "false");
        setJpfProperties(props);
    }
    
    @Test
    public void testSimple() {
        if(verifyNoPropertyViolation())
            system.runDirectly(new Simple());
    }
    
    @Test
    public void testReduce() {
        if(verifyNoPropertyViolation())
            system.runDirectly(new Reduce());
    }
    
    @Test
    public void testSimpleGroups() {
        if(verifyNoPropertyViolation())
            system.runDirectly(new SimpleGroups());
    }
    
    @Test
    public void testQuicksort() {
        if(verifyNoPropertyViolation())
            /* More parallel threads would kill JPF... */
            system.runDirectly(new QuickSort(5));
    }
}

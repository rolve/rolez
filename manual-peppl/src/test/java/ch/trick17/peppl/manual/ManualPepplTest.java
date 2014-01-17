package ch.trick17.peppl.manual;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import ch.trick17.peppl.manual.quicksort.QuickSort;
import ch.trick17.peppl.manual.reduce.Reduce;
import ch.trick17.peppl.manual.simple.Simple;
import ch.trick17.peppl.manual.simplegroups.SimpleGroups;
import ch.trick17.simplejpf.test.JpfTest;

public class ManualPepplTest extends JpfTest {
    
    @Before
    public void setJpfProps() {
        final Map<String, String> props = new HashMap<>();
        props.put("vm.por", "false");
        setJpfProperties(props);
    }
    
    @Test
    public void testSimple() {
        if(verifyNoPropertyViolation()) {
            Simple.main(new String[0]);
        }
    }
    
    @Test
    public void testReduce() {
        if(verifyNoPropertyViolation()) {
            Reduce.main(new String[0]);
        }
    }
    
    @Test
    public void testSimpleGroups() {
        if(verifyNoPropertyViolation()) {
            SimpleGroups.main(new String[0]);
        }
    }
    
    @Test
    public void testQuicksort() {
        if(verifyNoPropertyViolation()) {
            /* More parallel threads would kill JPF... */
            new QuickSort(6).run();
        }
    }
}

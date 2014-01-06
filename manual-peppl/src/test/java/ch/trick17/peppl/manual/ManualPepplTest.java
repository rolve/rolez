package ch.trick17.peppl.manual;

import org.junit.Test;

import ch.trick17.peppl.manual.reduce.Reduce;
import ch.trick17.peppl.manual.simple.Simple;
import ch.trick17.peppl.manual.simplegroups.SimpleGroups;
import ch.trick17.simplejpf.test.JpfTest;

public class ManualPepplTest extends JpfTest {
    
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
}

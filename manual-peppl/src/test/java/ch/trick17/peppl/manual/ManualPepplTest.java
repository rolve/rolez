package ch.trick17.peppl.manual;

import org.junit.Test;

import ch.trick17.peppl.manual.simple.Simple;
import ch.trick17.simplejpf.JpfUnitTest;

public class ManualPepplTest extends JpfUnitTest {
    
    @Test
    public void testSimple() {
        if(verifyNoPropertyViolation(args)) {
            Simple.main(new String[0]);
        }
    }
}

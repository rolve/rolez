package ch.trick17.simplejpf;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ch.trick17.simplejpf.test.JpfTest;

@RunWith(Parameterized.class)
public class ParameterizedJpfUnitTestTest extends JpfTest {
    
    @Parameters(name = "{0}, {1}")
    public static List<?> parameters() {
        return Arrays.asList(new Object[][] { {1, true}, {2, false}, {3, true},
                {4, false}});
    }
    
    private final int number;
    private final boolean isOdd;
    
    public ParameterizedJpfUnitTestTest(final int number, final boolean isOdd) {
        this.number = number;
        this.isOdd = isOdd;
    }
    
    @Test
    public void testOdd() {
        if(verifyNoPropertyViolation())
            assertEquals(isOdd, number % 2 == 1);
    }
}

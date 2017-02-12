package rolez.lang;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MathExtraTest {
    
    @Test
    public void testLog2() {
        // IMPROVE: Do not generate the $task parameter for methods that "obviously" don't need it
        assertEquals(0, MathExtra.INSTANCE.log2(1, null));
        assertEquals(1, MathExtra.INSTANCE.log2(2, null));
        assertEquals(2, MathExtra.INSTANCE.log2(4, null));
        assertEquals(4, MathExtra.INSTANCE.log2(16, null));
        
        assertEquals(1, MathExtra.INSTANCE.log2(3, null));
        assertEquals(4, MathExtra.INSTANCE.log2(31, null));
    }
}

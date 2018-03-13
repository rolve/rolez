package rolez.lang;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MathExtraTest {
    
    @Test
    public void testLog2() {
        // IMPROVE: Do not generate the $task parameter for methods that "obviously" don't need it
        assertEquals(0, MathExtra.INSTANCE.log2(1, 0L));
        assertEquals(1, MathExtra.INSTANCE.log2(2, 0L));
        assertEquals(2, MathExtra.INSTANCE.log2(4, 0L));
        assertEquals(4, MathExtra.INSTANCE.log2(16, 0L));
        
        assertEquals(1, MathExtra.INSTANCE.log2(3, 0L));
        assertEquals(4, MathExtra.INSTANCE.log2(31, 0L));
    }
}

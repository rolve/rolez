package rolez.lang;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MathExtraTest {
    
    @Test
    public void testLog2() {
        assertEquals(0, MathExtra.INSTANCE.log2(1));
        assertEquals(1, MathExtra.INSTANCE.log2(2));
        assertEquals(2, MathExtra.INSTANCE.log2(4));
        assertEquals(4, MathExtra.INSTANCE.log2(16));
        
        assertEquals(1, MathExtra.INSTANCE.log2(3));
        assertEquals(4, MathExtra.INSTANCE.log2(31));
    }
}

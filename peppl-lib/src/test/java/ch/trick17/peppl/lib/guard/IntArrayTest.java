package ch.trick17.peppl.lib.guard;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class IntArrayTest {
    
    @Test
    public void testConstructor() {
        final IntArray some = new IntArray(0, 1, 2, 3);
        assertEquals(4, some.size());
        for(int i = 0; i < some.size(); i++)
            assertEquals(i, some.data[i]);
        
        final IntArray single = new IntArray(0);
        assertEquals(1, single.size());
    }
}

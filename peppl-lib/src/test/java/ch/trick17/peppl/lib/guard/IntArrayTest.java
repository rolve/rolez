package ch.trick17.peppl.lib.guard;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class IntArrayTest {
    
    @Test
    public void testConstructors() {
        final IntArray some = new IntArray(0, 1, 2, 3);
        assertEquals(4, some.length());
        for(int i = 0; i < some.length(); i++)
            assertEquals(i, some.data[i]);
        
        final IntArray empty = new IntArray(0);
        assertEquals(0, empty.length());
    }
}

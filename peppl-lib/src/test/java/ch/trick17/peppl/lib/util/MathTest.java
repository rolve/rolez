package ch.trick17.peppl.lib.util;

import static java.math.BigInteger.valueOf;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MathTest {
    
    @Test
    public void testGdc() {
        for(int i = 0; i < 100; i++)
            for(int j = 0; j < 100; j++)
                assertEquals(Math.gcd(i, j), valueOf(i).gcd(valueOf(j))
                        .intValue());
    }
}

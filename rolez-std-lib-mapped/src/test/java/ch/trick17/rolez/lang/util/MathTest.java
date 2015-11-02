package ch.trick17.rolez.lang.util;

import static java.math.BigInteger.valueOf;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ch.trick17.rolez.lang.util.Math;

public class MathTest {
    
    @Test
    public void testGdc() {
        for(int i = 0; i < 100; i++)
            for(int j = 0; j < 100; j++)
                assertEquals(Math.gcd(i, j), valueOf(i).gcd(valueOf(j))
                        .intValue());
    }
}

package rolez.lang;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

public class GuardedArrayTest {
    
    @Test
    public void testClone() {
        GuardedArray<Object[]> objectArray = new GuardedArray<>(new Object[]{1, 2, 3});
        GuardedArray<Object[]> objectClone = objectArray.clone();
        assertArrayEquals(objectArray.data, objectClone.data);
        assertNotSame(objectArray.data, objectClone.data);
        
        GuardedArray<int[]> intArray = new GuardedArray<>(new int[]{1, 2, 3});
        GuardedArray<int[]> intClone = intArray.clone();
        assertArrayEquals(intArray.data, intClone.data);
        assertNotSame(intArray.data, intClone.data);
        
        GuardedArray<double[]> doubleArray = new GuardedArray<>(new double[]{1.1, 2.2, 3.3});
        GuardedArray<double[]> doubleClone = doubleArray.clone();
        assertArrayEquals(doubleArray.data, doubleClone.data, 0);
        assertNotSame(doubleArray.data, doubleClone.data);
        
        GuardedArray<boolean[]> booleanArray = new GuardedArray<>(new boolean[]{true, false, true});
        GuardedArray<boolean[]> booleanClone = booleanArray.clone();
        assertTrue(Arrays.equals(booleanArray.data, booleanClone.data));
        assertNotSame(booleanArray.data, booleanClone.data);
        
        GuardedArray<char[]> charArray = new GuardedArray<>(new char[]{'a', 'b', 'c'});
        GuardedArray<char[]> charClone = charArray.clone();
        assertArrayEquals(charArray.data, charClone.data);
        assertNotSame(charArray.data, charClone.data);
    }
}

package rolez.checked.lang;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;

import rolez.checked.lang.CheckedArray;

public class GuardedArrayTest {
	
	@BeforeClass
    public static void registerRootTask() {
    	Task.registerNewRootTask();
    }
	
    @Test
    public void testClone() {    	
        CheckedArray<Object[]> objectArray = new CheckedArray<>(new Object[]{1, 2, 3});
        CheckedArray<Object[]> objectClone = objectArray.clone();
        assertArrayEquals(objectArray.data, objectClone.data);
        assertNotSame(objectArray.data, objectClone.data);
        
        CheckedArray<int[]> intArray = new CheckedArray<>(new int[]{1, 2, 3});
        CheckedArray<int[]> intClone = intArray.clone();
        assertArrayEquals(intArray.data, intClone.data);
        assertNotSame(intArray.data, intClone.data);
        
        CheckedArray<double[]> doubleArray = new CheckedArray<>(new double[]{1.1, 2.2, 3.3});
        CheckedArray<double[]> doubleClone = doubleArray.clone();
        assertArrayEquals(doubleArray.data, doubleClone.data, 0);
        assertNotSame(doubleArray.data, doubleClone.data);
        
        CheckedArray<boolean[]> booleanArray = new CheckedArray<>(new boolean[]{true, false, true});
        CheckedArray<boolean[]> booleanClone = booleanArray.clone();
        assertTrue(Arrays.equals(booleanArray.data, booleanClone.data));
        assertNotSame(booleanArray.data, booleanClone.data);
        
        CheckedArray<char[]> charArray = new CheckedArray<>(new char[]{'a', 'b', 'c'});
        CheckedArray<char[]> charClone = charArray.clone();
        assertArrayEquals(charArray.data, charClone.data);
        assertNotSame(charArray.data, charClone.data);
    }
}

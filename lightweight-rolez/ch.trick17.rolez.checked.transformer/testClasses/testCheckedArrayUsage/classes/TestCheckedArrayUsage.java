package classes;

import rolez.annotation.Checked;
import rolez.annotation.Task;
import rolez.annotation.Readwrite;
import rolez.annotation.Readonly;

import rolez.checked.lang.CheckedArray;
import rolez.checked.lang.CheckedSlice;

@Checked
public class TestCheckedArrayUsage {

	public static void main(String[] args) {
		TestCheckedArrayUsage instance = new TestCheckedArrayUsage();
		
		// Test int array
		CheckedArray<int[]> intArray = new CheckedArray<int[]>(new int[] { 1, 2, 3, 4, 5, 6, 7, 8 } );
		for (int i=0; i<4; i++) {
			int begin = i*2;
			int end = i*2 + 2;
			CheckedSlice<int[]> slice = intArray.slice(begin, end);
			instance.doubleIntArrayElements(slice, true);
		}
		instance.printIntArray(intArray, true);
		
		// Test object array
		CheckedArray<A[]> objectArray = new CheckedArray<A[]>(new A[] { new A(1), new A(2) } );
		for (int i=0; i<2; i++) {
			int begin = i;
			int end = i + 1;
			CheckedSlice<A[]> slice = objectArray.slice(begin, end);
			instance.negateArrayElementFields(slice, true);
		}
		instance.printObjectArray(objectArray, true);
	}
	
	@Task
	void doubleIntArrayElements(@Readwrite CheckedSlice<int[]> slice, boolean $asTask) {
		for (int i = slice.getSliceRange().begin; i < slice.getSliceRange().end; i++) {
			slice.setInt(i, slice.getInt(i) * 2);
		}
		System.out.println("\n");
	}
	
	@Task
	void printIntArray(@Readonly CheckedArray<int[]> array, boolean $asTask) {
		for (int i = 0; i < array.arrayLength(); i++) {
			System.out.print(array.getInt(i) + " ");
		}
	}
	
	@Task
	void negateArrayElementFields(@Readwrite CheckedSlice<A[]> slice, boolean $asTask) {
		for (int i = slice.getSliceRange().begin; i < slice.getSliceRange().end; i++) {
			A a = slice.get(i);
			a.i = -a.i;
		}
	}
	
	@Task
	void printObjectArray(@Readonly CheckedArray<A[]> array, boolean $asTask) {
		for (int i = 0; i < array.arrayLength(); i++) {
			System.out.print(((A)array.get(i)).i + " ");
		}
		System.out.println("\n");
	}
}

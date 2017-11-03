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
		CheckedArray<int[]> checkedArray = new CheckedArray<int[]>(new int[] { 1, 2, 3, 4, 5, 6, 7, 8 } );
		for (int i=0; i<4; i++) {
			int begin = i*2;
			int end = i*2 + 2;
			CheckedSlice<int[]> slice = checkedArray.slice(begin, end);
			instance.doubleArrayElements(slice, true);
		}
		
		instance.printArray(checkedArray, true);
	}
	
	@Task
	void doubleArrayElements(@Readwrite CheckedSlice<int[]> slice, boolean $asTask) {
		for (int i = slice.getSliceRange().begin; i < slice.getSliceRange().end; i++) {
			slice.setInt(i, slice.getInt(i) * 2);
		}
	}
	
	@Task
	void printArray(@Readonly CheckedArray<int[]> array, boolean $asTask) {
		for (int i = 0; i < array.arrayLength(); i++) {
			System.out.print(array.getInt(i) + " ");
		}
	}
}

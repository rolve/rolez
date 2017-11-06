package classes;

import java.util.Random;

import rolez.annotation.Checked;
import rolez.annotation.Task;
import rolez.annotation.Readwrite;
import rolez.checked.lang.CheckedArray;
import rolez.checked.lang.CheckedSlice;

@Checked
public class TestMonteCarloSlices {

	public static void main(String[] args) {
		int cores = 4;
		int n = 1000000000;
		
		TestMonteCarloSlices instance = new TestMonteCarloSlices();
		CheckedArray<int[]> results = new CheckedArray<int[]>(new int[cores]);
		for (int i=0; i<cores; i++) {
			instance.simulate(results.slice(i,i+1), n/cores, true);
		}
		
		int totalHits = 0;
		for (int i=0; i<cores; i++) {
			totalHits += results.getInt(i);
		}
		
		System.out.println("Pi = " + totalHits / (0.25 * n));
	}
	
	@Task
	void simulate(@Readwrite CheckedSlice<int[]> result, int n, boolean $asTask) {
		Random r = new Random();
		int hits = 0;
		for (int i=0; i<n; i++) {
			double x = r.nextDouble();
			double y = r.nextDouble();
			if (x*x + y*y <= 1)
				hits++;
		}
		result.setInt(result.getSliceRange().begin, hits);
	}
}

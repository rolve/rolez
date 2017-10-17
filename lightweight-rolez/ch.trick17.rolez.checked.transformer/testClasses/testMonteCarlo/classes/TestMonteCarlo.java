package classes;

import java.util.Random;

import rolez.annotation.Checked;
import rolez.annotation.Roleztask;
import rolez.annotation.Readwrite;

@Checked
public class TestMonteCarlo {

	public static void main(String[] args) {
		boolean testSequential = false;
		int n = 1000000000;
		int cores = 4;
		
		if (testSequential) {
			
			Random r = new Random();
			int hits = 0;
			for (int i=0; i<n; i++) {
				double x = r.nextDouble();
				double y = r.nextDouble();
				if (x*x + y*y <= 1) {
					hits++;
				}
			}
			
			System.out.println("Pi = " + hits / (0.25 * n));
			
		} else {
			TestMonteCarlo instance = new TestMonteCarlo();
			Int[] results = new Int[cores];
			for (int i=0; i<cores; i++) {
				results[i] = new Int();
				instance.simulate(results[i], n/cores, true);
			}
			
			int totalHits = 0;
			for (int i=0; i<cores; i++) {
				totalHits += results[i].value;
			}
			
			System.out.println("Pi = " + totalHits / (0.25 * n));
		}
	}
	
	
	
	@Roleztask
	void simulate(@Readwrite Int result, int n, boolean $asTask) {
		Random r = new Random();
		int hits = 0;
		for (int i=0; i<n; i++) {
			double x = r.nextDouble();
			double y = r.nextDouble();
			if (x*x + y*y <= 1)
				hits++;
		}
		result.value = hits;
	}
}

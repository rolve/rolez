package test;

import org.junit.Test;

import test.util.Pipeline;

public class SlicePerformanceTest {
	
	@Test
	public void testMonteCarlo() {
		String methodName = "testMonteCarlo";
		String mainClass = "classes.TestMonteCarlo";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.runDefault(false, false);
	}

	@Test
	public void testMonteCarloSlices() {
		String methodName = "testMonteCarloSlices";
		String mainClass = "classes.TestMonteCarloSlices";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.runDefault(false, false);
	}
}

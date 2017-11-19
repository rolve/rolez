package test;

import org.junit.Test;

import test.util.Pipeline;

public class AppsTest {

	@Test
	public void testAppQuicksort() {
		String methodName = "appQuicksort";
		String mainClass = "classes.AppQuicksort";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.runDefault(true, false);
	}

	@Test
	public void testAppIdea() {
		String methodName = "appIdea";
		String mainClass = "classes.AppIdea";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.runDefault(true, false);
	}

	@Test
	public void testAppMergesort() {
		String methodName = "appMergesort";
		String mainClass = "classes.AppMergesort";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.runDefault(true, false);
	}

	@Test
	public void testAppMonteCarlo() {
		String methodName = "appMonteCarlo";
		String mainClass = "classes.AppMonteCarlo";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.runDefault(true, false);
	}

	@Test
	public void testAppKmeans() {
		String methodName = "appKmeans";
		String mainClass = "classes.AppKmeans";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.runDefault(true, false);
	}

	@Test
	public void testAppHistogram() {
		String methodName = "appHistogram";
		String mainClass = "classes.AppHistogram";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.runDefault(true, true);
	}
}

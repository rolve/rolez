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
}

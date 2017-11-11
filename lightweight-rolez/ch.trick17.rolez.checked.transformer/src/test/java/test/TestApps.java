package test;

import org.junit.Test;

import test.util.Pipeline;

public class TestApps {

	@Test
	public void appQuicksort() {
		String methodName = "appQuicksort";
		String mainClass = "classes.AppQuicksort";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.runDefault(true, false);
	}

	@Test
	public void appIdea() {
		String methodName = "appIdea";
		String mainClass = "classes.AppIdea";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.runDefault(true, false);
	}

	@Test
	public void appMergesort() {
		String methodName = "appMergesort";
		String mainClass = "classes.AppMergesort";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.runDefault(true, false);
	}
}

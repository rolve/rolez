package test;

import org.junit.Test;

import test.util.Pipeline;

public class TransformerTest {
	
	@Test
	public void testMain() {
		String methodName = "testMain";
		String mainClass = "classes.TestMain";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.run();
	}
}

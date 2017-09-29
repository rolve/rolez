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
	
	@Test
	public void testChecked() {
		String methodName = "testChecked";
		String mainClass = "classes.TestChecked";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.run();
	}
}

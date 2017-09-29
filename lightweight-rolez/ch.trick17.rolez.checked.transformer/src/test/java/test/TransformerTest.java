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
	
	@Test
	public void testTask() {
		String methodName = "testTask";
		String mainClass = "classes.TestTask";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.run();
	}
	
	@Test
	public void testTaskOverride() {
		String methodName = "testTaskOverride";
		String mainClass = "classes.TestTaskOverride";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.run();
	}
}

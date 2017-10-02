package test;

import org.junit.Test;

import test.util.Pipeline;

public class TransformerTest {
	
	@Test
	public void testMain() {
		String methodName = "testMain";
		String mainClass = "classes.TestMain";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.run(true, false);
	}
	
	@Test
	public void testChecked() {
		String methodName = "testChecked";
		String mainClass = "classes.TestChecked";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.run(true, false);
	}
	
	@Test
	public void testTask() {
		String methodName = "testTask";
		String mainClass = "classes.TestTask";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.run(true, false);
	}
	
	@Test
	public void testTaskOverride() {
		String methodName = "testTaskOverride";
		String mainClass = "classes.TestTaskOverride";
		Pipeline p = new Pipeline(methodName, mainClass);
		// Parallel programs don't allow file comparison
		p.run(false, false);
	}

	@Test
	public void testNestedTask() {
		String methodName = "testNestedTask";
		String mainClass = "classes.TestNestedTask";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.run(true, false);
	}

	@Test
	public void testReadonly() {
		String methodName = "testReadonly";
		String mainClass = "classes.TestReadonly";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.run(true, false);
	}

	@Test
	public void testReadwrite() {
		String methodName = "testReadwrite";
		String mainClass = "classes.TestReadwrite";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.run(true, false);
	}

	@Test(expected = RuntimeException.class)
	public void testReadonlyFail() {
		String methodName = "testReadonlyFail";
		String mainClass = "classes.TestReadonlyFail";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.run(false, false);
	}

	@Test
	public void testPure() {
		String methodName = "testPure";
		String mainClass = "classes.TestPure";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.run(true, false);
	}

	@Test(expected = RuntimeException.class)
	public void testPureFail() {
		String methodName = "testPureFail";
		String mainClass = "classes.TestPureFail";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.run(true, false);
	}
}

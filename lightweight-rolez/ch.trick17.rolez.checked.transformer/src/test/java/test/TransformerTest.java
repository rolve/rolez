package test;

import org.junit.Test;

import rolez.checked.transformer.exceptions.ConstructorTaskCallException;
import test.util.Pipeline;

public class TransformerTest {
	
	@Test
	public void testMain() {
		String methodName = "testMain";
		String mainClass = "classes.TestMain";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.runDefault(true, false);
	}
	
	@Test
	public void testChecked() {
		String methodName = "testChecked";
		String mainClass = "classes.TestChecked";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.runDefault(true, false);
	}
	
	@Test
	public void testTask() {
		String methodName = "testTask";
		String mainClass = "classes.TestTask";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.runDefault(true, false);
	}
	
	@Test
	public void testTaskOverload() {
		String methodName = "testTaskOverload";
		String mainClass = "classes.TestTaskOverload";
		Pipeline p = new Pipeline(methodName, mainClass);
		// Parallel programs don't allow file comparison
		p.runDefault(false, false);
	}

	@Test
	public void testNestedTask() {
		String methodName = "testNestedTask";
		String mainClass = "classes.TestNestedTask";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.runDefault(true, false);
	}

	@Test
	public void testReadonly() {
		String methodName = "testReadonly";
		String mainClass = "classes.TestReadonly";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.runDefault(true, false);
	}

	@Test
	public void testReadwrite() {
		String methodName = "testReadwrite";
		String mainClass = "classes.TestReadwrite";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.runDefault(true, false);
	}

	@Test
	public void testReadonlyFail() {
		String methodName = "testReadonlyFail";
		String mainClass = "classes.TestReadonlyFail";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.runExpectNonSufficientRoleError(false);
	}

	@Test
	public void testPure() {
		String methodName = "testPure";
		String mainClass = "classes.TestPure";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.runDefault(true, false);
	}

	@Test
	public void testPureFail() {
		String methodName = "testPureFail";
		String mainClass = "classes.TestPureFail";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.runExpectNonSufficientRoleError(false);
	}

	@Test
	public void testMultipleTaskCalls() {
		String methodName = "testMultipleTaskCalls";
		String mainClass = "classes.TestMultipleTaskCalls";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.runDefault(true, false);
	}

	@Test
	public void testSubclassTask() {
		String methodName = "testSubclassTask";
		String mainClass = "classes.TestSubclassTask";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.runDefault(true, false);
	}

	@Test
	public void testTaskOverride() {
		String methodName = "testTaskOverride";
		String mainClass = "classes.TestTaskOverride";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.runDefault(true, false);
	}

	@Test
	public void testConstructor() {
		String methodName = "testConstructor";
		String mainClass = "classes.TestConstructor";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.runDefault(true, false);
	}

	@Test
	public void testFinalFieldInitialization() {
		String methodName = "testFinalFieldInitialization";
		String mainClass = "classes.TestFinalFieldInitialization";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.runDefault(true, false);
	}

	@Test
	public void testGuardReadwrite() {
		String methodName = "testGuardReadwrite";
		String mainClass = "classes.TestGuardReadwrite";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.runDefault(true, false);
	}
}

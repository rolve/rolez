package test;

import org.junit.Test;

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

	@Test
	public void testGuardReadonly() {
		String methodName = "testGuardReadonly";
		String mainClass = "classes.TestGuardReadonly";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.runDefault(true, false);
	}

	@Test
	public void testTryCatch() {
		String methodName = "testTryCatch";
		String mainClass = "classes.TestTryCatch";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.runDefault(true, false);
	}

	@Test
	public void testForLoop() {
		String methodName = "testForLoop";
		String mainClass = "classes.TestForLoop";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.runDefault(true, false);
	}

	@Test
	public void testWhileLoop() {
		String methodName = "testWhileLoop";
		String mainClass = "classes.TestWhileLoop";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.runDefault(true, false);
	}

	@Test
	public void testDifferentPackages() {
		String methodName = "testDifferentPackages";
		String mainClass = "classes.TestDifferentPackages";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.runDefault(true, false);
	}

	@Test
	public void testTaskWithUncheckedParams() {
		String methodName = "testTaskWithUncheckedParams";
		String mainClass = "classes.TestTaskWithUncheckedParams";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.runDefault(true, false);
	}

	@Test
	public void testTaskWithSameParams() {
		String methodName = "testTaskWithSameParams";
		String mainClass = "classes.TestTaskWithSameParams";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.runDefault(true, false);
	}

	@Test
	public void testRefGuardingFail() {
		String methodName = "testRefGuardingFail";
		String mainClass = "classes.TestRefGuardingFail";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.runExpectNonSufficientRoleError(false);
	}

	@Test
	public void testCast() {
		String methodName = "testCast";
		String mainClass = "classes.TestCast";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.runDefault(true, false);
	}

	@Test
	public void testMultiShare() {
		String methodName = "testMultiShare";
		String mainClass = "classes.TestMultiShare";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.runDefault(true, false);
	}

	@Test
	public void testShareReadonly() {
		String methodName = "testShareReadonly";
		String mainClass = "classes.TestShareReadonly";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.runDefault(false, false);
	}

	@Test
	public void testInterfaces() {
		String methodName = "testInterfaces";
		String mainClass = "classes.TestInterfaces";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.runDefault(true, false);
	}

	@Test
	public void testCheckThis() {
		String methodName = "testCheckThis";
		String mainClass = "classes.TestCheckThis";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.runDefault(true, false);
	}

	@Test
	public void testOptimizedChecks() {
		String methodName = "testOptimizedChecks";
		String mainClass = "classes.TestOptimizedChecks";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.runDefault(true, false);
	}

	@Test
	public void testNotAsTask() {
		String methodName = "testNotAsTask";
		String mainClass = "classes.TestNotAsTask";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.runDefault(true, false);
	}

	@Test
	public void testMonteCarlo() {
		String methodName = "testMonteCarlo";
		String mainClass = "classes.TestMonteCarlo";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.runDefault(false, true);
	}
}

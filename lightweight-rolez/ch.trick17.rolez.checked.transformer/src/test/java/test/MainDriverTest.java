package test;

import org.junit.Test;

public class MainDriverTest {
	
	@Test
	public void testMainDriver() {
		String methodName = "testMainDriver";
		String mainClass = "classes.TestClass";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.run();
	}
}

package classes;

import rolez.annotation.Checked;
import rolez.annotation.Task;
import rolez.annotation.Readwrite;

@Checked
public class TestOptimizedChecks {

	public static void main(String[] args) {
		TestOptimizedChecks instance = new TestOptimizedChecks();
		A a = new A();
		System.out.println(a.s);
		a.s = "HELLO";
		System.out.println(a.s);
		a.s = "HELLO";
		System.out.println(a.s);
		instance.task(a, true);
		System.out.println(a.s);
		a.s = "HELLO";
		System.out.println(a.s);
		a.s = "HELLO";
		System.out.println(a.s);
		
		for (int i=0; i<4; i++) {
			a.s = "HELLO";
			System.out.println(a.s);
			instance.task(a, true);
		}
	}
	
	@Task
	void task(@Readwrite A a, boolean $asTask) {
		a.s = "Hello world!";
	}
}

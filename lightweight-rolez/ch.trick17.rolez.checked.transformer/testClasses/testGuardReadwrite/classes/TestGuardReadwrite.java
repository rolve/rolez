package classes;

import rolez.annotation.Checked;
import rolez.annotation.Task;
import rolez.annotation.Readwrite;

@Checked
public class TestGuardReadwrite {
	
	public static void main(String[] args) {
		TestGuardReadwrite instance = new TestGuardReadwrite();
		A a = new A();
		System.out.println(a.i);
		instance.task(a, true);
		a.i = 3;
		System.out.println(a.i);
	}
	
	@Task
	void task(@Readwrite A a, boolean $asTask) {
		// Sleep for a while to ensure guarding has to block at a.i = 3; in main
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) { }
		a.i = 2;
		System.out.println(a.i);
	}
}

package classes;

import rolez.annotation.Checked;
import rolez.annotation.Roleztask;
import rolez.annotation.Readonly;

@Checked
public class TestGuardReadonly {

	public static void main(String[] args) {
		TestGuardReadonly instance = new TestGuardReadonly();
		A a = new A();
		instance.task(a, true);
		a.i = 2;
		System.out.println(a.i);
	}
	
	@Roleztask
	void task(@Readonly A a, boolean $asTask) {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) { }
		System.out.println(a.i);
	}
}

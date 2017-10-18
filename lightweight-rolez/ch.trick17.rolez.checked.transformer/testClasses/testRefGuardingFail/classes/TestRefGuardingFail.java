package classes;

import rolez.annotation.Checked;
import rolez.annotation.Task;
import rolez.annotation.Readonly;

@Checked
public class TestRefGuardingFail {

	public static void main(String[] args) {
		TestRefGuardingFail instance = new TestRefGuardingFail();
		A a = new A();
		B b = new B();
		a.b = b;
		a.b.s = "Hello world!";
		instance.task(a, true);
	}
	
	@Task
	void task(@Readonly A a, boolean $asTask) {
		a.b.s = "Hello hell!";
		System.out.println(a.b.s);
	}
}

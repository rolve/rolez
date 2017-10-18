package classes;

import rolez.annotation.Checked;
import rolez.annotation.Task;
import rolez.annotation.Readonly;

@Checked
public class TestCast {

	public static void main(String[] args) {
		TestCast instance = new TestCast();
		B b = new B();
		instance.task(b, true);
	}
	
	@Task
	void task(@Readonly A a, boolean $asTask) {
		if (a instanceof B)
			System.out.println(((B)a).s);
	}
}

package classes;

import rolez.annotation.Task;
import rolez.annotation.Readonly;
import rolez.annotation.Checked;

@Checked
public class TestReadonlyFail {

	public static void main(String[] args) {
		TestReadonlyFail instance = new TestReadonlyFail();
		A a = new A();
		instance.task(a, true);
	}
	
	@Task
	void task(@Readonly A a, boolean $asTask) {
		a.message = "Hello world!";
		System.out.println("Hello world!");
	}
}

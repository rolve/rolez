package classes;

import rolez.annotation.Checked;
import rolez.annotation.Pure;
import rolez.annotation.Task;

@Checked
public class TestPureFail {

	public static void main(String[] args) {
		TestPureFail instance = new TestPureFail();
		A a = new A();
		instance.task(a, true);
	}
	
	@Task
	void task(@Pure A a, boolean $asTask) {
		System.out.println(a.message);
	}
}

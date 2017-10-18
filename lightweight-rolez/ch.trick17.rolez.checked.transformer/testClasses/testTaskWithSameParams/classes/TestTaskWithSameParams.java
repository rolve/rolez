package classes;

import rolez.annotation.Checked;
import rolez.annotation.Task;
import rolez.annotation.Readwrite;
import rolez.annotation.Readonly;

@Checked
public class TestTaskWithSameParams {

	public static void main(String[] args) {
		TestTaskWithSameParams instance = new TestTaskWithSameParams();
		A a = new A();
		instance.task(a, a, true);
	}

	@Task
	void task(@Readonly A a0, @Readwrite A a1, boolean $asTask) {
		// TODO: Is this expected to finish without an exception? What happens in rolez?
		System.out.println(a0.s);
		a0.s = "Hello hell!";
		System.out.println(a0.s);
		System.out.println(a1.s);
		a1.s = "Hello heaven!";
		System.out.println(a1.s);
	}
}

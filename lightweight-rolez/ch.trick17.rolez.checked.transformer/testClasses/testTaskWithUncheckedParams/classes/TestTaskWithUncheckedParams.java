package classes;

import rolez.annotation.Checked;
import rolez.annotation.Task;
import rolez.annotation.Readonly;
import rolez.annotation.Readwrite;

@Checked
public class TestTaskWithUncheckedParams {

	public static void main(String[] args) {
		TestTaskWithUncheckedParams instance = new TestTaskWithUncheckedParams();
		A a = new A();
		B b = new B();
		instance.task(a, 1, b, true);
	}

	@Task
	void task(@Readonly A a, int i, @Readwrite B b, boolean $asTask) {
		b.s = "Hello hell!";
		System.out.println(a.s);
		System.out.println(i);
		System.out.println(b.s);
	}
}

package classes;

import rolez.annotation.Checked;
import rolez.annotation.Task;
import rolez.annotation.Readonly;

@Checked
public class TestReadonly {
	
	public static void main(String[] args) {
		TestReadonly instance = new TestReadonly();
		A a = new A();
		instance.task(a, true);
	}
	
	@Task
	void task(@Readonly A a, boolean $asTask) {
		System.out.println(a.message);
	}
}

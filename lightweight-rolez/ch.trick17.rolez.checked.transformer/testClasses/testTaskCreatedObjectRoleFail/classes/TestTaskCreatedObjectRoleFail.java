package classes;

import rolez.annotation.Checked;
import rolez.annotation.Task;
import rolez.annotation.Readwrite;
import rolez.annotation.Readonly;

@Checked
public class TestTaskCreatedObjectRoleFail {

	public static void main(String[] args) {
		TestTaskCreatedObjectRoleFail instance = new TestTaskCreatedObjectRoleFail();
		A a = new A();
		instance.task(a, true);
		System.out.println(a.b.i);
	}
	
	@Task
	void task(@Readonly A a, boolean $asTask) {
		this.otherTask(a, true);
		a.b = new B(42);
	}
	
	@Task 
	void otherTask(@Readonly A a, boolean $asTask) {
		int i = a.b.i;
	}
}

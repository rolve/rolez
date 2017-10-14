package classes;

import rolez.annotation.Checked;
import rolez.annotation.Readonly;
import rolez.annotation.Roleztask;

@Checked
public class TestShareReadonly {

	public static void main(String[] args) {
		TestShareReadonly instance = new TestShareReadonly();
		A a = new A();
		instance.task1(a, true);
		instance.task2(a, true);
	}
	
	@Roleztask
	void task1(@Readonly A a, boolean $asTask) {
		for (int i=0; i<1000; i++) {
			System.out.println("Task 1: " + a.s);
		}
	}
	
	@Roleztask
	void task2(@Readonly A a, boolean $asTask) {
		for (int i=0; i<1000; i++) {
			System.out.println("Task 2: " + a.s);
		}
	}
}

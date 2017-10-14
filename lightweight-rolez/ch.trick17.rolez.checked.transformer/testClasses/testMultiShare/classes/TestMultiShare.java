package classes;

import rolez.annotation.Checked;
import rolez.annotation.Roleztask;
import rolez.annotation.Readonly;
import rolez.annotation.Readwrite;
import rolez.annotation.Pure;

@Checked
public class TestMultiShare {

	public static void main(String[] args) {
		TestMultiShare instance = new TestMultiShare();
		A a = new A();
		System.out.println("Starting task 1");
		instance.task1(a, true);
		System.out.println("Starting task 2");
		instance.task2(a, true);
		System.out.println("Starting task 1");
		instance.task1(a, true);
	}
	
	@Roleztask
	void task1(@Readonly A a, boolean $asTask) {
		try {
			// Sleep long enough that task 2 can start before
			Thread.sleep(5000);
			System.out.println("Task1 starts working");
			System.out.println(a.s);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Roleztask
	void task2(@Readwrite A a, boolean $asTask) {
		System.out.println("Hello from task 2");
		a.s = "Hello hell!";
	}
}

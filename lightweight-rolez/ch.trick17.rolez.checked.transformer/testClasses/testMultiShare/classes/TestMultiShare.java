package classes;

import rolez.annotation.Checked;
import rolez.annotation.Task;
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
		try {
			// Sleep long enough that task 2 can start before
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Starting task 1");
		instance.task1(a, true);
	}
	
	@Task
	void task1(@Readonly A a, boolean $asTask) {
		try {
			// Sleep long enough that main task can continue
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Task1 starts working");
		System.out.println(a.s);
	}

	@Task
	void task2(@Readwrite A a, boolean $asTask) {
		System.out.println("Hello from task 2");
		a.s = "Hello hell!";
	}
}

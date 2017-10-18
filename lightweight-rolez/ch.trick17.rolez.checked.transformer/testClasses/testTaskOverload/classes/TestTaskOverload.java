package classes;

import rolez.annotation.Checked;
import rolez.annotation.Task;

@Checked
public class TestTaskOverload {
	
	public static void main(String[] args) {
		TestTaskOverload instance = new TestTaskOverload();
		instance.task(true);
		instance.task(5, true);
	}
	
	@Task
	void task(boolean $asTask) {
		System.out.println("Hello world!");
	}
	
	@Task
	void task(int n, boolean $asTask) {
		for (int i = 0; i<n; i++) {
			System.out.println("Hello world " + i + "!");
		}
	}
}

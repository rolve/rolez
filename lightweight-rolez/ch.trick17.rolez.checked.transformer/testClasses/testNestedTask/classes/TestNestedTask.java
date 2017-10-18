package classes;

import rolez.annotation.Checked;
import rolez.annotation.Task;

@Checked
public class TestNestedTask {
	
	public static void main(String[] args) {
		TestNestedTask instance = new TestNestedTask();
		instance.task1(true);
	}
	
	@Task
	void task1(boolean $asTask) {
		this.task2(true);
	}
	
	@Task
	void task2(boolean $asTask) {
		System.out.println("Hello world!");
	}
}

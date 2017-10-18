package classes;

import rolez.annotation.Checked;
import rolez.annotation.Task;

@Checked
public class TestTask {
	
	public static void main(String[] args) {
		TestTask instance = new TestTask();
		instance.task(true);
	}
	
	@Task
	void task(boolean $asTask) {
		System.out.println("Hello world!");
	}
}

package classes;

import rolez.annotation.Checked;
import rolez.annotation.Task;

@Checked
public class TestNotAsTask {
	
	public static void main(String[] args) {
		TestNotAsTask instance = new TestNotAsTask();
		instance.task(false);
	}
	
	@Task
	void task(boolean $asTask) {
		System.out.println("Hello world!");
	}
}

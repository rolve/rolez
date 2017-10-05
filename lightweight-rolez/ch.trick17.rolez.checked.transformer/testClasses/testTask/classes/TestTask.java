package classes;

import rolez.annotation.Checked;
import rolez.annotation.Roleztask;

@Checked
public class TestTask {
	
	public static void main(String[] args) {
		TestTask instance = new TestTask();
		instance.task(true);
	}
	
	@Roleztask
	void task(boolean $asTask) {
		System.out.println("Hello world!");
	}
}

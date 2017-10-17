package classes;

import rolez.annotation.Checked;
import rolez.annotation.Roleztask;

@Checked
public class TestNotAsTask {
	
	public static void main(String[] args) {
		TestNotAsTask instance = new TestNotAsTask();
		instance.task(false);
	}
	
	@Roleztask
	void task(boolean $asTask) {
		System.out.println("Hello world!");
	}
}

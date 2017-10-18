package classes;

import rolez.annotation.Checked;
import rolez.annotation.Task;

@Checked
public class TestForLoop {

	public static void main(String[] args) {
		TestForLoop instance = new TestForLoop();
		for (int i=0; i<5; i++) {
			instance.task(true);
		}
	}
	
	@Task
	void task(boolean $asTask) {
		System.out.println("Hello world!");
	}
}

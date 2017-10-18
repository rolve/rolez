package classes;

import rolez.annotation.Checked;
import rolez.annotation.Task;

@Checked
public class TestWhileLoop {

	public static void main(String[] args) {
		TestWhileLoop instance = new TestWhileLoop();
		int i = 0;
		while (i<5) {
			instance.task(true);
			i++;
		}
	}
	
	@Task
	void task(boolean $asTask) {
		System.out.println("Hello world!");
	}
}

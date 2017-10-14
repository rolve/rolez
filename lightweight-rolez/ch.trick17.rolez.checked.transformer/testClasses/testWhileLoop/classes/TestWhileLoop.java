package classes;

import rolez.annotation.Checked;
import rolez.annotation.Roleztask;

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
	
	@Roleztask
	void task(boolean $asTask) {
		System.out.println("Hello world!");
	}
}

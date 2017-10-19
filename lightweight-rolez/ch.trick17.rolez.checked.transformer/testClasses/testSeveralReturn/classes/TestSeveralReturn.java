package classes;

import rolez.annotation.Checked;
import rolez.annotation.Task;

import java.util.Random;

@Checked
public class TestSeveralReturn {

	public static void main(String[] args) {
		TestSeveralReturn instance = new TestSeveralReturn();
		Random random = new Random();
		if (random.nextBoolean()) {
			instance.task(true);
			System.out.println("Hello world!");
			return;
		} else {
			instance.task(false);
			System.out.println("Hello world!");
			return;
		}
	}
	
	@Task
	void task(boolean $asTask) {
		Random random = new Random();
		if (random.nextBoolean()) {
			System.out.println("Hello world!");
			return;
		} else {
			System.out.println("Hello world!");
			return;
		}
	}
}

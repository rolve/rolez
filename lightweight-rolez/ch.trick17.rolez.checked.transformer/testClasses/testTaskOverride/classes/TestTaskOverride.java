package classes;

import rolez.annotation.Roleztask;

public class TestTaskOverride {
	
	public static void main(String[] args) {
		TestTaskOverride instance = new TestTaskOverride();
		instance.task(true);
		instance.task(5, true);
	}
	
	@Roleztask
	void task(boolean $asTask) {
		System.out.println("Hello world!");
	}
	
	@Roleztask
	void task(int n, boolean $asTask) {
		for (int i = 0; i<n; i++) {
			System.out.println("Hello world " + i + "!");
		}
	}
}

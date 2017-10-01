package classes;

import rolez.annotation.Roleztask;

public class TestNestedTask {
	
	public static void main(String[] args) {
		TestNestedTask instance = new TestNestedTask();
		instance.task1(true);
	}
	
	@Roleztask
	void task1(boolean $asTask) {
		this.task2(true);
	}
	
	@Roleztask
	void task2(boolean $asTask) {
		System.out.println("Hello world!");
	}
}

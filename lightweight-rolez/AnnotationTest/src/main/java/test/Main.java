package test;

import rolez.annotation.Roleztask;

public class Main {
	
	@Roleztask
	public static void main(String[] args) {
		Test test = new Test();
		test.run();
	}
}

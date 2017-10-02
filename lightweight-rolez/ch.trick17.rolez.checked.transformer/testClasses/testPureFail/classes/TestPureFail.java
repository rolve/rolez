package classes;

import rolez.annotation.Pure;
import rolez.annotation.Roleztask;

public class TestPureFail {

	public static void main(String[] args) {
		TestPureFail instance = new TestPureFail();
		A a = new A();
		instance.task(a, true);
	}
	
	@Roleztask
	void task(@Pure A a, boolean $asTask) {
		System.out.println(a.message);
	}
}

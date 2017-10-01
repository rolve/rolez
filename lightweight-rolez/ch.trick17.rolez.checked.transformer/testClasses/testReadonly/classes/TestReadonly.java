package classes;

import rolez.annotation.Roleztask;
import rolez.annotation.Readonly;;

public class TestReadonly {
	
	public static void main(String[] args) {
		TestReadonly instance = new TestReadonly();
		A a = new A();
		instance.task(a, true);
	}
	
	@Roleztask
	void task(@Readonly A a, boolean $asTask) {
		System.out.println(a.message);
	}
}

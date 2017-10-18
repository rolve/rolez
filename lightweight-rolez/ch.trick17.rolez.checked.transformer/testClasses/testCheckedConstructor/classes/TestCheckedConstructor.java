package classes;

import rolez.annotation.Checked;
import rolez.annotation.Roleztask;

@Checked
public class TestCheckedConstructor {

	public static void main(String[] args) {
		TestCheckedConstructor instance = new TestCheckedConstructor();
		B b = new B();
		A a1 = new A(instance);
		A a2 = new A(b);
		A a3 = new A(b, instance);
		System.out.println("Hello world!");
	}
}

package classes;

import classes.foo.A;
import rolez.annotation.Roleztask;

public class TestClass {
	
	int i;
	
	TestClass() {
		this.i = 1;
	}
	
	TestClass(int i) {
		this.i = i;
	}
	
	public static void main(String[] args) {
		TestClass test = new TestClass();
		test.foo(true);
	}
	
	@Roleztask
	void foo(boolean $asTask) {
		System.out.println("HELLO WORLD!");
		A a = new A(this);
		a.printCreator(true);
	}
	
	@Override
	public String toString() {
		return "Hello, I'm " + TestClass.class.getCanonicalName();
	}
}

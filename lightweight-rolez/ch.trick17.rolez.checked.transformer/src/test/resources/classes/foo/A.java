package classes.foo;

import rolez.annotation.Roleztask;
import classes.TestClass;

public class A {
	TestClass creator;
	
	public A(TestClass creator) {
		this.creator = creator;
	}
	
	@Roleztask
	public void printCreator(boolean $asTask) {
		System.out.println(this.creator);
	}
}

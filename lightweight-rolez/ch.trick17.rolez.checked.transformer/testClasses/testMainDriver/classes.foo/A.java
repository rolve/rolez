package classes.foo;

import rolez.annotation.Checked;
import rolez.annotation.Roleztask;
import classes.TestClass;

@Checked
public class A {
	TestClass creator;
	
	public A(TestClass creator) {
		this.creator = creator;
	}
	
	@Roleztask
	public void printCreator(boolean $asTask) {
		System.out.println(this.creator.toString());
	}
}

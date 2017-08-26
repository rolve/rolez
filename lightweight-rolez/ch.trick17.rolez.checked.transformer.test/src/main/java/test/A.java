package test;

import rolez.annotation.Checked;
import rolez.annotation.Roleztask;

@Checked
public class A {
	public int foo;
	
	A() {
		this.foo = 1;
	}
	
	A(int i) {
		this.foo = i;
	}
	
	@Roleztask
	public void bar(int i, final boolean $asTask) {
		this.foo = i;
	}
}

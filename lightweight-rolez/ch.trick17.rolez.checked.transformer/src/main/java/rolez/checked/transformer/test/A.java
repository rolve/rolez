package rolez.checked.transformer.test;

import rolez.annotation.Checked;

@Checked
public class A {
	public int foo;
	
	A() {
		this.foo = 1;
	}
	
	A(int i) {
		this.foo = i;
	}
}

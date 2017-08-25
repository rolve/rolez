package test;

import rolez.annotation.Checked;

@Checked
public class Test {

	public static void main(String[] args) {
		
	}
	
	void test() {
		A a = new A();	// Checked object
		a.foo = 2;
		int i = a.foo;
	}
}

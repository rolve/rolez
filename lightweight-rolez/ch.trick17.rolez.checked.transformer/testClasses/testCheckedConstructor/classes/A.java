package classes;

import rolez.annotation.Checked;

@Checked
class A {
	
	B b;
	TestCheckedConstructor instance;
	
	A() {
		this.b = new B();
		this.instance = new TestCheckedConstructor();
	}
	
	A(TestCheckedConstructor instance) {
		this.b = new B();
		this.instance = instance;
	}
	
	A(B b) {
		this.b = b;
		this.instance = new TestCheckedConstructor();
	}
	
	A(B b, TestCheckedConstructor instance) {
		this.b = b;
		this.instance = instance;
	}
	
}
package classes;

import rolez.annotation.Checked;
import rolez.annotation.Task;

@Checked
public class A {
	
	B b;
	
	A() {
		this.b = new B();
	}
	
	void setB(B b) {
		this.b = b;
	}
	
	B getB() {
		return this.b;
	}
}
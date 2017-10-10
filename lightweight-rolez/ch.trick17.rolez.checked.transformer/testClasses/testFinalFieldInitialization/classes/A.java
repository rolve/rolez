package classes;

import rolez.annotation.Checked;
import rolez.annotation.Roleztask;

@Checked
public class A {

	final String s;
	
	A() {
		this.s = "Hello world!";
	}

	@Roleztask
	void task(boolean $asTask) {
		System.out.println(this.s);
	}
}

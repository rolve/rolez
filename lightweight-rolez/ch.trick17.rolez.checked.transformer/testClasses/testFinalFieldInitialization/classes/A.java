package classes;

import rolez.annotation.Checked;
import rolez.annotation.Task;

@Checked
public class A {

	final String s;
	
	A() {
		this.s = "Hello world!";
	}

	A(String s) {
		this.s = s;
	}
	
	@Task
	void task(boolean $asTask) {
		System.out.println(this.s);
	}
}

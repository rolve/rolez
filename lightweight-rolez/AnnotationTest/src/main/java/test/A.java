package test;

import java.util.Random;

import rolez.annotation.Checked;
import rolez.annotation.Readonly;
import rolez.annotation.Task;

@Checked
public class A {
	int value;
	
	// Reference to whitelisted class
	String s;
	
	// Refernce to checked class
	B b;
	
	// Illegal references to unchecked classes (uncomment to see effect)
	//D d;
	//Random r;
	
	public int getValue() {
		return this.value;
	}

	public void setValue(int value) {
		this.value = value;
	}
	
	@Task
	public void task(@Readonly A a, final boolean $asTask) {
		int i = 1;
	}
}

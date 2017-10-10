package test;

import java.util.Random;

import rolez.annotation.Checked;
import rolez.annotation.Readonly;
import rolez.annotation.Roleztask;

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
	
	@Roleztask
	public void task(@Readonly A a, final boolean $asTask) {
		int i = 1;
	}
}

/*
import static rolez.lang.Guarded.*; 
 
public class A extends rolez.lang.Guarded {
	
	public int value;
	
	public A (final long $task) {
		super();
	}
	                
	public int getValue(final long $task) {
	    return this.value;
	}
	
	// DOES THE VALUE PARAMETER HAVE TO BE FINAL??
	public int setValue(final int value, final long $task) {
		this.value = value;
	}
}
*/
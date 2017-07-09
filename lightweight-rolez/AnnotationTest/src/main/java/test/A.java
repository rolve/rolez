package test;

import rolez.annotation.Checked;

@Checked
public class A {
	int value;

	public int getValue() {
		return this.value;
	}

	public void setValue(int value) {
		this.value = value;
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
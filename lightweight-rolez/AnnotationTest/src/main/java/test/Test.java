package test;

import rolez.annotation.*;
import java.util.Random;

/**
 * Class to test annotations for different Rolez tasks. Annotated elements are compiled into tasks from the Rolez runtime library and executed in separate threads.
 * 
 * @author Michael Giger
 *
 */

@Checked
public class Test {

	private A a;
	private B b;
	
	public Test() {
		this.a = new A();
		this.b = new B();
	}
	
	public void run() {
		
	}
	
	@Readwrite
	@Task
	private void thisWrite(final boolean $asTask) {
		this.b = new B();
	}
	
	@Task
	private void readwriteTask(@Readwrite A a, final boolean $asTask) {
		a.setValue(1);
	}

	@Task
	private void twoParamsTask(@Readwrite A a, @Readonly B b, final boolean $asTask) {
		int i = b.getValue();
		a.setValue(i);
	}
	
	@Task
	private void nestedTask(@Readwrite A a, final boolean $asTask) {
		readonlyTask(a, true);
	}
	
	@Task
	private void illegalTask(@Readonly A a, final boolean $asTask) {
		a.setValue(1);
	}
	
	@Task
	private void readonlyTask(@Readonly A a, final boolean $asTask) {
		int i = a.getValue();
	}
}

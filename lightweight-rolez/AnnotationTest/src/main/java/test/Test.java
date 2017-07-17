package test;

import rolez.annotation.*;
import rolez.checked.util.Random;

/**
 * Class to test annotations for different Rolez tasks. Annotated elements are compiled into tasks from the Rolez runtime library and executed in separate threads.
 * 
 * @author Michael Giger
 *
 */

public class Test {

	private A a;
	private B b;
	
	public Test() {
		this.a = new A();
		this.b = new B();
	}
	
	public void run() {
		
	}


	@Readonly
	@Roleztask
	private A thisRead(final boolean $asTask) {
		return this.a;
	}
	
	@Readwrite
	@Roleztask
	private void thisWrite(final boolean $asTask) {
		this.b = new B();
	}

	@Roleztask
	private int randomTask(@Readonly Random r, final boolean $asTask) {
		return r.nextInt();
	}
	
	@Roleztask
	private String stringTask(@Readonly String s, final boolean $asTask) {
		return s.concat("foo");
	}
	
	@Roleztask
	private void readwriteTask(@Readwrite A a, final boolean $asTask) {
		a.setValue(1);
	}

	@Roleztask
	private void twoParamsTask(@Readwrite A a, @Readonly B b, final boolean $asTask) {
		int i = b.getValue();
		a.setValue(i);
	}
	
	@Roleztask
	private void nestedTask(@Readwrite A a, final boolean $asTask) {
		readonlyTask(a, true);
	}
	
	//TODO: Can tasks return something?
	@Roleztask
	private int readonlyReturnTask(@Readonly A a, final boolean $asTask) {
		return a.getValue();
	}
	
	//TODO: Illegal operation, how and moreover WHEN to handle this?
	@Roleztask
	private void illegalTask(@Readonly A a, final boolean $asTask) {
		a.setValue(1);
	}
	
	@Roleztask
	private void readonlyTask(@Readonly A a, final boolean $asTask) {
		int i = a.getValue();
	}

	/*
	public void readonlyTask(final A a, final long $task) {
		int i = a.getValue();
	}
    
    public rolez.lang.Task<java.lang.Void> $readonlyTaskTask() {
        return new rolez.lang.Task<java.lang.Void>(new Object[]{}, new Object[]{a}) {
            @java.lang.Override
            protected java.lang.Void runRolez() {
                final long $task = idBits();
                int i = a.getValue();
                return null;
            }
        };
    }
	*/
}

package test;

import rolez.annotation.*;

public class Test {

	private A a;
	private B b;
	
	public Test() {
		this.a = new A();
		this.b = new B();
	}
	
	public void run() {
		rolezTask(a, b);
	}

	@Roleztask
	private void rolezTask(@Readwrite A a, @Readonly B b) {

	}

	@Roleztask
	private void anotherTask(@Readonly A a) {
		
	}
	
	@Roleztask
	private void foo(@Readwrite A a) {
		
	}
	
}

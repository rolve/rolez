package test;

import rolez.annotation.Roleztask;

public class Test {

	public void run() {
		final A a = new A();
		final B b = new B();
		rolezTask(a, b);
	}

	@Roleztask(readonly={"b"}, readwrite={"a"}) 
	private void rolezTask(final A a, final B b) {
		Thread t = new Thread() {
			public void run() {
				int i = b.getValue();
				a.setValue(i);
				
				// Start another task
				anotherTask(a);
			}
		};
		t.start();
	}

	@Roleztask(readonly={},readwrite={"a"})
	private void anotherTask(final A a) {
		Thread t = new Thread() {
			public void run() {
				a.setValue(2);
			}
		};
		t.start();
	}
}

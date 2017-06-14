package test;

import rolez.annotation.Roleztask;

public class Main {

	public static void main(String[] args) {

		final A a = new A();
		final B b = new B();
		rolezTask(a, b);
	}

	@Roleztask(readonly={"b"}, readwrite={"a"}) 
	public static void rolezTask(final A a, final B b) {
		
		Thread t = new Thread() {

			public void run() {
				a.setValue(1);
			}
			
		};
		t.start();
	}
}

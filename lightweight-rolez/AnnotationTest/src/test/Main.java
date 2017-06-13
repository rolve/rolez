package test;

import rolez.annotation.Roleztask;

public class Main {
	
	public static void main(String[] args) {
		
		final A a = new A();
		
		rolezTask(a);
	}
	
	@Roleztask(readwrite={"a"}) 
	public static void rolezTask(final A a) {
		Thread t = new Thread() {
			public void run() {
				a.setValue(1);
			}
		};
		t.start();
	}
}

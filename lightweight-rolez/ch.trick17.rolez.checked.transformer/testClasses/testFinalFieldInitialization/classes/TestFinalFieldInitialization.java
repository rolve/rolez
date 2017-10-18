package classes;

import rolez.annotation.Checked;

@Checked
public class TestFinalFieldInitialization {

	final String s;
	
	public TestFinalFieldInitialization() {
		this.s = "Foo!";
	}
	
	public TestFinalFieldInitialization(String s) {
		this.s = s;
	}
	
	public static void main(String[] args) {
		TestFinalFieldInitialization instance = new TestFinalFieldInitialization("Hello world!");
		System.out.println(instance.s);
		
		A a = new A("Hello world!");
		a.task(true);
	}
}

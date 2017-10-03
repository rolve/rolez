package classes;

import rolez.annotation.Checked;

@Checked
public class TestConstructor {

	String s;
	
	TestConstructor() {
		s = "Hello world!";
	}
	
	TestConstructor(String s) {
		this.s = s;
	}
	
	public static void main(String[] args) {
		TestConstructor instance1 = new TestConstructor();
		System.out.println(instance1.s);
		TestConstructor instance2 = new TestConstructor("Hello world!");
		System.out.println(instance2.s);
	}
}

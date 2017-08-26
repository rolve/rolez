package test;

import java.util.Random;

import rolez.annotation.Checked;
import rolez.annotation.Roleztask;

@Checked
public class Test {

	B b;
	
	public static void main(String[] args) {
		Random r = new Random();
		Test test = new Test();
		test.test(r.nextBoolean());
		
	}
	
	@Roleztask
	void test(final boolean $asTask) {
		A a = new A();	// Checked object
		a.foo = 2;
		a.bar(42, $asTask);
		System.out.println(a.foo);
	}
}

package classes;

import rolez.annotation.Checked;
import rolez.annotation.Task;
import rolez.annotation.Readwrite;
import rolez.annotation.Readonly;

import packageA.A;
import packageB.B;

@Checked
public class TestDifferentPackages {

	public static void main(String[] args) {
		TestDifferentPackages instance = new TestDifferentPackages();
		A a = new A();
		B b = new B();
		instance.task(a, b, true);
	}
	
	@Task
	void task(@Readwrite A a, @Readonly B b, boolean $asTask) {
		a.s = "Hello hell!";
		System.out.println(a.s);
		System.out.println(b.s);
	}
}

package classes;

import rolez.annotation.Checked;
import rolez.annotation.Task;
import rolez.annotation.Readwrite;

@Checked
public class TestReadwrite {
	
	public static void main(String[] args) {
		TestReadwrite instance = new TestReadwrite();
		A a = new A();
		instance.task(a, true);
	}
	
	@Task
	void task(@Readwrite A a, boolean $asTask) {
		a.message = "Hello world!";
		System.out.println(a.message);
	}
}

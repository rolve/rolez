package classes;

import rolez.annotation.Checked;
import rolez.annotation.Task;
import rolez.annotation.Readwrite;

@Checked
public class TestInterfaces {

	public static void main(String[] args) {
		TestInterfaces instance = new TestInterfaces();
		Interface a = new A();
		a.task(true);
		// Have to cast to a checked type
		instance.task((A)a, true);
	}
	
	// Cannot have the Interface as parameter type since the interface is not checked
	@Task
	void task(@Readwrite A a, boolean $asTask) {
		a.s = "Hello hell!";
		System.out.println(a.s);
	}
}

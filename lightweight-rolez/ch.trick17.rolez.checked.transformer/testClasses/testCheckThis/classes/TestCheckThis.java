package classes;

import rolez.annotation.Checked;
import rolez.annotation.Task;
import rolez.annotation.Readwrite;

@Checked
public class TestCheckThis {

	String s = "Hello world!";
	
	public static void main(String[] args) {
		TestCheckThis instance = new TestCheckThis();
		System.out.println(instance.s);
		instance.task(true);
		System.out.println(instance.s);
		instance.s = "Hello heaven!";
		System.out.println(instance.s);
	}
	
	@Readwrite
	@Task
	void task(boolean $asTask) {
		this.s = "Hello hell!";
	}
}

package classes;

import rolez.annotation.Checked;
import rolez.annotation.Task;

@Checked
public class A {

	@Task
	void task(boolean $asTask) {
		System.out.println("Hello world!");
	}
}

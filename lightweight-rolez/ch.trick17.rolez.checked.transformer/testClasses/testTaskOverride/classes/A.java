package classes;

import rolez.annotation.Checked;
import rolez.annotation.Task;
import rolez.annotation.Readonly;

@Checked
public class A {

	String message = "Hello world!";
	
	@Task
	void task(@Readonly A a, boolean $asTask) {
		System.out.println(a.message);
	}
}

package classes;

import rolez.annotation.Checked;
import rolez.annotation.Roleztask;
import rolez.annotation.Readonly;

@Checked
public class A {

	String message = "Hello world!";
	
	@Roleztask
	void task(@Readonly A a, boolean $asTask) {
		System.out.println(a.message);
	}
}

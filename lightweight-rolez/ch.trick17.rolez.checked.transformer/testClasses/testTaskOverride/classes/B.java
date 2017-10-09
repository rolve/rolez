package classes;

import rolez.annotation.Checked;
import rolez.annotation.Roleztask;
import rolez.annotation.Readwrite;

@Checked
public class B extends A {
	
	void task(@Readwrite A a, boolean $asTask) {
		a.message = "Hello hell!";
		System.out.println(a.message);
	}
}

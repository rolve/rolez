package classes;

import rolez.annotation.Checked;
import rolez.annotation.Roleztask;

@Checked
public class A {

	@Roleztask
	void task(boolean $asTask) {
		System.out.println("Hello world!");
	}
}
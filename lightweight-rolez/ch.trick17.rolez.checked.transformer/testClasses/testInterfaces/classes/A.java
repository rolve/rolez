package classes;

import rolez.annotation.Checked;
import rolez.annotation.Roleztask;
import rolez.annotation.Readonly;

@Checked
class A implements Interface {
	
	String s = "Hello world!";
	
	@Readonly
	@Roleztask
	public void task(boolean $asTask) {
		System.out.println(this.s);
	}
	
}
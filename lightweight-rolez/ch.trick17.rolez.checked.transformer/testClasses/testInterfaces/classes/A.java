package classes;

import rolez.annotation.Checked;
import rolez.annotation.Task;
import rolez.annotation.Readonly;

@Checked
class A implements Interface {
	
	String s = "Hello world!";
	
	@Readonly
	@Task
	public void task(boolean $asTask) {
		System.out.println(this.s);
	}
	
}
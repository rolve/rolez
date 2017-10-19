package classes;

import java.util.Random;

import rolez.annotation.Checked;
import rolez.annotation.Task;
import rolez.annotation.Readonly;

@Checked
class A implements Interface {
	
	String s = "Hello world!";
	
	@Readonly
	@Task
	public void task(@Readonly Random r, boolean $asTask) {
		boolean b = r.nextBoolean();
		
		if (b || !b)
			System.out.println(this.s);
	}
	
}
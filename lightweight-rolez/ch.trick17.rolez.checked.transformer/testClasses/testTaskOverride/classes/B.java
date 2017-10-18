package classes;

import rolez.annotation.Checked;
import rolez.annotation.Task;
import rolez.annotation.Readonly;

@Checked
public class B extends A {
	
	@Task
	void task(@Readonly A a, boolean $asTask) {
		String message = a.message;
		String newMessage = message.split(" ")[0] + " hell!";
		System.out.println(newMessage);
	}
}

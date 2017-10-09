package classes;

import rolez.annotation.Checked;
import rolez.annotation.Roleztask;
import rolez.annotation.Readonly;

@Checked
public class B extends A {
	
	@Roleztask
	void task(@Readonly A a, boolean $asTask) {
		String message = a.message;
		String newMessage = message.split(" ")[0] + " hell!";
		System.out.println(newMessage);
	}
}

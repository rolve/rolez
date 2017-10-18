package test;

import rolez.annotation.Checked;
import rolez.annotation.Readonly;
import rolez.annotation.Task;

@Checked
public class C extends B {

	@Task
	public void task(@Readonly A a, final boolean $asTask) {
		int i = 2;
	}
}

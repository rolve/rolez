package test;

import rolez.annotation.Checked;
import rolez.annotation.Readonly;
import rolez.annotation.Roleztask;

@Checked
public class C extends B {

	@Roleztask
	public void task(@Readonly A a, final boolean $asTask) {
		int i = 2;
	}
}

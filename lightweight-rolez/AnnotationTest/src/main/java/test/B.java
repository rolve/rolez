package test;
	
import rolez.annotation.Checked;
import rolez.annotation.Readonly;
import rolez.annotation.Roleztask;

@Checked
public class B extends A {

	@Roleztask
	public void task(@Readonly A a, final boolean $asTask) {
		int i = 2;
	}
}

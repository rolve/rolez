package test;
	
import rolez.annotation.Checked;
import rolez.annotation.Roleztask;

@Checked
public class B extends A {

	@Roleztask
	private void task(final boolean $asTask) {
		int i = 1;
	}
}

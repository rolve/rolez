package classes;

import rolez.annotation.Checked;
import rolez.annotation.Roleztask;

@Checked
public class TestTryCatch {

	public static void main(String[] args) {
		try {
			TestTryCatch instance = new TestTryCatch();
			instance.task(true);
			A a = new A();
			a.task(true);
		} catch (Exception e) { }
	}
	
	@Roleztask
	void task(boolean $asTask) {
		
	}
}

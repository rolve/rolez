package classes;

import rolez.annotation.Checked;
import rolez.annotation.Roleztask;

@Checked
public class TestConstructorTaskCall {

	TestConstructorTaskCall() {
		task(true);
	}
	
	public static void main(String[] args) {

	}
	
	@Roleztask
	void task(boolean $asTask) {
		
	}
}

package classes;

import rolez.annotation.Checked;
import rolez.annotation.Task;
import rolez.annotation.Readwrite;

import rolez.checked.util.Random;

@Checked
public class TestWrapperClassUsage {

	Random rf;
	
	public static void main(String[] args) {
		TestWrapperClassUsage instance = new TestWrapperClassUsage();
		instance.rf = new Random();
		instance.rf.nextInt();
		Random r = instance.rf;
		if (r instanceof Random)
			instance.task(r, true);
	}
	
	@Task
	void task(@Readwrite Random r, boolean $asTask) {
		System.out.println("Hello world!");
		java.util.Random jr = r.getUncheckedWriteInstance();
		int i = javaUtilRandomMethod(jr);
	}
	
	int javaUtilRandomMethod(java.util.Random r) {
		System.out.println("Hello world!");
		return r.nextInt();
	}
}

package classes;

import rolez.annotation.Checked;
import rolez.annotation.Task;
import rolez.annotation.Readwrite;

@Checked
public class TestWrapperClassUsage {

	rolez.checked.util.Random rf;
	
	public static void main(String[] args) {
		TestWrapperClassUsage instance = new TestWrapperClassUsage();
		
		rolez.checked.util.Random rl = new rolez.checked.util.Random();
		int i = rl.nextInt();
		
		instance.rf = new rolez.checked.util.Random();
		instance.rf.nextInt();
		rolez.checked.util.Random r = instance.rf;
		if (r instanceof rolez.checked.util.Random)
			instance.task(r, true);
	}
	
	@Task
	void task(@Readwrite rolez.checked.util.Random r, boolean $asTask) {
		System.out.println("Hello world!");
		java.util.Random jr = r.getUncheckedWriteInstance();
		int i = javaUtilRandomMethod(jr);
	}
	
	int javaUtilRandomMethod(java.util.Random r) {
		System.out.println("Hello world!");
		return r.nextInt();
	}
}

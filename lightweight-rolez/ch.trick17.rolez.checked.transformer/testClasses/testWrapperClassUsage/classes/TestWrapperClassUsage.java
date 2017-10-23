package classes;

import rolez.annotation.Checked;
import rolez.annotation.Task;
import rolez.annotation.Readwrite;

import java.util.Random;

@Checked
public class TestWrapperClassUsage {

	public static void main(String[] args) {
		TestWrapperClassUsage instance = new TestWrapperClassUsage();
		Random r = new Random();
		if (r instanceof Random)
			instance.task(r, true);
	}
	
	@Task
	void task(@Readwrite Random r, boolean $asTask) {
		r.nextInt();
		System.out.println("Hello world!");
	}
}

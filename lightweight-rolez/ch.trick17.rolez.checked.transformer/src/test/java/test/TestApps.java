package test;

import org.junit.Test;

import test.util.Pipeline;

public class TestApps {

	@Test
	public void quicksortChecked() {
		String methodName = "quicksortChecked";
		String mainClass = "classes.QuicksortChecked";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.runDefault(true, false);
	}

	@Test
	public void ideaChecked() {
		String methodName = "ideaChecked";
		String mainClass = "classes.IdeaChecked";
		Pipeline p = new Pipeline(methodName, mainClass);
		p.runDefault(true, false);
	}
}

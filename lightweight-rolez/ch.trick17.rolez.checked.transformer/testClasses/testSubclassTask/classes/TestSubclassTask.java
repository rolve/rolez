package classes;

import rolez.annotation.Checked;

@Checked
public class TestSubclassTask {

	public static void main(String[] args) {
		B b = new B();
		b.task(true);
	}
}

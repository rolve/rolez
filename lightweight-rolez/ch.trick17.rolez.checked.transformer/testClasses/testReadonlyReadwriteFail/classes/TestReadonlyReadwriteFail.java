package classes;

import rolez.annotation.Checked;
import rolez.annotation.Readonly;
import rolez.annotation.Readwrite;
import rolez.annotation.Task;

@Checked
public class TestReadonlyReadwriteFail {

	public static void main(String[] args) {
		TestReadonlyReadwriteFail instance = new TestReadonlyReadwriteFail();
		A a = new A();
		instance.readonly(a, true);
	}
	
	@Task
	void readonly(@Readonly A a, boolean $asTask) {
		int j = a.i;
		this.readwrite(a, true);
	}
	
	@Task
	void readwrite(@Readwrite A a, boolean $asTask) {
		a.i = 2;
	}
}

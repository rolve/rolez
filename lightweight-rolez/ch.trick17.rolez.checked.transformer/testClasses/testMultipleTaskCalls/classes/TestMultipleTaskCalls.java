package classes;

public class TestMultipleTaskCalls {

	public static void main(String[] args) {
		TestMultipleTaskCalls instance = new TestMultipleTaskCalls();
		instance.task(false);
		instance.task(true);
		instance.task(true);
		instance.task(false);
		instance.task(true);
		instance.task(true);
		instance.task(false);
		instance.task(false);
	}
	
	void task(boolean $asTask) {
		System.out.println("Hello world!");
	}
}

package classes;

public class TestTaskOverride {

	public static void main(String[] args) {
		B b0 = new B();
		b0.task(true);
		
		A b1 = new B();
		b1.task(true);
		
		A a = new A();
		a.task(true);
	}
}

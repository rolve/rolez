package classes;

public class TestTaskOverride {

	public static void main(String[] args) {
		
		B b0 = new B();
		b0.task(b0, true);
		
		// Guarantee output order
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			
		}
		
		A b1 = new B();
		b1.task(b1, true);

		// Guarantee output order
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			
		}
		
		A a = new A();
		a.task(a, true);
	}
}

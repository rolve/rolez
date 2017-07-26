package rolez.checked.transformer;

public class Test {
	
	@MyAnnotation
	public static void main(String[] args) {
		for (String arg : args) {
			System.out.println(arg);
		}
		System.out.println("Hello");
	}
}

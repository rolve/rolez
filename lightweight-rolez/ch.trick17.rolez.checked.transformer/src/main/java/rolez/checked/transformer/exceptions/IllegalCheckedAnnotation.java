package rolez.checked.transformer.exceptions;

public class IllegalCheckedAnnotation extends Exception {
	// Parameterless Constructor
    public IllegalCheckedAnnotation() {}

    // Constructor that accepts a message
    public IllegalCheckedAnnotation(String message) {
       super(message);
    }
}

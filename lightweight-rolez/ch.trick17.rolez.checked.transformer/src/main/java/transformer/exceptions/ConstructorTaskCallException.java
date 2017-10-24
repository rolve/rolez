package transformer.exceptions;

public class ConstructorTaskCallException extends RuntimeException {

	public ConstructorTaskCallException() { super(); }
	
	public ConstructorTaskCallException(String message) { super(message); }
}

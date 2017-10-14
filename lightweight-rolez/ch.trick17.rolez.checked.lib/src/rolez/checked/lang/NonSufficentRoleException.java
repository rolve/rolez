package rolez.checked.lang;

public class NonSufficentRoleException extends RuntimeException {

	public NonSufficentRoleException() {}
	
	public NonSufficentRoleException (String message) {
		super(message);
	}
}

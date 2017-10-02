package rolez.checked.lang;

public class NonSufficentRoleException extends RuntimeException {

	public NonSufficentRoleException() {}
	
	public NonSufficentRoleException(String message) {
		super(message);
	}
	
	public NonSufficentRoleException(Role declared, Checked object) {
		super("Cannot perform read operation on " + object.toString() + " declared role is "
				 + declared.toString() + ".");
	}
}

package rolez.checked.lang;

import rolez.lang.Guarded;
import static rolez.lang.Task.currentTask;

public class Checked extends Guarded {
	
	protected Checked() {
		super();
	}
	
	protected Checked(boolean initializeGuarding) {
		super(initializeGuarding);
	}
	
	
	public static <G extends Checked> G checkLegalRead(G checked, Role declaredRole) {
		// Case 1: Operation not allowed because of the declared role
		if (declaredRole == Role.PURE) {
			throw new RuntimeException("Cannot perform read operation on " + checked.toString() + " declared role is "
									 + declaredRole.toString() + ".");
		}

		// Case 2: Current role is less permissive as the declared role
		Role currentRole = getCurrentRole(checked);
		if (currentRole == Role.PURE) {
			return guardReadOnly(checked);
		}

		// Case 3: Current role matches the declared role
		return checked;
	}
	
	public static <G extends Checked> G checkLegalWrite(G checked, Role declaredRole) {
		// Case 1: Operation not allowed because of the declared role
		if (declaredRole == Role.PURE || declaredRole == Role.READONLY) {
			throw new RuntimeException("Cannot perform write operation on " + checked.toString() + " declared role is "
									 + declaredRole.toString() + ".");
		}

		// Case 2: Current role is less permissive as the declared role
		Role currentRole = getCurrentRole(checked);
		if (currentRole == Role.PURE || currentRole == Role.READONLY) {
			return guardReadWrite(checked);
		}
		
		// Case 3: Current role matches the declared role
		return checked;
	}
	
	private static <G extends Checked> Role getCurrentRole(G checked) {
		if (((Guarded)checked).getSharedCount().get() > 0) {
			return Role.READONLY;
		}
		if (((Guarded)checked).getOwner() == currentTask()) {
			return Role.READWRITE;
		}
		return Role.PURE;
	}
}
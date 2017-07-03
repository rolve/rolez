package rolez.checked.lang;

import rolez.lang.Guarded;
import static rolez.lang.Task.currentTask;

public class Checked extends Guarded {
	
	public Checked() {
		super();
	}
	
	protected Checked(boolean initializeGuarding) {
		super(initializeGuarding);
	}
	
	
	public static <G extends Guarded> G checkLegalRead(G guarded, Role declaredRole) {
		// Case 1: Operation not allowed because of the declared role
		if (declaredRole == Role.PURE) {
			throw new RuntimeException("Cannot perform read operation on " + guarded.toString() + " declared role is "
									 + declaredRole.toString() + ".");
		}

		// Case 2: Current role is less permissive as the declared role
		Role currentRole = getCurrentRole(guarded);
		if (currentRole == Role.PURE) {
			return guardReadOnly(guarded);
		}

		// Case 3: Current role matches the declared role
		return guarded;
	}
	
	public static <G extends Guarded> G checkLegalWrite(G guarded, Role declaredRole) {
		// Case 1: Operation not allowed because of the declared role
		if (declaredRole == Role.PURE || declaredRole == Role.READONLY) {
			throw new RuntimeException("Cannot perform write operation on " + guarded.toString() + " declared role is "
									 + declaredRole.toString() + ".");
		}

		// Case 2: Current role is less permissive as the declared role
		Role currentRole = getCurrentRole(guarded);
		if (currentRole == Role.PURE || currentRole == Role.READONLY) {
			return guardReadWrite(guarded);
		}
		
		// Case 3: Current role matches the declared role
		return guarded;
	}
	
	private static <G extends Guarded> Role getCurrentRole(G guarded) {
		if (guarded.getOwner() == currentTask()) {
			return Role.READWRITE;
		}
		if (guarded.getSharedCount().get() > 0) {
			return Role.READONLY;
		}
		return Role.PURE;
	}
}
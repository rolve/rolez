package rolez.checked.lang;

import java.util.Set;

import rolez.lang.Guarded;
import rolez.lang.Task;
import static rolez.lang.Task.currentTask;

public class Checked extends Guarded {
	
	protected Checked() {
		super();
	}
	
	protected Checked(boolean initializeGuarding) {
		super(initializeGuarding);
	}
	
	
	public static <G extends Checked> G checkLegalRead(G checked) {
		Role declaredRole = getDeclaredRole(checked);
		if (declaredRole == Role.PURE) {
			throw new RuntimeException("Cannot perform read operation on " + checked.toString() + " declared role is "
									 + declaredRole.toString() + ".");
		}
		return guardReadOnly(checked);
	}
	
	public static <G extends Checked> G checkLegalWrite(G checked) {
		Role declaredRole = getDeclaredRole(checked);
		if (declaredRole == Role.PURE || declaredRole == Role.READONLY) {
			throw new RuntimeException("Cannot perform write operation on " + checked.toString() + " declared role is "
									 + declaredRole.toString() + ".");
		}
		return guardReadWrite(checked);
	}
	
	private static <G extends Checked> Role getDeclaredRole(G checked) {
		
		Set<Guarded> passed = currentTask().getPassedReachable();
		if (passed.contains(checked)) {
			return Role.READWRITE;
		}
		
		Set<Guarded> shared = currentTask().getSharedReachable();
		if (shared.contains(checked)) {
			return Role.READONLY;
		}
		
		if (checked.hasAncestorTaskAsOwner()) {
			return Role.PURE;
		}
		
		return Role.READWRITE;
	}
	
	protected <G extends Checked> boolean hasAncestorTaskAsOwner() {
		return currentTask().isDescendantOf(((Guarded)this).getOwner());
	}
	
	protected <G extends Checked> Role getCurrentRole() {
		if (((Guarded)this).getSharedCount() > 0) {
			return Role.READONLY;
		}
		if (((Guarded)this).getOwner() == currentTask()) {
			return Role.READWRITE;
		}
		return Role.PURE;
	}
}
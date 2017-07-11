package rolez.checked.lang;

import java.util.Set;

import rolez.lang.Guarded;
import static rolez.lang.Task.currentTask;

public class Checked extends Guarded {
	
	protected Checked() {
		super();
	}
	
	protected Checked(boolean initializeGuarding) {
		super(initializeGuarding);
	}
	
	public static <G extends Checked> G checkLegalRead(G checked) {
		Role declaredRole = checked.getDeclaredRole();
		if (declaredRole == Role.PURE) {
			throw new RuntimeException("Cannot perform read operation on " + checked.toString() + " declared role is "
									 + declaredRole.toString() + ".");
		}
		return guardReadOnly(checked);
	}
	
	public static <G extends Checked> G checkLegalWrite(G checked) {
		Role declaredRole = checked.getDeclaredRole();
		if (declaredRole == Role.PURE || declaredRole == Role.READONLY) {
			throw new RuntimeException("Cannot perform write operation on " + checked.toString() + " declared role is "
									 + declaredRole.toString() + ".");
		}
		return guardReadWrite(checked);
	}
	
	protected <G extends Checked> Role getDeclaredRole() {
		// Was passed to this task
		Set<Guarded> passed = currentTask().getPassedReachable();
		if (passed.contains((Guarded)this)) {
			return Role.READWRITE;
		}
		
		// Was shared with this task
		Set<Guarded> shared = currentTask().getSharedReachable();
		if (shared.contains((Guarded)this)) {
			return Role.READONLY;
		}
		
		// Was declared in an ancestor task but accessible in this task 
		// -> must therefore be shared pure
		if (this.hasAncestorTaskAsOwner()) {
			return Role.PURE;
		}
		
		// Was declared in this task and not shared or passed
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
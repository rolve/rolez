package rolez.checked.lang;

import java.util.Set;

import static rolez.checked.lang.Task.currentTask;

/**
 * Superclass of all Checked objects. This includes all the operations of
 * Guarded but provides additional functionality to check the legality of 
 * an operation at runtime.
 * 
 * @author Michael Giger
 *
 */
public class Checked extends Guarded {
	
	protected Checked() {
		super();
	}
	
	protected Checked(boolean initializeGuarding) {
		super(initializeGuarding);
	}
	
	public static <G extends Checked> G checkLegalRead(G checked) {
		checked.isLegalRead();
		return guardReadOnly(checked);
	}
	
	public static <G extends Checked> G checkLegalRead(G checked, long currentTaskIdBits) {
		checked.isLegalRead();
		return guardReadOnly(checked, currentTaskIdBits);
	}
	
	protected <G extends Checked> void isLegalRead() {
		Role declaredRole = this.getDeclaredRole();
		if (declaredRole == Role.PURE) {
			throw new RuntimeException("Cannot perform read operation on " + this.toString() + " declared role is "
									 + declaredRole.toString() + ".");
		}
	}
	
	public static <G extends Checked> G checkLegalWrite(G checked) {
		checked.isLegalWrite();
		return guardReadWrite(checked);
	}
	
	public static <G extends Checked> G checkLegalWrite(G checked, long currentTaskIdBits) {
		checked.isLegalWrite();
		return guardReadOnly(checked, currentTaskIdBits);
	}
	
	protected <G extends Checked> void isLegalWrite() {
		Role declaredRole = this.getDeclaredRole();
		if (declaredRole == Role.PURE || declaredRole == Role.READONLY) {
			throw new RuntimeException("Cannot perform write operation on " + this.toString() + " declared role is "
									 + declaredRole.toString() + ".");
		}
	}
	
	protected <G extends Checked> Role getDeclaredRole() {
		// Was passed to this task
		Set<Guarded> passed = currentTask().getPassedReachable();
		if (passed.contains(this))
			return Role.READWRITE;
		
		// Was shared with this task
		Set<Guarded> shared = currentTask().getSharedReachable();
		if (shared.contains(this))
			return Role.READONLY;
		
		// Was declared in an ancestor task but accessible in this task 
		// -> must therefore be shared pure
		if (hasAncestorTaskAsOwner())
			return Role.PURE;
		
		// Was declared in this task and not shared or passed
		return Role.READWRITE;
	}
	
	protected <G extends Checked> boolean hasAncestorTaskAsOwner() {
		return currentTask().isDescendantOf(getOwner());
	}
}
package rolez.checked.transformer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rolez.checked.transformer.exceptions.PhantomClassException;
import rolez.checked.transformer.util.Constants;
import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.Jimple;
import soot.jimple.VirtualInvokeExpr;
import soot.util.Chain;

/**
 * Transformer inserts methods {@link rolez.checked.lang.Checked#checkLegalRead(rolez.checked.lang.Checked) checkLegalRead} to checked
 * field reads and {@link rolez.checked.lang.Checked#checkLegalWrite(rolez.checked.lang.Checked) checkLegalWrite} to checked field writes.
 * 
 * @author Michael Giger
 *
 */
public class CheckingTransformer extends BodyTransformer {
	
	static final Logger logger = LogManager.getLogger(CheckingTransformer.class);
	
	static final Jimple J = Jimple.v();
	
	int tempLocalCount = 0;
	
	Chain<Local> locals;
	Chain<Unit> units;
	
	@Override
	protected void internalTransform(Body b, String phaseName, Map options) {
		
		SootMethod currentMethod = b.getMethod();
		
		// No task calls allowed in constructors -> no guarding necessary here
		if (isConstructorWithoutArgs(currentMethod)) return;

		// Field reads in guardedRefs don't have to be guarded
		if (isGuardedRefsMethod(currentMethod)) return;
		
		logger.debug("Transforming " + currentMethod.getDeclaringClass() + ":" + currentMethod.getSignature());
		
		this.locals = b.getLocals();
		this.units = b.getUnits();
		
		List<AssignStmt> fieldReads = findCheckedFieldReads();
		List<AssignStmt> fieldWrites = findCheckedFieldWrites();
		
		for (AssignStmt read : fieldReads)
			addCheckLegalRead(read);
		
		for (AssignStmt write: fieldWrites)
			addCheckLegalWrite(write);
		
		// Reset tempLocalCount for next method
		tempLocalCount = 0;
	}
	
	private void addCheckLegalRead(AssignStmt read) {
		InstanceFieldRef rightOp = (InstanceFieldRef) read.getRightOp();
		Local base = (Local) rightOp.getBase();
		Type baseType = base.getType();
		
		// Add locals
		Local checkedTemp = J.newLocal("checkedTemp" + Integer.toString(tempLocalCount), Constants.CHECKED_CLASS.getType());
		Local temp = J.newLocal("temp" + Integer.toString(tempLocalCount), baseType);
		locals.add(checkedTemp);
		locals.add(temp);
		tempLocalCount++;
		
		// Insert the call to checkLegalRead
		Unit checkStmt = J.newAssignStmt(checkedTemp, J.newStaticInvokeExpr(
				Constants.CHECKED_CLASS.getMethod("checkLegalRead", Arrays.asList(new Type[] { Constants.CHECKED_CLASS.getType() })).makeRef(), 
				Arrays.asList(new Local[] { base })));
		units.insertBefore(checkStmt, read);
		
		// Insert a cast, to cast the result of checkLegalRead to the right type again
		Unit castStmt = J.newAssignStmt(temp, J.newCastExpr(checkedTemp, baseType));
		units.insertBefore(castStmt, read);
		
		// Change base of original field read to checked variable
		read.setRightOp(J.newInstanceFieldRef(temp, rightOp.getFieldRef()));
	}
	
	private void addCheckLegalWrite(AssignStmt write) {
		InstanceFieldRef leftOp = (InstanceFieldRef) write.getLeftOp();
		Local base = (Local) leftOp.getBase();
		Type baseType = base.getType();
		
		Local checkedTemp = J.newLocal("checkedTemp" + Integer.toString(tempLocalCount), Constants.CHECKED_CLASS.getType());
		Local temp = J.newLocal("temp" + Integer.toString(tempLocalCount), baseType);
		locals.add(checkedTemp);
		locals.add(temp);
		tempLocalCount++;
		
		// Insert the call to checkLegalWrite
		Unit checkStmt = J.newAssignStmt(checkedTemp, J.newStaticInvokeExpr(
				Constants.CHECKED_CLASS.getMethod("checkLegalWrite", Arrays.asList(new Type[] { Constants.CHECKED_CLASS.getType() })).makeRef(), 
				Arrays.asList(new Local[] { base })));
		units.insertBefore(checkStmt, write);
		
		// Insert a cast, to cast the result of checkLegalRead to the right type again
		Unit castStmt = J.newAssignStmt(temp, J.newCastExpr(checkedTemp, baseType));
		units.insertBefore(castStmt, write);
		
		// Change base of original field read to checked variable
		write.setLeftOp(J.newInstanceFieldRef(temp, leftOp.getFieldRef()));
	}

	private List<AssignStmt> findCheckedFieldReads() {
		
		// Set containing all values which were checked to a point in the unit chain, task calls remove values from the set
		Set<Value> checkedValues = new HashSet<Value>();
		
		ArrayList<AssignStmt> result = new ArrayList<AssignStmt>();
		for (Unit u : units) {
			// Because jimple uses 3-adress code, field reads are always a separate assign statement
			if (u instanceof AssignStmt) {
				AssignStmt a = (AssignStmt)u;
				Value rightOp = a.getRightOp();
				if (rightOp instanceof InstanceFieldRef) {
					InstanceFieldRef f = (InstanceFieldRef)rightOp;
					
					//No checks for final fields necessary, since reading them is always allowed
					if (f.getField().isFinal())
						continue;
					
					Value base = f.getBase();
					if (isSubtypeOfChecked(base.getType()) && !checkedValues.contains(base)) {
						logger.debug(a + " is a checked field read!");
						result.add(a);
						
						checkedValues.add(base);
						logger.debug("Added " + base + " to checkedValues.");
					}
				} else {
					// Remove base and all args of task calls from the checked values, after the task call they have to be checked again
					if (isTaskCall(rightOp)) {
						VirtualInvokeExpr taskCall = (VirtualInvokeExpr)rightOp;
						Value base = taskCall.getBase();
						if (checkedValues.remove(base))
							logger.debug("Removed " + base + " from checkedValues.");
						for (Value arg : taskCall.getArgs()) {
							if (checkedValues.remove(arg))
								logger.debug("Removed " + arg + " from checkedValues.");
						}
					}
				}
			}
		}
		return result;
	}
	
	private List<AssignStmt> findCheckedFieldWrites() {
		
		// Set containing all values which were checked to a point in the unit chain, task calls remove values from the set
		Set<Value> checkedValues = new HashSet<Value>();
				
		ArrayList<AssignStmt> result = new ArrayList<AssignStmt>();
		for (Unit u : units) {
			// Because jimple uses 3-adress code, field reads are always a separate assign statement
			if (u instanceof AssignStmt) {
				AssignStmt a = (AssignStmt)u;
				Value leftOp = a.getLeftOp();
				if (leftOp instanceof InstanceFieldRef) {
					InstanceFieldRef f = (InstanceFieldRef)leftOp;
					Value base = f.getBase();
					if (isSubtypeOfChecked(base.getType()) && !checkedValues.contains(base)) {
						logger.debug(a + " is a checked field write!");
						result.add(a);
						
						checkedValues.add(base);
						logger.debug("Added " + base + " to checkedValues.");
					}
				} else {
					// Remove base and all args of task calls from the checked values, after the task call they have to be checked again
					Value rightOp = a.getRightOp();
					if (isTaskCall(rightOp)) {
						VirtualInvokeExpr taskCall = (VirtualInvokeExpr)rightOp;
						Value base = taskCall.getBase();
						if (checkedValues.remove(base))
							logger.debug("Removed " + base + " from checkedValues.");
						for (Value arg : taskCall.getArgs()) {
							if (checkedValues.remove(arg))
								logger.debug("Removed " + arg + " from checkedValues.");
						}
					}
				}
			}
		}
		return result;
	}
	
	private boolean isTaskCall(Value op) {
		if (op instanceof VirtualInvokeExpr) {
			VirtualInvokeExpr vi = (VirtualInvokeExpr)op;
			return vi.getMethod().getReturnType().equals(Constants.TASK_CLASS.getType()) && !vi.getMethod().getName().equals("start");
		}
		return false;
	}
	
	/**
	 * Returns <code>true</code> if <code>t</code> is a subtype of the class {@link rolez.checked.lang.Checked}
	 * and <code>false</code> otherwise.
	 * @param t
	 * @return
	 */
	private boolean isSubtypeOfChecked(Type t) {
		SootClass classOfType = Scene.v().loadClass(t.toString(), SootClass.HIERARCHY);
		
		if (classOfType.isPhantom())
			throw new PhantomClassException(classOfType + " is a phantom class in.");
		
		SootClass currentClass = classOfType;
		 while(!currentClass.equals(Constants.OBJECT_CLASS)) {
			if (currentClass.equals(Constants.CHECKED_CLASS)) 
				return true;
			currentClass = currentClass.getSuperclass();
		}
		return false;
	}
	
	private boolean isGuardedRefsMethod(SootMethod m) {
		return m.getSubSignature().equals("java.lang.Iterable guardedRefs()");
	}
	
	private boolean isConstructorWithoutArgs(SootMethod m) {
		return m.getSubSignature().equals("void <init>()");
	}
}

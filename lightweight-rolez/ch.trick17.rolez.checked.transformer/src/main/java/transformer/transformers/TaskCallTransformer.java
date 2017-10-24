package transformer.transformers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.SootClass;
import soot.SootMethod;
import soot.Trap;
import soot.Unit;
import soot.Value;
import soot.jimple.IdentityStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.NopStmt;
import soot.jimple.VirtualInvokeExpr;
import soot.util.Chain;
import transformer.exceptions.ConstructorTaskCallException;
import transformer.util.Constants;
import transformer.util.UnitFactory;
import transformer.util.Util;

public class TaskCallTransformer extends BodyTransformer {

	static final Logger logger = LogManager.getLogger(TaskCallTransformer.class);

	static final Jimple J = Jimple.v();
	
	Local tasksLocal0;
	Local tasksLocal1;
	Local taskSystemLocal;
	
	int numTaskCalls = 0;
	
	SootMethod currentMethod;
	
	@Override
	protected void internalTransform(Body b, String phaseName, Map options) {
		
		this.currentMethod = b.getMethod();
		
		logger.debug("Transforming " + this.currentMethod.getDeclaringClass() + ":" + this.currentMethod.getSignature());
		
		Chain<Local> locals = b.getLocals();
		Chain<Unit> units = b.getUnits();
		Chain<Trap> traps = b.getTraps();
		
		List<InvokeStmt> taskCalls = findTaskCalls(units);
		
		// Task calls in constructors are not allowed
		if (this.currentMethod.getName().equals("<init>") && taskCalls.size() > 0) {
			throw new ConstructorTaskCallException("Task calls in constructors are not allowed!");
			// TODO: catch this exception by allowing to call the task as a method -> insert "true" constant as last parameter
		}
		
		if (taskCalls.size() > 0) {
			insertTaskLocalsAndStmts(locals, units, traps);
			for (InvokeStmt i : taskCalls) {
				transformTaskCall(locals, units, i);
				numTaskCalls++;
			}
		}
		
		// Remove all inserted nop statements
		removeNopStmts(units);
		
		// Reset number of task calls
		numTaskCalls = 0;
	}
	
	private void insertTaskLocalsAndStmts(Chain<Local> locals, Chain<Unit> units, Chain<Trap> traps) {
		
		tasksLocal0 = J.newLocal("tasks0", Constants.INTERNAL_TASKS_CLASS.getType());
		tasksLocal1 = J.newLocal("tasks1", Constants.INTERNAL_TASKS_CLASS.getType());
		taskSystemLocal = J.newLocal("taskSystem", Constants.TASK_SYSTEM_CLASS.getType());
		locals.add(tasksLocal0);
		locals.add(tasksLocal1);
		locals.add(taskSystemLocal);
		
		Unit firstRealStmt = findFirstNonIdentityStmt(units);
		
		Unit tasks = UnitFactory.newAssignNewExpr(tasksLocal1, Constants.INTERNAL_TASKS_CLASS);
		units.insertBefore(tasks, firstRealStmt);
		
		Unit tasksConstructor = UnitFactory.newSpecialInvokeExpr(tasksLocal1, Constants.INTERNAL_TASKS_CLASS, "<init>");
		units.insertBefore(tasksConstructor, firstRealStmt);
		
		Unit tasksAfterConstruction = J.newAssignStmt(tasksLocal0, tasksLocal1);
		units.insertBefore(tasksAfterConstruction, firstRealStmt);
		
		Unit getTaskSystem = UnitFactory.newAssignStaticInvokeExpr(taskSystemLocal, Constants.TASK_SYSTEM_CLASS, "getDefault");
		units.insertBefore(getTaskSystem, firstRealStmt);
		
		// Labels slip up with units.insertBefore(), place them at firstRealStmt again
		for(Trap t : traps)
			if (t.getBeginUnit().equals(tasks))
				t.setBeginUnit(firstRealStmt);
		
		// Surround by try finally
		Local throwable0 = J.newLocal("throwable0", Constants.THROWABLE_CLASS.getType());
		locals.add(throwable0);
		Local throwable1 = J.newLocal("throwable1", Constants.THROWABLE_CLASS.getType());
		locals.add(throwable1);
		
		Unit caughtException = J.newIdentityStmt(throwable0, J.newCaughtExceptionRef());
		Unit assignThwoable = J.newAssignStmt(throwable1, throwable0);
		Unit invokeJoinAll0 = J.newInvokeStmt(J.newVirtualInvokeExpr(tasksLocal0, Constants.INTERNAL_TASKS_CLASS.getMethodByName("joinAll").makeRef()));
		Unit throwStmt = J.newThrowStmt(throwable1);
		Unit invokeJoinAll1 = J.newInvokeStmt(J.newVirtualInvokeExpr(tasksLocal0, Constants.INTERNAL_TASKS_CLASS.getMethodByName("joinAll").makeRef()));
		Unit gotoStmt = J.newGotoStmt(invokeJoinAll1);
		
		Unit lastStmt = units.getLast();
		units.insertBefore(gotoStmt, lastStmt);
		units.insertBefore(caughtException, lastStmt);
		units.insertBefore(assignThwoable, lastStmt);
		units.insertBefore(invokeJoinAll0, lastStmt);
		units.insertBefore(throwStmt, lastStmt);
		units.insertBefore(invokeJoinAll1, lastStmt);
		
		Trap trap = J.newTrap(Constants.THROWABLE_CLASS, firstRealStmt, caughtException, caughtException);
		traps.add(trap);
	}
	
	private Unit findFirstNonIdentityStmt(Chain<Unit> units) {
		for (Unit u : units)
			if (!(u instanceof IdentityStmt))
				return u;
		
		// This point should never be reached because there is at least the task call stmt that was found
		throw new RuntimeException("There was no non-identity statement found!");
	}
	
	private void transformTaskCall(Chain<Local> locals, Chain<Unit> units, InvokeStmt taskCall) {
		InvokeExpr invokeExpr = taskCall.getInvokeExpr();
		VirtualInvokeExpr vInvokeExpr = (VirtualInvokeExpr) invokeExpr;
		Local base = ((Local) vInvokeExpr.getBase());
		SootMethod invokedMethod = invokeExpr.getMethod();
		SootClass declaringClass = invokedMethod.getDeclaringClass();
		List<Value> args = invokeExpr.getArgs();
		Value booleanValue = args.get(args.size()-1);
		
		// Insert nop stmts before and after the task call to prevent weird behavior with labels
		Unit nopBefore = J.newNopStmt();
		Unit nopAfter = J.newNopStmt();
		units.insertBefore(nopBefore, taskCall);
		units.insertAfter(nopAfter, taskCall);
		
		Unit ifStmt = J.newIfStmt(J.newEqExpr(booleanValue, IntConstant.v(0)), taskCall);
		units.insertAfter(ifStmt, nopBefore);
		
		Local taskLocal0 = J.newLocal("$task" + Integer.toString(numTaskCalls), Constants.TASK_CLASS.getType());
		Local taskLocal1 = J.newLocal("task" + Integer.toString(numTaskCalls), Constants.TASK_CLASS.getType());
		locals.add(taskLocal0);
		locals.add(taskLocal1);

		 Unit getTaskFromTaskMethod = J.newAssignStmt(
				 taskLocal0,
				 J.newVirtualInvokeExpr(
						 base, 
						 declaringClass.getMethod(Util.getTaskMethodNameFromMethod(invokedMethod), invokedMethod.getParameterTypes()).makeRef(),
						 invokeExpr.getArgs()));
		units.insertAfter(getTaskFromTaskMethod, ifStmt);
		
		Unit startTask = UnitFactory.newAssignVirtualInvokeExpr(taskLocal1, taskSystemLocal, Constants.TASK_SYSTEM_CLASS, "start", new Local[] { taskLocal0 });
		units.insertAfter(startTask, getTaskFromTaskMethod);
		
		Unit addInline = UnitFactory.newVirtualInvokeExpr(tasksLocal0, Constants.INTERNAL_TASKS_CLASS, "addInline", new Local[] { taskLocal1 });
		units.insertAfter(addInline, startTask);
		
		Unit gotoSucc = J.newGotoStmt(nopAfter);
		units.insertAfter(gotoSucc, addInline);
	}
	
	private void removeNopStmts(Chain<Unit> units) {
		Iterator<Unit> unitIter = units.snapshotIterator(); 
		while (unitIter.hasNext()) {
			Unit u = unitIter.next();
			if (u instanceof NopStmt) {
				units.remove(u);
			}
		}
	}
	
	private List<InvokeStmt> findTaskCalls(Chain<Unit> units) {
		ArrayList<InvokeStmt> result = new ArrayList<InvokeStmt>();
		for (Unit u : units) {
			if (u instanceof InvokeStmt) {
				InvokeStmt i = (InvokeStmt)u;
				InvokeExpr ie = i.getInvokeExpr();
				if (ie instanceof VirtualInvokeExpr) {
					SootMethod method = i.getInvokeExpr().getMethod();
					if (Util.isTask(method)) {
						result.add(i);
					}
				}
			}
		}
		return result;
	}
}

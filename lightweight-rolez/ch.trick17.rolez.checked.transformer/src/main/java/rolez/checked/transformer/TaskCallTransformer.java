package rolez.checked.transformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rolez.checked.transformer.util.Constants;
import rolez.checked.transformer.util.UnitFactory;
import rolez.checked.transformer.util.Util;
import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.IdentityStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.util.Chain;

public class TaskCallTransformer extends BodyTransformer {

	static final Logger logger = LogManager.getLogger(TaskCallTransformer.class);

	static final Jimple J = Jimple.v();
	
	Local tasksLocal0;
	Local tasksLocal1;
	Local taskSystemLocal;
	
	int numOfTaskCalls = 0;
	
	@Override
	protected void internalTransform(Body b, String phaseName, Map options) {
		logger.debug("Transforming " + b.getMethod().getDeclaringClass() + ":" + b.getMethod().getSignature());
		
		Chain<Local> locals = b.getLocals();
		Chain<Unit> units = b.getUnits();
		
		List<InvokeStmt> taskCalls = findTaskCalls(units);
		
		if (taskCalls.size() > 0) {
			insertTaskLocalsAndStmts(locals, units);
			
			for (InvokeStmt i : taskCalls) {
				transformTaskCall(locals, units, i);
				
				numOfTaskCalls++;
			}
		}
		
		// Reset number of task calls
		numOfTaskCalls = 0;
	}
	
	private void insertTaskLocalsAndStmts(Chain<Local> locals, Chain<Unit> units) {
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
	}
	
	private Unit findFirstNonIdentityStmt(Chain<Unit> units) {
		for (Unit u : units) {
			if (!(u instanceof IdentityStmt)) {
				return u;
			}
		}
		// TODO: This point should never be reached because there is at least the task call stmt that was found
		throw new RuntimeException("There was no non-identity statement found!");
	}
	
	private void transformTaskCall(Chain<Local> locals, Chain<Unit> units, InvokeStmt taskCall) {
		InvokeExpr invokeExpr = taskCall.getInvokeExpr();
		SootMethod method = invokeExpr.getMethod();
		SootClass declaringClass = method.getDeclaringClass();
		List<Value> args = invokeExpr.getArgs();
		Value booleanValue = args.get(args.size()-1);

		Unit prevStmt = units.getPredOf(taskCall);
		Unit succStmt = units.getSuccOf(taskCall);
		
		Unit ifStmt = J.newIfStmt(J.newEqExpr(booleanValue, IntConstant.v(0)), taskCall);
		units.insertAfter(ifStmt, prevStmt);
		
		Local taskLocal0 = J.newLocal("$task" + Integer.toString(numOfTaskCalls), Constants.TASK_CLASS.getType());
		Local taskLocal1 = J.newLocal("task" + Integer.toString(numOfTaskCalls), Constants.TASK_CLASS.getType());
		locals.add(taskLocal0);
		locals.add(taskLocal1);
		
		Unit getTaskFromTaskMethod = J.newAssignStmt(taskLocal0,
				J.newVirtualInvokeExpr(
						locals.getFirst(),
						declaringClass.getMethodByName(Util.getTaskMethodNameFromMethod(method)).makeRef(),
						invokeExpr.getArgs()));
		units.insertAfter(getTaskFromTaskMethod, ifStmt);
		
		Unit startTask = UnitFactory.newAssignVirtualInvokeExpr(taskLocal1, taskSystemLocal, Constants.TASK_SYSTEM_CLASS, "start", new Local[] { taskLocal0 });
		units.insertAfter(startTask, getTaskFromTaskMethod);
		
		Unit addInline = UnitFactory.newVirtualInvokeExpr(tasksLocal0, Constants.INTERNAL_TASKS_CLASS, "addInline", new Local[] { taskLocal1 });
		units.insertAfter(addInline, startTask);
		
		Unit gotoSucc = J.newGotoStmt(succStmt);
		units.insertAfter(gotoSucc, addInline);
	}
	
	private void surroundByTryFinally(Chain<Local> locals, Chain<Unit> units) {
		
	}
	
	private List<InvokeStmt> findTaskCalls(Chain<Unit> units) {
		ArrayList<InvokeStmt> result = new ArrayList<InvokeStmt>();
		for (Unit u : units) {
			if (u instanceof InvokeStmt) {
				InvokeStmt i = (InvokeStmt)u;
				SootMethod method = i.getInvokeExpr().getMethod();
				if (Util.hasRoleztaskAnnotation(method)) {
					logger.debug(u + " --> THIS METHOD IS A TASK CALL!");
					result.add(i);
				}
			}
		}
		return result;
	}
}

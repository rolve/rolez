package rolez.checked.transformer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rolez.checked.transformer.task.InnerClass;
import rolez.checked.transformer.task.InnerClassConstructor;
import rolez.checked.transformer.task.InnerClassRunRolezConcrete;
import rolez.checked.transformer.task.InnerClassRunRolezObject;
import rolez.checked.transformer.task.TaskMethod;
import rolez.checked.transformer.util.ClassWriter;
import rolez.checked.transformer.util.Constants;
import rolez.checked.transformer.util.JimpleWriter;
import rolez.checked.transformer.util.UnitFactory;
import soot.Body;
import soot.BooleanType;
import soot.Local;
import soot.SootClass;
import soot.SootMethod;
import soot.Trap;
import soot.Unit;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.util.Chain;


/**
 * This class is used for generating the task methods from the annotated methods
 * in the user code.
 * 
 * @author Michael Giger
 *
 */
public class TaskGenerator {
	
	static final Logger logger = LogManager.getLogger(TaskGenerator.class);
	
	static final Jimple J = Jimple.v();
	
	private SootClass targetClass;
	private SootMethod sourceMethod;
	private SootClass innerClass;
	
	public TaskGenerator(SootClass targetClass, SootMethod sourceMethod) {
		this.targetClass = targetClass;
		this.sourceMethod = sourceMethod;
	}

	public void generateTask() {
		generateInnerClass();
		generateTaskMethod();
		transformSourceMethod();
		
		// TODO: Add tags to outer class s.t. the outer class knows about the newly created inner class and vice versa!
		// Outer class should contain tag of the following form: [inner=rolez/checked/transformer/test/Test$1, outer=null, name=null,flags=0]
		// Inner class should contain tags of the following form: Signature: Lrolez/checked/lang/Task<Ljava/lang/Void;>;
		//                                                        Enclosing Class: rolez/checked/transformer/test/Test Enclosing Method: $testTask Sig: (Lrolez/checked/transformer/test/A;Lrolez/checked/transformer/test/A;)Lrolez/checked/lang/Task;
		//                                                        [inner=rolez/checked/transformer/test/Test$1, outer=null, name=null,flags=0]
		
		JimpleWriter.write(innerClass);
		ClassWriter.write(innerClass);
	}

	private void generateInnerClass() {
		innerClass = new InnerClass(getClassNameFromMethod(), targetClass, sourceMethod);
		generateInnerClassConstructor();
		generateRunRolezMethods();
	}
	
	private void generateTaskMethod() {
		TaskMethod taskMethod = new TaskMethod(getTaskMethodNameFromMethod(), targetClass, innerClass, sourceMethod);
		targetClass.addMethod(taskMethod);
	}
	
	private void generateInnerClassConstructor() {
		InnerClassConstructor innerClassConstructor = new InnerClassConstructor(innerClass, targetClass, sourceMethod);
		innerClass.addMethod(innerClassConstructor);
	}
	
	private void generateRunRolezMethods() {
		InnerClassRunRolezConcrete concreteMethod = new InnerClassRunRolezConcrete(innerClass, sourceMethod);
		innerClass.addMethod(concreteMethod);
		innerClass.addMethod(new InnerClassRunRolezObject(innerClass, concreteMethod));		
	}
	
	/**
	 * Transforms the original method by inserting an if-statement at the beginning, which checks
	 * the <code>$asTask</code> variable. If <code>$asTask == true</code>, then the method gets a
	 * task and runs it. If <code>$asTask == false</code>, then the method executes the original 
	 * method body.
	 */
	private void transformSourceMethod() {
		Body body = sourceMethod.getActiveBody();
		int parameterCount = sourceMethod.getParameterCount();
		
		Chain<Local> locals = body.getLocals();
		Chain<Unit> units = body.getUnits();
		
		Iterator<Local> localIter = locals.iterator();
		List<Local> paramLocals = new ArrayList<Local>();
		
		// Jump over "this"
		Local thisLocal = localIter.next();
		
		for (int i = 0; i < parameterCount; i++) {
			paramLocals.add(localIter.next());
		}

		// TODO: Add rule in annotation processor that $asTask has to be the last parameter in the list!
		Local asTaskLocal = paramLocals.get(parameterCount - 1);
		
		assert(paramLocals.size() == parameterCount);
		assert(asTaskLocal.getType().equals(BooleanType.v()));
		
		// Add new locals for the task starting
		Local tasksLocal1 = J.newLocal("tasks", Constants.INTERNAL_TASKS_CLASS.getType());
		Local tasksLocal2 = J.newLocal("tasks", Constants.INTERNAL_TASKS_CLASS.getType());
		Local throwLocal1 = J.newLocal("throwable1", Constants.THROWABLE_CLASS.getType());
		Local throwLocal2 = J.newLocal("throwable2", Constants.THROWABLE_CLASS.getType());
		Local taskSystemLocal = J.newLocal("tasksystem", Constants.TASK_SYSTEM_CLASS.getType());
		Local taskLocal1 = J.newLocal("task1", Constants.TASK_CLASS.getType());
		Local taskLocal2 = J.newLocal("task2", Constants.TASK_CLASS.getType());
		locals.add(tasksLocal1);
		locals.add(tasksLocal2);
		locals.add(throwLocal1);
		locals.add(throwLocal2);
		locals.add(taskSystemLocal);
		locals.add(taskLocal1);
		locals.add(taskLocal2);
		
		// Iterator is used to find the last parameter assignment statement and the first "real" statement
		Iterator<Unit> unitIter = units.iterator();
		Unit lastParamStmt = null;
		unitIter.next();
		for (int i=0; i<parameterCount; i++)
			 lastParamStmt = unitIter.next();

		// This is the first task related stmt, it has to be added after the last stmt of the original body
		Unit internalTasksAssignment = UnitFactory.newAssignNewExpr(tasksLocal2, Constants.INTERNAL_TASKS_CLASS);
		units.insertAfter(internalTasksAssignment, units.getLast());
		
		// Insert the if statement as the very first statement after the parameter assignments
		Unit ifStmt = J.newIfStmt(J.newNeExpr(asTaskLocal, IntConstant.v(0)), internalTasksAssignment);
		units.insertAfter(ifStmt, lastParamStmt);

		Unit invokeTasksConstructor = UnitFactory.newSpecialInvokeExpr(tasksLocal2, Constants.INTERNAL_TASKS_CLASS, "<init>");
		units.insertAfter(invokeTasksConstructor, internalTasksAssignment);
		
		Unit assignTasksResult = J.newAssignStmt(tasksLocal1, taskLocal2);
		units.insertAfter(assignTasksResult, invokeTasksConstructor);
		
		Unit taskSystemInvoke = UnitFactory.newAssignStaticInvokeExpr(taskSystemLocal, Constants.TASK_SYSTEM_CLASS, "getDefault");
		units.insertAfter(taskSystemInvoke, assignTasksResult);
		
		Unit getTaskFromTaskMethod = UnitFactory.newAssignVirtualInvokeExpr(taskLocal1, thisLocal, targetClass, getTaskMethodNameFromMethod(), paramLocals);
		units.insertAfter(getTaskFromTaskMethod, taskSystemInvoke);

		Unit startTask = UnitFactory.newAssignVirtualInvokeExpr(taskLocal2, taskSystemLocal, Constants.TASK_SYSTEM_CLASS, "start", new Local[] { taskLocal1 });
		units.insertAfter(startTask, getTaskFromTaskMethod);
		
		Unit addInline = UnitFactory.newVirtualInvokeExpr(tasksLocal1, Constants.INTERNAL_TASKS_CLASS, "addInline", new Local[] { taskLocal2 });
		units.insertAfter(addInline, startTask);
			
		Unit caughtException = J.newIdentityStmt(throwLocal2, J.newCaughtExceptionRef());
		units.insertAfter(caughtException, addInline);
		
		Unit assignException = J.newAssignStmt(throwLocal1, throwLocal2);
		units.insertAfter(assignException, caughtException);
		
		Unit invokeJoinAll = UnitFactory.newVirtualInvokeExpr(tasksLocal1, Constants.INTERNAL_TASKS_CLASS, "joinAll");
		units.insertAfter(invokeJoinAll, assignException);
		
		Unit throwException = J.newThrowStmt(throwLocal1);
		units.insertAfter(throwException, invokeJoinAll);
		
		// Construct finally statement and goto finally
		Unit finallyStmt = UnitFactory.newVirtualInvokeExpr(tasksLocal1, Constants.INTERNAL_TASKS_CLASS, "joinAll");
		units.insertAfter(finallyStmt, throwException);
		
		units.insertAfter(J.newReturnVoidStmt(), finallyStmt);
		
		Unit gotoFinally = J.newGotoStmt(finallyStmt);
		units.insertAfter(gotoFinally, addInline);
		
		Trap trap = J.newTrap(Constants.THROWABLE_CLASS, taskSystemInvoke, caughtException, caughtException);
		body.getTraps().add(trap);
	}
	
	private String getTaskMethodNameFromMethod() {
		String srcMethodName = sourceMethod.getName();
		return "$" + srcMethodName + "Task";
	}
	
	private String getClassNameFromMethod() {
		String srcMethodName = sourceMethod.getName();
		String className = srcMethodName.substring(0,1).toUpperCase() + srcMethodName.substring(1);
		return targetClass.getName() + "$" + className;
	}
	
	public SootClass getInnerClass() {
		return this.innerClass;
	}
}

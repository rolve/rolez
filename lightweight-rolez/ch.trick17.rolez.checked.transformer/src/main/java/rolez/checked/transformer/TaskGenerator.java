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
import rolez.checked.transformer.util.JimpleWriter;
import soot.Body;
import soot.BooleanType;
import soot.Local;
import soot.SootClass;
import soot.SootMethod;
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
	
	private SootClass targetClass;
	private SootMethod sourceMethod;
	private SootClass innerClass;
	
	public TaskGenerator(SootClass targetClass, SootMethod sourceMethod) {
		this.targetClass = targetClass;
		this.sourceMethod = sourceMethod;
	}

	public void generateMethod() {
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
		localIter.next();
		
		for (int i = 0; i < parameterCount; i++) {
			paramLocals.add(localIter.next());
		}

		// TODO: Add rule in annotation processor that $asTask has to be the last parameter in the list!
		Local asTaskLocal = paramLocals.get(parameterCount - 1);
		
		assert(paramLocals.size() == parameterCount);
		assert(asTaskLocal.getType().equals(BooleanType.v()));
		
		// Iterator is used to find the last parameter assignment statement and the first "real" statement
		Iterator<Unit> unitIter = units.iterator();
		Unit lastParamStmt = null;
		unitIter.next();
		for (int i=0; i<parameterCount; i++)
			 lastParamStmt = unitIter.next();
		Unit firstRealStmt = unitIter.next();
		
		// Insert the if statement as the very first statement after the parameter assignments
		Unit ifStmt = Jimple.v().newIfStmt(Jimple.v().newEqExpr(asTaskLocal, IntConstant.v(0)), firstRealStmt);
		units.insertAfter(ifStmt, lastParamStmt);
		
		// add the task invoke method
		// TODO: add the correct call here! Is it task.run() or TaskSystem.start(task)?
		Unit taskInvokeStmt = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(locals.getFirst(), targetClass.getMethodByName(getTaskMethodNameFromMethod()).makeRef(), paramLocals));
		units.insertAfter(taskInvokeStmt, ifStmt);
		
		// Add the goto statement to jump right to the return statement of the original method
		units.insertAfter(Jimple.v().newGotoStmt(units.getLast()), taskInvokeStmt);
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
}

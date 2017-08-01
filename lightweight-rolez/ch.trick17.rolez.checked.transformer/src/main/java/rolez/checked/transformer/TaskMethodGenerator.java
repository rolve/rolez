package rolez.checked.transformer;

import java.util.ArrayList;

import rolez.checked.lang.Task;
import soot.Local;
import soot.Modifier;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;


/**
 * This class is used for generating the task methods from the annotated methods
 * in the user code.
 * 
 * @author Michael Giger
 *
 */
public class TaskMethodGenerator {
	
	private SootClass targetClass;
	private SootMethod srcMethod;

	private SootClass taskClass;
	
	public TaskMethodGenerator(SootClass targetClass, SootMethod srcMethod) {
		this.targetClass = targetClass;
		this.srcMethod = srcMethod;

		taskClass = Scene.v().loadClassAndSupport(Task.class.getCanonicalName());
	}

	// TODO: How to handle generic tasks..? Generics are a java source-level feature...
	public void generateMethod() {
		generateInnerClass();
		//SootMethod taskMethod = new SootMethod("$" + srcMethod.getName() + "Task", srcMethod.getParameterTypes(), RefType.v(taskClass), srcMethod.getModifiers());
		//generateTaskMethodBody(taskMethod);
		//targetClass.addMethod(taskMethod);
	}

	// TODO: Create correct method which returns a task
	private void generateTaskMethodBody(SootMethod taskMethod) {
		JimpleBody methodBody = Jimple.v().newBody(taskMethod);
		Local taskLocal = Jimple.v().newLocal("task", RefType.v(taskClass));
		methodBody.getLocals().add(taskLocal);
		taskMethod.setActiveBody(methodBody);
	}
	
	private void generateInnerClass() {
		SootClass innerTaskClass = new SootClass(getClassNameFromMethod());
		innerTaskClass.setSuperclass(taskClass);
		SootMethod runRolezMethod = new SootMethod("runRolez", new ArrayList<>(), srcMethod.getReturnType(), Modifier.PROTECTED);
		runRolezMethod.setActiveBody(srcMethod.retrieveActiveBody());
		innerTaskClass.addMethod(runRolezMethod);
		innerTaskClass.setOuterClass(targetClass);
		Scene.v().addClass(innerTaskClass);
	}
	
	private String getClassNameFromMethod() {
		String originMethodName = srcMethod.getName();
		String className = originMethodName.substring(0,1).toUpperCase() + originMethodName.substring(1);
		return targetClass.getName() + "$" + className;
	}
}

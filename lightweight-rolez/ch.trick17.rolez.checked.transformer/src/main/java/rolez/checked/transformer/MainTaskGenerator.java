package rolez.checked.transformer;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rolez.checked.transformer.main.MainInnerClass;
import rolez.checked.transformer.main.MainInnerClassConstructor;
import rolez.checked.transformer.main.MainInnerClassRunRolezConcrete;
import rolez.checked.transformer.main.MainTaskMethod;
import rolez.checked.transformer.task.InnerClassRunRolezObject;
import rolez.checked.transformer.util.ClassWriter;
import rolez.checked.transformer.util.Constants;
import rolez.checked.transformer.util.JimpleWriter;
import soot.ArrayType;
import soot.Body;
import soot.Local;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Jimple;
import soot.util.Chain;

public class MainTaskGenerator {
	
	static final Logger logger = LogManager.getLogger(TaskGenerator.class);
	
	static final Jimple J = Jimple.v();
	
	private SootClass targetClass;
	private SootMethod mainMethod;
	private SootClass innerClass;
	
	public MainTaskGenerator(SootClass targetClass, SootMethod mainMethod) {
		this.targetClass = targetClass;
		this.mainMethod = mainMethod;
	}

	public void generateMainTask() {
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
		innerClass = new MainInnerClass(getClassNameFromMethod(), targetClass);
		generateInnerClassConstructor();
		generateRunRolezMethods();
	}
	
	private void generateTaskMethod() {
		MainTaskMethod taskMethod = new MainTaskMethod(targetClass, innerClass);
		targetClass.addMethod(taskMethod);
	}
	
	private void generateInnerClassConstructor() {
		MainInnerClassConstructor innerClassConstructor = new MainInnerClassConstructor(innerClass,targetClass);
		innerClass.addMethod(innerClassConstructor);
	}
	
	private void generateRunRolezMethods() {
		MainInnerClassRunRolezConcrete concreteMethod = new MainInnerClassRunRolezConcrete(innerClass, mainMethod);
		innerClass.addMethod(concreteMethod);
		innerClass.addMethod(new InnerClassRunRolezObject(innerClass, concreteMethod));		
	}
	

	private void transformSourceMethod() {
		Body body = J.newBody(mainMethod);
		
		Chain<Local> locals = body.getLocals();
		Chain<Unit> units = body.getUnits();
		
		Local stringArrLocal = J.newLocal("r0", ArrayType.v(RefType.v(Constants.STRING_CLASS), 1));
		locals.add(stringArrLocal);
		Local taskSystemLocal = J.newLocal("$r1", RefType.v(Constants.TASK_SYSTEM_CLASS));
		locals.add(taskSystemLocal);
		Local targetClassLocal = J.newLocal("$r2", RefType.v(targetClass));
		locals.add(targetClassLocal);
		Local taskLocal = J.newLocal("$r3", RefType.v(Constants.TASK_CLASS));
		locals.add(taskLocal);
		
		ArrayList<Local> runArgs = new ArrayList<Local>();
		runArgs.add(taskLocal);
		
		units.add(J.newIdentityStmt(stringArrLocal, J.newParameterRef(stringArrLocal.getType(), 0)));
		units.add(J.newAssignStmt(taskSystemLocal, J.newStaticInvokeExpr(Constants.TASK_SYSTEM_CLASS.getMethodByName("getDefault").makeRef())));
		units.add(J.newAssignStmt(targetClassLocal, J.newNewExpr(targetClass.getType())));
		units.add(J.newInvokeStmt(J.newSpecialInvokeExpr(targetClassLocal, targetClass.getMethodByName("<init>").makeRef())));
		units.add(J.newAssignStmt(taskLocal, J.newVirtualInvokeExpr(targetClassLocal, targetClass.getMethodByName("$mainTask").makeRef())));
		units.add(J.newInvokeStmt(J.newVirtualInvokeExpr(taskSystemLocal, Constants.TASK_SYSTEM_CLASS.getMethodByName("run").makeRef(), runArgs)));
		units.add(J.newReturnVoidStmt());
		
		mainMethod.setActiveBody(body);
	}

	private String getClassNameFromMethod() {
		return targetClass.getName() + "$Main";
	}
	
	public SootClass getInnerClass() {
		return this.innerClass;
	}
}

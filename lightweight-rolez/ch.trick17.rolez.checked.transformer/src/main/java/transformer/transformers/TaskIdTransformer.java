package transformer.transformers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import soot.Body;
import soot.Local;
import soot.LongType;
import soot.Modifier;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.VoidType;
import soot.jimple.IdentityStmt;
import soot.jimple.Jimple;
import soot.jimple.ParameterRef;
import soot.util.Chain;
import transformer.checking.GuardedRefs;
import transformer.id.TaskIdStmtTransformerSwitch;
import transformer.task.MainTaskInnerClassConstructor;
import transformer.task.MainTaskInnerClassRunRolezConcrete;
import transformer.task.MainTaskMethod;
import transformer.task.TaskInnerClassConstructor;
import transformer.task.TaskInnerClassRunRolezConcrete;
import transformer.task.TaskInnerClassRunRolezObject;
import transformer.task.TaskMethod;
import transformer.util.Constants;
import transformer.util.Util;

public class TaskIdTransformer extends SceneTransformer {

	static final Logger logger = LogManager.getLogger(TaskIdTransformer.class);
	
	Map<SootMethod, SootMethod> changedMethods = new HashMap<SootMethod, SootMethod>();
	SootClass mainClass;
	
	@Override
	protected void internalTransform(String phaseName, Map options) {
		Chain<SootClass> classesToProcess = Scene.v().getApplicationClasses();
		mainClass = Scene.v().getMainClass();
		
		for (SootClass c : classesToProcess) {
			for (SootMethod m : c.getMethods()) {
				if (!isMethodToTransform(m)) continue;
				
				if (m instanceof MainTaskInnerClassRunRolezConcrete || 
					m instanceof TaskInnerClassRunRolezConcrete) {
					// Run rolez method of tasks will get a fresh id
					Local taskIdLocal = addTaskIdLocal(m);
					addTaskIdAssignStmt(m, taskIdLocal);
				} else {
					// All other methods get the task id via the parameter $taskId parameter
					addTaskIdParameter(m);
					if (!m.isAbstract()) {
						Local taskIdLocal = addTaskIdLocal(m);
						addTaskIdIdentityStmt(m,taskIdLocal);
					}
				}
			}
		}
		
		
		for (SootClass c : classesToProcess) {
			for (SootMethod m : c.getMethods()) {
				if (!isMethodToTransform(m)) continue;
				if (m.isAbstract()) continue;
				
				Body b = m.retrieveActiveBody();
				
				Local taskIdLocal = Util.getTaskIdLocal(b.getLocals());
				if (taskIdLocal == null) 
					throw new RuntimeException("Could not find task local.");
				
				for (Unit u : b.getUnits()) 
					u.apply(new TaskIdStmtTransformerSwitch(changedMethods, taskIdLocal, u));
			}
		}
		
		for (SootMethod m : changedMethods.keySet()) {
			m.getDeclaringClass().removeMethod(m);
		}

		addInstanceConstructorToMainClass();
	}
	
	private void addInstanceConstructorToMainClass() {
		SootMethod constructor = new SootMethod("<init>", new ArrayList<Type>(), VoidType.v(), Modifier.PRIVATE);
		Body b = Jimple.v().newBody(constructor);
		constructor.setActiveBody(b);
		Chain<Local> locals = b.getLocals();
		Chain<Unit> units = b.getUnits();
		
		Local instanceLocal = Jimple.v().newLocal("instance", mainClass.getType());
		locals.add(instanceLocal);
		units.add(Jimple.v().newIdentityStmt(instanceLocal, Jimple.v().newThisRef(mainClass.getType())));
		units.add(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(instanceLocal,Constants.CHECKED_CLASS.getMethod("void <init>()").makeRef())));
		units.add(Jimple.v().newReturnVoidStmt());
		
		mainClass.addMethod(constructor);
	}

	private boolean isMethodToTransform(SootMethod m) {
		return !(
			m.isMain() ||
			m instanceof GuardedRefs ||
			m instanceof TaskInnerClassRunRolezObject ||
			m instanceof TaskMethod ||
			m instanceof MainTaskMethod ||
			m instanceof TaskInnerClassConstructor ||
			m instanceof MainTaskInnerClassConstructor
		);
	}

	private Local addTaskIdLocal(SootMethod m) {
		Body b = m.retrieveActiveBody();
		Chain<Local> locals = b.getLocals();
		Local taskIdLocal = Jimple.v().newLocal(Constants.TASK_ID_LOCAL_NAME, LongType.v());
		locals.add(taskIdLocal);
		return taskIdLocal;
	}
	
	private void addTaskIdAssignStmt(SootMethod m, Local taskIdLocal) {
		Body b = m.retrieveActiveBody();
		Chain<Local> locals = b.getLocals();
		Chain<Unit> units = b.getUnits();
		Iterator<Unit> iter = units.snapshotIterator();
		while (iter.hasNext()) {
			Unit u = iter.next();
			if (!(u instanceof IdentityStmt)) {
				units.insertBefore(Jimple.v().newAssignStmt(
						taskIdLocal, 
						Jimple.v().newVirtualInvokeExpr(getThisLocal(locals), Constants.TASK_CLASS.getMethod("long idBits()").makeRef())),
					u);
				break;
			}
		}
	}
	
	private void addTaskIdIdentityStmt(SootMethod m, Local taskIdLocal) {
		Body b = m.retrieveActiveBody();
		Chain<Unit> units = b.getUnits();
		Iterator<Unit> iter = units.snapshotIterator();
		int currentNumber = 0;
		while (iter.hasNext()) {
			Unit u = iter.next();
			if (u instanceof IdentityStmt) {
				Value rightOp = ((IdentityStmt) u).getRightOp();
				if (rightOp instanceof ParameterRef) {
					ParameterRef pRef = (ParameterRef)rightOp;
					currentNumber = pRef.getIndex() + 1;
				}
			} else {
				units.insertBefore(Jimple.v().newIdentityStmt(taskIdLocal, Jimple.v().newParameterRef(LongType.v(), currentNumber)), u);
				break;
			}
		}
	}
	
	private void addTaskIdParameter(SootMethod m) {
		List<Type> parameterTypes = new ArrayList<Type>();
		parameterTypes.addAll(m.getParameterTypes());
		parameterTypes.add(LongType.v());	
		SootMethod newMethod = new SootMethod(m.getName(), parameterTypes, m.getReturnType(), m.getModifiers(), m.getExceptions());
		newMethod.addAllTagsOf(m);
		m.getDeclaringClass().addMethod(newMethod);
		if (!m.isAbstract()) {
			Body b = m.retrieveActiveBody();
			newMethod.setActiveBody(b);
		}
		changedMethods.put(m, newMethod);
	}
	
	private Local getThisLocal(Chain<Local> locals) {
		for (Local local : locals) {
			if (local.getName().equals("inner")) return local;
		}
		throw new RuntimeException("Could not find 'this' local.");
	}
}

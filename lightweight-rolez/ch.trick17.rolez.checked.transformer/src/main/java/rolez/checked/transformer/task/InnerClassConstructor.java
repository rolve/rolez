package rolez.checked.transformer.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rolez.checked.lang.Task;
import soot.ArrayType;
import soot.Local;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.VoidType;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.util.Chain;

public class InnerClassConstructor extends SootMethod {

	static final Logger logger = LogManager.getLogger(InnerClassConstructor.class);
	
	static final SootClass OBJECT_CLASS = Scene.v().loadClassAndSupport(Object.class.getCanonicalName());
	static final SootClass TASK_CLASS = Scene.v().loadClassAndSupport(Task.class.getCanonicalName());
	static final ArrayType OBJECT_ARRAY_TYPE = ArrayType.v(RefType.v(OBJECT_CLASS),1);
	
	private SootClass containingClass;
	private SootClass outerClass;
	private SootMethod sourceMethod;
	
	List<Type> sourceMethodParameterTypes; 
	
	public InnerClassConstructor(SootClass containingClass, SootClass outerClass, SootMethod sourceMethod) {
		// Defer setting of parameter types
		super("<init>", new ArrayList<Type>(), VoidType.v());
		
		this.containingClass = containingClass;
		this.outerClass = outerClass;
		this.sourceMethod = sourceMethod;
		sourceMethodParameterTypes = sourceMethod.getParameterTypes();

		// Find and set parameter types
		this.setParameterTypes(findParameterTypes());
		generateMethodBody();
	}
	
	private void generateMethodBody() {
		
		// Useful variables
		int offset = 5;
		int numberOffset = 4;
		
		JimpleBody body = Jimple.v().newBody(this);
		this.setActiveBody(body);
		
		// All local variables that have to be added to the body
		List<Local> locals = new ArrayList<Local>();
		locals.add(Jimple.v().newLocal("r0", containingClass.getType()));
		locals.add(Jimple.v().newLocal("r1", outerClass.getType()));
		locals.add(Jimple.v().newLocal("r2", OBJECT_ARRAY_TYPE));
		locals.add(Jimple.v().newLocal("r3", OBJECT_ARRAY_TYPE));
		locals.add(Jimple.v().newLocal("r4", OBJECT_ARRAY_TYPE));
		for (int i=0; i<sourceMethodParameterTypes.size(); i++) {
			locals.add(Jimple.v().newLocal("r"+Integer.toString(offset + i), sourceMethodParameterTypes.get(i)));
		}
		
		// Add the locals to the body
		Chain<Local> bodyLocals = body.getLocals();
		bodyLocals.addAll(locals);
		
		Chain<Unit> units = body.getUnits();
		units.add(Jimple.v().newIdentityStmt(locals.get(0), Jimple.v().newThisRef(containingClass.getType())));
		units.add(Jimple.v().newIdentityStmt(locals.get(1), Jimple.v().newParameterRef(outerClass.getType(), 0)));
		units.add(Jimple.v().newIdentityStmt(locals.get(2), Jimple.v().newParameterRef(OBJECT_ARRAY_TYPE, 1)));
		units.add(Jimple.v().newIdentityStmt(locals.get(3), Jimple.v().newParameterRef(OBJECT_ARRAY_TYPE, 2)));
		units.add(Jimple.v().newIdentityStmt(locals.get(4), Jimple.v().newParameterRef(OBJECT_ARRAY_TYPE, 3)));
		for (int i=0; i<sourceMethodParameterTypes.size(); i++) {
			units.add(Jimple.v().newIdentityStmt(locals.get(i+offset), Jimple.v().newParameterRef(sourceMethodParameterTypes.get(i), i+numberOffset)));
		}
		
		// Set field field for outer class ref
		units.add(Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(locals.get(0), containingClass.getFieldByName("this$0").makeRef()), locals.get(1)));
		
		// Set fields for method parameters
		for (int i=0; i<sourceMethod.getParameterCount(); i++) {
			units.add(Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(locals.get(0), containingClass.getFieldByName("val$f" + Integer.toString(i)).makeRef()), locals.get(offset + i)));
		}
		
		// Add the call to superclass constructor
		units.add(Jimple.v().newInvokeStmt(
				Jimple.v().newSpecialInvokeExpr(
						locals.get(0), 
						TASK_CLASS.getMethodByName("<init>").makeRef(), 
						Arrays.asList(new Local[] {
								locals.get(2), 
								locals.get(3), 
								locals.get(4)})
		)));
		
		// Add return statement
		units.add(Jimple.v().newReturnVoidStmt());
	}
	
	private List<Type> findParameterTypes() {
		// List of constructor parameters
		List<Type> parameterTypes = new ArrayList<Type>();
		
		// Add "this"
		parameterTypes.add(outerClass.getType());
		
		// Add three Object array types to pass passed, shared and pure objects
		parameterTypes.add(OBJECT_ARRAY_TYPE);
		parameterTypes.add(OBJECT_ARRAY_TYPE);
		parameterTypes.add(OBJECT_ARRAY_TYPE);
		
		// TODO: Don't add the boolean $asTask to the list... Or does it even matter?
		// Add all original method parameters
		for (Type t : sourceMethodParameterTypes) {
			parameterTypes.add(t);
		}
		
		return parameterTypes;
	}
}

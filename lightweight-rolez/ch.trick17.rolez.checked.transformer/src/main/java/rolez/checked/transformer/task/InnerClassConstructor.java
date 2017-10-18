package rolez.checked.transformer.task;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rolez.checked.transformer.util.Constants;
import rolez.checked.transformer.util.UnitFactory;
import soot.Local;
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


	static final Jimple J = Jimple.v();
	
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
		
		JimpleBody body = J.newBody(this);
		this.setActiveBody(body);
		
		Chain<Local> locals = body.getLocals();
		
		// All local variables that have to be added to the body
		Local containingClassLocal = J.newLocal("r0", containingClass.getType());
		locals.add(containingClassLocal);
		Local outerClassLocal = J.newLocal("r1", outerClass.getType());
		locals.add(outerClassLocal);
		Local readwriteArrayLocal = J.newLocal("r2", Constants.OBJECT_ARRAY_TYPE);
		locals.add(readwriteArrayLocal);
		Local readonlyArrayLocal = J.newLocal("r3", Constants.OBJECT_ARRAY_TYPE);
		locals.add(readonlyArrayLocal);
		Local pureArrayLocal = J.newLocal("r4", Constants.OBJECT_ARRAY_TYPE);
		locals.add(pureArrayLocal);

		List<Local> parameterLocals = new ArrayList<Local>();
		for (int i=0; i<sourceMethodParameterTypes.size(); i++)
			parameterLocals.add(J.newLocal("arg"+Integer.toString(i), sourceMethodParameterTypes.get(i)));
		locals.addAll(parameterLocals);
		
		Chain<Unit> units = body.getUnits();
		int paramRefNumber = 0;
		units.add(UnitFactory.newThisRef(containingClassLocal, containingClass.getType()));
		units.add(UnitFactory.newParameterRef(outerClassLocal, outerClass.getType(), paramRefNumber++));
		units.add(UnitFactory.newParameterRef(readwriteArrayLocal, Constants.OBJECT_ARRAY_TYPE, paramRefNumber++));
		units.add(UnitFactory.newParameterRef(readonlyArrayLocal, Constants.OBJECT_ARRAY_TYPE, paramRefNumber++));
		units.add(UnitFactory.newParameterRef(pureArrayLocal, Constants.OBJECT_ARRAY_TYPE, paramRefNumber++));
		for (int i=0; i<sourceMethodParameterTypes.size(); i++)
			units.add(UnitFactory.newParameterRef(parameterLocals.get(i), sourceMethodParameterTypes.get(i), paramRefNumber++));
		
		// Set field for outer class reference
		units.add(UnitFactory.newAssignLocalToFieldExpr(containingClassLocal, containingClass, "val$f0", outerClassLocal));
		
		// Set fields for method parameters
		for (int i=0; i<sourceMethod.getParameterCount(); i++)
			units.add(UnitFactory.newAssignLocalToFieldExpr(containingClassLocal, containingClass, "val$f" + Integer.toString(i+1), parameterLocals.get(i)));
		
		// Add the call to superclass constructor
		units.add(UnitFactory.newSpecialInvokeExpr(containingClassLocal, Constants.TASK_CLASS, "<init>", new Local[] {	readwriteArrayLocal, readonlyArrayLocal, pureArrayLocal }));
		
		// Add return statement
		units.add(J.newReturnVoidStmt());
	}
	
	private List<Type> findParameterTypes() {
		// List of constructor parameters
		List<Type> parameterTypes = new ArrayList<Type>();
		
		// Add "this"
		parameterTypes.add(outerClass.getType());
		
		// Add three Object array types to pass passed, shared and pure objects
		parameterTypes.add(Constants.OBJECT_ARRAY_TYPE);
		parameterTypes.add(Constants.OBJECT_ARRAY_TYPE);
		parameterTypes.add(Constants.OBJECT_ARRAY_TYPE);
		
		// Add all original method parameters
		for (Type t : sourceMethodParameterTypes)
			parameterTypes.add(t);
		
		return parameterTypes;
	}
}

package rolez.checked.transformer.main;

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

public class MainInnerClassConstructor extends SootMethod {

	static final Logger logger = LogManager.getLogger(MainInnerClassConstructor.class);

	static final Jimple J = Jimple.v();
	
	private SootClass containingClass;
	private SootClass outerClass;
	
	public MainInnerClassConstructor(SootClass containingClass, SootClass outerClass) {
		// Defer setting of parameter types
		super("<init>", new ArrayList<Type>(), VoidType.v());
		
		this.containingClass = containingClass;
		this.outerClass = outerClass;

		this.setParameterTypes(findParameterTypes());
		
		generateMethodBody();
	}
	
	private void generateMethodBody() {
		
		JimpleBody body = J.newBody(this);
		
		// All local variables that have to be added to the body
		List<Local> locals = new ArrayList<Local>();
		locals.add(J.newLocal("r0", containingClass.getType()));
		locals.add(J.newLocal("r1", outerClass.getType()));
		locals.add(J.newLocal("r2", Constants.OBJECT_ARRAY_TYPE));
		locals.add(J.newLocal("r3", Constants.OBJECT_ARRAY_TYPE));
		locals.add(J.newLocal("r4", Constants.OBJECT_ARRAY_TYPE));
		
		// Add the locals to the body
		Chain<Local> bodyLocals = body.getLocals();
		bodyLocals.addAll(locals);
		
		Chain<Unit> units = body.getUnits();
		units.add(UnitFactory.newThisRef(locals.get(0), containingClass.getType()));
		units.add(UnitFactory.newParameterRef(locals.get(1), outerClass.getType(), 0));
		units.add(UnitFactory.newParameterRef(locals.get(2), Constants.OBJECT_ARRAY_TYPE, 1));
		units.add(UnitFactory.newParameterRef(locals.get(3), Constants.OBJECT_ARRAY_TYPE, 2));
		units.add(UnitFactory.newParameterRef(locals.get(4), Constants.OBJECT_ARRAY_TYPE, 3));
		
		// Set field field for outer class ref
		units.add(UnitFactory.newAssignLocalToFieldExpr(locals.get(0), containingClass, "val$f0", locals.get(1)));
		
		// Add the call to superclass constructor
		units.add(UnitFactory.newSpecialInvokeExpr(locals.get(0), Constants.TASK_CLASS, "<init>", new Local[] {	locals.get(2), locals.get(3), locals.get(4)}));
		
		// Add return statement
		units.add(J.newReturnVoidStmt());
		
		this.setActiveBody(body);
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
		
		return parameterTypes;
	}
}

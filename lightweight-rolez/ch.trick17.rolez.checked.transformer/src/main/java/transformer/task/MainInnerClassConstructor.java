package transformer.task;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import soot.Local;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.VoidType;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.util.Chain;
import transformer.util.Constants;
import transformer.util.UnitFactory;

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
		
		Chain<Unit> units = body.getUnits();
		units.add(UnitFactory.newThisRef(containingClassLocal, containingClass.getType()));
		units.add(UnitFactory.newParameterRef(outerClassLocal, outerClass.getType(), 0));
		units.add(UnitFactory.newParameterRef(readwriteArrayLocal, Constants.OBJECT_ARRAY_TYPE, 1));
		units.add(UnitFactory.newParameterRef(readonlyArrayLocal, Constants.OBJECT_ARRAY_TYPE, 2));
		units.add(UnitFactory.newParameterRef(pureArrayLocal, Constants.OBJECT_ARRAY_TYPE, 3));
		
		// Set field field for outer class ref
		units.add(UnitFactory.newAssignLocalToFieldExpr(containingClassLocal, containingClass, "val$f0", outerClassLocal));
		
		// Add the call to superclass constructor
		units.add(UnitFactory.newSpecialInvokeExpr(containingClassLocal, Constants.TASK_CLASS, "<init>", new Local[] { readwriteArrayLocal, readonlyArrayLocal, pureArrayLocal}));
		
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

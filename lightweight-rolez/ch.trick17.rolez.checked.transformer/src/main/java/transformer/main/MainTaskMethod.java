package transformer.main;

import java.util.ArrayList;
import java.util.List;

import soot.ArrayType;
import soot.Local;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.util.Chain;
import transformer.util.Constants;
import transformer.util.UnitFactory;

public class MainTaskMethod extends SootMethod {

	private SootClass containingClass;
	private SootClass innerClass;
	
	static final Jimple J = Jimple.v();
	
	public MainTaskMethod(SootClass containingClass, SootClass innerClass) {
		super("$mainTask", new ArrayList<Type>(), Constants.TASK_CLASS.getType());
		
		this.containingClass = containingClass;
		this.innerClass = innerClass;
		
		generateMethodBody();
	}
	
	private void generateMethodBody() {		
	
		JimpleBody body = J.newBody(this);

		// Set up the locals
		Chain<Local> locals = body.getLocals();
				
		Local thisReferenceLocal = J.newLocal("r0", containingClass.getType());
		locals.add(thisReferenceLocal);
		
		Local innerClassReferenceLocal = J.newLocal("$r1", innerClass.getType());
		locals.add(innerClassReferenceLocal);
		
		List<Local> objectArrayLocals = new ArrayList<Local>();
		ArrayType objectArrayType = ArrayType.v(RefType.v(Constants.OBJECT_CLASS),1);
		for (int i=2; i<5; i++) {
			Local l = J.newLocal("$r" + Integer.toString(i), objectArrayType);
			objectArrayLocals.add(l);
			locals.add(l);
		}
		
		// Add units
		Chain<Unit> units = body.getUnits();
		units.add(UnitFactory.newThisRef(thisReferenceLocal, containingClass.getType()));
		units.add(UnitFactory.newAssignNewExpr(innerClassReferenceLocal, innerClass));

		units.add(UnitFactory.newAssignNewArrayExpr(objectArrayLocals.get(0), Constants.OBJECT_CLASS.getType(), 1));
		units.add(UnitFactory.newAssignNewArrayExpr(objectArrayLocals.get(1), Constants.OBJECT_CLASS.getType(), 0));
		units.add(UnitFactory.newAssignNewArrayExpr(objectArrayLocals.get(2), Constants.OBJECT_CLASS.getType(), 0));

		units.add(UnitFactory.newAssignToArrayExpr(objectArrayLocals.get(0), 0, thisReferenceLocal));
		
		ArrayList<Local> constructorArgs = new ArrayList<Local>();
		constructorArgs.add(thisReferenceLocal);
		constructorArgs.add(objectArrayLocals.get(0));
		constructorArgs.add(objectArrayLocals.get(1));
		constructorArgs.add(objectArrayLocals.get(2));
		
		// Call constructor of inner class
		units.add(UnitFactory.newSpecialInvokeExpr(innerClassReferenceLocal, innerClass, "<init>", constructorArgs));
		
		// Return inner class
		units.add(J.newReturnStmt(innerClassReferenceLocal));
		
		this.setActiveBody(body);
	}
}

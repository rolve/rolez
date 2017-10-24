package transformer.task;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import soot.Body;
import soot.Local;
import soot.Modifier;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.IdentityStmt;
import soot.jimple.Jimple;
import soot.jimple.NullConstant;
import soot.jimple.ReturnVoidStmt;
import soot.util.Chain;
import transformer.util.Constants;
import transformer.util.UnitFactory;

public class TaskInnerClassRunRolezConcrete extends SootMethod {
	
	static final Logger logger = LogManager.getLogger(TaskInnerClassRunRolezConcrete.class);

	static final Jimple J = Jimple.v();
	
	private SootClass containingClass;
	private SootMethod sourceMethod;
	
	public TaskInnerClassRunRolezConcrete(SootClass containingClass, SootMethod sourceMethod) {
		super("runRolez", new ArrayList<Type>(), Constants.VOID_TYPE, Modifier.PROTECTED);
		
		this.containingClass = containingClass;
		this.sourceMethod = sourceMethod;

		generateMethodBody();
	}

	private void generateMethodBody() {

		RefType innerClassType = containingClass.getType();
		
		// Body of source method
		Body srcMethodBody = sourceMethod.retrieveActiveBody();
		
		// Body of created runRolez method is initialized as a copy of the source method's body
		this.setActiveBody((Body)srcMethodBody.clone());
		Body body = this.getActiveBody();
		
		// Create new local for the inner class type
		Chain<Local> locals = body.getLocals();
		Local innerClassLocal = J.newLocal("inner", containingClass.getType());
		locals.addFirst(innerClassLocal);

		// Transform the units of the source method
		Chain<Unit> units = body.getUnits();
		
		// Refer to fields instead of parameters for the first n + 1 identity statements (n = #params + 1)
		int n = sourceMethod.getParameterCount() + 1;
		int i = 0;
		Iterator<Unit> unitIter = units.snapshotIterator(); 
		while (unitIter.hasNext()) {
			Unit u = unitIter.next();
			if (i < n) {
				// Transform to field assignments (the first n statements should always be identity statements)
				IdentityStmt idStmt = (IdentityStmt) u;
				Value leftOp = idStmt.getLeftOp();
				Unit newUnit = UnitFactory.newAssignFieldToLocalExpr((Local)leftOp, locals.getFirst(), containingClass, "val$f"+Integer.toString(i));
				units.insertBefore(newUnit, u);
				units.remove(u);
			} else if (u instanceof ReturnVoidStmt) {
				// The other statements except the return statements should work with the initialization from above.
				Unit newReturn = J.newReturnStmt(NullConstant.v());
				units.insertBefore(newReturn, u);
				units.remove(u);
			}
			i++;
		}
		
		units.addFirst(J.newIdentityStmt(innerClassLocal, J.newThisRef(innerClassType)));
	}
}

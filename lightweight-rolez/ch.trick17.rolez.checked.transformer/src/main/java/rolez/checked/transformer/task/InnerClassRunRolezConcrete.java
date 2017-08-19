package rolez.checked.transformer.task;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rolez.checked.transformer.Constants;
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
import soot.jimple.ParameterRef;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.ThisRef;
import soot.util.Chain;

public class InnerClassRunRolezConcrete extends SootMethod {
	
	static final Logger logger = LogManager.getLogger(InnerClassRunRolezConcrete.class);

	static final Jimple J = Jimple.v();
	
	private SootClass containingClass;
	private SootMethod sourceMethod;
	
	public InnerClassRunRolezConcrete(SootClass containingClass, SootMethod sourceMethod) {
		// Defer setting of return type
		super("runRolez", new ArrayList<Type>(), Constants.VOID_TYPE, Modifier.PROTECTED);
		
		this.containingClass = containingClass;
		this.sourceMethod = sourceMethod;

		// Find and set return type
		this.setReturnType(findReturnType());
		generateMethodBody();
	}

	private void generateMethodBody() {

		RefType innerClassType = containingClass.getType();
		
		// Body of source method
		Body srcMethodBody = sourceMethod.retrieveActiveBody();
		//logger.debug("SOURCE BEFORE\n" + srcMethodBody);
		
		// Body of created runRolez method is initialized as a copy of the source method's body
		this.setActiveBody((Body)srcMethodBody.clone());
		Body body = this.getActiveBody();
		
		// Change type of first local to inner class type
		Chain<Local> locals = body.getLocals();
		locals.getFirst().setType(innerClassType);

		// Transform the units of the source method
		Chain<Unit> units = body.getUnits();
		
		// Refer to fields instead of parameters for the first n identity statements (n = #params + 1)
		int n = sourceMethod.getParameterCount() + 1;

		int i = 0;
		if (sourceMethod.isStatic()) i = 1;
		
		Iterator<Unit> unitIter = units.snapshotIterator(); 
		while (unitIter.hasNext()) {
			Unit u = unitIter.next();
			if (i == 0) {
				// This ref
				try {
					if (u instanceof IdentityStmt) {
						IdentityStmt idStmt = (IdentityStmt) u;
						if (idStmt.getRightOp() instanceof ThisRef) {
							Value leftOp = idStmt.getLeftOp();
							Unit newUnit = J.newIdentityStmt(leftOp, J.newThisRef(innerClassType));
							units.insertBefore(newUnit, u);
							units.remove(u);
						} else {
							// TODO: This is NOT true for static methods (e.g. main method) !!
							// Right hand side should always be a this ref
							throw new Exception(u.toString() + ": Right hand side should be a `this` ref.");
						}
					} else {
						// Should always be an identity statement
						throw new Exception();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} 
			
			else if (i > 0 && i < n) {
				// Parameter assignments
				try {
					if (u instanceof IdentityStmt) {
						IdentityStmt idStmt = (IdentityStmt) u;
						if (idStmt.getRightOp() instanceof ParameterRef) {
							Value leftOp = idStmt.getLeftOp();
							Unit newUnit = J.newAssignStmt(leftOp, J.newInstanceFieldRef(locals.getFirst(), containingClass.getFieldByName("val$f"+Integer.toString(i-1)).makeRef()));
							units.insertBefore(newUnit, u);
							units.remove(u);
						} else {
							// Right hand side should always be a parameter ref
							throw new Exception();
						}
					} else {
						// Should always be an identity statement
						throw new Exception();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			else {
				// The other statements except the return statements should work with the initialization from above.
				if (u instanceof ReturnVoidStmt) {
					Unit newReturn = J.newReturnStmt(NullConstant.v());
					units.insertBefore(newReturn, u);
					units.remove(u);
				}
			}
			
			i++;
		}
	}
	
	private Type findReturnType() {
		Type returnType = null;
		
		// TODO: Handle primitive types!!! --> This switch may grow large... :-/
		switch (sourceMethod.getReturnType().toString()) {
			case("void"):
				returnType = Constants.VOID_TYPE;
				break;
			default:
				returnType = sourceMethod.getReturnType();
				break;
		}
		
		return returnType;
	}
}

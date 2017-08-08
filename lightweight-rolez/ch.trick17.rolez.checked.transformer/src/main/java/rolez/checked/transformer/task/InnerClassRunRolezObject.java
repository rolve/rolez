package rolez.checked.transformer.task;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import soot.Local;
import soot.Modifier;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.util.Chain;

public class InnerClassRunRolezObject extends SootMethod {

	static final Logger logger = LogManager.getLogger(InnerClassRunRolezObject.class);
	
	static final SootClass OBJECT_CLASS = Scene.v().loadClassAndSupport(Object.class.getCanonicalName());
	
	private SootClass containingClass;
	private SootMethod concreteMethod;
	
	public InnerClassRunRolezObject(SootClass containingClass, SootMethod concreteMethod) {
		super("runRolez", new ArrayList<Type>(), OBJECT_CLASS.getType(), Modifier.VOLATILE | Modifier.PROTECTED);
		this.containingClass = containingClass;
		this.concreteMethod = concreteMethod;
		
		generateMethodBody();
	}
	
	private void generateMethodBody() {
		logger.debug("Generating runRolez() : java.lang.Object");
		
		RefType innerClassType = containingClass.getType();
		RefType voidType = RefType.v("java.lang.Void");
		
		JimpleBody body = Jimple.v().newBody(this);
		this.setActiveBody(body);
		
		Chain<Local> bodyLocals = body.getLocals();
		Local thisLocal = Jimple.v().newLocal("r0", innerClassType);
		bodyLocals.add(thisLocal);
		Local returnLocal = Jimple.v().newLocal("$r1", voidType);
		bodyLocals.add(returnLocal);

		Chain<Unit> units = body.getUnits();
		units.add(Jimple.v().newIdentityStmt(thisLocal, Jimple.v().newThisRef(innerClassType)));
		units.add(Jimple.v().newAssignStmt(returnLocal, Jimple.v().newVirtualInvokeExpr(thisLocal, concreteMethod.makeRef())));
		units.add(Jimple.v().newReturnStmt(returnLocal));
	}
}

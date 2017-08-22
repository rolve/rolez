package rolez.checked.transformer.task;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rolez.checked.transformer.util.Constants;
import rolez.checked.transformer.util.UnitFactory;
import soot.Local;
import soot.Modifier;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.util.Chain;

public class InnerClassRunRolezObject extends SootMethod {

	static final Logger logger = LogManager.getLogger(InnerClassRunRolezObject.class);

	static final Jimple J = Jimple.v();
	
	private SootClass containingClass;
	private SootMethod concreteMethod;
	
	public InnerClassRunRolezObject(SootClass containingClass, SootMethod concreteMethod) {
		super("runRolez", new ArrayList<Type>(), Constants.OBJECT_CLASS.getType(), Modifier.VOLATILE | Modifier.PROTECTED);
		this.containingClass = containingClass;
		this.concreteMethod = concreteMethod;
		
		generateMethodBody();
	}
	
	private void generateMethodBody() {		
		RefType innerClassType = containingClass.getType();
		
		JimpleBody body = J.newBody(this);
		this.setActiveBody(body);
		
		Chain<Local> bodyLocals = body.getLocals();
		Local thisLocal = J.newLocal("r0", innerClassType);
		bodyLocals.add(thisLocal);
		Local returnLocal = J.newLocal("$r1", concreteMethod.getReturnType());
		bodyLocals.add(returnLocal);

		Chain<Unit> units = body.getUnits();
		units.add(UnitFactory.newThisRef(thisLocal, innerClassType));
		units.add(UnitFactory.newAssignVirtualInvokeExpr(returnLocal, thisLocal, concreteMethod));
		units.add(J.newReturnStmt(returnLocal));
	}
}

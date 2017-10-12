package rolez.checked.transformer.main;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rolez.checked.transformer.util.Constants;
import rolez.checked.transformer.util.UnitFactory;
import soot.Body;
import soot.Local;
import soot.Modifier;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.Trap;
import soot.Type;
import soot.Unit;
import soot.jimple.Jimple;
import soot.jimple.NullConstant;
import soot.util.Chain;

public class MainInnerClassRunRolezConcrete extends SootMethod {
	
	static final Logger logger = LogManager.getLogger(MainInnerClassRunRolezConcrete.class);

	static final Jimple J = Jimple.v();
	
	private SootClass containingClass;
	private SootMethod sourceMethod;
	
	public MainInnerClassRunRolezConcrete(SootClass containingClass, SootMethod sourceMethod) {
		super("runRolez", new ArrayList<Type>(), Constants.VOID_TYPE, Modifier.PROTECTED);
		this.containingClass = containingClass;
		this.sourceMethod = sourceMethod;

		// Find and set return type
		generateMethodBody();
	}

	private void generateMethodBody() {

		RefType innerClassType = containingClass.getType();
		
		Body srcMethodBody = sourceMethod.retrieveActiveBody();
		
		// Body of created runRolez method is initialized as a copy of the source method's body
		this.setActiveBody((Body)srcMethodBody.clone());
		Body body = this.getActiveBody();
		
		// Change type of first local to inner class type
		Chain<Local> locals = body.getLocals();
		locals.removeFirst();
		Local thisLocal = J.newLocal("r0", innerClassType);
		locals.addFirst(thisLocal);

		// Transform the units of the source method
		Chain<Unit> units = body.getUnits();
		units.removeFirst();
		units.addFirst(UnitFactory.newThisRef(thisLocal, innerClassType));

		Unit returnStmt = J.newReturnStmt(NullConstant.v());
		units.insertBefore(returnStmt, units.getLast());
		units.removeLast();
	}
}

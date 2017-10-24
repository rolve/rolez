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
import soot.jimple.Jimple;
import soot.jimple.NullConstant;
import soot.jimple.ReturnVoidStmt;
import soot.util.Chain;
import transformer.util.Constants;
import transformer.util.UnitFactory;

public class MainTaskInnerClassRunRolezConcrete extends SootMethod {
	
	static final Logger logger = LogManager.getLogger(MainTaskInnerClassRunRolezConcrete.class);

	static final Jimple J = Jimple.v();
	
	private SootClass containingClass;
	private SootMethod sourceMethod;
	
	public MainTaskInnerClassRunRolezConcrete(SootClass containingClass, SootMethod sourceMethod) {
		super("runRolez", new ArrayList<Type>(), Constants.VOID_TYPE, Modifier.PROTECTED);
		
		this.containingClass = containingClass;
		this.sourceMethod = sourceMethod;

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

		Chain<Unit> units = body.getUnits();
		units.removeFirst();
		units.addFirst(UnitFactory.newThisRef(thisLocal, innerClassType));

		// Return null at the end of the method
		Iterator<Unit> unitIter = units.snapshotIterator();
		while (unitIter.hasNext()) {
			Unit u = unitIter.next();
			if (u instanceof ReturnVoidStmt) {
				Unit returnStmt = J.newReturnStmt(NullConstant.v());
				units.insertBefore(returnStmt, u);
				units.remove(u);
			}
		}
	}
}

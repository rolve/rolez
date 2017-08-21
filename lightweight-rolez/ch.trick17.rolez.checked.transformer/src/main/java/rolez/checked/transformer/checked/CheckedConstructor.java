package rolez.checked.transformer.checked;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rolez.checked.transformer.Constants;
import soot.Body;
import soot.Local;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.VoidType;
import soot.jimple.Jimple;
import soot.util.Chain;

public class CheckedConstructor extends SootMethod {

	static final Logger logger = LogManager.getLogger(CheckedConstructor.class);
	
	SootMethod sourceConstructor;
	
	public CheckedConstructor(SootMethod sourceConstructor) {
		super ("<init>", sourceConstructor.getParameterTypes(), VoidType.v());
		this.sourceConstructor = sourceConstructor;
		
		generateMethodBody();
	}
	
	private void generateMethodBody() {
		Body sourceConstructorBody = sourceConstructor.retrieveActiveBody();
		
		this.setActiveBody((Body)sourceConstructorBody.clone());
		Body body = this.getActiveBody();
		
		Chain<Local> locals = body.getLocals();
		Local thisLocal = locals.getFirst();
		
		Chain<Unit> units = body.getUnits();
		Iterator<Unit> iterator = units.iterator();
		Unit firstRealStatement = iterator.next();
		int parameterCount = sourceConstructor.getParameterCount();
		for (int i = 0; i < parameterCount + 1; i++) {
			firstRealStatement = iterator.next();
		}
		
		units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(thisLocal, Constants.CHECKED_CLASS.getMethod("<init>", new ArrayList<Type>()).makeRef())), firstRealStatement);
		units.remove(firstRealStatement);
	}
}

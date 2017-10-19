package rolez.checked.transformer;

import java.util.ArrayList;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rolez.checked.transformer.util.ClassMapping;
import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.SootClass;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.NewExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.VirtualInvokeExpr;
import soot.util.Chain;

public class WrapperClassTransformer extends BodyTransformer {

	static final Logger logger = LogManager.getLogger(WrapperClassTransformer.class);
	
	Chain<Local> locals;
	Chain<Unit> units;
	
	@Override
	protected void internalTransform(Body b, String phaseName, Map options) {
		logger.debug("Transforming " + b.getMethod().getDeclaringClass() + ":" + b.getMethod());
		this.locals = b.getLocals();
		this.units = b.getUnits();
		
		replaceWithWrapperClasses();
	}

	private void replaceWithWrapperClasses() {
		
	}
	
}

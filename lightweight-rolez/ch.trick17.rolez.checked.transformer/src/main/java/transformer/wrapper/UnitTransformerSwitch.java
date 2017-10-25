package transformer.wrapper;

import java.util.Map;

import soot.Local;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeStmt;

public class UnitTransformerSwitch extends AbstractStmtSwitch {
	
	ExpressionTransformerSwitch expressionTransformerSwitch;
	
	public UnitTransformerSwitch(SootClass availableClass, Local local, 
			Map<SootMethod, SootMethod> changedMethods, Map<SootField,SootField> changedFields) {
		this.expressionTransformerSwitch = new ExpressionTransformerSwitch(availableClass, local, changedMethods, changedFields);
	}

	public void caseInvokeStmt(InvokeStmt stmt) {
    	stmt.getInvokeExpr().apply(this.expressionTransformerSwitch);
    }

    public void caseAssignStmt(AssignStmt stmt) {
		stmt.getRightOp().apply(this.expressionTransformerSwitch);
		stmt.getLeftOp().apply(this.expressionTransformerSwitch);
    }
}

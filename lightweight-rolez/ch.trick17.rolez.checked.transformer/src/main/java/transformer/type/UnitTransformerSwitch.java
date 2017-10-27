package transformer.type;

import java.util.Map;

import soot.SootField;
import soot.SootMethod;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeStmt;

public class UnitTransformerSwitch extends AbstractStmtSwitch {
	
	ExpressionTransformerSwitch expressionTransformerSwitch;
	
	public UnitTransformerSwitch(
			Map<SootMethod, SootMethod> changedMethods, 
			Map<SootField,SootField> changedFields) {
		this.expressionTransformerSwitch = new ExpressionTransformerSwitch(changedMethods, changedFields);
	}

	public void caseInvokeStmt(InvokeStmt stmt) {
    	stmt.getInvokeExpr().apply(this.expressionTransformerSwitch);
    }

    public void caseAssignStmt(AssignStmt stmt) {
		stmt.getRightOp().apply(this.expressionTransformerSwitch);
		stmt.getLeftOp().apply(this.expressionTransformerSwitch);
    }
}

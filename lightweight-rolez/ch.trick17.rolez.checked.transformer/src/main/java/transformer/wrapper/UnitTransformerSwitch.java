package transformer.wrapper;

import java.util.Map;

import soot.Local;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeStmt;

public class UnitTransformerSwitch extends AbstractStmtSwitch {
	
	ExpressionTransformerSwitch expressionTransformerSwitch;
	
	public UnitTransformerSwitch(SootClass availableClass, Local local, 
			Map<String, SootMethod> changedMethodSignatures) {
		this.expressionTransformerSwitch = new ExpressionTransformerSwitch(availableClass, local, changedMethodSignatures);
	}

	public void caseInvokeStmt(InvokeStmt stmt) {
    	stmt.getInvokeExpr().apply(this.expressionTransformerSwitch);
    }

    public void caseAssignStmt(AssignStmt stmt) {
		stmt.getRightOp().apply(this.expressionTransformerSwitch);
    }
}

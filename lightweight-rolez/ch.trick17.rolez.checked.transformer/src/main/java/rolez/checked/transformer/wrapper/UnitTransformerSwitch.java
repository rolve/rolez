package rolez.checked.transformer.wrapper;

import java.util.Map;

import soot.Local;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeStmt;

public class UnitTransformerSwitch extends AbstractStmtSwitch {
	
	SootClass availableClass;
	Local local;
	Map<String, SootMethod> changedMethodSignatures;
	
	public UnitTransformerSwitch(SootClass availableClass, Local local, 
			Map<String, SootMethod> changedMethodSignatures) {
		this.availableClass = availableClass;
		this.local = local;
		this.changedMethodSignatures = changedMethodSignatures;
	}

	public void caseInvokeStmt(InvokeStmt stmt) {
    	stmt.getInvokeExpr().apply(new ExpressionTransformerSwitch(this.availableClass, this.local, this.changedMethodSignatures));
    }

    public void caseAssignStmt(AssignStmt stmt) {
		stmt.getRightOp().apply(new ExpressionTransformerSwitch(this.availableClass, this.local, this.changedMethodSignatures));
    }
}

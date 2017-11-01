package transformer.id;

import java.util.Map;

import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeStmt;

public class TaskIdStmtTransformerSwitch extends AbstractStmtSwitch {

	private TaskIdExprTransformerSwitch exprTransformerSwitch;
	
	public TaskIdStmtTransformerSwitch(Map<SootMethod, SootMethod> changedMethods, Local taskIdLocal, Unit unit) {
		this.exprTransformerSwitch = new TaskIdExprTransformerSwitch(changedMethods, taskIdLocal, unit);
	}
	
	@Override
	public void caseInvokeStmt(InvokeStmt stmt) {
		stmt.getInvokeExpr().apply(this.exprTransformerSwitch);
	}

	@Override
	public void caseAssignStmt(AssignStmt stmt) {
		Value rightOp = stmt.getRightOp();
		rightOp.apply(this.exprTransformerSwitch);
	}
}

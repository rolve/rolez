package transformer.checking;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.VirtualInvokeExpr;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;

public class ReadCheckAnalysis extends CheckingAnalysis {

	static final Logger logger = LogManager.getLogger(ReadCheckAnalysis.class);
	FlowSet emptySet = new ArraySparseSet();
	
	public ReadCheckAnalysis(DirectedGraph<Unit> graph) {
		super(graph);
	}

	@Override
	protected void flowThrough(FlowSet in, Unit d, FlowSet out) {
		in.copy(out);
		if (d instanceof AssignStmt) {
			AssignStmt a = (AssignStmt)d;
			Value rightOp = a.getRightOp();
			
			// Is the assignment a task call?
			if (isTaskCall(rightOp)) {
				out.clear();
				return;
			}
			
			// Is it a read from a checked field?
			if (rightOp instanceof InstanceFieldRef) {
				InstanceFieldRef fieldRef = (InstanceFieldRef)rightOp;
				Value base = fieldRef.getBase();
				
				// Final field reads are never checked
				if (fieldRef.getField().isFinal())
					return;
				
				if (isSubtypeOfChecked(fieldRef.getBase().getType()))
					out.add(base);
				return;
			}
			
			// Is it a checked method invocation?
			if (rightOp instanceof VirtualInvokeExpr) {
				VirtualInvokeExpr vInvokeExpr = (VirtualInvokeExpr)rightOp;
				Value base = vInvokeExpr.getBase();
				if (isSubtypeOfChecked(base.getType())) 
					if (isReadMethodInvocation(vInvokeExpr.getMethod())) 
						out.add(base);
				return;
			}
		}
		
		// Is it a checked method invocation without an assignment?
		if (d instanceof InvokeStmt) {
			InvokeStmt invokeStmt = (InvokeStmt)d;
			InvokeExpr invokeExpr = invokeStmt.getInvokeExpr();
			if (invokeExpr instanceof VirtualInvokeExpr) {
				VirtualInvokeExpr vInvokeExpr = (VirtualInvokeExpr)invokeExpr;
				Value base = vInvokeExpr.getBase();
				if (isSubtypeOfChecked(base.getType()))
					if (isReadMethodInvocation(vInvokeExpr.getMethod())) 
						out.add(base);
			}
			return;
		}
	}
}

package transformer.checking;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.InstanceFieldRef;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;

public class WriteCheckAnalysis extends CheckingAnalysis {

	static final Logger logger = LogManager.getLogger(WriteCheckAnalysis.class);
	
	FlowSet emptySet = new ArraySparseSet();
	
	public WriteCheckAnalysis(DirectedGraph<Unit> graph) {
		super(graph);
	}

	@Override
	protected void flowThrough(FlowSet in, Unit d, FlowSet out) {
		in.copy(out);
		if (d instanceof AssignStmt) {
			AssignStmt a = (AssignStmt)d;
			Value rightOp = a.getRightOp();
			Value leftOp = a.getLeftOp();
			
			// Is it a write to a checked field?
			if (leftOp instanceof InstanceFieldRef) {
				InstanceFieldRef fieldRef = (InstanceFieldRef)leftOp;
				Value base = fieldRef.getBase();
				if (isSubtypeOfChecked(fieldRef.getBase().getType())) {
					out.add(base);
				}
			}
				
			// Is the assignment a task call?
			if (isTaskCall(rightOp))
				out.clear();
		}
	}
}
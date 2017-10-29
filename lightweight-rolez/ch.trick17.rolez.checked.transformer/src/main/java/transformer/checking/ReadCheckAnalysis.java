package transformer.checking;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.InstanceFieldRef;
import soot.tagkit.AnnotationTag;
import soot.tagkit.Tag;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import transformer.util.Constants;

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
			if (isTaskCall(rightOp))
				out.clear();
			
			// Is it a read from a checked field?
			if (rightOp instanceof InstanceFieldRef) {
				InstanceFieldRef fieldRef = (InstanceFieldRef)rightOp;
				Value base = fieldRef.getBase();
				
				// TODO: actually writes also contain a read, have to look ahead two units, to find out if it has to be read or write checked
				if (base.getType().equals(Constants.CHECKED_ARRAY_CLASS.getType())) {
					for (Tag t : d.getTags()) {
						if (t instanceof AnnotationTag) {
							AnnotationTag aTag = (AnnotationTag)t;
							if (aTag.getType().equals("Read")) 
								out.add(base);
						}
					}
					return;
				}
				
				// Final field reads are never checked
				if (fieldRef.getField().isFinal())
					return;
				
				if (isSubtypeOfChecked(fieldRef.getBase().getType())) {
					out.add(base);
				}
			}
		}
	}
}

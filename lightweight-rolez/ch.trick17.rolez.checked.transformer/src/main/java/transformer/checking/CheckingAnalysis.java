package transformer.checking;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.VirtualInvokeExpr;
import soot.tagkit.AnnotationTag;
import soot.tagkit.Tag;
import soot.tagkit.VisibilityAnnotationTag;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import transformer.exceptions.PhantomClassException;
import transformer.util.Constants;

public abstract class CheckingAnalysis extends ForwardFlowAnalysis<Unit, FlowSet> {

	FlowSet emptySet = new ArraySparseSet();
	
	public CheckingAnalysis(DirectedGraph<Unit> graph) {
		super(graph);
	}

	public void run() {
		doAnalysis();
	}
	
	@Override
	protected abstract void flowThrough(FlowSet in, Unit d, FlowSet out);
	
	@Override
	protected FlowSet newInitialFlow() {
		return this.emptySet.clone();
	}

	@Override
	protected FlowSet entryInitialFlow() {
		return this.emptySet.clone();
	}

	@Override
	protected void merge(FlowSet in1, FlowSet in2, FlowSet out) {
		in1.intersection(in2, out);
	}

	@Override
	protected void copy(FlowSet source, FlowSet dest) {
		source.copy(dest);
	}
	
	protected boolean isTaskCall(Value op) {
		if (op instanceof VirtualInvokeExpr) {
			VirtualInvokeExpr viexpr = (VirtualInvokeExpr)op;
			return viexpr.getMethod().getReturnType().equals(Constants.TASK_CLASS.getType()) && !viexpr.getMethod().getName().equals("start");
		}
		return false;
	}
	
	protected boolean isSubtypeOfChecked(Type t) {
		SootClass classOfType = Scene.v().loadClass(t.toString(), SootClass.HIERARCHY);
		
		if (classOfType.isPhantom())
			throw new PhantomClassException(classOfType + " is a phantom class in.");
		
		SootClass currentClass = classOfType;
		 while(!currentClass.equals(Constants.OBJECT_CLASS)) {
			if (currentClass.equals(Constants.CHECKED_CLASS)) 
				return true;
			currentClass = currentClass.getSuperclass();
		}
		return false;
	}
	
	protected boolean isCheckedSlice(Type t) {
		return t.equals(Constants.CHECKED_SLICE_CLASS.getType()) || t.equals(Constants.CHECKED_ARRAY_CLASS.getType());
	}
	
	protected boolean isReadMethodInvocation(SootMethod m) {
		for (Tag t : m.getTags()) 
			if (t instanceof VisibilityAnnotationTag) 
				for (AnnotationTag aTag : ((VisibilityAnnotationTag) t).getAnnotations()) 
					if (aTag.getType().equals(Constants.READ_ANNOTATION)) 
						return true;
		return false;
	}
	
	protected boolean isWriteMethodInvocation(SootMethod m) {
		for (Tag t : m.getTags()) 
			if (t instanceof VisibilityAnnotationTag) 
				for (AnnotationTag aTag : ((VisibilityAnnotationTag) t).getAnnotations()) 
					if (aTag.getType().equals(Constants.WRITE_ANNOTATION)) 
						return true;
		return false;
	}
}

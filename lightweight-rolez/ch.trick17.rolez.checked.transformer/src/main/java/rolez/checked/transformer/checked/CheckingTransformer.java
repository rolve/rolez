package rolez.checked.transformer.checked;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rolez.checked.transformer.util.Constants;
import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.Jimple;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.util.Chain;

/**
 * Transformer inserts methods {@link rolez.checked.lang.Checked#checkLegalRead(rolez.checked.lang.Checked) checkLegalRead} to checked
 * field reads and {@link rolez.checked.lang.Checked#checkLegalWrite(rolez.checked.lang.Checked) checkLegalWrite} to checked field writes.
 * 
 * @author Michael Giger
 *
 */
public class CheckingTransformer extends BodyTransformer {
	
	static final Logger logger = LogManager.getLogger(CheckingTransformer.class);
	
	static final Jimple J = Jimple.v();
	
	int tempLocalCount = 0;
	
	Chain<Local> locals;
	Chain<Unit> units;
	
	@Override
	protected void internalTransform(Body b, String phaseName, Map options) {
		
		SootMethod currentMethod = b.getMethod();
		
		// No task calls allowed in constructors -> no guarding necessary here
		if (isConstructorWithoutArgs(currentMethod)) return;

		// Field reads in guardedRefs don't have to be guarded
		if (isGuardedRefsMethod(currentMethod)) return;
		
		logger.debug("Transforming " + currentMethod.getDeclaringClass() + ":" + currentMethod.getSignature());
		
		this.locals = b.getLocals();
		this.units = b.getUnits();
		
		ReadCheckAnalysis readAnalysis = doReadFlowAnalysis(b);
		WriteCheckAnalysis writeAnalysis = doWriteFlowAnalysis(b);
		
		// Use flow analysis to insert checks only where necessary
		Iterator<Unit> unitIter = units.snapshotIterator();
		while (unitIter.hasNext()) {
			Unit u = unitIter.next();
			if (u instanceof AssignStmt) {
				AssignStmt astmt = (AssignStmt)u;
				if (increasesWriteSet(astmt, writeAnalysis))
					addCheckLegalWrite(astmt);
				else if (increasesReadSet(astmt, readAnalysis))
					addCheckLegalRead(astmt);
			}
		}
		
		// Reset tempLocalCount for next method
		tempLocalCount = 0;
	}
	
	/**
	 * Method checks if a unit did increase the set of the read-checked variables
	 * @param u
	 * @param analysis
	 * @return
	 */
	private boolean increasesReadSet(Unit u, ReadCheckAnalysis analysis) {
		FlowSet beforeRead = analysis.getFlowBefore(u);
		FlowSet afterRead = analysis.getFlowAfter(u);
		FlowSet diff = new ArraySparseSet();
		afterRead.difference(beforeRead, diff);
		return diff.size() > 0;
	}
	
	/**
	 * Method checks if a unit did increase the set of the write-checked variables
	 * @param u
	 * @param analysis
	 * @return
	 */
	private boolean increasesWriteSet(Unit u, WriteCheckAnalysis analysis) {
		FlowSet beforeWrite = analysis.getFlowBefore(u);
		FlowSet afterWrite = analysis.getFlowAfter(u);
		FlowSet diff = new ArraySparseSet();
		afterWrite.difference(beforeWrite, diff);
		return diff.size() > 0;
	}
	
	private ReadCheckAnalysis doReadFlowAnalysis(Body b) {
		ExceptionalUnitGraph graph = new ExceptionalUnitGraph(b);
		return new ReadCheckAnalysis(graph);
	}
	
	private WriteCheckAnalysis doWriteFlowAnalysis(Body b) {
		ExceptionalUnitGraph graph = new ExceptionalUnitGraph(b);
		return new WriteCheckAnalysis(graph);
	}
	
	private void addCheckLegalRead(AssignStmt read) {
		InstanceFieldRef rightOp = (InstanceFieldRef) read.getRightOp();
		Local base = (Local) rightOp.getBase();
		Type baseType = base.getType();
		
		// Add locals
		Local checkedTemp = J.newLocal("checkedTemp" + Integer.toString(tempLocalCount), Constants.CHECKED_CLASS.getType());
		Local temp = J.newLocal("temp" + Integer.toString(tempLocalCount), baseType);
		locals.add(checkedTemp);
		locals.add(temp);
		tempLocalCount++;
		
		// Insert the call to checkLegalRead
		Unit checkStmt = J.newAssignStmt(checkedTemp, J.newStaticInvokeExpr(
				Constants.CHECKED_CLASS.getMethod("checkLegalRead", Arrays.asList(new Type[] { Constants.CHECKED_CLASS.getType() })).makeRef(), 
				Arrays.asList(new Local[] { base })));
		units.insertBefore(checkStmt, read);
		
		// Insert a cast, to cast the result of checkLegalRead to the right type again
		Unit castStmt = J.newAssignStmt(temp, J.newCastExpr(checkedTemp, baseType));
		units.insertBefore(castStmt, read);
		
		// Change base of original field read to checked variable
		read.setRightOp(J.newInstanceFieldRef(temp, rightOp.getFieldRef()));
	}
	
	private void addCheckLegalWrite(AssignStmt write) {
		InstanceFieldRef leftOp = (InstanceFieldRef) write.getLeftOp();
		Local base = (Local) leftOp.getBase();
		Type baseType = base.getType();
		
		Local checkedTemp = J.newLocal("checkedTemp" + Integer.toString(tempLocalCount), Constants.CHECKED_CLASS.getType());
		Local temp = J.newLocal("temp" + Integer.toString(tempLocalCount), baseType);
		locals.add(checkedTemp);
		locals.add(temp);
		tempLocalCount++;
		
		// Insert the call to checkLegalWrite
		Unit checkStmt = J.newAssignStmt(checkedTemp, J.newStaticInvokeExpr(
				Constants.CHECKED_CLASS.getMethod("checkLegalWrite", Arrays.asList(new Type[] { Constants.CHECKED_CLASS.getType() })).makeRef(), 
				Arrays.asList(new Local[] { base })));
		units.insertBefore(checkStmt, write);
		
		// Insert a cast, to cast the result of checkLegalRead to the right type again
		Unit castStmt = J.newAssignStmt(temp, J.newCastExpr(checkedTemp, baseType));
		units.insertBefore(castStmt, write);
		
		// Change base of original field read to checked variable
		write.setLeftOp(J.newInstanceFieldRef(temp, leftOp.getFieldRef()));
	}
	
	private boolean isGuardedRefsMethod(SootMethod m) {
		return m.getSubSignature().equals("java.lang.Iterable guardedRefs()");
	}
	
	private boolean isConstructorWithoutArgs(SootMethod m) {
		return m.getSubSignature().equals("void <init>()");
	}
}

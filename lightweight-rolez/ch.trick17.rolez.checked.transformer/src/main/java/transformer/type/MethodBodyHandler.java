package transformer.type;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import soot.ArrayType;
import soot.Body;
import soot.Local;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.NewArrayExpr;
import soot.tagkit.AnnotationTag;
import soot.util.Chain;
import transformer.util.ClassMapping;
import transformer.util.Constants;

public class MethodBodyHandler {

	static final Jimple J = Jimple.v();
	
	Map<SootMethod, SootMethod> changedMethods;
	Map<SootField, SootField> changedFields;
	
	ArrayList<Local> arrayLocalsToKeep = new ArrayList<Local>();
	int tempLocalCount = 0;
	
	Chain<Unit> units;
	Chain<Local> locals;
	
	public MethodBodyHandler(Map<SootMethod, SootMethod> changedMethods, Map<SootField, SootField> changedFields) {
		this.changedMethods = changedMethods;
		this.changedFields = changedFields;
	}
	
	public void handle(Body b) {
		this.locals = b.getLocals();
		this.units = b.getUnits();
		
		// Set new types for all units
		Iterator<Unit> unitIter = units.snapshotIterator();
		while (unitIter.hasNext()) {
			Unit u = unitIter.next();
			handleArrayUnit(u);
			u.apply(new UnitTransformerSwitch(changedMethods, changedFields));
		}
		
		// Set type of the locals AFTER units have transformed, since the unit transformer
		// needs the old types of the local variables to find the possibly new ones.
		handleLocalVariables();
	}
	
	private void handleLocalVariables() {
		Iterator<Local> localIter = locals.snapshotIterator();
		while (localIter.hasNext()) {
			Local l = localIter.next();
			Type type = l.getType();

			// For the remaining array variables, check whether their types have an available wrapper type
			if (this.arrayLocalsToKeep.contains(l)) {
				// should always be an array type
				ArrayType arrType = (ArrayType)type;
				SootClass availableArrayClass = ClassMapping.MAP.get(arrType.getArrayElementType().toString());
				if (availableArrayClass != null)
					l.setType(ArrayType.v(availableArrayClass.getType(), arrType.numDimensions));
				
				continue;
			}

			// Replace arrays by checked arrays
			if (type instanceof ArrayType) {
				l.setType(Constants.CHECKED_ARRAY_CLASS.getType());
				continue;
			}

			// Replace classes by wrapper classes if available
			SootClass availableClass = ClassMapping.MAP.get(type.toString());
			if (availableClass != null)  
				l.setType(availableClass.getType());
		}
	}
	
	private void handleArrayUnit(Unit u) {
		// Handle array expressions
		if (u instanceof AssignStmt) {
			AssignStmt aStmt = (AssignStmt)u;
			Value leftOp = aStmt.getLeftOp();
			Value rightOp = aStmt.getRightOp();
			if (rightOp instanceof NewArrayExpr) {
				NewArrayExpr newArrExpr = (NewArrayExpr)rightOp;
				Local temp0 = J.newLocal("checkedArr" + tempLocalCount, Constants.CHECKED_ARRAY_CLASS.getType());
				Local temp1 = J.newLocal("data" + tempLocalCount, newArrExpr.getBaseType().makeArrayType());
				arrayLocalsToKeep.add(temp1);
				locals.add(temp0);
				locals.add(temp1);
				Unit newCheckedArrayStmt = J.newAssignStmt(temp0, J.newNewExpr(Constants.CHECKED_ARRAY_CLASS.getType()));
				units.insertBefore(newCheckedArrayStmt, u);
				Unit newArrayStmt = J.newAssignStmt(temp1, newArrExpr);
				units.insertBefore(newArrayStmt, u);
				Unit invokeConstrStmt = J.newInvokeStmt(J.newSpecialInvokeExpr(temp0,Constants.CHECKED_ARRAY_CLASS.getMethod("void <init>(java.lang.Object)").makeRef(), temp1));
				units.insertBefore(invokeConstrStmt, u);
				Unit newAssignStmt = J.newAssignStmt(aStmt.getLeftOp(), temp0);
				units.insertBefore(newAssignStmt, u);
				units.remove(u);
				tempLocalCount++;
			} else if (leftOp instanceof ArrayRef) {
				ArrayRef arrRef = (ArrayRef)leftOp;
				Local temp0 = J.newLocal("data" + tempLocalCount, Constants.OBJECT_CLASS.getType());
				Local temp1 = J.newLocal("$data" + tempLocalCount, arrRef.getType().makeArrayType());
				arrayLocalsToKeep.add(temp1);
				locals.add(temp0);
				locals.add(temp1);
				Unit getData = J.newAssignStmt(
						temp0, 
						J.newInstanceFieldRef(arrRef.getBase(), Constants.CHECKED_SLICE_CLASS.getField("java.lang.Object data").makeRef()));
				units.insertBefore(getData, u);
				getData.addTag(new AnnotationTag("Write"));
				Unit castData = J.newAssignStmt(temp1, J.newCastExpr(temp0, temp1.getType()));
				units.insertBefore(castData, u);
				Unit assignData = J.newAssignStmt(J.newArrayRef(temp1, arrRef.getIndex()), rightOp);
				units.insertBefore(assignData, u);
				units.remove(u);
				tempLocalCount++;
			} else if (rightOp instanceof ArrayRef) {
				ArrayRef arrRef = (ArrayRef)rightOp;
				Local temp0 = J.newLocal("data" + tempLocalCount, Constants.OBJECT_CLASS.getType());
				Local temp1 = J.newLocal("$data" + tempLocalCount, arrRef.getType().makeArrayType());
				arrayLocalsToKeep.add(temp1);
				locals.add(temp0);
				locals.add(temp1);
				Unit getData = J.newAssignStmt(
						temp0, 
						J.newInstanceFieldRef(arrRef.getBase(), Constants.CHECKED_SLICE_CLASS.getField("java.lang.Object data").makeRef()));
				getData.addTag(new AnnotationTag("Read"));
				units.insertBefore(getData, u);
				Unit castData = J.newAssignStmt(temp1, J.newCastExpr(temp0, temp1.getType()));
				units.insertBefore(castData, u);
				Unit assignData = J.newAssignStmt(leftOp, J.newArrayRef(temp1, arrRef.getIndex()));
				units.insertBefore(assignData, u);
				units.remove(u);
				tempLocalCount++;
			} else if (rightOp instanceof InvokeExpr) {
				InvokeExpr invokeExpr = (InvokeExpr)rightOp;
				if (invokeExpr.getType() instanceof ArrayType) {
					ArrayType arrType = (ArrayType)invokeExpr.getType();
					Local temp0 = J.newLocal("data" + tempLocalCount, arrType);
					Local temp1 = J.newLocal("checkedArr" + tempLocalCount, Constants.CHECKED_ARRAY_CLASS.getType());
					locals.add(temp0);
					locals.add(temp1);
					Unit dataArrayAssignment = J.newAssignStmt(temp0, rightOp);
					units.insertBefore(dataArrayAssignment, u);
					Unit newCheckedArrayStmt = J.newAssignStmt(temp1, J.newNewExpr(Constants.CHECKED_ARRAY_CLASS.getType()));
					units.insertBefore(newCheckedArrayStmt, u);
					Unit invokeConstrStmt = J.newInvokeStmt(J.newSpecialInvokeExpr(temp1,Constants.CHECKED_ARRAY_CLASS.getMethod("void <init>(java.lang.Object)").makeRef(), temp0));
					units.insertBefore(invokeConstrStmt, u);
					Unit newAssignStmt = J.newAssignStmt(aStmt.getLeftOp(), temp1);
					units.insertBefore(newAssignStmt, u);
					units.remove(u);
					tempLocalCount++;
				}
			}
		}
	}
}

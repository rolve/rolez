package transformer.util;

import java.util.Arrays;
import java.util.List;

import soot.Local;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;

public class UnitFactory {

	static final Jimple J = Jimple.v();
	
	public static Unit newThisRef(Local left, RefType type) {
		return J.newIdentityStmt(left, J.newThisRef(type));
	}
	
	public static Unit newParameterRef(Local left, Type type, int number) {
		return J.newIdentityStmt(left, J.newParameterRef(type, number));
	}
	
	public static Unit newSpecialInvokeExpr(Local base, SootClass baseClass, String methodName) {
		return J.newInvokeStmt(J.newSpecialInvokeExpr(base, baseClass.getMethodByName(methodName).makeRef()));
	}

	public static Unit newSpecialInvokeExpr(Local base, SootClass baseClass, String methodName, Local[] args) {
		return J.newInvokeStmt(J.newSpecialInvokeExpr(base, baseClass.getMethodByName(methodName).makeRef(), Arrays.asList(args)));
	}

	public static Unit newSpecialInvokeExpr(Local base, SootClass baseClass, String methodName, List<Local> args) {
		return J.newInvokeStmt(J.newSpecialInvokeExpr(base, baseClass.getMethodByName(methodName).makeRef(), args));
	}
	
	public static Unit newVirtualInvokeExpr(Local base, SootClass baseClass, String methodName) {
		return J.newInvokeStmt(J.newVirtualInvokeExpr(
				base, 
				baseClass.getMethodByName(methodName).makeRef()));
	}
	
	public static Unit newVirtualInvokeExpr(Local base, SootClass baseClass, String methodName, Local[] args) {
		return J.newInvokeStmt(J.newVirtualInvokeExpr(
				base, 
				baseClass.getMethodByName(methodName).makeRef(), 
				Arrays.asList(args)));
	}
	
	public static Unit newVirtualInvokeExpr(Local base, SootClass baseClass, String methodName, List<Local> args) {
		return J.newInvokeStmt(J.newVirtualInvokeExpr(
				base, 
				baseClass.getMethodByName(methodName).makeRef(), 
				args));
	}
	
	// Assign Statements
	public static Unit newAssignNewExpr(Local left, SootClass newClass) {
		return J.newAssignStmt(left, J.newNewExpr(newClass.getType()));
	}
	
	public static Unit newAssignNewArrayExpr(Local left, Type arrayType, int size) {
		return J.newAssignStmt(left, J.newNewArrayExpr(arrayType, IntConstant.v(size)));
	}
	
	public static Unit newAssignToArrayExpr(Local base, int index, Local right) {
		return J.newAssignStmt(J.newArrayRef(base, IntConstant.v(index)), right);
	}
	
	
	public static Unit newAssignStaticInvokeExpr(Local left, SootClass baseClass, String methodName) {
		return J.newAssignStmt(left, J.newStaticInvokeExpr(baseClass.getMethodByName(methodName).makeRef()));
	}
	
	public static Unit newAssignStaticInvokeExpr(Local left, SootClass baseClass, String methodName, Local[] args) {
		return J.newAssignStmt(left, J.newStaticInvokeExpr(baseClass.getMethodByName(methodName).makeRef(), Arrays.asList(args)));
	}
	
	public static Unit newAssignStaticInvokeExpr(Local left, SootClass baseClass, String methodName, List<Local> args) {
		return J.newAssignStmt(left, J.newStaticInvokeExpr(baseClass.getMethodByName(methodName).makeRef(), args));
	}
	
	public static Unit newAssignVirtualInvokeExpr(Local left, Local base, SootClass baseClass, String methodName){
		return J.newAssignStmt(left, J.newVirtualInvokeExpr(base, baseClass.getMethodByName(methodName).makeRef()));
	}
	
	public static Unit newAssignVirtualInvokeExpr(Local left, Local base, SootClass baseClass, String methodName, Local[] args) {
		return J.newAssignStmt(left, J.newVirtualInvokeExpr(base, baseClass.getMethodByName(methodName).makeRef(), Arrays.asList(args)));
	}
	
	public static Unit newAssignVirtualInvokeExpr(Local left, Local base, SootClass baseClass, String methodName, List<Local> args) {
		return J.newAssignStmt(left, J.newVirtualInvokeExpr(base, baseClass.getMethodByName(methodName).makeRef(), args));
	}
	
	public static Unit newAssignVirtualInvokeExpr(Local left, Local base, SootMethod method) {
		return J.newAssignStmt(left, J.newVirtualInvokeExpr(base, method.makeRef()));
	}
	
	public static Unit newAssignLocalToFieldExpr(Local base, SootClass fieldClass, String fieldName, Local right) {
		return J.newAssignStmt(J.newInstanceFieldRef(base, fieldClass.getFieldByName(fieldName).makeRef()), right);
	}
	
	public static Unit newAssignFieldToLocalExpr(Local left, Local base, SootClass fieldClass, String fieldName) {
		return J.newAssignStmt(left, J.newInstanceFieldRef(base, fieldClass.getFieldByName(fieldName).makeRef()));
	}
}

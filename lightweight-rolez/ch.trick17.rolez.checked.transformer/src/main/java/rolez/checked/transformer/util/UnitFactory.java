package rolez.checked.transformer.util;

import java.util.Arrays;
import java.util.List;

import soot.Local;
import soot.SootClass;
import soot.Unit;
import soot.jimple.Jimple;

public class UnitFactory {

	static final Jimple J = Jimple.v();
	
	public static Unit newSpecialInvokeExpr(Local base, SootClass baseClass, String methodName) {
		return J.newInvokeStmt(J.newSpecialInvokeExpr(base, baseClass.getMethodByName(methodName).makeRef()));
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
}

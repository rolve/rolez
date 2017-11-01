package transformer.id;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AbstractJimpleValueSwitch;
import soot.jimple.AssignStmt;
import soot.jimple.DynamicInvokeExpr;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.VirtualInvokeExpr;

public class TaskIdExprTransformerSwitch extends AbstractJimpleValueSwitch {

	private Map<SootMethod, SootMethod> changedMethods;
	private Local taskIdLocal;
	private Unit unit;
	
	public TaskIdExprTransformerSwitch(Map<SootMethod, SootMethod> changedMethods, Local taskIdLocal, Unit unit) {
		this.changedMethods = changedMethods;
		this.taskIdLocal = taskIdLocal;
		this.unit = unit;
	}

	@Override
	public void caseDynamicInvokeExpr(DynamicInvokeExpr v) {
		
	}

	@Override
	public void caseInterfaceInvokeExpr(InterfaceInvokeExpr v) {
		SootMethod m = v.getMethod();
		if (changedMethods.containsKey(m)) {
			SootMethod newMethod = changedMethods.get(m);
			List<Value> newArgs = new ArrayList<Value>();
			newArgs.addAll(v.getArgs());
			newArgs.add(taskIdLocal);
			if (unit instanceof AssignStmt) {
				((AssignStmt) unit).setRightOp(Jimple.v().newInterfaceInvokeExpr((Local)v.getBase(), newMethod.makeRef(), newArgs));
			} else if (unit instanceof InvokeStmt) {
				((InvokeStmt) unit).setInvokeExpr(Jimple.v().newInterfaceInvokeExpr((Local)v.getBase(), newMethod.makeRef(), newArgs));
			}
		}
	}

	@Override
	public void caseSpecialInvokeExpr(SpecialInvokeExpr v) {
		SootMethod m = v.getMethod();
		if (changedMethods.containsKey(m)) {
			SootMethod newMethod = changedMethods.get(m);
			List<Value> newArgs = new ArrayList<Value>();
			newArgs.addAll(v.getArgs());
			newArgs.add(taskIdLocal);
			if (unit instanceof AssignStmt) {
				((AssignStmt) unit).setRightOp(Jimple.v().newSpecialInvokeExpr((Local)v.getBase(), newMethod.makeRef(), newArgs));
			} else if (unit instanceof InvokeStmt) {
				((InvokeStmt) unit).setInvokeExpr(Jimple.v().newSpecialInvokeExpr((Local)v.getBase(), newMethod.makeRef(), newArgs));
			}
		}
	}

	@Override
	public void caseStaticInvokeExpr(StaticInvokeExpr v) {
		SootMethod m = v.getMethod();
		if (changedMethods.containsKey(m)) {
			SootMethod newMethod = changedMethods.get(m);
			List<Value> newArgs = new ArrayList<Value>();
			newArgs.addAll(v.getArgs());
			newArgs.add(taskIdLocal);
			if (unit instanceof AssignStmt) {
				((AssignStmt) unit).setRightOp(Jimple.v().newStaticInvokeExpr(newMethod.makeRef(), newArgs));
			} else if (unit instanceof InvokeStmt) {
				((InvokeStmt) unit).setInvokeExpr(Jimple.v().newStaticInvokeExpr(newMethod.makeRef(), newArgs));
			}
		}
	}

	@Override
	public void caseVirtualInvokeExpr(VirtualInvokeExpr v) {
		SootMethod m = v.getMethod();
		if (changedMethods.containsKey(m)) {
			SootMethod newMethod = changedMethods.get(m);
			List<Value> newArgs = new ArrayList<Value>();
			newArgs.addAll(v.getArgs());
			newArgs.add(taskIdLocal);
			if (unit instanceof AssignStmt) {
				((AssignStmt) unit).setRightOp(Jimple.v().newVirtualInvokeExpr((Local)v.getBase(), newMethod.makeRef(), newArgs));
			} else if (unit instanceof InvokeStmt) {
				((InvokeStmt) unit).setInvokeExpr(Jimple.v().newVirtualInvokeExpr((Local)v.getBase(), newMethod.makeRef(), newArgs));
			}
		}
	}
}

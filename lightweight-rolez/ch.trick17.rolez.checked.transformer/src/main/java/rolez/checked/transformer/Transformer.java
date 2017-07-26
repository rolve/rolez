package rolez.checked.transformer;

import java.util.Iterator;
import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.RefType;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.util.Chain;

public class Transformer extends BodyTransformer {
	
	@Override
	protected void internalTransform(Body body, String phase, Map options) {
		SootMethod method = body.getMethod();
		
		if (method.isConstructor())
			return;
		
		System.out.println("instrumenting method: " + method.getSignature());
		
		// Show jimple
		System.out.println();
		System.out.println("BEFORE TRANSFORMATION");
		System.out.println("---------------------");
		System.out.println(body.toString());
		
		System.out.println();
		System.out.println("TRANSFORMATION OUTPUT");
		System.out.println("---------------------");
		
		Chain<Unit> units = body.getUnits();
		Iterator<Unit> stmtIt = units.snapshotIterator();
		while (stmtIt.hasNext()) {
			Stmt s = (Stmt) stmtIt.next();
			
			System.out.println(padString(s.getClass().toString(),45) + s.toString());
			if (s instanceof InvokeStmt) {
				Local tmpRef = Jimple.v().newLocal("tmpRef", RefType.v("java.io.PrintStream"));
				body.getLocals().add(tmpRef);
				SootMethod toCall = Scene.v().getMethod("<java.io.PrintStream: void println(java.lang.String)>");
				units.swapWith(s, Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(tmpRef, toCall.makeRef(), StringConstant.v("Hello world!"))));
			} else if (s instanceof ReturnVoidStmt) {
				
			}
		}
		
		// Show transformed jimple
		System.out.println();
		System.out.println("AFTER TRANSFORMATION");
		System.out.println("--------------------");
		System.out.println(body.toString());
	}
	
	private String padString(String s, int length) {
		StringBuilder sb = new StringBuilder();
		sb.append(s);
		for (int i = s.length(); i<50; i++) {
			sb.append(" ");
		}
		return sb.toString();
	}
}

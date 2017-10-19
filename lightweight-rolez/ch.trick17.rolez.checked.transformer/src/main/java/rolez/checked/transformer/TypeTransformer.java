package rolez.checked.transformer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rolez.checked.transformer.util.ClassMapping;
import soot.Body;
import soot.Local;
import soot.RefType;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.CastExpr;
import soot.jimple.IdentityStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.NewExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.VirtualInvokeExpr;
import soot.util.Chain;

public class TypeTransformer extends SceneTransformer {
	
	static final Logger logger = LogManager.getLogger(TypeTransformer.class);

	Map<String, SootMethod> changedMethodSignatures = new HashMap<String, SootMethod>();
	
	@Override
	protected void internalTransform(String phaseName, Map options) {
		Chain<SootClass> classesToProcess = Scene.v().getApplicationClasses();
		for (SootClass c : classesToProcess) {
			
			logger.debug("Processing class: " + c.getName());
			
			// Transform fields
			for (SootField f : c.getFields())
				f.setType(getAvailableWrapperType(f.getType()));
			
			for (SootMethod m : c.getMethods()) {
				
				boolean change = false;
				String signature = m.getSubSignature();
				
				logger.debug("Transforming method " + m.getName());
				
				// Transform return type
				Type returnType = m.getReturnType();
				Type availableReturnType = getAvailableWrapperType(returnType);
				if (!returnType.equals(availableReturnType)) {
					m.setReturnType(availableReturnType);
					change = true;
				}
				
				// Transform parameters
				List<Type> newParameterTypes = new ArrayList<Type>();
				for (Type t : m.getParameterTypes()) {
					Type availableType = getAvailableWrapperType(t);
					if (!t.equals(availableType)) {
						logger.debug("Found available type " + availableType);
						newParameterTypes.add(availableType);
						change = true;
					} else {
						newParameterTypes.add(t);
					}
				}
				m.setParameterTypes(newParameterTypes);
			
				if (change) 
					changedMethodSignatures.put(signature, m);
			}
		}
		
		// Transform body after ALL signatures were transformed
		for (SootClass c : classesToProcess) {
			if (!c.isInterface()) {
				for (SootMethod m : c.getMethods()) {
					// Transform body
					Body b = m.retrieveActiveBody();
					transformMethodBody(b);
					transformMethodCalls(b);
					System.out.println(b);
				}
			}
		}
	}
	
	private void transformMethodBody(Body b) {
		Chain<Local> locals = b.getLocals();
		Chain<Unit> units = b.getUnits();
		
		for (Local l : locals){
			Type type = l.getType();
			SootClass availableClass = ClassMapping.MAP.get(type.toString());
			if (availableClass != null) {
				logger.debug("Wrapper class available for local: " + l + " " + type + " -> " + availableClass);

				RefType availableType = availableClass.getType();
				
				// Set type for local
				l.setType(availableType);
				
				// Set new type for all units
				for (Unit u : units) {
					if (u instanceof IdentityStmt) {
						logger.debug("IdentityStmt " + u);
					} else if (u instanceof AssignStmt) {
						logger.debug("AssignStmt " + u);
						AssignStmt as = (AssignStmt)u;
						Value rightOp = as.getRightOp();
						Value leftOp = as.getLeftOp();
						if (leftOp.equals(l) && rightOp instanceof NewExpr) { 
							NewExpr ne = (NewExpr)rightOp;
							ne.setBaseType(availableType);
							logger.debug("TRANSFORMED: " + u);
						}
						if (leftOp.equals(l) && rightOp instanceof CastExpr) {
							CastExpr ce = (CastExpr)rightOp;
							if (ce.getCastType().equals(type)) {
								ce.setCastType(availableType);
							}
						}
					} else if (u instanceof InvokeStmt) {
						logger.debug("InvokeStmt " + u);
						InvokeStmt ins = (InvokeStmt)u;
						InvokeExpr ine = ins.getInvokeExpr();
						if (ine instanceof SpecialInvokeExpr) {
							SpecialInvokeExpr sine = (SpecialInvokeExpr)ine;
							if (sine.getBase().equals(l)) {
								logger.debug(availableClass.getMethods());
								SootMethod newmethod = availableClass.getMethod(sine.getMethod().getName(), sine.getMethod().getParameterTypes(), sine.getMethod().getReturnType());
								sine.setMethodRef(newmethod.makeRef());
								logger.debug("TRANSFORMED: " + ins);
							}
						}
						if (ine instanceof VirtualInvokeExpr) {
							VirtualInvokeExpr vine = (VirtualInvokeExpr)ine;
							if (vine.getBase().equals(l)) {
								logger.debug(availableClass.getMethods());
								SootMethod newmethod = availableClass.getMethod(vine.getMethod().getName(), vine.getMethod().getParameterTypes(), vine.getMethod().getReturnType());
								vine.setMethodRef(newmethod.makeRef());
								logger.debug("TRANSFORMED: " + ins);
							}
						}
					}
				}
			}
		}
	}
	
	private void transformMethodCalls(Body b) {
		Chain<Unit> units = b.getUnits();
		for (Unit u : units) {
			if (u instanceof AssignStmt) {
				AssignStmt as = (AssignStmt)u;
				Value rightOp = as.getRightOp();
				if (rightOp instanceof VirtualInvokeExpr) { 
					VirtualInvokeExpr vine = (VirtualInvokeExpr)rightOp;
					for (String methodSignature : changedMethodSignatures.keySet()) {
						SootMethod method = vine.getMethod();
						if (method.getSubSignature() == methodSignature) {
							vine.setMethodRef(changedMethodSignatures.get(methodSignature).makeRef());
						}
					}
				}
			} else if (u instanceof InvokeStmt) {
				InvokeStmt ins = (InvokeStmt)u;
				InvokeExpr ine = ins.getInvokeExpr();
				if (ine instanceof SpecialInvokeExpr) {
					SpecialInvokeExpr sine = (SpecialInvokeExpr)ine;
					for (String methodSignature : changedMethodSignatures.keySet()) {
						SootMethod method = sine.getMethod();
						if (method.getSubSignature() == methodSignature) {
							sine.setMethodRef(changedMethodSignatures.get(methodSignature).makeRef());
						}
					}
				} else if (ine instanceof VirtualInvokeExpr) {
					VirtualInvokeExpr vine = (VirtualInvokeExpr)ine;
					for (String methodSignature : changedMethodSignatures.keySet()) {
						SootMethod method = vine.getMethod();
						if (method.getSubSignature() == methodSignature) {
							vine.setMethodRef(changedMethodSignatures.get(methodSignature).makeRef());
						}
					}
				}
			}
		}
	}
	
	private Type getAvailableWrapperType(Type t) {
		if (ClassMapping.MAP.get(t.toString()) != null)
			return ClassMapping.MAP.get(t.toString()).getType();
		return t;
	}
}

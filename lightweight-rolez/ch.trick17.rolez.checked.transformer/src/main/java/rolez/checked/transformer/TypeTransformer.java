package rolez.checked.transformer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
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
		
		ArrayList<SootMethod> methodsToRemove = new ArrayList<SootMethod>();
		
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
			
				if (change) {
					SootMethod newMethod = new SootMethod(m.getName(), newParameterTypes, availableReturnType, m.getModifiers(), m.getExceptions());
					c.addMethod(newMethod);
					if (!m.isAbstract())
						newMethod.setActiveBody(m.retrieveActiveBody());
					
					// Old methods has to be removed
					methodsToRemove.add(m);
					changedMethodSignatures.put(signature, newMethod);
				}
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
				}
			}
		}
		
		for (SootMethod m : methodsToRemove) {
			logger.debug("REMOVING METHOD " + m + " from class " + m.getDeclaringClass());
			m.getDeclaringClass().removeMethod(m);
		}
	}
	
	/**
	 * Changes all occuring types in a method body for which a wrapper class is available
	 * @param b
	 */
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
				Iterator<Unit> unitIter = units.snapshotIterator();
				while (unitIter.hasNext()) {
					Unit u = unitIter.next();
					
					// Cases when unit is an assign statement
					if (u instanceof AssignStmt) {
						AssignStmt as = (AssignStmt)u;
						Value rightOp = as.getRightOp();
						Value leftOp = as.getLeftOp();
						
						// Cases when left op is a local for which a class is available
						if (leftOp.equals(l) && rightOp instanceof NewExpr) {
							Unit newUnit = Jimple.v().newAssignStmt(l, 
									Jimple.v().newNewExpr(availableClass.getType()));
							units.insertBefore(newUnit, as);
							units.remove(as);
						} else if (leftOp.equals(l) && rightOp instanceof CastExpr) {
							CastExpr ce = (CastExpr)rightOp;
							if (ce.getCastType().equals(type)) {
								Unit newUnit = Jimple.v().newAssignStmt(l,
										Jimple.v().newCastExpr(ce.getOp(), availableClass.getType()));
								units.insertBefore(newUnit, as);
								units.remove(as);
							}
						}
						
						// Cases when right ops are invoke expressions
						if (rightOp instanceof VirtualInvokeExpr) {
							VirtualInvokeExpr ie = (VirtualInvokeExpr)rightOp;
							if (ie.getBase().equals(l)) {
								Unit newUnit = Jimple.v().newAssignStmt(leftOp, 
										Jimple.v().newVirtualInvokeExpr(l, availableClass.getMethod(ie.getMethod().getSubSignature()).makeRef(), ie.getArgs()));
								units.insertBefore(newUnit, as);
								units.remove(as);
							}
						} else if (rightOp instanceof SpecialInvokeExpr) {
							SpecialInvokeExpr ie = (SpecialInvokeExpr)rightOp;
							if (ie.getBase().equals(l)) {
								Unit newUnit = Jimple.v().newAssignStmt(leftOp, 
										Jimple.v().newSpecialInvokeExpr(l, availableClass.getMethod(ie.getMethod().getSubSignature()).makeRef(), ie.getArgs()));
								units.insertBefore(newUnit, as);
								units.remove(as);
							}
						} else if (rightOp instanceof InterfaceInvokeExpr) {
							InterfaceInvokeExpr ie = (InterfaceInvokeExpr)rightOp;
							if (ie.getBase().equals(l)) {
								Unit newUnit = Jimple.v().newAssignStmt(leftOp, 
										Jimple.v().newSpecialInvokeExpr(l, availableClass.getMethod(ie.getMethod().getSubSignature()).makeRef(), ie.getArgs()));
								units.insertBefore(newUnit, as);
								units.remove(as);
							}
						}
					}
					
					// Cases when the unit is a invoke statement
					if (u instanceof InvokeStmt) {
						InvokeStmt ins = (InvokeStmt)u;
						InvokeExpr ine = ins.getInvokeExpr();
						if (ine instanceof SpecialInvokeExpr) {
							SpecialInvokeExpr ie = (SpecialInvokeExpr)ine;
							if (ie.getBase().equals(l)) {
								Unit newUnit = Jimple.v().newInvokeStmt(
										Jimple.v().newSpecialInvokeExpr(l, availableClass.getMethod(ie.getMethod().getSubSignature()).makeRef(), ie.getArgs()));
								units.insertBefore(newUnit, ins);
								units.remove(ins);
							}
						}
						if (ine instanceof VirtualInvokeExpr) {
							VirtualInvokeExpr ie = (VirtualInvokeExpr)ine;
							if (ie.getBase().equals(l)) {
								Unit newUnit = Jimple.v().newInvokeStmt(
										Jimple.v().newVirtualInvokeExpr(l, availableClass.getMethod(ie.getMethod().getSubSignature()).makeRef(), ie.getArgs()));
								units.insertBefore(newUnit, ins);
								units.remove(ins);
							}
						}
						if (ine instanceof InterfaceInvokeExpr) {
							InterfaceInvokeExpr ie = (InterfaceInvokeExpr)ine;
							if (ie.getBase().equals(l)) {
								Unit newUnit = Jimple.v().newInvokeStmt(
										Jimple.v().newInterfaceInvokeExpr(l, availableClass.getMethod(ie.getMethod().getSubSignature()).makeRef(), ie.getArgs()));
								units.insertBefore(newUnit, ins);
								units.remove(ins);
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Transforms calls to methods where the signature changed due to an available wrapper type
	 * @param b
	 */
	private void transformMethodCalls(Body b) {
		Chain<Unit> units = b.getUnits();
		for (Unit u : units) {
			if (u instanceof AssignStmt) {
				AssignStmt as = (AssignStmt)u;
				Value rightOp = as.getRightOp();
				if (rightOp instanceof VirtualInvokeExpr) { 
					VirtualInvokeExpr ie = (VirtualInvokeExpr)rightOp;
					for (String methodSignature : changedMethodSignatures.keySet()) {
						SootMethod method = ie.getMethod();
						if (method.getSubSignature().equals(methodSignature)) {
							ie.setMethodRef(changedMethodSignatures.get(methodSignature).makeRef());
						}
					}
				} else if (rightOp instanceof SpecialInvokeExpr) { 
					SpecialInvokeExpr ie = (SpecialInvokeExpr)rightOp;
					for (String methodSignature : changedMethodSignatures.keySet()) {
						SootMethod method = ie.getMethod();
						if (method.getSubSignature().equals(methodSignature)) {
							ie.setMethodRef(changedMethodSignatures.get(methodSignature).makeRef());
						}
					}
				} else if (rightOp instanceof InterfaceInvokeExpr) {
					InterfaceInvokeExpr ie = (InterfaceInvokeExpr)rightOp;
					for (String methodSignature : changedMethodSignatures.keySet()) {
						SootMethod method = ie.getMethod();
						if (method.getSubSignature().equals(methodSignature)) {
							ie.setMethodRef(changedMethodSignatures.get(methodSignature).makeRef());
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
						if (method.getSubSignature().equals(methodSignature)) {
							sine.setMethodRef(changedMethodSignatures.get(methodSignature).makeRef());
						}
					}
				} else if (ine instanceof VirtualInvokeExpr) {
					VirtualInvokeExpr vine = (VirtualInvokeExpr)ine;
					for (String methodSignature : changedMethodSignatures.keySet()) {
						SootMethod method = vine.getMethod();
						if (method.getSubSignature().equals(methodSignature)) {
							vine.setMethodRef(changedMethodSignatures.get(methodSignature).makeRef());
						}
					}
				} else if (ine instanceof InterfaceInvokeExpr) {
					InterfaceInvokeExpr iine = (InterfaceInvokeExpr)ine;
					for (String methodSignature : changedMethodSignatures.keySet()) {
						SootMethod method = iine.getMethod();
						if (method.getSubSignature().equals(methodSignature)) {
							iine.setMethodRef(changedMethodSignatures.get(methodSignature).makeRef());
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

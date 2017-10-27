package transformer.transformers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import soot.ArrayType;
import soot.Body;
import soot.Local;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.NewArrayExpr;
import soot.jimple.ParameterRef;
import soot.util.Chain;
import transformer.type.MethodBodyHandler;
import transformer.type.UnitTransformerSwitch;
import transformer.util.ClassMapping;
import transformer.util.Constants;

public class TypeTransformer extends SceneTransformer {
	
	static final Logger logger = LogManager.getLogger(TypeTransformer.class);

	Map<SootMethod, SootMethod> changedMethods = new HashMap<SootMethod, SootMethod>();
	Map<SootField, SootField> changedFields = new HashMap<SootField, SootField>();
	
	@Override
	protected void internalTransform(String phaseName, Map options) {
		Chain<SootClass> classesToProcess = Scene.v().getApplicationClasses();
		
		for (SootClass c : classesToProcess) {
			// Transform fields
			transformFields(c);

			// Transform method signatures
			for (SootMethod m : c.getMethods())
				transformMethodSignature(m);
		}

		// Transform body after ALL signatures were transformed
		for (SootClass c : classesToProcess) {
			if (!c.isAbstract())
				for (SootMethod m : c.getMethods())
					transformMethodBody(m);
		}
		
		// Remove methods with old types
		for (SootMethod m : changedMethods.keySet())
			m.getDeclaringClass().removeMethod(m);
		
		// Remove fields with old types
		for (SootField f : changedFields.keySet())
			f.getDeclaringClass().removeField(f);
	}
	
	private void transformFields(SootClass c) {
		Chain<SootField> fields = c.getFields();
		Iterator<SootField> iterator = fields.snapshotIterator();
		while (iterator.hasNext()) {
			SootField f = iterator.next();
			Type fieldType = f.getType();
			Type newType = getAvailableWrapperType(fieldType);
			if (!newType.equals(fieldType)) {
				SootField newField = new SootField(f.getName(), newType, f.getModifiers());
				c.addField(newField);
				changedFields.put(f, newField);
			}
			
			// Handle arrays
			if (f.getType() instanceof ArrayType) {
				SootField newField = new SootField(f.getName(), Constants.CHECKED_ARRAY_CLASS.getType(), f.getModifiers());
				c.addField(newField);
				changedFields.put(f, newField);
			}
		}
	}
	
	/**
	 * Transforms the signatures of the methods containing a type for which a checked
	 * wrapper is available.
	 * @param m
	 */
	private void transformMethodSignature(SootMethod m) {
		boolean change = false;
		
		// Transform return type
		Type returnType = m.getReturnType();
		Type newReturnType = getAvailableWrapperType(returnType);
		if (!returnType.equals(newReturnType)) {
			change = true;
		} else if (returnType instanceof ArrayType) {
			newReturnType = Constants.CHECKED_ARRAY_CLASS.getType();
			change = true;
		}
		
		// Transform parameters
		List<Type> newParameterTypes = new ArrayList<Type>();
		for (Type t : m.getParameterTypes()) {
			Type availableType = getAvailableWrapperType(t);
			if (!t.equals(availableType)) {
				newParameterTypes.add(availableType);
				change = true;
			} else if (t instanceof ArrayType && !m.isMain()) {
				newParameterTypes.add(Constants.CHECKED_ARRAY_CLASS.getType());
				change = true;
			} else {
				newParameterTypes.add(t);
			}
		}
	
		if (change) {
			SootMethod newMethod = new SootMethod(m.getName(), newParameterTypes, newReturnType, m.getModifiers(), m.getExceptions());
			newMethod.addAllTagsOf(m);
			m.getDeclaringClass().addMethod(newMethod);
			if (!m.isAbstract()) {
				newMethod.setActiveBody(m.retrieveActiveBody());
				setIdentityStmts(newMethod.getActiveBody().getUnits(), newParameterTypes);
			}
			
			changedMethods.put(m, newMethod);
		}
	}
	
	/**
	 * Method to set identity statements of methods, whose parameter types changed
	 * @param units
	 * @param parameterTypes
	 */
	private void setIdentityStmts(Chain<Unit> units, List<Type> parameterTypes) {
		Iterator<Unit> unitIter = units.snapshotIterator();
		int i = 0;
		while (unitIter.hasNext()) {
			Unit u = unitIter.next();
			if (u instanceof IdentityStmt) {
				IdentityStmt idStmt = (IdentityStmt)u;
				Value leftOp = idStmt.getLeftOp();
				Value rightOp = idStmt.getRightOp();
				if (rightOp instanceof ParameterRef) {
					units.insertBefore(Jimple.v().newIdentityStmt(leftOp, Jimple.v().newParameterRef(parameterTypes.get(i), i)), u);
					units.remove(u);
					i++;
				}
			}
		}
	}
	
	/**
	 * Changes all occuring types in a method body for which a wrapper class is available.
	 * Also changes the occurences of arrays to checked arrays
	 * @param b
	 */
	private void transformMethodBody(SootMethod m) {
		Body b = m.retrieveActiveBody();
		
		MethodBodyHandler methodBodyHandler = new MethodBodyHandler(changedMethods, changedFields);
		methodBodyHandler.handle(b);
	}
	
	/**
	 * Method to get an available wrapper type
	 * @param t
	 * @return - the wrapper type if it exists, or the type given as parameter if not.
	 */
	private Type getAvailableWrapperType(Type t) {
		if (ClassMapping.MAP.get(t.toString()) != null)
			return ClassMapping.MAP.get(t.toString()).getType();
		return t;
	}
}

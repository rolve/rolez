package transformer.transformers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
import soot.util.Chain;
import transformer.util.ClassMapping;
import transformer.wrapper.UnitTransformerSwitch;

public class WrapperTypeTransformer extends SceneTransformer {
	
	static final Logger logger = LogManager.getLogger(WrapperTypeTransformer.class);

	Map<SootMethod, SootMethod> changedMethods = new HashMap<SootMethod, SootMethod>();
	Map<SootField, SootField> changedFields = new HashMap<SootField, SootField>();
	
	@Override
	protected void internalTransform(String phaseName, Map options) {
		Chain<SootClass> classesToProcess = Scene.v().getApplicationClasses();

		for (SootClass c : classesToProcess) {			
			// Transform fields
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
			}

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
		
		for (SootField f : changedFields.keySet())
			f.getDeclaringClass().removeField(f);
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
		Type availableReturnType = getAvailableWrapperType(returnType);
		if (!returnType.equals(availableReturnType)) {
			change = true;
		}
		
		// Transform parameters
		List<Type> newParameterTypes = new ArrayList<Type>();
		for (Type t : m.getParameterTypes()) {
			Type availableType = getAvailableWrapperType(t);
			if (!t.equals(availableType)) {
				newParameterTypes.add(availableType);
				change = true;
			} else {
				newParameterTypes.add(t);
			}
		}
	
		if (change) {
			SootMethod newMethod = new SootMethod(m.getName(), newParameterTypes, availableReturnType, m.getModifiers(), m.getExceptions());
			newMethod.addAllTagsOf(m);
			m.getDeclaringClass().addMethod(newMethod);
			if (!m.isAbstract())
				newMethod.setActiveBody(m.retrieveActiveBody());
			changedMethods.put(m, newMethod);
		}
	}
	
	/**
	 * Changes all occuring types in a method body for which a wrapper class is available
	 * @param b
	 */
	private void transformMethodBody(SootMethod m) {
		Body b = m.retrieveActiveBody();
		Chain<Local> locals = b.getLocals();
		Chain<Unit> units = b.getUnits();
		
		for (Local local : locals) {
			Type type = local.getType();
			SootClass availableClass = ClassMapping.MAP.get(type.toString());
			if (availableClass != null) {	
				// Set new type for all units
				for (Unit u : units)
					u.apply(new UnitTransformerSwitch(availableClass, local, changedMethods, changedFields));
				
				// Set the type of the local AFTER the transformation of the units!
				RefType availableType = availableClass.getType();
				local.setType(availableType);
			}
		}
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

package transformer.transformers;

import java.util.ArrayList;
import java.util.HashMap;
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

	Map<String, SootMethod> changedMethodSignatures = new HashMap<String, SootMethod>();
	List<SootMethod> methodsToRemove = new ArrayList<SootMethod>();
	
	@Override
	protected void internalTransform(String phaseName, Map options) {
		Chain<SootClass> classesToProcess = Scene.v().getApplicationClasses();
		
		for (SootClass c : classesToProcess) {			
			// Transform fields
			for (SootField f : c.getFields())
				f.setType(getAvailableWrapperType(f.getType()));

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
		for (SootMethod m : methodsToRemove)
			m.getDeclaringClass().removeMethod(m);
	}
	
	/**
	 * Transforms the signatures of the methods containing a type for which a checked
	 * wrapper is available.
	 * @param m
	 */
	private void transformMethodSignature(SootMethod m) {
		boolean change = false;
		String signature = m.getSubSignature();
		
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
			methodsToRemove.add(m);
			changedMethodSignatures.put(signature, newMethod);
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
					u.apply(new UnitTransformerSwitch(availableClass, local, changedMethodSignatures));
				
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

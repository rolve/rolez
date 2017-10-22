package rolez.checked.transformer.util;

import java.util.HashMap;
import java.util.Map;

import soot.Scene;
import soot.SootClass;

public class ClassMapping {

	public static final Map<String, SootClass> MAP = new HashMap<String, SootClass>();

	public static void initializeMapping() {
		MAP.put("java.util.Random", Scene.v().forceResolve(rolez.checked.lang.Random.class.getCanonicalName(), SootClass.SIGNATURES));
	}
}

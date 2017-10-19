package rolez.checked.transformer.util;

import java.util.HashMap;
import java.util.Map;

import soot.SootClass;

public class ClassMapping {

	public static final Map<String, SootClass> MAP = new HashMap<String, SootClass>();

	static {
		MAP.put("java.util.Random", Constants.RANDOM_CLASS);
	}
}

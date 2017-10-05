package rolez.checked.transformer.util;

import java.util.List;

import rolez.checked.lang.Role;
import soot.SootClass;
import soot.SootMethod;
import soot.tagkit.AnnotationTag;
import soot.tagkit.Tag;
import soot.tagkit.VisibilityAnnotationTag;

public class Util {
	
	/**
	 * A method is a rolez task if it has the @Roleztask annotation or
	 * it overrides a method, which is a rolez task.
	 * @param method
	 * @return
	 */
	public static boolean isRolezTask(SootMethod method) {
		if (hasRoleztaskAnnotation(method))
			return true;
		if (isOverridingARoleztask(method))
			return true;
		return false;
	}
	
	private static boolean hasRoleztaskAnnotation(SootMethod method) {
		for (Tag t : method.getTags()) 
			if (t instanceof VisibilityAnnotationTag) 
				for (AnnotationTag aTag : ((VisibilityAnnotationTag) t).getAnnotations()) 
					if (aTag.getType().equals(Constants.ROLEZTASK_ANNOTATION)) 
						return true;
		return false;
	}
	
	private static boolean isOverridingARoleztask(SootMethod method) {
		
		// Get declaring class and return if it's Object
		SootClass currentClass = method.getDeclaringClass();
		if (currentClass.equals(Constants.OBJECT_CLASS)) return false;
		
		// Try to find a method in the superclasses which is overriden by the parameter method
		String methodSignature = method.getSubSignature();
		while (!currentClass.getSuperclass().equals(Constants.OBJECT_CLASS)) {
			currentClass = currentClass.getSuperclass();
			for (SootMethod m : currentClass.getMethods())
				if (m.getSubSignature().equals(methodSignature))
					if (hasRoleztaskAnnotation(m))
						return true;
		}
		return false;
	}
	
	public static boolean hasCheckedAnnotation(SootClass clazz) {
		List<Tag> classTags = clazz.getTags();
		for (Tag t : classTags) 
			if (t instanceof VisibilityAnnotationTag) 
				for (AnnotationTag aTag : ((VisibilityAnnotationTag) t).getAnnotations()) 
					if (aTag.getType().equals(Constants.CHECKED_ANNOTATION))
						return true;
		return false;
	}
	
	public static Role getThisRole(SootMethod method) {
		for (Tag t : method.getTags()) 
			if (t instanceof VisibilityAnnotationTag) 
				for (AnnotationTag aTag : ((VisibilityAnnotationTag) t).getAnnotations()) {
					if (aTag.getType().equals(Constants.READWRITE_ANNOTATION))
						return Role.READWRITE;
					if (aTag.getType().equals(Constants.READONLY_ANNOTATION)) 
						return Role.READONLY;
				}
		
		// Default is pure
		return Role.PURE;
	}
	
	public static String getTaskMethodNameFromMethod(SootMethod method) {
		String methodName = method.getName();
		return "$" + methodName + "Task";
	}
}

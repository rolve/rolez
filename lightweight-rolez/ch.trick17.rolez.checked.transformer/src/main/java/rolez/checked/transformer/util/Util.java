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
	 * Returns true if the method has the @Roleztask annotation.
	 * @param method
	 * @return
	 */
	public static boolean isRolezTask(SootMethod method) {
		for (Tag t : method.getTags()) 
			if (t instanceof VisibilityAnnotationTag) 
				for (AnnotationTag aTag : ((VisibilityAnnotationTag) t).getAnnotations()) 
					if (aTag.getType().equals(Constants.ROLEZTASK_ANNOTATION)) 
						return true;
		return false;
	}
	
	/**
	 * Returns true if the class has the @Checked annotation, false otherwise.
	 * @param clazz
	 * @return
	 */
	public static boolean isCheckedClass(SootClass clazz) {
		
		if (clazz.equals(Constants.OBJECT_CLASS)) return false;
		
		List<Tag> classTags = clazz.getTags();
		for (Tag t : classTags) 
			if (t instanceof VisibilityAnnotationTag) 
				for (AnnotationTag aTag : ((VisibilityAnnotationTag) t).getAnnotations()) 
					if (aTag.getType().equals(Constants.CHECKED_ANNOTATION))
						return true;
		
		return isCheckedClass(clazz.getSuperclass());
	}
	
	/**
	 * Returns true if the class is extending from java.lang.Object.
	 * @param clazz
	 * @return
	 */
	public static boolean isExtendingObject(SootClass clazz) {
		return clazz.getSuperclass().equals(Constants.OBJECT_CLASS);
	}
	
	/**
	 * Returns the role a task has on "this". Either Role.READWRITE, Role.READONLY or Role.PURE (default).
	 * @param task
	 * @return
	 */
	public static Role getThisRole(SootMethod task) {
		for (Tag t : task.getTags()) 
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
	
	/**
	 * Returns the name for the generated task method.
	 * @param method
	 * @return
	 */
	public static String getTaskMethodNameFromMethod(SootMethod method) {
		String methodName = method.getName();
		return "$" + methodName + "Task";
	}
}

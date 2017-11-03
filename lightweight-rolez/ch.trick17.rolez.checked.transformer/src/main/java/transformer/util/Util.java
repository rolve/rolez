package transformer.util;

import java.util.List;

import rolez.checked.lang.Role;
import soot.Local;
import soot.SootClass;
import soot.SootMethod;
import soot.tagkit.AnnotationTag;
import soot.tagkit.Tag;
import soot.tagkit.VisibilityAnnotationTag;
import soot.util.Chain;

public class Util {

	/**
	 * Returns true if the method has the @Task annotation.
	 * @param m
	 * @return
	 */
	public static boolean isTask(SootMethod m) {
		for (Tag t : m.getTags()) 
			if (t instanceof VisibilityAnnotationTag) 
				for (AnnotationTag aTag : ((VisibilityAnnotationTag) t).getAnnotations()) 
					if (aTag.getType().equals(Constants.TASK_ANNOTATION)) 
						return true;
		return false;
	}
	
	/**
	 * Returns true if the class has the @Checked annotation, false otherwise.
	 * @param c
	 * @return
	 */
	public static boolean isCheckedClass(SootClass c) {
		
		if (c.equals(Constants.OBJECT_CLASS)) return false;
		
		List<Tag> classTags = c.getTags();
		for (Tag t : classTags) 
			if (t instanceof VisibilityAnnotationTag) 
				for (AnnotationTag aTag : ((VisibilityAnnotationTag) t).getAnnotations()) 
					if (aTag.getType().equals(Constants.CHECKED_ANNOTATION))
						return true;
		
		return isCheckedClass(c.getSuperclass());
	}
	
	/**
	 * Returns true if the class is extending from java.lang.Object.
	 * @param c
	 * @return
	 */
	public static boolean isExtendingObject(SootClass c) {
		return c.getSuperclass().equals(Constants.OBJECT_CLASS);
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
	 * @param m
	 * @return
	 */
	public static String getTaskMethodNameFromMethod(SootMethod m) {
		String methodName = m.getName();
		return "$" + methodName + "Task";
	}
	
	public static Local getTaskIdLocal(Chain<Local> locals) {
		Local taskIdLocal = null;
		for (Local l : locals) {
			if (l.getName().equals(Constants.TASK_ID_LOCAL_NAME)) {
				taskIdLocal = l;
				break;
			}
		}
		return taskIdLocal;
	}
}

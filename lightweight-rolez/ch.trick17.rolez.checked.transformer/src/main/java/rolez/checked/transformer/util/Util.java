package rolez.checked.transformer.util;

import java.util.List;

import rolez.checked.lang.Role;
import soot.SootClass;
import soot.SootMethod;
import soot.tagkit.AnnotationTag;
import soot.tagkit.Tag;
import soot.tagkit.VisibilityAnnotationTag;

public class Util {
	
	public static boolean hasRoleztaskAnnotation(SootMethod m) {
		for (Tag t : m.getTags()) 
			if (t instanceof VisibilityAnnotationTag) 
				for (AnnotationTag aTag : ((VisibilityAnnotationTag) t).getAnnotations()) 
					if (aTag.getType().equals(Constants.ROLEZTASK_ANNOTATION)) 
						return true;
		return false;
	}
	
	public static boolean hasCheckedAnnotation(SootClass c) {
		List<Tag> classTags = c.getTags();
		for (Tag t : classTags) 
			if (t instanceof VisibilityAnnotationTag) 
				for (AnnotationTag aTag : ((VisibilityAnnotationTag) t).getAnnotations()) 
					if (aTag.getType().equals(Constants.CHECKED_ANNOTATION))
						return true;
		return false;
	}
	
	public static Role getThisRole(SootMethod m) {
		for (Tag t : m.getTags()) 
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

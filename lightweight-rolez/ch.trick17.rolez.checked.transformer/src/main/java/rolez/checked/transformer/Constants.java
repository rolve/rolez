package rolez.checked.transformer;

import java.util.Arrays;
import java.util.List;

import rolez.checked.lang.Checked;
import rolez.checked.lang.Task;
import rolez.checked.lang.TaskSystem;
import soot.ArrayType;
import soot.RefType;
import soot.SootClass;
import soot.SootResolver;
import soot.Type;

public class Constants {	
	public static final SootClass CHECKED_CLASS = SootResolver.v().resolveClass(Checked.class.getCanonicalName(), SootClass.SIGNATURES);
	public static final SootClass OBJECT_CLASS = SootResolver.v().resolveClass(Object.class.getCanonicalName(), SootClass.SIGNATURES);
	public static final SootClass TASK_SYSTEM_CLASS = SootResolver.v().resolveClass(TaskSystem.class.getCanonicalName(), SootClass.SIGNATURES);
	public static final SootClass STRING_CLASS = SootResolver.v().resolveClass("java.lang.String", SootClass.SIGNATURES);
	public static final SootClass TASK_CLASS = SootResolver.v().resolveClass(Task.class.getCanonicalName(), SootClass.SIGNATURES);
	public static final SootClass LIST_CLASS = SootResolver.v().resolveClass(List.class.getCanonicalName(), SootClass.SIGNATURES);
	public static final SootClass ARRAYS_CLASS = SootResolver.v().resolveClass(Arrays.class.getCanonicalName(), SootClass.SIGNATURES);
	
	public static final ArrayType OBJECT_ARRAY_TYPE = ArrayType.v(RefType.v(OBJECT_CLASS),1);
	public static final Type VOID_TYPE = RefType.v("java.lang.Void");
	
	public static final String ROLEZTASK_ANNOTATION = "Lrolez/annotation/Roleztask;";
	public static final String CHECKED_ANNOTATION = "Lrolez/annotation/Checked;";
	public static final String READONLY_ANNOTATION = "Lrolez/annotation/Readonly;";
	public static final String READWRITE_ANNOTATION = "Lrolez/annotation/Readwrite;";
	public static final String PURE_ANNOTATION = "Lrolez/annotation/Pure;";
}

package transformer.util;

import java.util.Arrays;
import java.util.List;

import rolez.checked.internal.Tasks;
import rolez.checked.lang.Checked;
import rolez.checked.lang.Task;
import rolez.checked.lang.TaskSystem;
import soot.ArrayType;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.Type;

public class Constants {
	// Library classes
	public static SootClass CHECKED_CLASS;
	public static SootClass OBJECT_CLASS;
	public static SootClass TASK_SYSTEM_CLASS;
	public static SootClass STRING_CLASS;
	public static SootClass TASK_CLASS;
	public static SootClass LIST_CLASS;
	public static SootClass ARRAYS_CLASS;
	public static SootClass THROWABLE_CLASS;
	public static SootClass INTERNAL_TASKS_CLASS;
	public static SootClass VOID_CLASS;
	public static SootClass ITERABLE_CLASS;
	
	// Types
	public static final ArrayType OBJECT_ARRAY_TYPE = ArrayType.v(RefType.v("java.lang.Object"),1);
	public static final Type VOID_TYPE = RefType.v("java.lang.Void");
	
	// Annotations
	public static final String TASK_ANNOTATION = "Lrolez/annotation/Task;";
	public static final String CHECKED_ANNOTATION = "Lrolez/annotation/Checked;";
	public static final String READONLY_ANNOTATION = "Lrolez/annotation/Readonly;";
	public static final String READWRITE_ANNOTATION = "Lrolez/annotation/Readwrite;";
	public static final String PURE_ANNOTATION = "Lrolez/annotation/Pure;";
	
	public static void resolveClasses() {
		CHECKED_CLASS = Scene.v().forceResolve(Checked.class.getCanonicalName(), SootClass.SIGNATURES);
		OBJECT_CLASS = Scene.v().forceResolve(Object.class.getCanonicalName(), SootClass.SIGNATURES);
		TASK_SYSTEM_CLASS = Scene.v().forceResolve(TaskSystem.class.getCanonicalName(), SootClass.SIGNATURES);
		STRING_CLASS = Scene.v().forceResolve(java.lang.String.class.getCanonicalName(), SootClass.SIGNATURES);
		TASK_CLASS = Scene.v().forceResolve(Task.class.getCanonicalName(), SootClass.SIGNATURES);
		LIST_CLASS = Scene.v().forceResolve(List.class.getCanonicalName(), SootClass.SIGNATURES);
		ARRAYS_CLASS = Scene.v().forceResolve(Arrays.class.getCanonicalName(), SootClass.SIGNATURES);
		THROWABLE_CLASS = Scene.v().forceResolve(Throwable.class.getCanonicalName(), SootClass.SIGNATURES);
		INTERNAL_TASKS_CLASS = Scene.v().forceResolve(Tasks.class.getCanonicalName(), SootClass.SIGNATURES);
		VOID_CLASS = Scene.v().forceResolve(java.lang.Void.class.getCanonicalName(), SootClass.SIGNATURES);
		ITERABLE_CLASS = Scene.v().forceResolve(java.lang.Iterable.class.getCanonicalName(), SootClass.SIGNATURES);
	}
}

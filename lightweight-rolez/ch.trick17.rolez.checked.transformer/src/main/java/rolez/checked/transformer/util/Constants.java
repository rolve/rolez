package rolez.checked.transformer.util;

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
	public static final SootClass CHECKED_CLASS = Scene.v().forceResolve(Checked.class.getCanonicalName(), SootClass.SIGNATURES);
	public static final SootClass OBJECT_CLASS = Scene.v().forceResolve(Object.class.getCanonicalName(), SootClass.SIGNATURES);
	public static final SootClass TASK_SYSTEM_CLASS = Scene.v().forceResolve(TaskSystem.class.getCanonicalName(), SootClass.SIGNATURES);
	public static final SootClass STRING_CLASS = Scene.v().forceResolve("java.lang.String", SootClass.SIGNATURES);
	public static final SootClass TASK_CLASS = Scene.v().forceResolve(Task.class.getCanonicalName(), SootClass.SIGNATURES);
	public static final SootClass LIST_CLASS = Scene.v().forceResolve(List.class.getCanonicalName(), SootClass.SIGNATURES);
	public static final SootClass ARRAYS_CLASS = Scene.v().forceResolve(Arrays.class.getCanonicalName(), SootClass.SIGNATURES);
	public static final SootClass THROWABLE_CLASS = Scene.v().forceResolve(Throwable.class.getCanonicalName(), SootClass.SIGNATURES);
	public static final SootClass INTERNAL_TASKS_CLASS = Scene.v().forceResolve(Tasks.class.getCanonicalName(), SootClass.SIGNATURES);
	public static final SootClass VOID_CLASS = Scene.v().forceResolve("java.lang.Void", SootClass.SIGNATURES);
	public static final SootClass ITERABLE_CLASS = Scene.v().forceResolve("java.lang.Iterable", SootClass.SIGNATURES);
	
	public static final ArrayType OBJECT_ARRAY_TYPE = ArrayType.v(RefType.v(OBJECT_CLASS),1);
	public static final Type VOID_TYPE = RefType.v("java.lang.Void");
	
	public static final String ROLEZTASK_ANNOTATION = "Lrolez/annotation/Roleztask;";
	public static final String CHECKED_ANNOTATION = "Lrolez/annotation/Checked;";
	public static final String READONLY_ANNOTATION = "Lrolez/annotation/Readonly;";
	public static final String READWRITE_ANNOTATION = "Lrolez/annotation/Readwrite;";
	public static final String PURE_ANNOTATION = "Lrolez/annotation/Pure;";
}

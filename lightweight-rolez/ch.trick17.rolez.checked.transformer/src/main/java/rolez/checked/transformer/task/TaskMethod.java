package rolez.checked.transformer.task;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rolez.checked.lang.Role;
import rolez.checked.lang.Task;
import soot.ArrayType;
import soot.Local;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.tagkit.AnnotationTag;
import soot.tagkit.Tag;
import soot.tagkit.VisibilityAnnotationTag;
import soot.tagkit.VisibilityParameterAnnotationTag;
import soot.util.Chain;

public class TaskMethod extends SootMethod {

	static final Logger logger = LogManager.getLogger(TaskMethod.class);
	
	static final SootClass TASK_CLASS = Scene.v().loadClassAndSupport(Task.class.getCanonicalName());
	static final SootClass OBJECT_CLASS = Scene.v().loadClassAndSupport(Object.class.getCanonicalName());

	static final String READONLY_ANNOTATION = "Lrolez/annotation/Readonly;";
	static final String READWRITE_ANNOTATION = "Lrolez/annotation/Readwrite;";
	static final String PURE_ANNOTATION = "Lrolez/annotation/Pure;";

	private SootClass containingClass;
	private SootClass innerClass;
	private SootMethod sourceMethod;

	public TaskMethod(String name, SootClass containingClass, SootClass innerClass, SootMethod sourceMethod) {
		super(name, sourceMethod.getParameterTypes(), TASK_CLASS.getType(), sourceMethod.getModifiers());
		this.containingClass = containingClass;
		this.innerClass = innerClass;
		this.sourceMethod = sourceMethod;
		
		generateMethodBody();
	}
	
	private void generateMethodBody() {		
		/*
		 public rolez.checked.lang.Task $testTask(rolez.checked.transformer.test.A, rolez.checked.transformer.test.A)
	    {
	        rolez.checked.transformer.test.Test r0;
	        rolez.checked.transformer.test.A r1, r2;
	        rolez.checked.transformer.test.Test$1 $r3;
	        java.lang.Object[] $r4, $r5, $r6;
	
	        r0 := @this: rolez.checked.transformer.test.Test;
	        r1 := @parameter0: rolez.checked.transformer.test.A;
	        r2 := @parameter1: rolez.checked.transformer.test.A;
	        $r3 = new rolez.checked.transformer.test.Test$1;
	        $r4 = newarray (java.lang.Object)[1];
	        $r4[0] = r2;
	        $r5 = newarray (java.lang.Object)[1];
	        $r5[0] = r1;
	        $r6 = newarray (java.lang.Object)[0];
	        specialinvoke $r3.<rolez.checked.transformer.test.Test$1: void <init>(rolez.checked.transformer.test.Test,java.lang.Object[],java.lang.Object[],java.lang.Object[],rolez.checked.transformer.test.A,rolez.checked.transformer.test.A)>(r0, $r4, $r5, $r6, r2, r1);
	        return $r3;
	    }
		 */
		
		// Get parameter roles from the annotations
		List<Role> parameterRoles = getParameterRoles();
		
		JimpleBody body = Jimple.v().newBody(this);
		this.setActiveBody(body);

		// Set up the locals
		Chain<Local> locals = body.getLocals();
		
		int localCount = 0;
		Local thisReferenceLocal = Jimple.v().newLocal("r"+Integer.toString(localCount), containingClass.getType());
		locals.add(thisReferenceLocal);
		localCount++;
		
		List<Local> paramLocals = new ArrayList<Local>();
		List<Type> srcParamTypes = sourceMethod.getParameterTypes();
		for (Type t : srcParamTypes) {
			Local l = Jimple.v().newLocal("r"+Integer.toString(localCount),t);
			paramLocals.add(l);
			locals.add(l);
			localCount++;
		}
		
		Local innerClassReferenceLocal = Jimple.v().newLocal("$r"+Integer.toString(localCount), innerClass.getType());
		locals.add(innerClassReferenceLocal);
		localCount++;
		
		List<Local> objectArrayLocals = new ArrayList<Local>();
		ArrayType objectArrayType = ArrayType.v(RefType.v(OBJECT_CLASS),1);
		for (int i=0; i<3; i++, localCount++) {
			Local l = Jimple.v().newLocal("$r" + Integer.toString(localCount), objectArrayType);
			objectArrayLocals.add(l);
			locals.add(l);
			localCount++;
		}
		
		// Add units
		Chain<Unit> units = body.getUnits();
		
		units.add(Jimple.v().newIdentityStmt(thisReferenceLocal, Jimple.v().newThisRef(containingClass.getType())));
		
		int paramNumber = 0;
		for (Local l : paramLocals) {
			units.add(Jimple.v().newIdentityStmt(l, Jimple.v().newParameterRef(l.getType(), paramNumber)));
			paramNumber++;
		}
		
		units.add(Jimple.v().newAssignStmt(innerClassReferenceLocal, Jimple.v().newNewExpr(innerClass.getType())));
		
		int[] objectArraySizes = getObjectArraySizes(parameterRoles);
		units.add(Jimple.v().newAssignStmt(objectArrayLocals.get(0), Jimple.v().newNewArrayExpr(objectArrayType, IntConstant.v(objectArraySizes[0]))));
		units.add(Jimple.v().newAssignStmt(objectArrayLocals.get(1), Jimple.v().newNewArrayExpr(objectArrayType, IntConstant.v(objectArraySizes[1]))));
		units.add(Jimple.v().newAssignStmt(objectArrayLocals.get(2), Jimple.v().newNewArrayExpr(objectArrayType, IntConstant.v(objectArraySizes[2]))));
		
		// Assign the locals to the object array depending on their role
		int rwIndex = 0, roIndex = 0, puIndex = 0;
		for (int i=0; i<paramLocals.size(); i++) {
			Role r = parameterRoles.get(i);
			if (r != null) {
				switch (r) {
					case READWRITE:
						units.add(Jimple.v().newAssignStmt(Jimple.v().newArrayRef(objectArrayLocals.get(0),  IntConstant.v(rwIndex)), paramLocals.get(i)));
						rwIndex++;
						break;
					case READONLY:
						units.add(Jimple.v().newAssignStmt(Jimple.v().newArrayRef(objectArrayLocals.get(1),  IntConstant.v(roIndex)), paramLocals.get(i)));
						roIndex++;
						break;
					case PURE:
						units.add(Jimple.v().newAssignStmt(Jimple.v().newArrayRef(objectArrayLocals.get(2),  IntConstant.v(puIndex)), paramLocals.get(i)));
						puIndex++;
						break;
					default:
						// Should not happen
						break;
				}
			}
		}
		
		// TODO: How to determine the order of the constructor arguments
//		List<Local> constructorArgs = new ArrayList<Local>();
//		constructorArgs.add(thisReferenceLocal);
//		units.add(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(innerClassReferenceLocal, innerClass.getMethodByName("<init>"), )))
	}

	private List<Role> getParameterRoles() {
		List<Role> parameterRoles = new ArrayList<Role>();
		List<Tag> tags = sourceMethod.getTags();
		for (Tag t : tags) {
			if (t instanceof VisibilityParameterAnnotationTag) {
				VisibilityParameterAnnotationTag vTag = (VisibilityParameterAnnotationTag) t;
				for (VisibilityAnnotationTag vaTag : vTag.getVisibilityAnnotations()) {
					ArrayList<AnnotationTag> annotations = vaTag.getAnnotations();
					if (annotations != null) {
						for (AnnotationTag aTag : vaTag.getAnnotations()) {
							switch (aTag.getType()) {
								case (READONLY_ANNOTATION):
									parameterRoles.add(Role.READONLY);
									break;
								case (READWRITE_ANNOTATION):
									parameterRoles.add(Role.READWRITE);
									break;
								case (PURE_ANNOTATION):
									parameterRoles.add(Role.PURE);
									break;
								default:
									// Should not happen
									break;
							}
						}
					} else {
						parameterRoles.add(null);
					}
				}
			}
		}
		return parameterRoles;
	}

	private int[] getObjectArraySizes(List<Role> parameterRoles) {
		int[] result = new int[3];
		for (Role r : parameterRoles) {
			if (r != null) {
				switch (r) {
					case READWRITE:
						result[0]++;
						break;
					case READONLY:
						result[1]++;
						break;
					case PURE:
						result[2]++;
						break;
					default:
						break;					
				}
			}
		}
		return result;
	}
}

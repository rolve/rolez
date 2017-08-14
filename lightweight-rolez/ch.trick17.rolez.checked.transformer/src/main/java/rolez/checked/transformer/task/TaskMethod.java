package rolez.checked.transformer.task;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rolez.checked.lang.Role;
import rolez.checked.lang.Task;
import soot.ArrayType;
import soot.Local;
import soot.Modifier;
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

	static final Jimple J = Jimple.v();
	
	private SootClass containingClass;
	private SootClass innerClass;
	private SootMethod sourceMethod;
	
	// Array contains the roles of the parameters, null for primitive types
	private Role[] parameterRoles;

	public TaskMethod(String name, SootClass containingClass, SootClass innerClass, SootMethod sourceMethod) {
		super(name, sourceMethod.getParameterTypes(), TASK_CLASS.getType(), sourceMethod.getModifiers());
		this.containingClass = containingClass;
		this.innerClass = innerClass;
		this.sourceMethod = sourceMethod;
		
		parameterRoles = new Role[sourceMethod.getParameterCount()];
		
		// if there are no reference types, we can add null as role for every parameter
		if (hasRefTypeParameters())
			findParameterRoles();
		else
			for (int i=0; i<parameterRoles.length; i++) parameterRoles[i] = null;

		generateMethodBody();
	}
	
	private void generateMethodBody() {		
		
		JimpleBody body = J.newBody(this);
		this.setActiveBody(body);

		// Set up the locals
		Chain<Local> locals = body.getLocals();
		
		int localCount = 0;
		
		Local thisReferenceLocal = J.newLocal("r"+Integer.toString(localCount), containingClass.getType());
		locals.add(thisReferenceLocal);
		localCount++;
		
		List<Local> parameterLocals = new ArrayList<Local>();
		List<Type> sourceParameterTypes = sourceMethod.getParameterTypes();
		for (Type t : sourceParameterTypes) {
			Local l = J.newLocal("r"+Integer.toString(localCount),t);
			parameterLocals.add(l);
			locals.add(l);
			localCount++;
		}
		
		Local innerClassReferenceLocal = J.newLocal("$r"+Integer.toString(localCount), innerClass.getType());
		locals.add(innerClassReferenceLocal);
		localCount++;
		
		List<Local> objectArrayLocals = new ArrayList<Local>();
		ArrayType objectArrayType = ArrayType.v(RefType.v(OBJECT_CLASS),1);
		for (int i=0; i<3; i++, localCount++) {
			Local l = J.newLocal("$r" + Integer.toString(localCount), objectArrayType);
			objectArrayLocals.add(l);
			locals.add(l);
		}
		
		// Add units
		Chain<Unit> units = body.getUnits();
		
		units.add(J.newIdentityStmt(thisReferenceLocal, J.newThisRef(containingClass.getType())));
		
		int paramNumber = 0;
		for (Local l : parameterLocals) {
			units.add(J.newIdentityStmt(l, J.newParameterRef(l.getType(), paramNumber)));
			paramNumber++;
		}
		
		units.add(J.newAssignStmt(innerClassReferenceLocal, J.newNewExpr(innerClass.getType())));
		
		int[] objectArraySizes = getObjectArraySizes();
		units.add(J.newAssignStmt(objectArrayLocals.get(0), J.newNewArrayExpr(objectArrayType, IntConstant.v(objectArraySizes[0]))));
		units.add(J.newAssignStmt(objectArrayLocals.get(1), J.newNewArrayExpr(objectArrayType, IntConstant.v(objectArraySizes[1]))));
		units.add(J.newAssignStmt(objectArrayLocals.get(2), J.newNewArrayExpr(objectArrayType, IntConstant.v(objectArraySizes[2]))));
		
		// Assign the locals to the object array depending on their role
		int rwIndex = 0, roIndex = 0, puIndex = 0;
		for (int i = 0; i < parameterLocals.size(); i++) {
			Role r = parameterRoles[i];
			if (r != null) {
				switch (r) {
					case READWRITE:
						units.add(J.newAssignStmt(J.newArrayRef(objectArrayLocals.get(0),  IntConstant.v(rwIndex)), parameterLocals.get(i)));
						rwIndex++;
						break;
					case READONLY:
						units.add(J.newAssignStmt(J.newArrayRef(objectArrayLocals.get(1),  IntConstant.v(roIndex)), parameterLocals.get(i)));
						roIndex++;
						break;
					case PURE:
						units.add(J.newAssignStmt(J.newArrayRef(objectArrayLocals.get(2),  IntConstant.v(puIndex)), parameterLocals.get(i)));
						puIndex++;
						break;
					default:
						// Should not happen
						break;
				}
			}
		}
		
		// Set up arguments for constructor of inner class
		List<Local> constructorArgs = new ArrayList<Local>();
		constructorArgs.add(thisReferenceLocal);
		for (Local l : objectArrayLocals)
			constructorArgs.add(l);
		for (Local l : parameterLocals)
			constructorArgs.add(l);
		
		// Call constructor of inner class
		units.add(J.newInvokeStmt(J.newSpecialInvokeExpr(innerClassReferenceLocal, innerClass.getMethodByName("<init>").makeRef(), constructorArgs)));
		
		// Return inner class
		units.add(J.newReturnStmt(innerClassReferenceLocal));
	}

	/**
	 * Method does retrieve the roles of the parameters from the given annotations and sets them in the 
	 * <code>parameterRoles</code> array.
	 */
	private void findParameterRoles() {
		List<Tag> tags = sourceMethod.getTags();
		for (Tag t : tags) {
			if (t instanceof VisibilityParameterAnnotationTag) {
				VisibilityParameterAnnotationTag vpaTag = (VisibilityParameterAnnotationTag) t;
				for (VisibilityAnnotationTag vaTag : vpaTag.getVisibilityAnnotations()) {
					ArrayList<AnnotationTag> annotations = vaTag.getAnnotations();
					int arrayIndex = 0;
					// TODO: What happens with other annotations in the code? Have to handle this case eventually...
					if (annotations != null) {
						for (AnnotationTag aTag : vaTag.getAnnotations()) {
							switch (aTag.getType()) {
								case (READONLY_ANNOTATION):
									parameterRoles[arrayIndex] = Role.READONLY;
									break;
								case (READWRITE_ANNOTATION):
									parameterRoles[arrayIndex] = Role.READWRITE;
									break;
								case (PURE_ANNOTATION):
									parameterRoles[arrayIndex] = Role.PURE;
									break;
								default:
									// Should not happen
									break;
							}
							arrayIndex++;
						}
					}
				}
			}
		}
	}

	/**
	 * Method returns a 3 element array with [0] containing the readwrite count,
	 * [1] containing the readonly count and [2] containing the pure count.
	 * @return
	 */
	private int[] getObjectArraySizes() {
		int[] arraySizes = new int[3];
		for (Role r : parameterRoles) {
			if (r != null) {
				switch (r) {
					case READWRITE:
						arraySizes[0]++;
						break;
					case READONLY:
						arraySizes[1]++;
						break;
					case PURE:
						arraySizes[2]++;
						break;
					default:
						// Should not happen
						break;		
				}
			}
		}
		return arraySizes;
	}
	
	/**
	 * Method that checks whether the parameter list of the source method
	 * contains reference types (which have to be checked) or not.
	 * @return
	 */
	private boolean hasRefTypeParameters() {
		List<Type> parameterTypes = sourceMethod.getParameterTypes();
		for (Type t : parameterTypes) {
			if (t instanceof RefType) return true;
			
			// Also return true for array types with ref types as elements
			if (t instanceof ArrayType) {
				ArrayType at = (ArrayType) t;
				if (at.getArrayElementType() instanceof RefType) return true;
			}
		}
		return false;
	}
}

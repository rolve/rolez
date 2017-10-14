package rolez.checked.transformer.task;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rolez.checked.lang.Role;
import rolez.checked.transformer.util.Constants;
import rolez.checked.transformer.util.UnitFactory;
import rolez.checked.transformer.util.Util;
import soot.ArrayType;
import soot.Local;
import soot.Modifier;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.tagkit.AnnotationTag;
import soot.tagkit.Tag;
import soot.tagkit.VisibilityAnnotationTag;
import soot.tagkit.VisibilityParameterAnnotationTag;
import soot.util.Chain;

public class TaskMethod extends SootMethod {

	static final Logger logger = LogManager.getLogger(TaskMethod.class);

	static final Jimple J = Jimple.v();
	
	private SootClass containingClass;
	private SootClass innerClass;
	private SootMethod sourceMethod;
	
	// Array contains the roles of the parameters, null for primitive types
	private Role[] parameterRoles;
	private Role thisRole;

	public TaskMethod(String name, SootClass containingClass, SootClass innerClass, SootMethod sourceMethod) {
		super(name, sourceMethod.getParameterTypes(), Constants.TASK_CLASS.getType(), Modifier.PUBLIC);
		this.containingClass = containingClass;
		this.innerClass = innerClass;
		this.sourceMethod = sourceMethod;
		
		parameterRoles = new Role[sourceMethod.getParameterCount()];
		thisRole = Util.getThisRole(sourceMethod);
		
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
		ArrayType objectArrayType = ArrayType.v(RefType.v(Constants.OBJECT_CLASS),1);
		for (int i=0; i<3; i++, localCount++) {
			Local l = J.newLocal("$r" + Integer.toString(localCount), objectArrayType);
			objectArrayLocals.add(l);
			locals.add(l);
		}
		
		// Add units
		Chain<Unit> units = body.getUnits();
		
		units.add(UnitFactory.newThisRef(thisReferenceLocal, containingClass.getType()));
		
		int paramNumber = 0;
		for (Local l : parameterLocals) {
			units.add(UnitFactory.newParameterRef(l, l.getType(), paramNumber));
			paramNumber++;
		}
				
		units.add(UnitFactory.newAssignNewExpr(innerClassReferenceLocal, innerClass));
		
		int[] objectArraySizes = getObjectArraySizes();
		
		units.add(UnitFactory.newAssignNewArrayExpr(objectArrayLocals.get(0), Constants.OBJECT_CLASS.getType(), objectArraySizes[0]));
		units.add(UnitFactory.newAssignNewArrayExpr(objectArrayLocals.get(1), Constants.OBJECT_CLASS.getType(), objectArraySizes[1]));
		units.add(UnitFactory.newAssignNewArrayExpr(objectArrayLocals.get(2), Constants.OBJECT_CLASS.getType(), objectArraySizes[2]));
		
		// Assign the locals to the object array depending on their role
		int rwIndex = 0, roIndex = 0, puIndex = 0;
		switch (thisRole) {
			case READWRITE:
				units.add(UnitFactory.newAssignToArrayExpr(objectArrayLocals.get(0), rwIndex, thisReferenceLocal));
				rwIndex++;
				break;
			case READONLY:
				units.add(UnitFactory.newAssignToArrayExpr(objectArrayLocals.get(1), roIndex, thisReferenceLocal));
				roIndex++;
				break;
			case PURE:
				units.add(UnitFactory.newAssignToArrayExpr(objectArrayLocals.get(2), puIndex, thisReferenceLocal));
				puIndex++;
				break;
			default:
				// Should not happen
				break;
		}
		for (int i = 0; i < parameterLocals.size(); i++) {
			Role r = parameterRoles[i];
			if (r != null) {
				switch (r) {
					case READWRITE:
						units.add(UnitFactory.newAssignToArrayExpr(objectArrayLocals.get(0), rwIndex, parameterLocals.get(i)));
						rwIndex++;
						break;
					case READONLY:
						units.add(UnitFactory.newAssignToArrayExpr(objectArrayLocals.get(1), roIndex, parameterLocals.get(i)));
						roIndex++;
						break;
					case PURE:
						units.add(UnitFactory.newAssignToArrayExpr(objectArrayLocals.get(2), puIndex, parameterLocals.get(i)));
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
		units.add(UnitFactory.newSpecialInvokeExpr(innerClassReferenceLocal, innerClass, "<init>", constructorArgs));
		
		// Return inner class
		units.add(J.newReturnStmt(innerClassReferenceLocal));
	}

	/**
	 * Method does retrieve the roles of the parameters from the given annotations and sets them in the 
	 * <code>parameterRoles</code> array.
	 */
	private void findParameterRoles() {
		int roleArrayIndex = 0;
		List<Tag> tags = sourceMethod.getTags();
		for (Tag t : tags) {
			if (t instanceof VisibilityParameterAnnotationTag) {
				VisibilityParameterAnnotationTag vpaTag = (VisibilityParameterAnnotationTag) t;
				for (VisibilityAnnotationTag vaTag : vpaTag.getVisibilityAnnotations()) {
					ArrayList<AnnotationTag> annotations = vaTag.getAnnotations();
					if (annotations != null) {
						for (AnnotationTag aTag : vaTag.getAnnotations()) {
							switch (aTag.getType()) {
								case (Constants.READONLY_ANNOTATION):
									parameterRoles[roleArrayIndex] = Role.READONLY;
									break;
								case (Constants.READWRITE_ANNOTATION):
									parameterRoles[roleArrayIndex] = Role.READWRITE;
									break;
								case (Constants.PURE_ANNOTATION):
									parameterRoles[roleArrayIndex] = Role.PURE;
									break;
								default:
									// Should not happen
									break;
							}
						}
					}
					roleArrayIndex++;
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
		
		// Also get space for "this" role
		switch (thisRole) {
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

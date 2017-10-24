package transformer.task;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rolez.checked.lang.Role;
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
import transformer.exceptions.RoleUnknownException;
import transformer.util.Constants;
import transformer.util.UnitFactory;
import transformer.util.Util;

public class TaskMethod extends SootMethod {

	static final Logger logger = LogManager.getLogger(TaskMethod.class);

	static final Jimple J = Jimple.v();
	
	private SootClass containingClass;
	private SootClass innerClass;
	private SootMethod sourceMethod;
	
	// Array contains the role of "this" and the roles of the parameters (null for primitive types)
	private ArrayList<Role> roles;

	public TaskMethod(String name, SootClass containingClass, SootClass innerClass, SootMethod sourceMethod) {
		super(name, sourceMethod.getParameterTypes(), Constants.TASK_CLASS.getType(), Modifier.PUBLIC);
		this.containingClass = containingClass;
		this.innerClass = innerClass;
		this.sourceMethod = sourceMethod;
		
		roles = new ArrayList<Role>();
		roles.add(Util.getThisRole(sourceMethod));
		findParameterRoles();

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
		
		int[] objectArraySizes = getRoleArraySizes();
		
		units.add(UnitFactory.newAssignNewArrayExpr(objectArrayLocals.get(0), Constants.OBJECT_CLASS.getType(), objectArraySizes[0]));
		units.add(UnitFactory.newAssignNewArrayExpr(objectArrayLocals.get(1), Constants.OBJECT_CLASS.getType(), objectArraySizes[1]));
		units.add(UnitFactory.newAssignNewArrayExpr(objectArrayLocals.get(2), Constants.OBJECT_CLASS.getType(), objectArraySizes[2]));
		
		ArrayList<Local> thisAndParamLocals = new ArrayList<Local>();
		thisAndParamLocals.add(thisReferenceLocal);
		thisAndParamLocals.addAll(parameterLocals);
		int rwIndex = 0, roIndex = 0, puIndex = 0;
		for (int i=0; i<roles.size(); i++) {
			Role r = roles.get(i);
			if (r == null)
				continue;
			switch (r) {
				case READWRITE:
					units.add(UnitFactory.newAssignToArrayExpr(objectArrayLocals.get(0), rwIndex, thisAndParamLocals.get(i)));
					rwIndex++;
					break;
				case READONLY:
					units.add(UnitFactory.newAssignToArrayExpr(objectArrayLocals.get(1), roIndex, thisAndParamLocals.get(i)));
					roIndex++;
					break;
				case PURE:
					units.add(UnitFactory.newAssignToArrayExpr(objectArrayLocals.get(2), puIndex, thisAndParamLocals.get(i)));
					puIndex++;
					break;
				default:
					throw new RoleUnknownException("The role found is unknown.");
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
	 * Method does retrieve the roles of the parameters from the given annotations and adds them to the 
	 * <code>roles</code> ArrayList.
	 */
	private void findParameterRoles() {
		List<Tag> tags = sourceMethod.getTags();
		for (Tag t : tags) {
			if (t instanceof VisibilityParameterAnnotationTag) {
				VisibilityParameterAnnotationTag vpaTag = (VisibilityParameterAnnotationTag) t;
				for (VisibilityAnnotationTag vaTag : vpaTag.getVisibilityAnnotations()) {
					ArrayList<AnnotationTag> annotations = vaTag.getAnnotations();
					boolean foundRole = false;
					if (annotations != null) {
						for (AnnotationTag aTag : vaTag.getAnnotations()) {
							switch (aTag.getType()) {
								case (Constants.READONLY_ANNOTATION):
									roles.add(Role.READONLY);
									foundRole = true;
									break;
								case (Constants.READWRITE_ANNOTATION):
									roles.add(Role.READWRITE);
									foundRole = true;
									break;
								case (Constants.PURE_ANNOTATION):
									roles.add(Role.PURE);
									foundRole = true;
									break;
								default:
									break;
							}
						}
					}
					// Add null for not annotated parameters
					if (!foundRole) roles.add(null);
				}
			}
		}
	}

	/**
	 * Method returns a 3 element array with [0] containing the readwrite count,
	 * [1] containing the readonly count and [2] containing the pure count.
	 * @return
	 */
	private int[] getRoleArraySizes() {
		int[] arraySizes = new int[3];
		for (Role r : roles)
			if (r != null)
				arraySizes = increaseRoleArraySize(arraySizes, r);
		
		return arraySizes;
	}
	
	private int[] increaseRoleArraySize(int[] arraySizes, Role r) {
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
				throw new RoleUnknownException("The role found is unknown.");
		}
		return arraySizes;
	}
}

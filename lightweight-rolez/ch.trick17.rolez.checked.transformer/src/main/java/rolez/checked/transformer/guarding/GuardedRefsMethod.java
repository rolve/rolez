package rolez.checked.transformer.guarding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rolez.checked.lang.Checked;
import soot.ArrayType;
import soot.Body;
import soot.Local;
import soot.Modifier;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.util.Chain;

public class GuardedRefsMethod extends SootMethod {

	static final Logger logger = LogManager.getLogger(GuardedRefsMethod.class);
	
	static final SootClass LIST_CLASS = Scene.v().loadClassAndSupport(List.class.getCanonicalName());
	static final SootClass ARRAYS_CLASS = Scene.v().loadClassAndSupport(Arrays.class.getCanonicalName());
	static final SootClass CHECKED_CLASS = Scene.v().loadClassAndSupport(Checked.class.getCanonicalName());

	static final Jimple J = Jimple.v();
	
	private SootClass containingClass;
	
	public GuardedRefsMethod(SootClass containingClass) {
		super("guardedRefs", new ArrayList<Type>(), RefType.v(LIST_CLASS), Modifier.PROTECTED);
		this.containingClass = containingClass;
		
		generateMethodBody();
	}
	
	private void generateMethodBody() {
		
		Body body = J.newBody(this);
		this.setActiveBody(body);
		
		// Set up the locals
		Chain<Local> locals = body.getLocals();
		int localsCount = 0;
		
		Local thisReferenceLocal = J.newLocal("r" + Integer.toString(localsCount), containingClass.getType());
		locals.add(thisReferenceLocal);
		localsCount++;

		List<SootField> checkedFields = getCheckedFields();
		
		Local checkedRefArrayLocal = J.newLocal("$r" + Integer.toString(localsCount), ArrayType.v(RefType.v(CHECKED_CLASS), 1));
		locals.add(checkedRefArrayLocal);
		localsCount++;
		
		List<Local> checkedFieldsLocals = new ArrayList<Local>();
		for (SootField f : checkedFields) {
			Local l = J.newLocal("$r" + Integer.toString(localsCount), f.getType());
			checkedFieldsLocals.add(l);
			locals.add(l);
			localsCount++;
		}
		
		Local resultListLocal = J.newLocal("$r" + Integer.toString(localsCount), RefType.v(LIST_CLASS));
		locals.add(resultListLocal);
		
		// Add units
		Chain<Unit> units = body.getUnits();
		
		units.add(J.newIdentityStmt(thisReferenceLocal, J.newThisRef(containingClass.getType())));
		units.add(J.newAssignStmt(checkedRefArrayLocal, J.newNewArrayExpr(checkedRefArrayLocal.getType(), IntConstant.v(checkedFieldsLocals.size()))));
		
		for (int i = 0; i < checkedFields.size(); i++) {
			units.add(J.newAssignStmt(checkedFieldsLocals.get(i), J.newInstanceFieldRef(thisReferenceLocal, checkedFields.get(i).makeRef())));
			units.add(J.newAssignStmt(J.newArrayRef(checkedRefArrayLocal, IntConstant.v(i)), checkedFieldsLocals.get(i)));
		}
		
		SootMethod asListMethod = ARRAYS_CLASS.getMethodByName("asList");
		ArrayList<Local> args = new ArrayList<Local>();
		args.add(checkedRefArrayLocal);
		units.add(J.newInvokeStmt(J.newStaticInvokeExpr(asListMethod.makeRef(), args)));
		
		units.add(J.newReturnStmt(checkedRefArrayLocal));
	}
	
	private List<SootField> getCheckedFields() {
		Chain<SootField> fields = containingClass.getFields();
		List<SootField> checkedFields = new ArrayList<SootField>();
		for (SootField f : fields) {
			if (isCheckedType(f.getType())) {
				checkedFields.add(f);
			}
		}
		return checkedFields;
	}
	
	private boolean isCheckedType(Type t) {
		// TODO: Implement this method
		return true;
	}
}

package transformer.wrapper;

import java.util.Map;

import soot.Local;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.jimple.AbstractJimpleValueSwitch;
import soot.jimple.CastExpr;
import soot.jimple.DynamicInvokeExpr;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InstanceOfExpr;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.VirtualInvokeExpr;

public class ExpressionTransformerSwitch extends AbstractJimpleValueSwitch {

	SootClass availableClass;
	Local local;
	Map<SootMethod,SootMethod> changedMethods;
	Map<SootField,SootField> changedFields;
	
	public ExpressionTransformerSwitch(SootClass availableClass, 
			Local local, 
			Map<SootMethod,SootMethod> changedMethods,
			Map<SootField,SootField> changedFields) {
		this.availableClass = availableClass;
		this.local = local;
		this.changedMethods = changedMethods;
		this.changedFields = changedFields;
	}

	@Override
	public void caseDynamicInvokeExpr(DynamicInvokeExpr v) {
		defaultCase(v);
	}

    public void caseInterfaceInvokeExpr(InterfaceInvokeExpr v) {
        setRefToWrapperMethod(v);
		setRefToChangedMethod(v);
    }

    public void caseSpecialInvokeExpr(SpecialInvokeExpr v) {
        setRefToWrapperMethod(v);
		setRefToChangedMethod(v);
    }

    public void caseStaticInvokeExpr(StaticInvokeExpr v) {
		setRefToChangedMethod(v);
    }

    public void caseVirtualInvokeExpr(VirtualInvokeExpr v) {
        setRefToWrapperMethod(v);
		setRefToChangedMethod(v);
    }

    public void caseCastExpr(CastExpr v) {
    	if (v.getType().equals(local.getType()))
    		v.setCastType(availableClass.getType());
    }

    public void caseInstanceOfExpr(InstanceOfExpr v) {
        if (v.getCheckType().equals(local.getType())) {
        	v.setCheckType(availableClass.getType());
        }
    }

    public void caseNewArrayExpr(NewArrayExpr v) {
        defaultCase(v);
    }

    public void caseNewMultiArrayExpr(NewMultiArrayExpr v) {
        defaultCase(v);
    }

    public void caseNewExpr(NewExpr v) {
    	if (v.getType().equals(local.getType()))
    		v.setBaseType(availableClass.getType());
    }

    @Override
	public void caseInstanceFieldRef(InstanceFieldRef v) {
    	SootField field = v.getField();
		if (this.changedFields.containsKey(field)) {
			v.setFieldRef(this.changedFields.get(field).makeRef());
		}
	}

	/**
     * Sets method reference to the methods of the wrapper class. E.g. the method nextInt()
     * from java.util.Random gets mapped to the method nextInt() from rolez.checked.util.Random.
     * @param v
     */
	private void setRefToWrapperMethod(InstanceInvokeExpr v) {
		if (v.getBase().equals(local)) {
        	v.setMethodRef(availableClass.getMethod(v.getMethod().getSubSignature()).makeRef());
        }
	}

	/**
	 * Sets method reference to method, whose signatures were changed due to the wrapper class
	 * E.g. if the code contains a method<br>
	 * <pre>
	 * {@code
	 * java.util.Random getNextRandomInt(java.util.Random rand) {
	 *   rand.nextInt();
	 * }
	 * </pre>
	 * Then the transformer changed the signature to 
	 * <pre>
	 * {@code
	 * rolez.checked.util.Random getNextRandomInt(rolez.checked.util.Random rand) {
	 *   rand.nextInt();
	 * }
	 * </pre>
	 * 
	 * This method transforms calls to such changed methods, to conform with the new signature.
	 * 
	 * @param v
	 */
	private void setRefToChangedMethod(InvokeExpr v) {
		SootMethod method = v.getMethod();
        if (this.changedMethods.containsKey(method)) {
    		v.setMethodRef(this.changedMethods.get(method).makeRef());
        }
	}
}

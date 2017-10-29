package transformer.type;

import java.util.Map;

import soot.Scene;
import soot.SootField;
import soot.SootMethod;
import soot.jimple.AbstractJimpleValueSwitch;
import soot.jimple.CastExpr;
import soot.jimple.DynamicInvokeExpr;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceOfExpr;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.VirtualInvokeExpr;
import transformer.util.ClassMapping;

public class ExpressionTransformerSwitch extends AbstractJimpleValueSwitch {

	Map<SootMethod,SootMethod> changedMethods;
	Map<SootField,SootField> changedFields;
	
	public ExpressionTransformerSwitch(
			Map<SootMethod,SootMethod> changedMethods,
			Map<SootField,SootField> changedFields) {
		this.changedMethods = changedMethods;
		this.changedFields = changedFields;
	}
	
	@Override
	public void caseNewArrayExpr(NewArrayExpr v) {
		String baseType = v.getBaseType().toString();
		if (ClassMapping.MAP.containsKey(baseType)){
    		v.setBaseType(ClassMapping.MAP.get(baseType).getType());
    	}
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
    	setRefToWrapperMethod(v);
		setRefToChangedMethod(v);
    }

    public void caseVirtualInvokeExpr(VirtualInvokeExpr v) {
        setRefToWrapperMethod(v);
		setRefToChangedMethod(v);
    }

    public void caseCastExpr(CastExpr v) {
    	String castType = v.getCastType().toString();
    	if (ClassMapping.MAP.containsKey(castType)){
    		v.setCastType(ClassMapping.MAP.get(castType).getType());
    	}
    }

    public void caseInstanceOfExpr(InstanceOfExpr v) {
    	String checkType = v.getCheckType().toString();
    	if (ClassMapping.MAP.containsKey(checkType)){
    		v.setCheckType(ClassMapping.MAP.get(checkType).getType());
    	}
    }

    public void caseNewExpr(NewExpr v) {
    	String baseType = v.getBaseType().toString();
    	if (ClassMapping.MAP.containsKey(baseType)){
    		v.setBaseType(ClassMapping.MAP.get(baseType).getType());
    	}
    }

    @Override
	public void caseInstanceFieldRef(InstanceFieldRef v) {
    	SootField field = v.getField();
		if (this.changedFields.containsKey(field))
			v.setFieldRef(this.changedFields.get(field).makeRef());
	}

	/**
     * Sets method reference to the methods of the wrapper class. E.g. the method nextInt()
     * from java.util.Random gets mapped to the method nextInt() from rolez.checked.util.Random.
     * @param v
     */
	private void setRefToWrapperMethod(InvokeExpr v) {
		String declaringClass = v.getMethod().getDeclaringClass().getName();
		if (ClassMapping.MAP.containsKey(declaringClass))
			v.setMethodRef(ClassMapping.MAP.get(declaringClass).getMethod(v.getMethod().getSubSignature()).makeRef());
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
        if (this.changedMethods.containsKey(method))
    		v.setMethodRef(this.changedMethods.get(method).makeRef());
	}
}

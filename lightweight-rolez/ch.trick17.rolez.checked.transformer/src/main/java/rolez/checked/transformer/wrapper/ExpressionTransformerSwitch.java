package rolez.checked.transformer.wrapper;

import java.util.Map;

import soot.Local;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.AbstractJimpleValueSwitch;
import soot.jimple.CastExpr;
import soot.jimple.DynamicInvokeExpr;
import soot.jimple.InstanceOfExpr;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.VirtualInvokeExpr;

public class ExpressionTransformerSwitch extends AbstractJimpleValueSwitch {

	SootClass availableClass;
	Local local;
	Map<String,SootMethod> changedMethodSignatures;
	
	public ExpressionTransformerSwitch(SootClass availableClass, Local local, Map<String,SootMethod> changedMethodSignatures) {
		this.availableClass = availableClass;
		this.local = local;
		this.changedMethodSignatures = changedMethodSignatures;
	}

	@Override
	public void caseDynamicInvokeExpr(DynamicInvokeExpr v)
	{
		defaultCase(v);
	}

    public void caseInterfaceInvokeExpr(InterfaceInvokeExpr v)
    {
		if (v.getBase().equals(local)) {
			v.setMethodRef(availableClass.getMethod(v.getMethod().getSubSignature()).makeRef());
		}
		
		String methodSignature = v.getMethod().getSubSignature();
        if (this.changedMethodSignatures.containsKey(methodSignature)) {
    		v.setMethodRef(this.changedMethodSignatures.get(methodSignature).makeRef());
        }
    }

    public void caseSpecialInvokeExpr(SpecialInvokeExpr v)
    {    	
    	if (v.getBase().equals(local)) {
    		v.setMethodRef(availableClass.getMethod(v.getMethod().getSubSignature()).makeRef());
        }
    	
    	String methodSignature = v.getMethod().getSubSignature();
        if (this.changedMethodSignatures.containsKey(methodSignature)) {
    		v.setMethodRef(this.changedMethodSignatures.get(methodSignature).makeRef());
        }
    }

    public void caseStaticInvokeExpr(StaticInvokeExpr v)
    {
    	defaultCase(v);
    }

    public void caseVirtualInvokeExpr(VirtualInvokeExpr v)
    {        
        if (v.getBase().equals(local)) {
        	v.setMethodRef(availableClass.getMethod(v.getMethod().getSubSignature()).makeRef());
        }
        
        String methodSignature = v.getMethod().getSubSignature();
        if (this.changedMethodSignatures.containsKey(methodSignature)) {
    		v.setMethodRef(this.changedMethodSignatures.get(methodSignature).makeRef());
        }
    }

    public void caseCastExpr(CastExpr v)
    {
    	if (v.getType().equals(local.getType()))
    		v.setCastType(availableClass.getType());
    }

    public void caseInstanceOfExpr(InstanceOfExpr v)
    {
        defaultCase(v);
    }

    public void caseNewArrayExpr(NewArrayExpr v)
    {
        defaultCase(v);
    }

    public void caseNewMultiArrayExpr(NewMultiArrayExpr v)
    {
        defaultCase(v);
    }

    public void caseNewExpr(NewExpr v)
    {
    	if (v.getType().equals(local.getType()))
    		v.setBaseType(availableClass.getType());
    }
}

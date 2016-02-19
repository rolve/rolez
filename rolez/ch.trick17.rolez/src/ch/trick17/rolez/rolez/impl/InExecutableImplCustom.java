package ch.trick17.rolez.rolez.impl;

import org.eclipse.emf.ecore.EObject;

import ch.trick17.rolez.rolez.Executable;
import ch.trick17.rolez.rolez.InExecutable;
import ch.trick17.rolez.rolez.Method;

public class InExecutableImplCustom extends InExecutableImpl {
    
    @Override
    public Executable enclosingExecutable() {
        return enclosingExecutable(this);
    }
    
    /**
     * Static version of {@link #enclosingExecutable()}, to be reused by
     * <code>ParameterizedParam</code>.
     */
    public static Executable enclosingExecutable(InExecutable it) {
        EObject container = it.eContainer();
        if(container instanceof Executable)
            return (Executable) container;
        else if(container instanceof InExecutable)
            return ((InExecutable) container).enclosingExecutable();
        else
            return null;
    }
    
    @Override
    public Method enclosingMethod() {
        return enclosingMethod(this);
    }
    
    /**
     * Static version of {@link #enclosingMethod()}, to be reused by <code>ParameterizedParam</code>
     * .
     */
    public static Method enclosingMethod(InExecutable it) {
        EObject container = it.eContainer();
        if(container instanceof Method)
            return (Method) container;
        else if(container instanceof InExecutable)
            return ((InExecutable) container).enclosingMethod();
        else
            return null;
    }
}

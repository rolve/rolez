package ch.trick17.rolez.rolez.impl;

import static org.eclipse.xtext.xbase.lib.IterableExtensions.join;
import static org.eclipse.xtext.xbase.lib.IterableExtensions.map;

import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.xbase.lib.Functions.Function1;

import ch.trick17.rolez.RolezResource;
import ch.trick17.rolez.rolez.Block;
import ch.trick17.rolez.rolez.Instr;
import ch.trick17.rolez.rolez.Type;
import ch.trick17.rolez.rolez.Typed;

public class MethodImplCustom extends MethodImpl {
    
    @Override
    public boolean isMapped() {
        return getJvmMethod() != null;
    }
    
    @Override
    public boolean isAsync() {
        return isDeclaredAsync() || isOverriding() && getSuperMethod().isAsync();
    }
    
    @Override
    public boolean isTask() {
        return isDeclaredTask() || isOverriding() && getSuperMethod().isTask();
    }
    
    @Override
    public boolean isMain() {
        return getName().equals("main");
    }
    
    @Override
    public boolean isOverriding() {
        return getSuperMethod() != null && !getSuperMethod().eIsProxy();
        // If super method could not be resolved, don't do any overriding checks
    }
    
    @Override
    public QualifiedName getQualifiedName() {
        return ((RolezResource) eResource()).qualifiedNameProvider().getFullyQualifiedName(this);
    }
    
    @Override
    public void setCode(Instr code) {
        if(!(code instanceof Block))
            throw new IllegalArgumentException("method code needs to be a block");
        super.setCode(code);
    }
    
    @Override
    public Block getBody() {
        return (Block) getCode();
    }
    
    @Override
    public String toString() {
        return thisRole + " " + getQualifiedName()
                + join(getRoleParams(), "[", ",", "]", toStr())
                + join(map(getParams(), toType()), "(", ",", ")", toStr())
                + ": " + type;
    }
    
    private static <E> Function1<E, String> toStr() {
        return new Function1<E, String>() {
            public String apply(E e) {
                return e.toString();
            }
        };
    }
    
    private static Function1<Typed, Type> toType() {
        return new Function1<Typed, Type>() {
            public Type apply(Typed t) {
                return t.getType();
            }
        };
    }
}

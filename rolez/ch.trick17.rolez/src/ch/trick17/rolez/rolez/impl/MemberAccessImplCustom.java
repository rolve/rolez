package ch.trick17.rolez.rolez.impl;

import ch.trick17.rolez.rolez.Field;
import ch.trick17.rolez.rolez.Method;

public class MemberAccessImplCustom extends MemberAccessImpl {
    @Override
    public boolean isFieldAccess() {
        return getMember() instanceof Field;
    }
    
    @Override
    public boolean isMethodInvoke() {
        return getMember() instanceof Method;
    }
    
    @Override
    public Field getField() {
        return (Field) getMember();
    }
    
    @Override
    public Method getMethod() {
        return (Method) getMember();
    }
}

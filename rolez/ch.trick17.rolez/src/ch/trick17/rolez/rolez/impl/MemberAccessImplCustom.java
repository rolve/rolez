package ch.trick17.rolez.rolez.impl;

import static com.google.common.collect.Iterables.concat;
import static java.util.Arrays.asList;

import ch.trick17.rolez.rolez.Executable;
import ch.trick17.rolez.rolez.Expr;
import ch.trick17.rolez.rolez.Field;
import ch.trick17.rolez.rolez.Method;

public class MemberAccessImplCustom extends MemberAccessImpl {
    @Override
    public boolean isFieldAccess() {
        return getMember() instanceof Field;
    }
    
    @Override
    public boolean isMethodInvoke() {
        return getMember() instanceof Method && !isTaskStart();
    }
    
    @Override
    public Field getField() {
        return (Field) getMember();
    }
    
    @Override
    public Method getMethod() {
        return (Method) getMember();
    }
    
    @Override
    public Executable getExecutable() {
        return getMethod();
    }
    
    @Override
    public Iterable<Expr> getAllArgs() {
        return concat(asList(target), getArgs());
    }
}

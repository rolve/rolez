package ch.trick17.rolez.rolez.impl;

public class MethodImplCustom extends MethodImpl {
    @Override
    public boolean isMapped() {
        return getJvmMethod() != null;
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
}

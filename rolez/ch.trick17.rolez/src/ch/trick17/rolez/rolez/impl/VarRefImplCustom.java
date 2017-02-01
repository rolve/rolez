package ch.trick17.rolez.rolez.impl;

public class VarRefImplCustom extends VarRefImpl {
    
    @Override
    public String toString() {
        if(getVariable() != null && getVariable().getName() != null)
            return getVariable().getName();
        else
            return super.toString();
    }
}

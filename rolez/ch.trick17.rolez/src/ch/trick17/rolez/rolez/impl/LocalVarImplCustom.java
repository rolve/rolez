package ch.trick17.rolez.rolez.impl;

public class LocalVarImplCustom extends LocalVarImpl {
    
    @Override
    public String toString() {
        if(getName() != null)
            return getName();
        else
            return super.toString();
    }
}

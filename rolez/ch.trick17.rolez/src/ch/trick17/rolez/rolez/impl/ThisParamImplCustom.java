package ch.trick17.rolez.rolez.impl;

import ch.trick17.rolez.rolez.RoleType;
import ch.trick17.rolez.rolez.Type;

public class ThisParamImplCustom extends ThisParamImpl {
    
    @Override
    public RoleType getType() {
        return (RoleType) type;
    }
    
    @Override
    public void setType(Type type) {
        if(!(type instanceof RoleType))
            throw new IllegalArgumentException();
        super.setType(type);
    }
}

package ch.trick17.rolez.rolez.impl;

public class PrimitiveTypeImplCustom extends PrimitiveTypeImpl {
    
    @Override
    public String getName() {
        return eClass().getName().toLowerCase();
    }
    
    @Override
    public String toString() {
        return getName();
    }
}

package ch.trick17.rolez.rolez.impl;

public class BuiltInRoleImplCustom extends BuiltInRoleImpl {
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        return true;
    }
}

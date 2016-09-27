package ch.trick17.rolez.rolez.impl;

public class TypeParamRefImplCustom extends TypeParamRefImpl {
    @Override
    public String toString() {
        return getParam().getName() +
                (getRestrictingRole() != null ? " with " + getRestrictingRole() : "");
    }
}

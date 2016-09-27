package ch.trick17.rolez.rolez.impl;

public class RoleParamImplCustom extends RoleParamImpl {
    @Override
    public String toString() {
        return name + " includes " + upperBound;
    }
}

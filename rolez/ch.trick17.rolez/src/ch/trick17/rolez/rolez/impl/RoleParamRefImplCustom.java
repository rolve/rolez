package ch.trick17.rolez.rolez.impl;

public class RoleParamRefImplCustom extends RoleParamRefImpl {
    @Override
    public String getName() {
        return getParam().getName();
    }
}

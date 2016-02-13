package ch.trick17.rolez.rolez.impl;

public class MethodImplCustom extends MethodImpl {
    @Override
    public boolean isMapped() {
        return getJvmMethod() != null;
    }
}

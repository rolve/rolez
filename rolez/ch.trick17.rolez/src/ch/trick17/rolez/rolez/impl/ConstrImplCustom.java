package ch.trick17.rolez.rolez.impl;

public class ConstrImplCustom extends ConstrImpl {
    @Override
    public boolean isMapped() {
        return getJvmConstr() != null;
    }
}

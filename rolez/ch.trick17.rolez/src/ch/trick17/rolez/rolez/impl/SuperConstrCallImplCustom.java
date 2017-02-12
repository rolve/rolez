package ch.trick17.rolez.rolez.impl;

import ch.trick17.rolez.rolez.Executable;

public class SuperConstrCallImplCustom extends SuperConstrCallImpl {
    @Override
    public Executable getExecutable() {
        return getConstr();
    }
}

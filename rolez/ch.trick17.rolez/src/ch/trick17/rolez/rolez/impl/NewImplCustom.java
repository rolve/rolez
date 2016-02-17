package ch.trick17.rolez.rolez.impl;

import ch.trick17.rolez.rolez.Executable;

public class NewImplCustom extends NewImpl {
    @Override
    public Executable getExecutable() {
        return getConstr();
    }
}

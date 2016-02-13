package ch.trick17.rolez.rolez.impl;

import ch.trick17.rolez.rolez.VarKind;

public class ParamImplCustom extends ParamImpl {
    @Override
    public VarKind getKind() {
        return VarKind.VAL;
    }
}

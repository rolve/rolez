package ch.trick17.rolez.rolez.impl;

public class FieldImplCustom extends FieldImpl {
    @Override
    public boolean isMapped() {
        return getJvmField() != null;
    }
}

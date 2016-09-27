package ch.trick17.rolez.rolez.impl;

public class SimpleClassRefImplCustom extends SimpleClassRefImpl {
    @Override
    public String toString() {
        return getClazz().getQualifiedName().toString();
    }
}

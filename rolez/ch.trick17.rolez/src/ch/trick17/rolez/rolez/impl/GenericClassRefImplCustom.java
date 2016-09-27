package ch.trick17.rolez.rolez.impl;

public class GenericClassRefImplCustom extends GenericClassRefImpl {
    @Override
    public String toString() {
        return getClazz().getQualifiedName() + "[" + typeArg + "]";
    }
}

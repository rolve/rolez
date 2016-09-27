package ch.trick17.rolez.rolez.impl;

public class NormalClassImplCustom extends NormalClassImpl {
    @Override
    public boolean isSingleton() {
        return false;
    }
    
    @Override
    public String toString() {
        return getQualifiedName().toString() +
                (getTypeParam() != null ? "[" + typeParam + "]" : "");
    }
}

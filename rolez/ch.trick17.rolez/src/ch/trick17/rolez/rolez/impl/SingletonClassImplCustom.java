package ch.trick17.rolez.rolez.impl;

public class SingletonClassImplCustom extends SingletonClassImpl {
    @Override
    public boolean isSingleton() {
        return true;
    }
    
    @Override
    public boolean isPure() {
        return true;
    }
}

package ch.trick17.rolez.rolez.impl;

import ch.trick17.rolez.rolez.ClassRef;
import ch.trick17.rolez.rolez.NormalClass;

public class ClassImplCustom extends ClassImpl {
    @Override
    public NormalClass getSuperclass() {
        ClassRef ref = getSuperclassRef();
        if(ref != null && ref.getClazz() instanceof NormalClass)
            return (NormalClass) ref.getClazz();
        else
            return null;
    }
}

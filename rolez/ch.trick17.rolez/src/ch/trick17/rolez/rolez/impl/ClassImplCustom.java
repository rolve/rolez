package ch.trick17.rolez.rolez.impl;

import static org.eclipse.xtext.xbase.lib.IterableExtensions.filter;

import ch.trick17.rolez.rolez.ClassRef;
import ch.trick17.rolez.rolez.Field;
import ch.trick17.rolez.rolez.Method;
import ch.trick17.rolez.rolez.NormalClass;

public class ClassImplCustom extends ClassImpl {
    @Override
    public boolean isMapped() {
        return getJvmClass() != null;
    }
    
    @Override
    public NormalClass getSuperclass() {
        ClassRef ref = getSuperclassRef();
        if(ref != null && ref.getClazz() instanceof NormalClass)
            return (NormalClass) ref.getClazz();
        else
            return null;
    }
    
    @Override
    public Iterable<Field> getFields() {
        return filter(getMembers(), Field.class);
    }
    
    @Override
    public Iterable<Method> getMethods() {
        return filter(getMembers(), Method.class);
    }
}

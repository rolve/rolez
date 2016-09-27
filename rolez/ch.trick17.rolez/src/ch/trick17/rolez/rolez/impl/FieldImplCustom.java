package ch.trick17.rolez.rolez.impl;

import org.eclipse.xtext.naming.QualifiedName;

import ch.trick17.rolez.RolezResource;

public class FieldImplCustom extends FieldImpl {
    @Override
    public boolean isMapped() {
        return getJvmField() != null;
    }
    
    @Override
    public QualifiedName getQualifiedName() {
        return ((RolezResource) eResource()).qualifiedNameProvider().getFullyQualifiedName(this);
    }
    
    @Override
    public String toString() {
        return getQualifiedName() + ": " + type;
    }
}

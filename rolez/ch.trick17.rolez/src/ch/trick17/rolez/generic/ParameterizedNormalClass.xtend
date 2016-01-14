package ch.trick17.rolez.generic

import ch.trick17.rolez.rolez.ClassRef
import ch.trick17.rolez.rolez.NormalClass
import ch.trick17.rolez.rolez.Type
import ch.trick17.rolez.rolez.TypeParam
import java.util.Map
import org.eclipse.xtext.common.types.JvmGenericType
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EStructuralFeature

import static ch.trick17.rolez.rolez.RolezPackage.Literals.*

package class ParameterizedNormalClass extends ParameterizedEObject<NormalClass> implements NormalClass {
    
    new(NormalClass eObject, EObject eContainer, Map<TypeParam, Type> typeArgs) {
        super(eObject, eContainer, typeArgs)
    }
    
    override getName()          { eObject.name }
    override getTypeParam()     { /* TODO: Is this correct? */ eObject.typeParam }
    override isMapped()         { eObject.isMapped }
    override getJvmClass()      { eObject.jvmClass }
    override getSuperclassRef() { /* TODO: Is this correct? */  eObject.superclassRef.parameterized }
    override getConstrs()       { new ParameterizedConstrList(eObject.constrs, this, typeArgs) }
    override getMembers()       { new ParameterizedMemberList(eObject.members, this, typeArgs) }
    
    override eGet(EStructuralFeature feature) {
        if(feature === CLASS__SUPERCLASS_REF) superclassRef
        else if(feature === NORMAL_CLASS__CONSTRS) constrs
        else if(feature === CLASS__MEMBERS) members
        else eObject.eGet(feature)
    }
    
    override setName(String             value) { throw new AssertionError }
    override setTypeParam(TypeParam     value) { throw new AssertionError }
    override setMapped(boolean          value) { throw new AssertionError }
    override setJvmClass(JvmGenericType value) { throw new AssertionError }
    override setSuperclassRef(ClassRef  value) { throw new AssertionError }
}
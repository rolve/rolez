package ch.trick17.rolez.generic

import ch.trick17.rolez.rolez.ClassRef
import ch.trick17.rolez.rolez.NormalClass
import ch.trick17.rolez.rolez.Role
import ch.trick17.rolez.rolez.RoleParam
import ch.trick17.rolez.rolez.Type
import ch.trick17.rolez.rolez.TypeParam
import java.util.Map
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EStructuralFeature
import org.eclipse.xtext.common.types.JvmGenericType

import static ch.trick17.rolez.rolez.RolezPackage.Literals.*

class ParameterizedNormalClass extends ParameterizedEObject<NormalClass> implements NormalClass {
    
    new(NormalClass eObject, EObject eContainer, Map<TypeParam, Type> typeArgs, Map<RoleParam, Role> roleArgs) {
        super(eObject, eContainer, typeArgs, roleArgs)
    }
    
    override isPure()           { eObject.pure }
    override getName()          { eObject.name }
    override getTypeParam()     { eObject.typeParam }
    override getJvmClass()      { eObject.jvmClass }
    override getSuperclassRef() { eObject.superclassRef.parameterized }
    override getConstrs()       { new ParameterizedConstrList(eObject.constrs, this, typeArgs, roleArgs) }
    override getMembers()       { new ParameterizedMemberList(eObject.members, this, typeArgs, roleArgs) }
    
    override isSingleton()   { eObject.isSingleton }
    override isMapped()      { eObject.isMapped }
    override getSuperclass() { eObject.superclass }
    override getFields()     { eObject.fields }
    override getMethods()    { eObject.methods }

    override eGet(EStructuralFeature feature) {
        if(feature === CLASS__SUPERCLASS_REF) superclassRef
        else if(feature === NORMAL_CLASS__CONSTRS) constrs
        else if(feature === CLASS__MEMBERS) members
        else eObject.eGet(feature)
    }
    
    override setPure(boolean            value) { throw new AssertionError }
    override setName(String             value) { throw new AssertionError }
    override setTypeParam(TypeParam     value) { throw new AssertionError }
    override setJvmClass(JvmGenericType value) { throw new AssertionError }
    override setSuperclassRef(ClassRef  value) { throw new AssertionError }
}
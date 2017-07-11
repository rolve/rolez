package ch.trick17.rolez.generic

import ch.trick17.rolez.rolez.ClassRef
import ch.trick17.rolez.rolez.Field
import ch.trick17.rolez.rolez.Member
import ch.trick17.rolez.rolez.Method
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
    
    override isPure()             { eObject.pure }
    override getName()            { eObject.name }
    override getTypeParam()       { eObject.typeParam }
    override getJvmClass()        { eObject.jvmClass }
    override getSuperclassRef()   { eObject.superclassRef.parameterized }
    override getConstrs()         { new ParameterizedConstrList(eObject.constrs, this, typeArgs, roleArgs) }
    override getUnslicedMembers() { new ParameterizedMemberList(eObject.unslicedMembers, this, typeArgs, roleArgs) }
    override getMembers()         { eObject.members.map[parameterized] }
    override getSlices()          { eObject.slices } // should be fine as long as generic classes (which are mapped) cannot be sliced
    
    override isSingleton()      { eObject.isSingleton }
    override isMapped()         { eObject.isMapped }
    override getSuperclass()    { eObject.superclass?.parameterized }
    override getFields()        { eObject.fields.map[parameterized] }
    override getMethods()       { eObject.methods.map[parameterized] }
    override getQualifiedName() { eObject.qualifiedName }

    override eGet(EStructuralFeature feature) {
        if(feature === CLASS__SUPERCLASS_REF) superclassRef
        else if(feature === NORMAL_CLASS__CONSTRS) constrs
        else if(feature === CLASS__UNSLICED_MEMBERS) unslicedMembers
        else eObject.eGet(feature)
    }
    
    override setPure(boolean            value) { throw new AssertionError }
    override setName(String             value) { throw new AssertionError }
    override setTypeParam(TypeParam     value) { throw new AssertionError }
    override setJvmClass(JvmGenericType value) { throw new AssertionError }
    override setSuperclassRef(ClassRef  value) { throw new AssertionError }
    
    def parameterized(Field  it) { new ParameterizedField (it, this, typeArgs, roleArgs) }
    def parameterized(Method it) { new ParameterizedMethod(it, this, typeArgs, roleArgs) }
    def Member parameterized(Member it) { switch(it) { Field: parameterized Method: parameterized }}
}
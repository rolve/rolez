package ch.trick17.rolez.generic

import ch.trick17.rolez.rolez.Block
import ch.trick17.rolez.rolez.Method
import ch.trick17.rolez.rolez.Role
import ch.trick17.rolez.rolez.RoleParam
import ch.trick17.rolez.rolez.Type
import ch.trick17.rolez.rolez.TypeParam
import java.util.Map
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EStructuralFeature
import org.eclipse.xtext.common.types.JvmOperation

import static ch.trick17.rolez.rolez.RolezPackage.Literals.*

class ParameterizedMethod extends ParameterizedEObject<Method> implements Method {
    
    package new(Method eObject, EObject eContainer, Map<TypeParam, Type> typeArgs, Map<RoleParam, Role> roleArgs) {
        super(eObject, eContainer, typeArgs, roleArgs)
    }
    
    override getJvmMethod()   { eObject.jvmMethod }
    override getRoleParams()  { eObject.roleParams }
    override getThisRole()    { eObject.thisRole.parameterized }
    override getSuperMethod() { eObject.superMethod }
    override getName()        { eObject.name }
    override getParams()      { eObject.parameterizedParams }
    override getType()        { eObject.type.parameterized }
    override getBody()        { eObject.body }
    
    override isMapped() { eObject.isMapped }
    
    override eGet(EStructuralFeature feature) {
        if(feature === METHOD__THIS_ROLE) roleParams
        else if(feature === PARAMETERIZED_BODY__PARAMS) params
        else if(feature === TYPED__TYPE) type
        else eObject.eGet(feature)
    }
    
    override setJvmMethod(JvmOperation value) { throw new AssertionError }
    override setSuperMethod(    Method value) { throw new AssertionError }
    override setThisRole(         Role value) { throw new AssertionError }
    override setName(           String value) { throw new AssertionError }
    override setType(             Type value) { throw new AssertionError }
    override setBody(            Block value) { throw new AssertionError }
}
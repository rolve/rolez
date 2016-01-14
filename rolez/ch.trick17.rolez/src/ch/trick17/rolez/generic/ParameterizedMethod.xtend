package ch.trick17.rolez.generic

import ch.trick17.rolez.rolez.Block
import ch.trick17.rolez.rolez.Method
import ch.trick17.rolez.rolez.Role
import ch.trick17.rolez.rolez.Type
import ch.trick17.rolez.rolez.TypeParam
import java.util.Map
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.common.types.JvmOperation
import org.eclipse.emf.ecore.EStructuralFeature

import static ch.trick17.rolez.rolez.RolezPackage.Literals.*

package class ParameterizedMethod extends ParameterizedEObject<Method> implements Method {
    
    new(Method eObject, EObject eContainer, Map<TypeParam, Type> typeArgs) {
        super(eObject, eContainer, typeArgs)
    }
    
    override getJvmMethod()        { eObject.jvmMethod }
    override getThisRole()         { eObject.thisRole }
    override getOverriddenMethod() { eObject.overriddenMethod }
    override getName()             { eObject.name }
    override getParams()           { eObject.parameterizedParams }
    override getType()             { eObject.type.parameterized }
    override getBody()             { eObject.body }
    
    override eGet(EStructuralFeature feature) {
        if(feature === PARAMETERIZED_BODY__PARAMS) params
        else if(feature === TYPED__TYPE) type
        else eObject.eGet(feature)
    }
    
    override setJvmMethod( JvmOperation value) { throw new AssertionError }
    override setOverriddenMethod(Method value) { throw new AssertionError }
    override setThisRole(          Role value) { throw new AssertionError }
    override setName(            String value) { throw new AssertionError }
    override setType(              Type value) { throw new AssertionError }
    override setBody(             Block value) { throw new AssertionError }
}
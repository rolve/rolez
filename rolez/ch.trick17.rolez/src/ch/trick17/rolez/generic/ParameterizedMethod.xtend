package ch.trick17.rolez.generic

import ch.trick17.rolez.rolez.Instr
import ch.trick17.rolez.rolez.Method
import ch.trick17.rolez.rolez.Role
import ch.trick17.rolez.rolez.RoleParam
import ch.trick17.rolez.rolez.ThisParam
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
    
    override getJvmMethod()    { eObject.jvmMethod }
    override isDeclaredAsync() { eObject.isDeclaredAsync }
    override isDeclaredTask()  { eObject.isDeclaredTask }
    override getSuperMethod()  { eObject.superMethod }
    override getRoleParams()   { eObject.roleParams }
    override getThisParam()    { eObject.thisParam.parameterized }
    override getName()         { eObject.name }
    override getParams()       { eObject.parameterizedParams }
    override getType()         { eObject.type.parameterized }
    override getCode()         { eObject.code }
    
    override isMapped()         { eObject.isMapped }
    override isAsync()          { eObject.isAsync }
    override isTask()           { eObject.isTask }
    override isMain()           { eObject.isMain }
    override isOverriding()     { eObject.isOverriding }
    override getQualifiedName() { eObject.qualifiedName }
    override getBody()          { eObject.body }
    override getAllParams()     { eObject.allParams }
    
    override eGet(EStructuralFeature feature) {
        if(feature === METHOD__THIS_PARAM) thisParam
        else if(feature === EXECUTABLE__PARAMS) params
        else if(feature === TYPED__TYPE) type
        else eObject.eGet(feature)
    }
    
    override setJvmMethod(JvmOperation value) { throw new AssertionError }
    override setDeclaredAsync( boolean value) { throw new AssertionError }
    override setDeclaredTask(  boolean value) { throw new AssertionError }
    override setSuperMethod(    Method value) { throw new AssertionError }
    override setThisParam(   ThisParam value) { throw new AssertionError }
    override setName(           String value) { throw new AssertionError }
    override setType(             Type value) { throw new AssertionError }
    override setCode(            Instr value) { throw new AssertionError }
}
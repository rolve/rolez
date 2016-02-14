package ch.trick17.rolez.generic

import ch.trick17.rolez.rolez.Block
import ch.trick17.rolez.rolez.Constr
import ch.trick17.rolez.rolez.Role
import ch.trick17.rolez.rolez.RoleParam
import ch.trick17.rolez.rolez.Type
import ch.trick17.rolez.rolez.TypeParam
import java.util.Map
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EStructuralFeature
import org.eclipse.xtext.common.types.JvmConstructor

import static ch.trick17.rolez.rolez.RolezPackage.Literals.EXECUTABLE__PARAMS

class ParameterizedConstr extends ParameterizedEObject<Constr> implements Constr {
    
    new(Constr eObject, EObject eContainer, Map<TypeParam, Type> typeArgs, Map<RoleParam, Role> roleArgs) {
        super(eObject, eContainer, typeArgs, roleArgs)
    }
    
    override getJvmConstr() { eObject.jvmConstr }
    override getParams()    { eObject.parameterizedParams }
    override getBody()      { eObject.body }
    
    override isMapped() { eObject.isMapped }
    
    override eGet(EStructuralFeature feature) {
        if(feature === EXECUTABLE__PARAMS) params
        else eObject.eGet(feature)
    }
    
    override setJvmConstr(JvmConstructor value) { throw new AssertionError }
    override setBody(Block value)               { throw new AssertionError }
}
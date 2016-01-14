package ch.trick17.rolez.generic

import ch.trick17.rolez.rolez.Constr
import java.util.Map
import ch.trick17.rolez.rolez.TypeParam
import ch.trick17.rolez.rolez.Type
import org.eclipse.xtext.common.types.JvmConstructor
import ch.trick17.rolez.rolez.Block
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EStructuralFeature

import static ch.trick17.rolez.rolez.RolezPackage.Literals.PARAMETERIZED_BODY__PARAMS

class ParameterizedConstr extends ParameterizedEObject<Constr> implements Constr {
    
    new(Constr eObject, EObject eContainer, Map<TypeParam, Type> typeArgs) {
        super(eObject, eContainer, typeArgs)
    }
    
    override getJvmConstr() { eObject.jvmConstr }
    override getParams()    { eObject.parameterizedParams }
    override getBody()      { eObject.body }
    
    override eGet(EStructuralFeature feature) {
        if(feature === PARAMETERIZED_BODY__PARAMS) params
        else eObject.eGet(feature)
    }
    
    override setJvmConstr(JvmConstructor value) { throw new AssertionError }
    override setBody(Block value)               { throw new AssertionError }
}
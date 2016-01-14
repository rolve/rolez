package ch.trick17.rolez.generic

import ch.trick17.rolez.generic.ParameterizedEObject
import ch.trick17.rolez.rolez.GenericClassRef
import java.util.Map
import ch.trick17.rolez.rolez.Type
import ch.trick17.rolez.rolez.TypeParam
import ch.trick17.rolez.rolez.NormalClass
import org.eclipse.emf.ecore.EObject

import static ch.trick17.rolez.rolez.RolezPackage.Literals.*
import org.eclipse.emf.ecore.EStructuralFeature

package class ParameterizedGenericClassRef extends ParameterizedEObject<GenericClassRef> implements GenericClassRef {
    
    new(GenericClassRef eObject, EObject eContainer, Map<TypeParam, Type> typeArgs) {
        super(eObject, eContainer, typeArgs)
    }
    
    override getClazz()   { new ParameterizedNormalClass(eObject.clazz, this, typeArgs) }
    override getTypeArg() { eObject.typeArg.parameterized }
    
    override eGet(EStructuralFeature feature) {
        if(feature === GENERIC_CLASS_REF__CLAZZ) clazz
        else if(feature === GENERIC_CLASS_REF__TYPE_ARG) typeArg
        else throw new AssertionError
    }
    
    override setClazz(NormalClass value) { throw new AssertionError }
    override setTypeArg(     Type value) { throw new AssertionError }
}
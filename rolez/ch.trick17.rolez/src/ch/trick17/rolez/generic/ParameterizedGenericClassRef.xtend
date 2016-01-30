package ch.trick17.rolez.generic

import ch.trick17.rolez.rolez.GenericClassRef
import ch.trick17.rolez.rolez.NormalClass
import ch.trick17.rolez.rolez.Role
import ch.trick17.rolez.rolez.RoleParam
import ch.trick17.rolez.rolez.Type
import ch.trick17.rolez.rolez.TypeParam
import java.util.Map
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EStructuralFeature

import static ch.trick17.rolez.rolez.RolezPackage.Literals.*

package class ParameterizedGenericClassRef extends ParameterizedEObject<GenericClassRef> implements GenericClassRef {
    
    new(GenericClassRef eObject, EObject eContainer, Map<TypeParam, Type> typeArgs, Map<RoleParam, Role> roleArgs) {
        super(eObject, eContainer, typeArgs, roleArgs)
    }
    
    override getClazz()   { eObject.clazz.parameterizedWith(#{eObject.clazz.typeParam -> typeArg}) }
    override getTypeArg() { eObject.typeArg.parameterized }
    
    override eGet(EStructuralFeature feature) {
        if(feature === GENERIC_CLASS_REF__CLAZZ) clazz
        else if(feature === GENERIC_CLASS_REF__TYPE_ARG) typeArg
        else throw new AssertionError
    }
    
    override setClazz(NormalClass value) { throw new AssertionError }
    override setTypeArg(     Type value) { throw new AssertionError }
}
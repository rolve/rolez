package ch.trick17.rolez.generic

import ch.trick17.rolez.rolez.Class
import ch.trick17.rolez.rolez.SimpleClassRef
import ch.trick17.rolez.rolez.Type
import ch.trick17.rolez.rolez.TypeParam
import java.util.Map
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EStructuralFeature

import static ch.trick17.rolez.rolez.RolezPackage.Literals.SIMPLE_CLASS_REF__CLAZZ

package class ParameterizedSimpleClassRef extends ParameterizedEObject<SimpleClassRef> implements SimpleClassRef {
    
    new(SimpleClassRef eObject, EObject eContainer, Map<TypeParam, Type> typeArgs) {
        super(eObject, eContainer, typeArgs)
    }
    
    override getClazz() { eObject.clazz }
    
    override eGet(EStructuralFeature feature) {
        if(feature === SIMPLE_CLASS_REF__CLAZZ) clazz
        else throw new AssertionError
    }
    
    override setClazz(Class value) { throw new AssertionError }
}
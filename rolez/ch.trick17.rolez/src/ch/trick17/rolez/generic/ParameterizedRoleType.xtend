package ch.trick17.rolez.generic

import ch.trick17.rolez.rolez.ClassRef
import ch.trick17.rolez.rolez.Role
import ch.trick17.rolez.rolez.RoleType
import ch.trick17.rolez.rolez.Type
import ch.trick17.rolez.rolez.TypeParam
import java.util.Map
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EStructuralFeature

import static ch.trick17.rolez.rolez.RolezPackage.Literals.ROLE_TYPE__BASE

package class ParameterizedRoleType extends ParameterizedEObject<RoleType> implements RoleType {
    
    new(RoleType eObject, EObject eContainer, Map<TypeParam, Type> typeArgs) {
        super(eObject, eContainer, typeArgs)
    }
    
    override getBase() { eObject.base.parameterized }
    override getRole() { eObject.role }
    
    override eGet(EStructuralFeature feature) {
        if(feature === ROLE_TYPE__BASE) base
        else eObject.eGet(feature)
    }
    
    override setBase(ClassRef value) { throw new AssertionError }
    override setRole(    Role value) { throw new AssertionError }
}
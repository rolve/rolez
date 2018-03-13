package ch.trick17.rolez.generic

import ch.trick17.rolez.rolez.ClassRef
import ch.trick17.rolez.rolez.Role
import ch.trick17.rolez.rolez.RoleParam
import ch.trick17.rolez.rolez.RoleType
import ch.trick17.rolez.rolez.Slice
import ch.trick17.rolez.rolez.Type
import ch.trick17.rolez.rolez.TypeParam
import java.util.Map
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EStructuralFeature

import static ch.trick17.rolez.rolez.RolezPackage.Literals.*

package class ParameterizedRoleType extends ParameterizedEObject<RoleType> implements RoleType {
    
    new(RoleType eObject, EObject eContainer, Map<TypeParam, Type> typeArgs, Map<RoleParam, Role> roleArgs) {
        super(eObject, eContainer, typeArgs, roleArgs)
    }
    
    override getBase()  { eObject.base.parameterized }
    override getRole()  { eObject.role.parameterized }
    override getSlice() { eObject.slice } // should be fine as long as generic classes (which are mapped) cannot be sliced
    
    override isSliced() { eObject.isSliced }
    
    override eGet(EStructuralFeature feature) {
        if(feature === ROLE_TYPE__BASE) base
        else if(feature === ROLE_TYPE__ROLE) role
        else slice
    }
    
    override setBase(ClassRef value) { throw new AssertionError }
    override setRole(    Role value) { throw new AssertionError }
    override setSlice(  Slice value) { throw new AssertionError }
}
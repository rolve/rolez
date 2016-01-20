package ch.trick17.rolez.generic

import ch.trick17.rolez.rolez.Expr
import ch.trick17.rolez.rolez.Field
import ch.trick17.rolez.rolez.Role
import ch.trick17.rolez.rolez.RoleParam
import ch.trick17.rolez.rolez.Type
import ch.trick17.rolez.rolez.TypeParam
import ch.trick17.rolez.rolez.VarKind
import java.util.Map
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EStructuralFeature
import org.eclipse.xtext.common.types.JvmField

import static ch.trick17.rolez.rolez.RolezPackage.Literals.TYPED__TYPE

package class ParameterizedField extends ParameterizedEObject<Field> implements Field {
    
    new(Field eObject, EObject eContainer, Map<TypeParam, Type> typeArgs, Map<RoleParam, Role> roleArgs) {
        super(eObject, eContainer, typeArgs, roleArgs)
    }
    
    override getJvmField()    { eObject.jvmField }
    override getKind()        { eObject.kind }
    override getName()        { eObject.name }
    override getType()        { eObject.type.parameterized }
    override getInitializer() { eObject.initializer }
    
    override eGet(EStructuralFeature feature) {
        if(feature === TYPED__TYPE) type
        else eObject.eGet(feature)
    }
    
    override setJvmField(JvmField value) { throw new AssertionError }
    override setKind(VarKind      value) { throw new AssertionError }
    override setName(String       value) { throw new AssertionError }
    override setType(Type         value) { throw new AssertionError }
    override setInitializer(Expr  value) { throw new AssertionError }
}
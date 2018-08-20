package ch.trick17.rolez.generic

import ch.trick17.rolez.rolez.Role
import ch.trick17.rolez.rolez.RoleParam
import ch.trick17.rolez.rolez.ThisParam
import ch.trick17.rolez.rolez.Type
import ch.trick17.rolez.rolez.TypeParam
import java.util.Map
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EStructuralFeature

import static ch.trick17.rolez.RolezExtensions.*
import static ch.trick17.rolez.rolez.RolezPackage.Literals.TYPED__RAW_TYPE

package class ParameterizedThisParam extends ParameterizedEObject<ThisParam> implements ThisParam {
    
    new(ThisParam eObject, EObject eContainer, Map<TypeParam, Type> typeArgs, Map<RoleParam, Role> roleArgs) {
        super(eObject, eContainer, typeArgs, roleArgs)
    }
    
    override getName()    { eObject.name }
    override getRawType() { eObject.rawType }
    
    override getType()             { eObject.type.parameterized }
    override getKind()             { eObject.kind }
    override enclosingExecutable() { enclosingExecutable(this) }
    override enclosingMethod()     { enclosingMethod(this) }
    
    override eGet(EStructuralFeature feature) {
        if(feature === TYPED__RAW_TYPE) rawType
        else eObject.eGet(feature)
    }
    
    override setName(String  value) { throw new AssertionError }
    override setRawType(Type value) { throw new AssertionError }
}
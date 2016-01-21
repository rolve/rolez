package ch.trick17.rolez.generic

import ch.trick17.rolez.rolez.Method
import ch.trick17.rolez.rolez.NormalClass
import ch.trick17.rolez.rolez.Role
import ch.trick17.rolez.rolez.RoleParam
import ch.trick17.rolez.rolez.Type
import ch.trick17.rolez.rolez.TypeParam
import java.util.Map

/**
 * A parameterized program element, e.g., a parameterized method, represents an "instantiation" 
 * of a generic element, e.g., a method that has type parameters. In a parameterized element,
 * all type parameters of the generic element are replaced with the corresponding type arguments.
 * <p>
 * Note that, in general, type arguments can be type parameters again, i.e., a parameterized
 * element can again be generic.
 */
abstract class Parameterized {
    
    static def NormalClass parameterizedWith(NormalClass it, Map<TypeParam, Type> typeArgs) {
        // TODO: return original if type args are empty
        new ParameterizedNormalClass(it, eContainer, typeArgs, emptyMap)
    }
    
    static def Method parameterizedWith(Method it, Map<RoleParam, Role> roleArgs) {
        // TODO: return original if role args are empty
        if(it instanceof ParameterizedMethod)
            new ParameterizedMethod(genericEObject, eContainer, typeArgs, roleArgs)
        else
            new ParameterizedMethod(it, eContainer, emptyMap, roleArgs)
    }
    
    package val Map<TypeParam, Type> typeArgs
    package val Map<RoleParam, Role> roleArgs
    
    package new(Map<TypeParam, Type> typeArgs, Map<RoleParam, Role> roleArgs) {
        if(typeArgs == null || roleArgs == null)
            throw new NullPointerException
        this.typeArgs = typeArgs
        this.roleArgs = roleArgs
    }
    
    package new(Parameterized base) {
        this.typeArgs = base.typeArgs
        this.roleArgs = base.roleArgs
    }
    
    override equals(Object other) {
        if(this === other)
            true
        else if(other instanceof Parameterized)
            typeArgs == other.typeArgs && roleArgs == other.roleArgs
        else
            false
    }
    
    override hashCode() { typeArgs.hashCode + roleArgs.hashCode + 1 }
}
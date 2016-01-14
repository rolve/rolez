package ch.trick17.rolez.generic

import ch.trick17.rolez.rolez.NormalClass
import ch.trick17.rolez.rolez.Type
import ch.trick17.rolez.rolez.TypeParam
import java.util.Map
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.util.EcoreUtil

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
        EcoreUtil.resolve(it, null as EObject)
        new ParameterizedNormalClass(it, eContainer, typeArgs)
    }
    
    package val Map<TypeParam, Type> typeArgs
    
    new(Map<TypeParam, Type> typeArgs) {
        if(typeArgs == null)
            throw new NullPointerException
        this.typeArgs = typeArgs
    }
    
    new(Parameterized base) {
        this.typeArgs = base.typeArgs
    }
    
    override equals(Object other) {
        if(this === other)
            true
        else if(other instanceof Parameterized)
            typeArgs == other.typeArgs
        else
            false
    }
    
    override hashCode() { typeArgs.hashCode + 1 }
}
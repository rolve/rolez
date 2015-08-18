package ch.trick17.peppl.lang.scoping

import ch.trick17.peppl.lang.peppl.Field
import ch.trick17.peppl.lang.peppl.FieldSelector
import ch.trick17.peppl.lang.peppl.MemberAccess
import ch.trick17.peppl.lang.peppl.Method
import ch.trick17.peppl.lang.peppl.MethodSelector
import ch.trick17.peppl.lang.peppl.RoleType
import ch.trick17.peppl.lang.peppl.WithParameters
import ch.trick17.peppl.lang.typesystem.PepplSystem
import ch.trick17.peppl.lang.typesystem.PepplTypeUtils
import ch.trick17.peppl.lang.validation.PepplValidator
import javax.inject.Inject
import org.eclipse.emf.ecore.EReference
import org.eclipse.xtext.scoping.IScope
import org.eclipse.xtext.scoping.Scopes
import org.eclipse.xtext.scoping.impl.AbstractDeclarativeScopeProvider

import static ch.trick17.peppl.lang.validation.PepplValidator.*

class PepplScopeProvider extends AbstractDeclarativeScopeProvider {
    
    @Inject private PepplSystem system
    @Inject private PepplValidator validator
    @Inject private extension PepplTypeUtils
    
    def IScope scope_FieldSelector_field(FieldSelector s, EReference ref) {
        val targetType = system.type(envFor(s), (s.eContainer as MemberAccess).target).value
        if(targetType instanceof RoleType)
            Scopes.scopeFor(targetType.base.allMembers.filter(Field))
        else
            IScope.NULLSCOPE;
    }
    
    def IScope scope_MethodSelector_method(MethodSelector s, EReference ref) {
        val targetType = system.type(envFor(s), (s.eContainer as MemberAccess).target).value
        if(targetType instanceof RoleType) {
            // Find most specific method, following
            // http://docs.oracle.com/javase/specs/jls/se8/html/jls-15.html#jls-15.12.2
            val applicable = targetType.base.allMembers.filter(Method).filter[
                name.equals(s.methodName) && system.validArgumentsSucceeded(envFor(s), s, it)
            ].toList
            
            val maximallySpecific = applicable.filter[m |
                applicable.forall[
                    m == it || !it.strictlyMoreSpecificThan(m)
                ]
            ]
            
            if(maximallySpecific.size <= 1)
                Scopes.scopeFor(maximallySpecific)
            else {
                validator.delayedError("Method call is ambiguous", s, ref, AMBIGUOUS_CALL)
                Scopes.scopeFor(maximallySpecific)
            }
        }
        else
            IScope.NULLSCOPE;
    }
    
    private def strictlyMoreSpecificThan(WithParameters target, WithParameters other) {
        target.moreSpecificThan(other) && !other.moreSpecificThan(target)
    }
    
    private def moreSpecificThan(WithParameters target, WithParameters other) {
        // Assume both targets have the same number of parameters
        val i = other.params.iterator
        target.params.forall[system.subtypeSucceeded(envFor(target), it.type, i.next.type)]
    }
}

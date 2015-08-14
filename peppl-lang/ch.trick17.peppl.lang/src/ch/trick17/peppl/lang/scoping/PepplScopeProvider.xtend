package ch.trick17.peppl.lang.scoping

import ch.trick17.peppl.lang.peppl.Field
import ch.trick17.peppl.lang.peppl.FieldSelector
import ch.trick17.peppl.lang.peppl.MemberAccess
import ch.trick17.peppl.lang.peppl.Method
import ch.trick17.peppl.lang.peppl.MethodSelector
import ch.trick17.peppl.lang.peppl.RoleType
import ch.trick17.peppl.lang.typesystem.PepplSystem
import ch.trick17.peppl.lang.typesystem.PepplTypeUtils
import javax.inject.Inject
import org.eclipse.emf.ecore.EReference
import org.eclipse.xtext.scoping.IScope
import org.eclipse.xtext.scoping.Scopes
import org.eclipse.xtext.scoping.impl.AbstractDeclarativeScopeProvider

class PepplScopeProvider extends AbstractDeclarativeScopeProvider {
    
    @Inject private PepplSystem system
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
            // Find method that matches best (in case of overloading)
            val matching = targetType.base.allMembers.filter(Method).filter[
                system.validArgumentsSucceeded(envFor(s), s, it)
            ]
            // TODO
            Scopes.scopeFor(matching)
        }
        else
            IScope.NULLSCOPE;
    }
}

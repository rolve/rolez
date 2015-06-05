package ch.trick17.peppl.lang.scoping

import ch.trick17.peppl.lang.peppl.FieldAccess
import ch.trick17.peppl.lang.peppl.Role
import ch.trick17.peppl.lang.typesystem.PepplSystem
import ch.trick17.peppl.lang.typesystem.PepplTypeUtils
import it.xsemantics.runtime.RuleEnvironment
import it.xsemantics.runtime.RuleEnvironmentEntry
import javax.inject.Inject
import org.eclipse.emf.ecore.EReference
import org.eclipse.xtext.scoping.IScope
import org.eclipse.xtext.scoping.impl.AbstractDeclarativeScopeProvider
import org.eclipse.xtext.scoping.Scopes
import ch.trick17.peppl.lang.peppl.RoleType
import ch.trick17.peppl.lang.peppl.Field
import ch.trick17.peppl.lang.peppl.Class
import java.util.List
import java.util.ArrayList
import ch.trick17.peppl.lang.peppl.MethodInvoke
import ch.trick17.peppl.lang.peppl.Method
import ch.trick17.peppl.lang.peppl.Member

class PepplScopeProvider extends AbstractDeclarativeScopeProvider {
    
    @Inject private extension PepplSystem system
    @Inject private extension PepplTypeUtils __
    
    def IScope scope_FieldAccess_field(FieldAccess a, EReference ref) {
        val thisType = roleType(Role.READWRITE, a.enclosingClass)
        val G = new RuleEnvironment(new RuleEnvironmentEntry("this", thisType));
        
        val targetType = system.type(G, a.target).value
        if(targetType instanceof RoleType)
            Scopes.scopeFor(targetType.base.allMembers.filter(Field))
        else
            IScope.NULLSCOPE;
    }
    
    def IScope scope_MethodInvoke_method(MethodInvoke i, EReference ref) {
        val thisType = roleType(Role.READWRITE, i.enclosingClass)
        val G = new RuleEnvironment(new RuleEnvironmentEntry("this", thisType));
        
        val targetType = system.type(G, i.target).value
        if(targetType instanceof RoleType)
            Scopes.scopeFor(targetType.base.allMembers.filter(Method))
        else
            IScope.NULLSCOPE;
    }
    
    def List<Member> allMembers(Class clazz) {
        if(clazz.actualSuperclass == null)
            return emptyList
        else {
            val result = new ArrayList(clazz.members)
            result.addAll(clazz.actualSuperclass.allMembers)
            result
        }
    }
}

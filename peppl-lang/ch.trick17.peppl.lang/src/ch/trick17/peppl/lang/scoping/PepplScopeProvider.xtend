package ch.trick17.peppl.lang.scoping

import ch.trick17.peppl.lang.peppl.Block
import ch.trick17.peppl.lang.peppl.Field
import ch.trick17.peppl.lang.peppl.FieldSelector
import ch.trick17.peppl.lang.peppl.LocalVarDecl
import ch.trick17.peppl.lang.peppl.MemberAccess
import ch.trick17.peppl.lang.peppl.Method
import ch.trick17.peppl.lang.peppl.MethodSelector
import ch.trick17.peppl.lang.peppl.ParameterizedBody
import ch.trick17.peppl.lang.peppl.RoleType
import ch.trick17.peppl.lang.peppl.Stmt
import ch.trick17.peppl.lang.peppl.Var
import ch.trick17.peppl.lang.peppl.VarRef
import ch.trick17.peppl.lang.typesystem.PepplSystem
import ch.trick17.peppl.lang.typesystem.PepplUtils
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
    @Inject private extension PepplUtils
    
    def IScope scope_FieldSelector_field(FieldSelector s, EReference ref) {
        val targetType = system.type(envFor(s), (s.eContainer as MemberAccess).target).value
        if(targetType instanceof RoleType)
            Scopes.scopeFor(targetType.base.clazz.allMembers.filter(Field))
        else
            IScope.NULLSCOPE;
    }
    
    def IScope scope_MethodSelector_method(MethodSelector s, EReference ref) {
        val targetType = system.type(envFor(s), (s.eContainer as MemberAccess).target).value
        if(targetType instanceof RoleType) {
            val maxSpecific = targetType.base.clazz.allMembers.filter(Method)
                .filter[name.equals(s.methodName)].maximallySpecific(s)
            
            if(maxSpecific.size <= 1)
                Scopes.scopeFor(maxSpecific)
            else {
                validator.delayedError("Method call is ambiguous", s, ref, AMBIGUOUS_CALL)
                Scopes.scopeFor(maxSpecific)
            }
        }
        else
            IScope.NULLSCOPE;
    }
    
    
    def IScope scope_VarRef_variable(VarRef varRef, EReference eRef) {
        val stmt = varRef.enclosingStmt
        Scopes.scopeFor(varsAbove(stmt.eContainer, stmt))
    }
    
    private def dispatch Iterable<? extends Var> varsAbove(Stmt container, Stmt s) {
        varsAbove(container.eContainer, container)
    }
    
    private def dispatch Iterable<? extends Var> varsAbove(Block b, Stmt s) {
        b.stmts.takeWhile[it != s].filter(LocalVarDecl).map[variable]
            + varsAbove(b.eContainer, s)
    }
    
    private def dispatch Iterable<? extends Var> varsAbove(ParameterizedBody p, Stmt s) {
        p.params
    }
}

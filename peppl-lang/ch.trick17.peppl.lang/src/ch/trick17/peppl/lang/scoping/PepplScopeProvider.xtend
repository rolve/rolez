package ch.trick17.peppl.lang.scoping

import ch.trick17.peppl.lang.peppl.Block
import ch.trick17.peppl.lang.peppl.ElemWithBody
import ch.trick17.peppl.lang.peppl.Field
import ch.trick17.peppl.lang.peppl.FieldSelector
import ch.trick17.peppl.lang.peppl.LocalVarDecl
import ch.trick17.peppl.lang.peppl.MemberAccess
import ch.trick17.peppl.lang.peppl.Method
import ch.trick17.peppl.lang.peppl.MethodSelector
import ch.trick17.peppl.lang.peppl.Parameterized
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
                name.equals(s.methodName) && system.validArgsSucceeded(envFor(s), s, it)
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
    
    private def strictlyMoreSpecificThan(Parameterized target, Parameterized other) {
        target.moreSpecificThan(other) && !other.moreSpecificThan(target)
    }
    
    private def moreSpecificThan(Parameterized target, Parameterized other) {
        // Assume both targets have the same number of parameters
        val i = other.params.iterator
        target.params.forall[system.subtypeSucceeded(envFor(target), it.type, i.next.type)]
    }
    
    def IScope scope_VarRef_variable(VarRef varRef, EReference eRef) {
        val stmt = varRef.enclosingStmt
        Scopes.scopeFor(collectVars(stmt.eContainer, stmt))
    }
    
    private def dispatch Iterable<? extends Var> collectVars(Stmt container, Stmt s) {
        collectVars(container.eContainer, container)
    }
    
    private def dispatch Iterable<? extends Var> collectVars(Block b, Stmt s) {
        b.stmts.takeWhile[it != s].filter(LocalVarDecl).map[variable]
            + collectVars(b.eContainer, s)
    }
    
    private def dispatch Iterable<? extends Var> collectVars(Parameterized p, Stmt s) {
        p.params
    }
    
    private def dispatch Iterable<? extends Var> collectVars(ElemWithBody p, Stmt s) {
        emptyList
    }
}

package ch.trick17.rolez.lang.scoping

import ch.trick17.rolez.lang.RolezExtensions
import ch.trick17.rolez.lang.RolezUtils
import ch.trick17.rolez.lang.rolez.Block
import ch.trick17.rolez.lang.rolez.Field
import ch.trick17.rolez.lang.rolez.LocalVarDecl
import ch.trick17.rolez.lang.rolez.MemberAccess
import ch.trick17.rolez.lang.rolez.Method
import ch.trick17.rolez.lang.rolez.New
import ch.trick17.rolez.lang.rolez.NormalClass
import ch.trick17.rolez.lang.rolez.ParameterizedBody
import ch.trick17.rolez.lang.rolez.RoleType
import ch.trick17.rolez.lang.rolez.Stmt
import ch.trick17.rolez.lang.rolez.SuperConstrCall
import ch.trick17.rolez.lang.rolez.Var
import ch.trick17.rolez.lang.rolez.VarRef
import ch.trick17.rolez.lang.typesystem.RolezSystem
import ch.trick17.rolez.lang.validation.RolezValidator
import javax.inject.Inject
import org.eclipse.emf.ecore.EReference
import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.xtext.nodemodel.util.NodeModelUtils
import org.eclipse.xtext.scoping.IScope
import org.eclipse.xtext.scoping.Scopes
import org.eclipse.xtext.scoping.impl.AbstractDeclarativeScopeProvider

import static ch.trick17.rolez.lang.rolez.RolezPackage.Literals.*
import static ch.trick17.rolez.lang.validation.RolezValidator.*

class RolezScopeProvider extends AbstractDeclarativeScopeProvider {
    
    @Inject extension RolezExtensions
    @Inject RolezSystem system
    @Inject RolezValidator validator
    @Inject RolezUtils utils
    
    def IScope scope_MemberAccess_member(MemberAccess it, EReference ref) {
        val targetType = system.type(utils.envFor(it), target).value
        
        if(targetType instanceof RoleType) {
            val fields = targetType.base.clazz.allMembers.filter(Field)
                .filter[f | f.name == memberName]
            if(args.isEmpty && !fields.isEmpty)
                Scopes.scopeFor(fields)
            else {
                val candidates = targetType.base.clazz.allMembers.filter(Method)
                    .filter[m | m.name == memberName]
                val maxSpecific = utils.maximallySpecific(candidates, it)
                
                if(maxSpecific.size <= 1)
                    Scopes.scopeFor(maxSpecific)
                else {
                    validator.delayedError("Method invoke is ambiguous", it, ref, AMBIGUOUS_CALL)
                    Scopes.scopeFor(maxSpecific)
                }
            }
        }
        else
            IScope.NULLSCOPE;
    }
    
    def IScope scope_New_target(New it, EReference ref) {
        val clazz = classRef.clazz
        if(clazz instanceof NormalClass) {
            val maxSpecific = utils.maximallySpecific(clazz.constrs, it)
            
            if(maxSpecific.size <= 1)
                Scopes.scopeFor(maxSpecific, [QualifiedName.create("new")], IScope.NULLSCOPE)
            else {
                validator.delayedError("Constructor call is ambiguous", it, ref, AMBIGUOUS_CALL)
                Scopes.scopeFor(maxSpecific)
            }
        }
        else
            IScope.NULLSCOPE
    }
    
    def IScope scope_SuperConstrCall_target(SuperConstrCall it, EReference ref) {
        val maxSpecific = utils.maximallySpecific(enclosingClass.actualSuperclass.constrs, it)
        
        if(maxSpecific.size <= 1)
            Scopes.scopeFor(maxSpecific, [QualifiedName.create("super")], IScope.NULLSCOPE)
        else {
            validator.delayedError("Constructor call is ambiguous", it, ref, AMBIGUOUS_CALL)
            Scopes.scopeFor(maxSpecific)
        }
    }
    
    private def memberName(MemberAccess it) {
        NodeModelUtils.findNodesForFeature(it, MEMBER_ACCESS__MEMBER).get(0).text
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

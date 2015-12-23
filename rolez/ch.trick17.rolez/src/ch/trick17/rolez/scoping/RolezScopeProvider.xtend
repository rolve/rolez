package ch.trick17.rolez.scoping

import ch.trick17.rolez.RolezExtensions
import ch.trick17.rolez.RolezUtils
import ch.trick17.rolez.rolez.Constr
import ch.trick17.rolez.rolez.Field
import ch.trick17.rolez.rolez.MemberAccess
import ch.trick17.rolez.rolez.Method
import ch.trick17.rolez.rolez.New
import ch.trick17.rolez.rolez.NormalClass
import ch.trick17.rolez.rolez.RoleType
import ch.trick17.rolez.rolez.SuperConstrCall
import ch.trick17.rolez.rolez.VarRef
import ch.trick17.rolez.typesystem.RolezSystem
import ch.trick17.rolez.validation.JavaMapper
import ch.trick17.rolez.validation.RolezValidator
import javax.inject.Inject
import org.eclipse.emf.ecore.EReference
import org.eclipse.xtext.common.types.JvmVisibility
import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.xtext.nodemodel.util.NodeModelUtils
import org.eclipse.xtext.scoping.IScope
import org.eclipse.xtext.scoping.impl.AbstractDeclarativeScopeProvider

import static ch.trick17.rolez.rolez.RolezPackage.Literals.*
import static ch.trick17.rolez.validation.RolezValidator.*
import static org.eclipse.xtext.scoping.Scopes.scopeFor

class RolezScopeProvider extends AbstractDeclarativeScopeProvider {
    
    @Inject extension RolezExtensions
    @Inject extension JavaMapper
    @Inject RolezSystem system
    @Inject RolezValidator validator
    @Inject RolezUtils utils
    
    def scope_MemberAccess_member(MemberAccess it, EReference ref) {
        val targetType = system.type(utils.envFor(it), target).value
        
        if(targetType instanceof RoleType) {
            val fields = targetType.base.clazz.allMembers.filter(Field)
                .filter[f | f.name == memberName]
            if(args.isEmpty && !fields.isEmpty)
                scopeFor(fields)
            else {
                val candidates = targetType.base.clazz.allMembers.filter(Method)
                    .filter[m | m.name == memberName]
                val maxSpecific = utils.maximallySpecific(candidates, it)
                
                if(maxSpecific.size <= 1)
                    scopeFor(maxSpecific)
                else {
                    validator.delayedError("Method invoke is ambiguous", it, ref, AMBIGUOUS_CALL)
                    scopeFor(maxSpecific)
                }
            }
        }
        else
            IScope.NULLSCOPE;
    }
    
    def scope_New_constr(New it, EReference ref) {
        val clazz = classRef.clazz
        if(clazz instanceof NormalClass) {
            val maxSpecific = utils.maximallySpecific(clazz.constrs, it)
            
            if(maxSpecific.size <= 1)
                scopeFor(maxSpecific, [QualifiedName.create("new")], IScope.NULLSCOPE)
            else {
                validator.delayedError("Constructor call is ambiguous", it, ref, AMBIGUOUS_CALL)
                scopeFor(maxSpecific)
            }
        }
        else
            IScope.NULLSCOPE
    }
    
    def scope_SuperConstrCall_constr(SuperConstrCall it, EReference ref) {
        val maxSpecific = utils.maximallySpecific(enclosingClass.superclass.constrs, it)
        
        if(maxSpecific.size <= 1)
            scopeFor(maxSpecific, [QualifiedName.create("super")], IScope.NULLSCOPE)
        else {
            validator.delayedError("Constructor call is ambiguous", it, ref, AMBIGUOUS_CALL)
            scopeFor(maxSpecific)
        }
    }
    
    def scope_Field_jvmField(Field it, EReference ref) {
        val candidates = enclosingClass.jvmClass.declaredFields.filter[f |
            f.simpleName == name && f.visibility == JvmVisibility.PUBLIC
                && f.isStatic == enclosingClass.isSingleton
        ]
        scopeFor(candidates, [QualifiedName.create("mapped")], IScope.NULLSCOPE)
    }
    
    def scope_Method_jvmMethod(Method it, EReference ref) {
        val candidates = enclosingClass.jvmClass.declaredOperations.filter[m |
            val javaParams = m.parameters.iterator
            m.simpleName == name
                && m.visibility == JvmVisibility.PUBLIC
                && m.isStatic == enclosingClass.isSingleton
                && params.size == m.parameters.size
                && params.forall[type.mapsTo(javaParams.next.parameterType)]
        ]
        scopeFor(candidates, [QualifiedName.create("mapped")], IScope.NULLSCOPE)
    }
    
    def scope_Constr_jvmConstr(Constr it, EReference ref) {
        val candidates = enclosingClass.jvmClass.declaredConstructors.filter[c |
            val javaParams = c.parameters.iterator
            c.visibility == JvmVisibility.PUBLIC
                && params.size == c.parameters.size
                && params.forall[type.mapsTo(javaParams.next.parameterType)]
        ]
        scopeFor(candidates, [QualifiedName.create("mapped")], IScope.NULLSCOPE)
    }
    
    private def memberName(MemberAccess it) {
        NodeModelUtils.findNodesForFeature(it, MEMBER_ACCESS__MEMBER).get(0).text
    }
    
    def IScope scope_VarRef_variable(VarRef varRef, EReference eRef) {
        val stmt = varRef.enclosingStmt
        scopeFor(utils.varsAbove(stmt.eContainer, stmt))
    }
}

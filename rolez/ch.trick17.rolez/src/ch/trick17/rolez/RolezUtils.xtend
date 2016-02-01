package ch.trick17.rolez

import ch.trick17.rolez.rolez.Assignment
import ch.trick17.rolez.rolez.Block
import ch.trick17.rolez.rolez.Class
import ch.trick17.rolez.rolez.ClassRef
import ch.trick17.rolez.rolez.Constr
import ch.trick17.rolez.rolez.Expr
import ch.trick17.rolez.rolez.ForLoop
import ch.trick17.rolez.rolez.GenericClassRef
import ch.trick17.rolez.rolez.Instr
import ch.trick17.rolez.rolez.LocalVarDecl
import ch.trick17.rolez.rolez.MemberAccess
import ch.trick17.rolez.rolez.Method
import ch.trick17.rolez.rolez.New
import ch.trick17.rolez.rolez.NormalClass
import ch.trick17.rolez.rolez.ParameterizedBody
import ch.trick17.rolez.rolez.Role
import ch.trick17.rolez.rolez.RoleType
import ch.trick17.rolez.rolez.RolezFactory
import ch.trick17.rolez.rolez.SimpleClassRef
import ch.trick17.rolez.rolez.Start
import ch.trick17.rolez.rolez.Stmt
import ch.trick17.rolez.rolez.Task
import ch.trick17.rolez.rolez.This
import ch.trick17.rolez.rolez.Type
import ch.trick17.rolez.rolez.Var
import it.xsemantics.runtime.RuleEnvironment
import it.xsemantics.runtime.RuleEnvironmentEntry
import javax.inject.Inject
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.xtext.scoping.IScopeProvider

import static ch.trick17.rolez.rolez.RolezPackage.Literals.*
import static ch.trick17.rolez.rolez.VarKind.VAL

import static extension org.eclipse.emf.ecore.util.EcoreUtil.resolve

/** 
 * Utility functions for Rolez language constructs
 * @author Michael Faes
 */
class RolezUtils {
    
    @Inject extension RolezFactory
    @Inject extension RolezExtensions
    @Inject IScopeProvider scopeProvider
    
    def newRoleType(Role r, ClassRef base) {
        val result = createRoleType
        result.role = r.copyIfNecessary
        result.base = base.copyIfNecessary
        result
    }
    
    def SimpleClassRef newClassRef(Class c) {
        val result = createSimpleClassRef
        result.clazz = c
        result
    }
    
    def newClassRef(NormalClass c, Type arg) {
        val result = createGenericClassRef
        result.clazz = c
        result.typeArg = arg.copyIfNecessary
        result
    }
    
    private def <T extends EObject> copyIfNecessary(T it) {
        if(eContainer == null) it else EcoreUtil.copy(it)
    }
    
    def createEnv(EObject context) {
        val body = context.enclosingBody
        switch(body) {
            case null: new RuleEnvironment
            Task: new RuleEnvironment
            Method: {
                val thisType = newRoleType(body.thisRole, newClassRef(body.enclosingClass))
                new RuleEnvironment(new RuleEnvironmentEntry("this", thisType))
            }
            Constr: {
                val thisType = newRoleType(createReadWrite, newClassRef(body.enclosingClass))
                new RuleEnvironment(new RuleEnvironmentEntry("this", thisType))
            }
        }
    }
    
    def findClass(QualifiedName name, EObject context) {
        scopeProvider.getScope(context, SIMPLE_CLASS_REF__CLAZZ)
            .getSingleElement(name)?.EObjectOrProxy?.resolve(context) as Class
    }
    
    def findNormalClass(QualifiedName name, EObject context) {
        scopeProvider.getScope(context, GENERIC_CLASS_REF__CLAZZ)
            .getSingleElement(name)?.EObjectOrProxy?.resolve(context) as NormalClass
    }
    
    /**
     * Returns <code>true</code> if the name and the types of the parameters of
     * the two given methods are the same, ignoring roles.
     */
    def equalSignatureWithoutRoles(Method it, Method other) {
        name == other.name && equalParamsWithoutRoles(other)
    }
    
    def equalParamsWithoutRoles(ParameterizedBody it, ParameterizedBody other) {
        val i = other.params.map[type].iterator
        params.size == other.params.size
            && params.map[type].forall[equalTypeWithoutRoles(i.next)]
    }
    
    private def dispatch boolean equalTypeWithoutRoles(RoleType it, RoleType other) {
        base.equalRefWithoutRoles(other.base)
    }
    private def dispatch boolean equalTypeWithoutRoles(Type it, Type other) {
        EcoreUtil.equals(it, other)
    }
    
    private def dispatch boolean equalRefWithoutRoles(GenericClassRef it, GenericClassRef other) {
        clazz == other.clazz && typeArg.equalTypeWithoutRoles(other.typeArg)
    }
    private def dispatch boolean equalRefWithoutRoles(ClassRef it, ClassRef other) {
        EcoreUtil.equals(it, other)
    }
    
    /**
     * Returns <code>true</code> iff the given expression is a kind of
     * expression that may have side effects, i.e., an assignment, a non-array
     * object instantiation, a task creation or a method invocation that is
     * not an array set.
     * Note that nested expressions may still have side effects.
     */
    def isSideFxExpr(Expr it) {
        switch(it) {
            Assignment: true
            New: !classRef.clazz.isArrayClass
            Start: true
            MemberAccess: isMethodInvoke && !isSliceGet && !isArrayGet
            default: false
        }
    }
    
    def isValFieldInit(Assignment it) {
        !(#[left].filter(MemberAccess).filter[isFieldAccess && target instanceof This]
            .map[field].filter[kind == VAL].isEmpty)
    }
    
    def assignedField(Assignment it) { (left as MemberAccess).field }
    
    def dispatch Iterable<? extends Var> varsAbove(Block container, Stmt s) {
        container.stmts.takeWhile[it != s].filter(LocalVarDecl).map[variable]
            + varsAbove(container.eContainer, s)
    }
    
    def dispatch Iterable<? extends Var> varsAbove(ForLoop container, Instr i) {
        (if(i === container.initializer) emptyList else #[container.initializer.variable as Var])
            + varsAbove(container.eContainer, container)
    }
    
    def dispatch Iterable<? extends Var> varsAbove(ParameterizedBody container, Stmt s) {
        container.params
    }
    
    def dispatch Iterable<? extends Var> varsAbove(Instr container, Instr i) {
        varsAbove(container.eContainer, container)
    }
}

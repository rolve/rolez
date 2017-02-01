package ch.trick17.rolez

import ch.trick17.rolez.rolez.Assignment
import ch.trick17.rolez.rolez.Block
import ch.trick17.rolez.rolez.Class
import ch.trick17.rolez.rolez.ClassRef
import ch.trick17.rolez.rolez.Executable
import ch.trick17.rolez.rolez.Expr
import ch.trick17.rolez.rolez.ForLoop
import ch.trick17.rolez.rolez.GenericClassRef
import ch.trick17.rolez.rolez.Instr
import ch.trick17.rolez.rolez.LocalVarDecl
import ch.trick17.rolez.rolez.MemberAccess
import ch.trick17.rolez.rolez.Method
import ch.trick17.rolez.rolez.New
import ch.trick17.rolez.rolez.NormalClass
import ch.trick17.rolez.rolez.Role
import ch.trick17.rolez.rolez.RoleType
import ch.trick17.rolez.rolez.RolezFactory
import ch.trick17.rolez.rolez.SimpleClassRef
import ch.trick17.rolez.rolez.Stmt
import ch.trick17.rolez.rolez.This
import ch.trick17.rolez.rolez.Type
import ch.trick17.rolez.rolez.TypeParam
import ch.trick17.rolez.rolez.Var
import ch.trick17.rolez.typesystem.RolezSystem
import ch.trick17.rolez.validation.JavaMapper
import java.util.HashSet
import java.util.Set
import javax.inject.Inject
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.xtext.scoping.IScopeProvider
import org.eclipse.xtext.util.OnChangeEvictingCache

import static ch.trick17.rolez.Constants.*
import static ch.trick17.rolez.rolez.RolezPackage.Literals.*
import static ch.trick17.rolez.rolez.VarKind.VAL

import static extension ch.trick17.rolez.RolezExtensions.*
import static extension ch.trick17.rolez.generic.Parameterized.parameterizedWith
import static extension org.eclipse.emf.ecore.util.EcoreUtil.resolve

/** 
 * Utility functions for Rolez language constructs
 * @author Michael Faes
 */
class RolezUtils {
    
    static extension val RolezFactory = RolezFactory.eINSTANCE
    
    /*
     * Static helper methods
     */
    
    static def newRoleType(Role role, ClassRef base) {
        val result = createRoleType
        result.role = role.copyIfNecessary
        result.base = base.copyIfNecessary
        result
    }
    
    static def SimpleClassRef newClassRef(Class clazz) {
        val result = createSimpleClassRef
        result.clazz = clazz
        result
    }
    
    static def newClassRef(NormalClass clazz, Type typeArg) {
        val result = createGenericClassRef
        result.clazz = clazz.parameterizedWith(#{clazz.typeParam -> typeArg})
        result.typeArg = typeArg.copyIfNecessary
        result
    }
    
    static def newTypeParamRef(TypeParam param, Role restrictingRole) {
        val result = createTypeParamRef
        result.param = param
        result.restrictingRole = restrictingRole?.copyIfNecessary
        return result
    }
    
    private static def <T extends EObject> copyIfNecessary(T it) {
        if(eContainer == null) it else EcoreUtil.copy(it)
    }
    
    /**
     * Returns <code>true</code> if the name and the types of the parameters of
     * the two given methods are the same, ignoring roles.
     */
    static def equalSignatureWithoutRoles(Method it, Method other) {
        name == other.name && equalParamsWithoutRoles(other)
    }
    
    static def equalParamsWithoutRoles(Executable it, Executable other) {
        val i = other.params.map[type].iterator
        params.size == other.params.size
            && params.map[type].forall[equalTypeWithoutRoles(i.next)]
    }
    
    private static def dispatch boolean equalTypeWithoutRoles(RoleType it, RoleType other) {
        base.equalRefWithoutRoles(other.base)
    }
    private static def dispatch boolean equalTypeWithoutRoles(Type it, Type other) {
        EcoreUtil.equals(it, other)
    }
    
    private static def dispatch boolean equalRefWithoutRoles(GenericClassRef it, GenericClassRef other) {
        clazz.qualifiedName == other.clazz.qualifiedName && typeArg.equalTypeWithoutRoles(other.typeArg)
    }
    private static def dispatch boolean equalRefWithoutRoles(ClassRef it, ClassRef other) {
        EcoreUtil.equals(it, other)
    }
    
    static def isValFieldInit(Assignment it) {
        !(#[left].filter(MemberAccess).filter[isFieldAccess && target instanceof This]
            .map[field].filter[kind == VAL].isEmpty)
    }
    
    static def assignedField(Assignment it) { (left as MemberAccess).field }
    
    static def dispatch Iterable<? extends Var> varsAbove(Block container, Stmt s) {
        container.stmts.takeWhile[it != s].filter(LocalVarDecl).map[variable]
            + varsAbove(container.eContainer, s)
    }
    
    static def dispatch Iterable<? extends Var> varsAbove(ForLoop container, Instr i) {
        (if(i === container.initializer) emptyList else #[container.initializer.variable as Var])
            + varsAbove(container.eContainer, container)
    }
    
    static def dispatch Iterable<? extends Var> varsAbove(Executable container, Stmt s) {
        container.allParams
    }
    
    static def dispatch Iterable<? extends Var> varsAbove(Instr container, Instr i) {
        varsAbove(container.eContainer, container)
    }
    
    /*
     * Non-static methods that depend on RolezSystem and IScopeProvider
     */
    
    @Inject RolezSystem system
    @Inject IScopeProvider scopeProvider
    @Inject extension JavaMapper
    
    val superclassesCache = new OnChangeEvictingCache
    
    def strictSuperclasses(Class it) {
        superclassesCache.get(qualifiedName, eResource, [
            val result = new HashSet
            collectSuperclasses(result)
            result
        ])
    }
    
    private def void collectSuperclasses(Class it, Set<QualifiedName> classes) {
        if(classes += qualifiedName)
            superclass?.collectSuperclasses(classes)
    }
    
    def findClass(QualifiedName name, EObject context) {
        scopeProvider.getScope(context.enclosingProgram, SIMPLE_CLASS_REF__CLAZZ)
            .getSingleElement(name)?.EObjectOrProxy?.resolve(context) as Class
    }
    
    def findNormalClass(QualifiedName name, EObject context) {
        scopeProvider.getScope(context.enclosingProgram, GENERIC_CLASS_REF__CLAZZ)
            .getSingleElement(name)?.EObjectOrProxy?.resolve(context) as NormalClass
    }
    
    def isSliceGet(MemberAccess it) {
        isMethodInvoke && method.name == "get" && system.type(target).value.isSliceType
    }
    
    def isSliceSet(MemberAccess it) {
        isMethodInvoke && method.name == "set" && system.type(target).value.isSliceType
    }
    
    def isArrayGet(MemberAccess it) {
        isMethodInvoke && method.name == "get" && system.type(target).value.isArrayType
    }
    
    def isArraySet(MemberAccess it) {
        isMethodInvoke && method.name == "set" && system.type(target).value.isArrayType
    }
    
    def isArrayLength(MemberAccess it) {
        isFieldAccess && field.name == "length" && field.enclosingClass.qualifiedName == arrayClassName
    }
    
    def isVectorGet(MemberAccess it) {
        isMethodInvoke && method.name == "get" && system.type(target).value.isVectorType
    }
    
    def isVectorLength(MemberAccess it) {
        isFieldAccess && field.name == "length" && field.enclosingClass.qualifiedName == vectorClassName
    }
    
    def isVectorBuilderGet(MemberAccess it) {
        isMethodInvoke && method.name == "get" && system.type(target).value.isVectorBuilderType
    }
    
    def isVectorBuilderSet(MemberAccess it) {
        isMethodInvoke && method.name == "set" && system.type(target).value.isVectorBuilderType
    }
    
    /**
     * Returns <code>true</code> iff the given expression is a kind of
     * expression that may have side effects, i.e., an assignment, a non-array
     * object instantiation, a task creation or a method invocation that is not
     * an array-, slice-, vector-, or vector builder get.
     * <p>
     * Note that nested expressions may still have side effects.
     */
    def isSideFxExpr(Expr it) {
        switch(it) {
            Assignment: true
            New: !classRef.clazz.isArrayClass
            MemberAccess: isMethodInvoke && !isSliceGet && !isArrayGet && !isVectorGet && !isVectorBuilderGet || isTaskStart
            default: false
        }
    }
    
    def isGuarded(Type it) {
        it instanceof RoleType && (it as RoleType).base.clazz.isGuarded
    }
    
    def isGuarded(Class it) {
        if(isObjectClass)
            true // Special case: some subclasses are guarded, some are not
        else
            // Otherwise, classes are guarded, except if pure or mapped to a non-guarded JVM class
            !pure && (!isMapped || jvmClass.isSubclassOf(jvmGuardedClassName, it))
    }
    
    def isGuarded(Method it) {
        isMapped && enclosingClass.isGuarded && !jvmMethod.isSafe
    }
}

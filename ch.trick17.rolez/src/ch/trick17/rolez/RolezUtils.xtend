package ch.trick17.rolez

import ch.trick17.rolez.rolez.ArithmeticUnaryExpr
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
import ch.trick17.rolez.rolez.OpArithmeticUnary
import ch.trick17.rolez.rolez.Role
import ch.trick17.rolez.rolez.RoleType
import ch.trick17.rolez.rolez.RolezFactory
import ch.trick17.rolez.rolez.SimpleClassRef
import ch.trick17.rolez.rolez.Slice
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
import ch.trick17.rolez.rolez.Parfor
import ch.trick17.rolez.rolez.ParallelStmt
import ch.trick17.rolez.rolez.OpAssignment
import ch.trick17.rolez.rolez.Ref
import ch.trick17.rolez.rolez.ExprStmt
import ch.trick17.rolez.rolez.WhileLoop
import ch.trick17.rolez.rolez.IfStmt

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
    
    static def newRoleType(Role role, ClassRef base, Slice slice) {
        val result = createRoleType
        result.role = role.copyIfNecessary
        result.base = base.copyIfNecessary
        result.slice = slice
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
        result.rawTypeArg = typeArg.copyIfNecessary
        result
    }
    
    static def newTypeParamRef(TypeParam param, Role restrictingRole) {
        val result = createTypeParamRef
        result.param = param
        result.restrictingRole = restrictingRole?.copyIfNecessary
        return result
    }
    
    private static def <T extends EObject> copyIfNecessary(T it) {
        if(eContainer === null) it else EcoreUtil.copy(it)
    }
    
    /**
     * Returns <code>true</code> if the name and the types of the parameters of the two given
     * methods are the same, ignoring roles and type arguments (which are "erased").
     * For mapped methods, type arguments to classes that map to Java arrays are NOT ignored.
     */
    static def equalErasedSignature(Method it, Method other) {
        name == other.name && equalErasedParams(other, !other.mapped)
    }
    
    static def equalErasedParams(Executable it, Executable other, boolean eraseArrays) {
        val i = other.params.map[type].iterator
        params.size == other.params.size
            && params.map[type].forall[equalErasedType(i.next, eraseArrays)]
    }
    
    private static def dispatch equalErasedType(RoleType it, RoleType other, boolean eraseArrays) {
        base.equalErasedClassRef(other.base, eraseArrays)
    }
    private static def dispatch equalErasedType(Type it, Type other, boolean eraseArrays) {
        EcoreUtil.equals(it, other)
    }
    
    private static def dispatch boolean equalErasedClassRef(GenericClassRef it,
            GenericClassRef other, boolean eraseArrays) {
        val sameName = clazz.qualifiedName == other.clazz.qualifiedName
        if(!eraseArrays && (clazz.isArrayClass || clazz.isVectorClass))
            sameName && typeArg.equalErasedType(other.typeArg, false)
        else
            sameName
    }
    private static def dispatch boolean equalErasedClassRef(ClassRef it, ClassRef other,
            boolean eraseArrays) {
        EcoreUtil.equals(it, other)
    }
    
    static def isValFieldInit(Expr it) {
        val lhs = assignmentLhs
        if(lhs instanceof MemberAccess)
            lhs.isFieldAccess && lhs.target instanceof This && lhs.field.kind == VAL
        else
            false
    }
    
    private static def assignmentLhs(Expr it) { switch(it) {
        Assignment: left
        ArithmeticUnaryExpr case op != OpArithmeticUnary.MINUS: expr
    }}
    
    static def assignedField(Expr it) { (assignmentLhs as MemberAccess).field }
    
    static def assignedVariable(Expr it) { (assignmentLhs as Ref).variable }
    
    static def dispatch Iterable<? extends Var> varsAbove(Block container, Stmt s) {
        container.stmts.takeWhile[it != s].filter(LocalVarDecl).map[variable]
            + varsAbove(container.eContainer, container)
    }
    
    static def dispatch Iterable<? extends Var> varsAbove(ParallelStmt container, Instr i) {
        val vars = (
        	if(i === container.part1) container.params1.map[variable as Var]
        	else if (i === container.part2) container.params2.map[variable as Var]
        	else emptyList
        	)
            + varsAbove(container.eContainer, container)
      	vars
    }
    
    static def dispatch Iterable<? extends Var> varsAbove(ForLoop container, Instr i) {
        (if(i === container.initializer) emptyList else #[container.initializer.variable as Var])
            + varsAbove(container.eContainer, container)
    }
    
    static def dispatch Iterable<? extends Var> varsAbove(Parfor container, Instr i) {
        (if(i === container.initializer) emptyList
        	else if (container.params.contains(i)) #[container.initializer.variable as Var]
        	else #[container.initializer.variable as Var] + container.params.map[variable as Var]
        	)
            + varsAbove(container.eContainer, container)
    }
    
    static def dispatch Iterable<? extends Var> varsAbove(Executable container, Instr i) {
        container.allParams
    }
    
    static def dispatch Iterable<? extends Var> varsAbove(Instr container, Instr i) {
        varsAbove(container.eContainer, container)
    }
    
    static def dispatch Set<String> parallelAssignmentVars(Block b) {
    	val result = new HashSet<String>
    	b.stmts.forEach[s | result.addAll(parallelAssignmentVars(s))]
    	result
    }
    
    static def dispatch Set<String> parallelAssignmentVars(IfStmt i) {
    	val result = parallelAssignmentVars(i.thenPart)
    	if (i.elsePart != null)
        	result.addAll(parallelAssignmentVars(i.elsePart))
        result
    }
    
    static def dispatch Set<String> parallelAssignmentVars(WhileLoop l) {
    	parallelAssignmentVars(l.body)
    }
    
    static def dispatch Set<String> parallelAssignmentVars(ForLoop l) {
    	parallelAssignmentVars(l.body)
    }
    
    static def dispatch Set<String> parallelAssignmentVars(Parfor l) {
    	parallelAssignmentVars(l.body)
    }
    
    static def dispatch Set<String> parallelAssignmentVars(ParallelStmt p) {
    	val result = parallelAssignmentVars(p.part1)
        result.addAll(parallelAssignmentVars(p.part2))
        result
    }
    
    static def dispatch Set<String> parallelAssignmentVars(ExprStmt es) {
    	parallelAssignmentVars(es.expr)
    }
    
    static def dispatch Set<String> parallelAssignmentVars(Assignment a) {
    	if (a.op == OpAssignment.PARALLEL_ASSIGN) {
    		val result = new HashSet<String>()
    		result.add(a.assignedVariable.name)
    		result
    	}
    	else emptySet
    }
    
    static def dispatch Set<String> parallelAssignmentVars(Expr e) {
    	emptySet
    }
    
    static def dispatch Set<String> parallelAssignmentVars(Stmt s) {
    	emptySet
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
            ArithmeticUnaryExpr: op != OpArithmeticUnary.MINUS
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

package ch.trick17.rolez.lang

import ch.trick17.rolez.lang.rolez.Argumented
import ch.trick17.rolez.lang.rolez.Assignment
import ch.trick17.rolez.lang.rolez.Class
import ch.trick17.rolez.lang.rolez.ClassRef
import ch.trick17.rolez.lang.rolez.Constr
import ch.trick17.rolez.lang.rolez.Expr
import ch.trick17.rolez.lang.rolez.MemberAccess
import ch.trick17.rolez.lang.rolez.Method
import ch.trick17.rolez.lang.rolez.New
import ch.trick17.rolez.lang.rolez.NormalClass
import ch.trick17.rolez.lang.rolez.ParameterizedBody
import ch.trick17.rolez.lang.rolez.Role
import ch.trick17.rolez.lang.rolez.RolezFactory
import ch.trick17.rolez.lang.rolez.Start
import ch.trick17.rolez.lang.rolez.Task
import ch.trick17.rolez.lang.rolez.This
import ch.trick17.rolez.lang.rolez.Type
import ch.trick17.rolez.lang.typesystem.RolezSystem
import it.xsemantics.runtime.RuleEnvironment
import it.xsemantics.runtime.RuleEnvironmentEntry
import javax.inject.Inject
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.xtext.scoping.IScopeProvider

import static ch.trick17.rolez.lang.Constants.*
import static ch.trick17.rolez.lang.rolez.Role.*
import static ch.trick17.rolez.lang.rolez.RolezPackage.Literals.*
import static ch.trick17.rolez.lang.rolez.VarKind.VAL

import static extension org.eclipse.emf.ecore.util.EcoreUtil.resolve

/** 
 * Utility functions for Rolez language constructs
 * @author Michael Faes
 */
class RolezUtils {
    
    @Inject extension RolezExtensions
    @Inject RolezSystem system
    @Inject IScopeProvider scopeProvider
    val factory = RolezFactory.eINSTANCE
    
    def newRoleType(Role r, ClassRef base) {
        if(base.eContainer != null)
            throw new IllegalArgumentException("base must not be contained")
        
        val result = factory.createRoleType()
        result.role = r
        result.base = base
        result
    }
    
    def newClassRef(Class c) {
        val result = factory.createSimpleClassRef
        result.clazz = c
        result
    }
    
    def newClassRef(NormalClass c, Type arg) {
        if(arg.eContainer != null)
            throw new IllegalArgumentException("arg must not be contained")
        
        val result = factory.createGenericClassRef
        result.clazz = c
        result.typeArg = arg
        result
    }

    def newIntType()     { factory.createInt     }
    def newDoubleType()  { factory.createDouble  }
    def newBooleanType() { factory.createBoolean }
    def newCharType()    { factory.createChar    }
    def newVoidType()    { factory.createVoid    }
    def newNullType()    { factory.createNull    }
    
    def envFor(EObject o) {
        val body = o.enclosingBody
        switch(body) {
            case null: new RuleEnvironment
            Task: new RuleEnvironment
            Method: {
                val thisType = newRoleType(body.thisRole, newClassRef(body.enclosingClass))
                new RuleEnvironment(new RuleEnvironmentEntry("this", thisType))
            }
            Constr: {
                val thisType = newRoleType(READWRITE, newClassRef(body.enclosingClass))
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
     * the two given methods are the same. Note that the "this role" is ignored,
     * just as is the containing class.
     */
    def equalSignature(Method left, Method right) {
        left.name == right.name && equalParams(left, right)
    }
    
    def equalParams(ParameterizedBody left, ParameterizedBody right) {
        val i = right.params.map[type].iterator
        left.params.size == right.params.size
            && left.params.map[type].forall[
                EcoreUtil.equals(it, i.next)
            ]
    }
    
    /**
     * Finds the maximally specific methods/constructors for the given argument
     * list, following
     * <a href="http://docs.oracle.com/javase/specs/jls/se8/html/jls-15.html#jls-15.12.2">
     * http://docs.oracle.com/javase/specs/jls/se8/html/jls-15.html#jls-15.12.2
     * </a>.
     */
    def maximallySpecific(Iterable<? extends ParameterizedBody> candidates,
            Argumented args) {
        val applicable = candidates.filter[
            system.validArgsSucceeded(envFor(args), args, it)
        ].toList
        
        applicable.filter[p |
            applicable.forall[
                p == it || !it.strictlyMoreSpecificThan(p)
            ]
        ]
    }
    
    private def strictlyMoreSpecificThan(ParameterizedBody target, ParameterizedBody other) {
        target.moreSpecificThan(other) && !other.moreSpecificThan(target)
    }
    
    private def moreSpecificThan(ParameterizedBody target, ParameterizedBody other) {
        // Assume both targets have the same number of parameters
        val i = other.params.iterator
        target.params.forall[system.subtypeSucceeded(envFor(target), it.type, i.next.type)]
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
            New: classRef.clazz.qualifiedName != arrayClassName
            Start: true
            MemberAccess: isMethodInvoke && !method.isArrayGet
            default: false
        }
    }
    
    def isValFieldInit(Assignment it) {
        !(#[left].filter(MemberAccess).filter[isFieldAccess && target instanceof This]
            .map[field].filter[kind == VAL].isEmpty)
    }
    
    def assignedField(Assignment it) { (left as MemberAccess).field }
}

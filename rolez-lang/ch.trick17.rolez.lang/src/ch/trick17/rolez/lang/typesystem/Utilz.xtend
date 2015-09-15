package ch.trick17.rolez.lang.typesystem

import ch.trick17.rolez.lang.rolez.Argumented
import ch.trick17.rolez.lang.rolez.Class
import ch.trick17.rolez.lang.rolez.ClassRef
import ch.trick17.rolez.lang.rolez.Constructor
import ch.trick17.rolez.lang.rolez.Field
import ch.trick17.rolez.lang.rolez.LocalVar
import ch.trick17.rolez.lang.rolez.Member
import ch.trick17.rolez.lang.rolez.Method
import ch.trick17.rolez.lang.rolez.MethodSelector
import ch.trick17.rolez.lang.rolez.ParameterizedBody
import ch.trick17.rolez.lang.rolez.Program
import ch.trick17.rolez.lang.rolez.Role
import ch.trick17.rolez.lang.rolez.RolezFactory
import ch.trick17.rolez.lang.rolez.Stmt
import ch.trick17.rolez.lang.rolez.Type
import it.xsemantics.runtime.RuleEnvironment
import it.xsemantics.runtime.RuleEnvironmentEntry
import javax.inject.Inject
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EReference
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.xtext.naming.IQualifiedNameProvider
import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.xtext.nodemodel.util.NodeModelUtils
import org.eclipse.xtext.scoping.IScopeProvider

import static ch.trick17.rolez.lang.rolez.RolezPackage.Literals.*
import static java.util.Arrays.asList

import static extension org.eclipse.emf.ecore.util.EcoreUtil.copy

/** 
 * Utility functions for Rolez language constructs
 * @author Michael Faes
 */
class Utilz {
    
    @Inject private extension IQualifiedNameProvider
    @Inject private RolezSystem system
    @Inject private IScopeProvider scopeProv
    private val factory = RolezFactory.eINSTANCE
    
    def roleType(Role r, ClassRef base) {
        val result = factory.createRoleType()
        result.setRole(r)
        result.setBase(base.copy) // Copy, because "base" is "contained" in the type
        result
    }
    
    def classRef(Class c) {
        val result = factory.createSimpleClassRef
        result.clazz = c
        result
    }
    
    def classRef(Class c, Type arg) {
        val result = factory.createGenericClassRef
        result.clazz = c
        result.typeArg = arg.copy
        result
    }

    def intType() {
        factory.createInt
    }

    def doubleType() {
        factory.createDouble
    }

    def booleanType() {
        factory.createBoolean
    }

    def charType() {
        factory.createChar
    }

    def unitType() {
        factory.createUnit
    }

    def nullType() {
        factory.createNull
    }

    def objectClassName() {
        QualifiedName.create("rolez", "lang", "Object")
    }

    def stringClassName() {
        QualifiedName.create("rolez", "lang", "String")
    }

    def arrayClassName() {
        QualifiedName.create("rolez", "lang", "Array")
    }

    def taskClassName() {
        QualifiedName.create("rolez", "lang", "Task")
    }
    
    def envFor(EObject o) {
        val method = o.enclosingMethod
        if(method == null)
            new RuleEnvironment
        else {
            val thisType = roleType(method.thisRole, classRef(method.enclosingClass))
            new RuleEnvironment(new RuleEnvironmentEntry("this", thisType))
        }
    }
    
    def Iterable<Class> classes(Program it) {
        elements.filter(Class)
    }
    
    def Iterable<Method> methods(Class it) {
        members.filter(Method)
    }
    
    def Iterable<Field> fields(Class it) {
        members.filter(Field)
    }
    
    def qualifiedName(Class it) {
        fullyQualifiedName
    }
    
    def simpleName(Class it) {
        fullyQualifiedName.lastSegment
    }
    
    def Iterable<Constructor> allConstructors(Class it) {
        if(!constructors.isEmpty) constructors
        else asList(factory.createConstructor)
    }
    
    def Iterable<Member> allMembers(Class it) {
        members + if(actualSuperclass != null) actualSuperclass.allMembers else emptyList
    }
    
    def actualSuperclass(Class it) {
        val objectClass = findClass(objectClassName, it)
        if(it == objectClass)
            null
        else if(superclass == null)
            objectClass
        else
            superclass
    }
    
    def findClass(QualifiedName name, EObject context) {
        scopeProv.getScope(context, CLASS__SUPERCLASS)
            .getSingleElement(name)?.EObjectOrProxy as Class
    }
    
    def variables(ParameterizedBody it) {
        body.eAllContents.filter(LocalVar).toList + params
    }
    
    def Stmt enclosingStmt(EObject it) {
        val container = it?.eContainer
        switch(container) {
            Stmt: container
            default: container?.enclosingStmt
        }
    }
    
    def Method enclosingMethod(EObject it) {
        val container = it?.eContainer
        switch(container) {
            Method: container
            default: container?.enclosingMethod
        }
    }
    
    def Class enclosingClass(EObject it) {
        val container = it?.eContainer
        switch(container) {
            Class: container
            default: container?.enclosingClass
        }
    }
    
    def Program enclosingProgram(EObject it) {
        val container = it?.eContainer
        switch(container) {
            Program: container
            default: container?.enclosingProgram
        }
    }
    
    def ParameterizedBody enclosingBody(EObject it) {
        val container = it?.eContainer
        switch(container) {
            ParameterizedBody: container
            default: container?.enclosingBody
        }
    }
    
    /**
     * Returns <code>true</code> if the name and the types of the parameters of
     * the two given methods are the same. Note that the "this role" is ignored,
     * just as is the containing class.
     */
    def equalSignature(Method left, Method right) {
        val i = right.params.map[type].iterator
        left.name.equals(right.name)
            && left.params.size == right.params.size
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
    def maximallySpecific(Iterable<? extends ParameterizedBody> candidates, Argumented args) {
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
    
    def methodName(MethodSelector it) {
        refText(it, METHOD_SELECTOR__METHOD, 0)
    }
    
    def refText(EObject o, EReference ref, int index) {
        NodeModelUtils.findNodesForFeature(o, ref).get(index).text
    }
}

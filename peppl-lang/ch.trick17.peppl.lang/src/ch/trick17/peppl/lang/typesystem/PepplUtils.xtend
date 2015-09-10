package ch.trick17.peppl.lang.typesystem

import ch.trick17.peppl.lang.peppl.Boolean
import ch.trick17.peppl.lang.peppl.Char
import ch.trick17.peppl.lang.peppl.Class
import ch.trick17.peppl.lang.peppl.ClassRef
import ch.trick17.peppl.lang.peppl.Constructor
import ch.trick17.peppl.lang.peppl.Field
import ch.trick17.peppl.lang.peppl.GenericClassRef
import ch.trick17.peppl.lang.peppl.Int
import ch.trick17.peppl.lang.peppl.LocalVar
import ch.trick17.peppl.lang.peppl.Member
import ch.trick17.peppl.lang.peppl.Method
import ch.trick17.peppl.lang.peppl.MethodSelector
import ch.trick17.peppl.lang.peppl.Null
import ch.trick17.peppl.lang.peppl.ParameterizedBody
import ch.trick17.peppl.lang.peppl.PepplFactory
import ch.trick17.peppl.lang.peppl.Program
import ch.trick17.peppl.lang.peppl.Role
import ch.trick17.peppl.lang.peppl.RoleType
import ch.trick17.peppl.lang.peppl.SimpleClassRef
import ch.trick17.peppl.lang.peppl.Stmt
import ch.trick17.peppl.lang.peppl.Type
import ch.trick17.peppl.lang.peppl.Var
import ch.trick17.peppl.lang.peppl.Void
import it.xsemantics.runtime.RuleEnvironment
import it.xsemantics.runtime.RuleEnvironmentEntry
import java.util.ArrayList
import java.util.HashSet
import java.util.List
import java.util.Set
import javax.inject.Inject
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EReference
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.xtext.nodemodel.util.NodeModelUtils
import org.eclipse.xtext.scoping.IGlobalScopeProvider

import static ch.trick17.peppl.lang.peppl.PepplPackage.Literals.*

import static extension org.eclipse.emf.ecore.util.EcoreUtil.copy
import ch.trick17.peppl.lang.peppl.Argumented

/** 
 * Utility functions for PEPPL language constructs
 * @author Michael Faes
 */
class PepplUtils {
    
    @Inject private PepplSystem system
    @Inject private IGlobalScopeProvider globalScopeProv
    private val factory = PepplFactory.eINSTANCE
    
    def RoleType roleType(Role r, ClassRef base) {
        val result = factory.createRoleType()
        result.setRole(r)
        result.setBase(base.copy) // TODO: So... when is copy necessary?..
        result
    }
    
    def SimpleClassRef classRef(Class c) {
        val result = factory.createSimpleClassRef
        result.clazz = c
        result
    }
    
    def GenericClassRef classRef(Class c, Type arg) {
        val result = factory.createGenericClassRef
        result.clazz = c
        result.typeArg = arg.copy
        result
    }

    def Int intType() {
        factory.createInt
    }

    def Boolean booleanType() {
        factory.createBoolean
    }

    def Char charType() {
        factory.createChar
    }

    def Void voidType() {
        factory.createVoid
    }

    def Null nullType() {
        factory.createNull
    }

    def QualifiedName objectClassName() {
        QualifiedName.create("Object")
    }

    def QualifiedName stringClassName() {
        QualifiedName.create("String")
    }

    def QualifiedName arrayClassName() {
        QualifiedName.create("Array")
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
    
    def Iterable<Class> classes(Program p) {
        p.elements.filter(Class)
    }
    
    def Iterable<Method> methods(Class c) {
        c.members.filter(Method)
    }
    
    def Iterable<Field> fields(Class c) {
        c.members.filter(Field)
    }
    
    def Set<Constructor> allConstructors(Class c) {
        val result = new HashSet(c.constructors)
        if(result.isEmpty)
            result.add(factory.createConstructor)
        result
    }
    
    def List<Member> allMembers(Class c) {
        val result = new ArrayList(c.members)
        if(c.actualSuperclass != null)
            result.addAll(c.actualSuperclass.allMembers)
        result
    }
    
    def actualSuperclass(Class c) {
        val objectClass = findClass(objectClassName, c)
        if(c == objectClass)
            null
        else if(c.superclass == null)
            objectClass
        else
            c.superclass
    }

    def findClass(QualifiedName name, EObject context) {
        globalScopeProv.getScope(context.eResource, CLASS__SUPERCLASS, [true])
            .getSingleElement(name)?.EObjectOrProxy as Class
        // FIXME: This still doesn't seem to work right! Reproduce in a test and fix!
    }
    
    def Iterable<Var> variables(ParameterizedBody b) {
        b.body.eAllContents.filter(LocalVar).toList + b.params
    }
    
    def Stmt enclosingStmt(EObject o) {
        val container = o?.eContainer
        switch(container) {
            Stmt: container
            default: container?.enclosingStmt
        }
    }
    
    def Method enclosingMethod(EObject o) {
        val container = o?.eContainer
        switch(container) {
            Method: container
            default: container?.enclosingMethod
        }
    }
    
    def Class enclosingClass(EObject o) {
        val container = o?.eContainer
        switch(container) {
            Class: container
            default: container?.enclosingClass
        }
    }
    
    def Program enclosingProgram(EObject o) {
        val container = o?.eContainer
        switch(container) {
            Program: container
            default: container?.enclosingProgram
        }
    }
    
    def ParameterizedBody enclosingBody(EObject o) {
        val container = o?.eContainer
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
    
    def methodName(MethodSelector s) {
        val result = refText(s, METHOD_SELECTOR__METHOD, 0)
        result
    }
    
    def refText(EObject o, EReference ref, int index) {
        NodeModelUtils.findNodesForFeature(o, ref).get(index).text
    }
}

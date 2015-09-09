package ch.trick17.peppl.lang.typesystem

import ch.trick17.peppl.lang.peppl.Boolean
import ch.trick17.peppl.lang.peppl.Char
import ch.trick17.peppl.lang.peppl.Class
import ch.trick17.peppl.lang.peppl.ClassRef
import ch.trick17.peppl.lang.peppl.Constructor
import ch.trick17.peppl.lang.peppl.ElemWithBody
import ch.trick17.peppl.lang.peppl.Field
import ch.trick17.peppl.lang.peppl.GenericClassRef
import ch.trick17.peppl.lang.peppl.Int
import ch.trick17.peppl.lang.peppl.LocalVar
import ch.trick17.peppl.lang.peppl.Main
import ch.trick17.peppl.lang.peppl.Member
import ch.trick17.peppl.lang.peppl.Method
import ch.trick17.peppl.lang.peppl.MethodSelector
import ch.trick17.peppl.lang.peppl.Null
import ch.trick17.peppl.lang.peppl.Parameterized
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

import static ch.trick17.peppl.lang.peppl.PepplPackage.Literals.*

import static extension org.eclipse.emf.ecore.util.EcoreUtil.copy

/** 
 * Utility functions for PEPPL language constructs
 * @author Michael Faes
 */
class PepplUtils {
    
    @Inject private extension PepplSystem
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
    
    def Main main(Program p) {
        p.elements.filter(Main).head
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
    
    def Iterable<Var> variables(ElemWithBody b) {
        b.body.eAllContents.filter(LocalVar).toList
            + if(b instanceof Parameterized) b.params else emptyList
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
    
    def ElemWithBody enclosingElemWithBody(EObject o) {
        val container = o?.eContainer
        switch(container) {
            ElemWithBody: container
            default: container?.enclosingElemWithBody
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
    
    def methodName(MethodSelector s) {
        val result = refText(s, METHOD_SELECTOR__METHOD, 0)
        result
    }
    
    def refText(EObject o, EReference ref, int index) {
        NodeModelUtils.findNodesForFeature(o, ref).get(index).text
    }
}

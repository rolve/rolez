package ch.trick17.peppl.lang.typesystem

import ch.trick17.peppl.lang.peppl.Boolean
import ch.trick17.peppl.lang.peppl.Char
import ch.trick17.peppl.lang.peppl.Class
import ch.trick17.peppl.lang.peppl.Constructor
import ch.trick17.peppl.lang.peppl.Field
import ch.trick17.peppl.lang.peppl.Int
import ch.trick17.peppl.lang.peppl.LocalVar
import ch.trick17.peppl.lang.peppl.Main
import ch.trick17.peppl.lang.peppl.Member
import ch.trick17.peppl.lang.peppl.Method
import ch.trick17.peppl.lang.peppl.MethodSelector
import ch.trick17.peppl.lang.peppl.Null
import ch.trick17.peppl.lang.peppl.PepplFactory
import ch.trick17.peppl.lang.peppl.PepplPackage
import ch.trick17.peppl.lang.peppl.Program
import ch.trick17.peppl.lang.peppl.Role
import ch.trick17.peppl.lang.peppl.RoleType
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
import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.xtext.nodemodel.util.NodeModelUtils

/** 
 * Utility functions for types
 * @author Michael Faes
 */
class PepplTypeUtils {
    
    @Inject private extension PepplSystem __
    
    def RoleType roleType(Role role, Class base) {
        var result = PepplFactory.eINSTANCE.createRoleType()
        result.setRole(role)
        result.setBase(base)
        result
    }

    def Int intType() {
        PepplFactory.eINSTANCE.createInt
    }

    def Boolean booleanType() {
        PepplFactory.eINSTANCE.createBoolean
    }

    def Char charType() {
        PepplFactory.eINSTANCE.createChar
    }

    def Void voidType() {
        PepplFactory.eINSTANCE.createVoid
    }

    def Null nullType() {
        PepplFactory.eINSTANCE.createNull
    }

    def QualifiedName objectClassName() {
        QualifiedName.create("Object")
    }

    def QualifiedName stringClassName() {
        QualifiedName.create("String")
    }
    
    def envFor(EObject o) {
        val method = o.enclosingMethod
        if(method == null)
            new RuleEnvironment
        else {
            val thisType = roleType(method.thisRole, method.enclosingClass)
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
            result.add(PepplFactory.eINSTANCE.createConstructor)
        result
    }
    
    def Iterable<Var> variables(Method m) {
        val List<Var> vars = new ArrayList(m.params)
        vars.addAll(m.body.eAllContents.filter(LocalVar).toList)
        vars
    }
    
    def List<Member> allMembers(Class c) {
        val result = new ArrayList(c.members)
        if(c.actualSuperclass != null)
            result.addAll(c.actualSuperclass.allMembers)
        result
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
                !equalType(envFor(left), it, i.next).failed
            ]
    }
    
    def methodName(MethodSelector s) {
        val result = refText(s, PepplPackage.Literals.METHOD_SELECTOR__METHOD, 0)
        result
    }
    
    def refText(EObject o, EReference ref, int index) {
        NodeModelUtils.findNodesForFeature(o, ref).get(index).text
    }
}

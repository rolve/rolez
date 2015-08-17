package ch.trick17.peppl.lang.typesystem

import ch.trick17.peppl.lang.peppl.Boolean
import ch.trick17.peppl.lang.peppl.Char
import ch.trick17.peppl.lang.peppl.Class
import ch.trick17.peppl.lang.peppl.Constructor
import ch.trick17.peppl.lang.peppl.Field
import ch.trick17.peppl.lang.peppl.Int
import ch.trick17.peppl.lang.peppl.LocalVariable
import ch.trick17.peppl.lang.peppl.Main
import ch.trick17.peppl.lang.peppl.Member
import ch.trick17.peppl.lang.peppl.Method
import ch.trick17.peppl.lang.peppl.Null
import ch.trick17.peppl.lang.peppl.PepplFactory
import ch.trick17.peppl.lang.peppl.Program
import ch.trick17.peppl.lang.peppl.Role
import ch.trick17.peppl.lang.peppl.RoleType
import ch.trick17.peppl.lang.peppl.Variable
import ch.trick17.peppl.lang.peppl.Void
import it.xsemantics.runtime.RuleEnvironment
import it.xsemantics.runtime.RuleEnvironmentEntry
import java.util.ArrayList
import java.util.HashSet
import java.util.List
import java.util.Set
import javax.inject.Inject
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.naming.QualifiedName

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
    
    def envFor(EObject e) {
        val method = e.enclosingMethod
        if(method == null)
            new RuleEnvironment
        else {
            val thisType = roleType(method.thisRole, method.enclosingClass)
            new RuleEnvironment(new RuleEnvironmentEntry("this", thisType))
        }
    }
    
    def Main main(Program program) {
        program.elements.filter(Main).head
    }
    
    def Iterable<Class> classes(Program program) {
        program.elements.filter(Class)
    }
    
    def Iterable<Method> methods(Class clazz) {
        clazz.members.filter(Method)
    }
    
    def Iterable<Field> fields(Class clazz) {
        clazz.members.filter(Field)
    }
    
    def Set<Constructor> allConstructors(Class clazz) {
        val result = new HashSet(clazz.constructors)
        if(result.isEmpty)
            result.add(PepplFactory.eINSTANCE.createConstructor)
        result
    }
    
    def Iterable<Variable> variables(Method m) {
        val List<Variable> vars = new ArrayList(m.params)
        vars.addAll(m.body.eAllContents.filter(LocalVariable).toList)
        vars
    }
    
    def List<Member> allMembers(Class clazz) {
        if(clazz.actualSuperclass == null)
            emptyList
        else {
            val result = new ArrayList(clazz.members)
            result.addAll(clazz.actualSuperclass.allMembers)
            result
        }
    }
    
    def equalSignature(Method left, Method right) {
        val i = right.params.map[type].iterator
        left.name.equals(right.name)
            && left.thisRole.equals(right.thisRole)
            && left.params.size == right.params.size
            && left.params.map[type].forall[
                !equalType(envFor(left), it, i.next).failed
            ]
    }
}

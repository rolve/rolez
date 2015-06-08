package ch.trick17.peppl.lang.typesystem

import ch.trick17.peppl.lang.peppl.Boolean
import ch.trick17.peppl.lang.peppl.Char
import ch.trick17.peppl.lang.peppl.Class
import ch.trick17.peppl.lang.peppl.Int
import ch.trick17.peppl.lang.peppl.Null
import ch.trick17.peppl.lang.peppl.PepplFactory
import ch.trick17.peppl.lang.peppl.Role
import ch.trick17.peppl.lang.peppl.RoleType
import ch.trick17.peppl.lang.peppl.Void
import it.xsemantics.runtime.RuleEnvironment
import it.xsemantics.runtime.RuleEnvironmentEntry
import javax.inject.Inject
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.naming.QualifiedName
import java.util.List
import ch.trick17.peppl.lang.peppl.Member
import java.util.ArrayList
import java.util.Set
import ch.trick17.peppl.lang.peppl.Constructor
import java.util.HashSet

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
        val enclosingClass = e.enclosingClass
        if(enclosingClass == null)
            new RuleEnvironment
        else {
            val thisType = roleType(Role.READWRITE, enclosingClass)
            new RuleEnvironment(new RuleEnvironmentEntry("this", thisType))
        }
    }
    
    def Set<Constructor> allConstructors(Class clazz) {
        val result = new HashSet(clazz.constructors)
        if(result.isEmpty)
            result.add(PepplFactory.eINSTANCE.createConstructor)
        result
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
}

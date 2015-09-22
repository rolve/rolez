package ch.trick17.rolez.lang.typesystem

import ch.trick17.rolez.lang.RolezExtensions
import ch.trick17.rolez.lang.rolez.Argumented
import ch.trick17.rolez.lang.rolez.Class
import ch.trick17.rolez.lang.rolez.ClassRef
import ch.trick17.rolez.lang.rolez.Constructor
import ch.trick17.rolez.lang.rolez.Method
import ch.trick17.rolez.lang.rolez.MethodSelector
import ch.trick17.rolez.lang.rolez.ParameterizedBody
import ch.trick17.rolez.lang.rolez.Role
import ch.trick17.rolez.lang.rolez.RolezFactory
import ch.trick17.rolez.lang.rolez.Task
import ch.trick17.rolez.lang.rolez.Type
import it.xsemantics.runtime.RuleEnvironment
import it.xsemantics.runtime.RuleEnvironmentEntry
import javax.inject.Inject
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.xtext.nodemodel.util.NodeModelUtils
import org.eclipse.xtext.scoping.IScopeProvider

import static ch.trick17.rolez.lang.rolez.Role.*
import static ch.trick17.rolez.lang.rolez.RolezPackage.Literals.*

/** 
 * Utility functions for Rolez language constructs
 * @author Michael Faes
 */
class RolezUtils {
    
    @Inject private extension RolezExtensions
    @Inject private RolezSystem system
    @Inject private IScopeProvider scopeProvider
    private val factory = RolezFactory.eINSTANCE
    
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
    
    def newClassRef(Class c, Type arg) {
        if(arg.eContainer != null)
            throw new IllegalArgumentException("arg must not be contained")
        
        val result = factory.createGenericClassRef
        result.clazz = c
        result.typeArg = arg
        result
    }

    def newIntType()     { factory.createInt }
    def newDoubleType()  { factory.createDouble }
    def newBooleanType() { factory.createBoolean }
    def newCharType()    { factory.createChar }
    def newUnitType()    { factory.createUnit }
    def newNullType()    { factory.createNull }
    
    def envFor(EObject o) {
        val body = o.enclosingBody
        switch(body) {
            case null: new RuleEnvironment
            Task: new RuleEnvironment
            Method: {
                val thisType = newRoleType(body.thisRole, newClassRef(body.enclosingClass))
                new RuleEnvironment(new RuleEnvironmentEntry("this", thisType))
            }
            Constructor: {
                val thisType = newRoleType(READWRITE, newClassRef(body.enclosingClass))
                new RuleEnvironment(new RuleEnvironmentEntry("this", thisType))
            }
        }
    }
    
    def findClass(QualifiedName name, EObject context) {
        scopeProvider.getScope(context, CLASS__SUPERCLASS)
            .getSingleElement(name)?.EObjectOrProxy as Class
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
        NodeModelUtils.findNodesForFeature(it, METHOD_SELECTOR__METHOD).get(0).text
    }
}

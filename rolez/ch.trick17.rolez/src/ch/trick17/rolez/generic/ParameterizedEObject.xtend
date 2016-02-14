package ch.trick17.rolez.generic

import ch.trick17.rolez.RolezResource
import ch.trick17.rolez.rolez.BuiltInRole
import ch.trick17.rolez.rolez.ClassRef
import ch.trick17.rolez.rolez.Executable
import ch.trick17.rolez.rolez.GenericClassRef
import ch.trick17.rolez.rolez.Null
import ch.trick17.rolez.rolez.PrimitiveType
import ch.trick17.rolez.rolez.Role
import ch.trick17.rolez.rolez.RoleParam
import ch.trick17.rolez.rolez.RoleParamRef
import ch.trick17.rolez.rolez.RoleType
import ch.trick17.rolez.rolez.SimpleClassRef
import ch.trick17.rolez.rolez.Type
import ch.trick17.rolez.rolez.TypeParam
import ch.trick17.rolez.rolez.TypeParamRef
import java.lang.reflect.InvocationTargetException
import java.util.Map
import org.eclipse.emf.common.notify.Notification
import org.eclipse.emf.common.notify.NotificationChain
import org.eclipse.emf.common.util.EList
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EOperation
import org.eclipse.emf.ecore.EStructuralFeature
import org.eclipse.emf.ecore.InternalEObject
import org.eclipse.emf.ecore.resource.Resource.Internal

abstract class ParameterizedEObject<E extends EObject>
        extends Parameterized implements EObject, InternalEObject {
    
    package val E eObject
    val EObject eContainer
    
    package new(E eObject, EObject eContainer, Map<TypeParam, Type> typeArgs, Map<RoleParam, Role> roleArgs) {
        super(typeArgs, roleArgs)
        if(eObject == null || eContainer == null) throw new NullPointerException
        this.eObject = eObject;
        this.eContainer = eContainer
    }
    
    def genericEObject() { eObject }
    
    /* Helper methods for subclasses */
    
    def Type parameterized(Type it) { switch(it) {
        RoleType             : new ParameterizedRoleType(it, this, typeArgs, roleArgs)
        Null                 : it
        PrimitiveType        : it
        TypeParamRef         : {
            if(!typeArgs.containsKey(param)) throw new AssertionError
            val type = typeArgs.get(param)
            // If the type parameter reference has a restricting role ("with"), return a new
            // role type with the least common superrole
            switch(type) {
                RoleType case restrictingRole != null:
                    utils.newRoleType(system.leastCommonSuperrole(type.role, restrictingRole.parameterized), type.base) 
                default:
                    type
            }
        }
    }}
    
    private def system() { (eResource as RolezResource).rolezSystem  }
    private def utils () { (eResource as RolezResource).rolezUtils }
    
    def parameterized(Role it) { switch(it) {
        BuiltInRole : it
        RoleParamRef: roleArgs.get(param) ?: it
    }}
    
    def ClassRef parameterized(ClassRef it) { switch(it) {
        SimpleClassRef : new ParameterizedSimpleClassRef (it, this, typeArgs, roleArgs)
        GenericClassRef: new ParameterizedGenericClassRef(it, this, typeArgs, roleArgs)
    }}
    
    def parameterizedParams(Executable it) {
        new ParameterizedParamList(params, this as Executable, typeArgs, roleArgs)
    }
    
    /* (Partially) supported eMethods */
    
    override eAdapters()           { eObject.eAdapters }
    override eClass()              { eObject.eClass }
    override eContainer()          { eContainer }
    override eContainingFeature()  { eObject.eContainingFeature }
    override eContainmentFeature() { eObject.eContainmentFeature }
    
    override eGet(EStructuralFeature feature, boolean resolve) {
        if(!resolve) throw new AssertionError
        eGet(feature)
    }
    
    override eIsProxy()                         { eObject.eIsProxy }
    override eIsSet(EStructuralFeature feature) { eObject.eIsSet(feature) }
    override eResource()                        { eObject.eResource }
    
    override eDirectResource()       { (eObject as InternalEObject).eDirectResource }
    override eProxyURI()             { (eObject as InternalEObject).eProxyURI }
    override eInternalContainer()    { (eObject as InternalEObject).eInternalContainer }
    override eInternalResource()     { (eObject as InternalEObject).eInternalResource }
    override eIsSet(int featureID)   { (eObject as InternalEObject).eIsSet(featureID) }
    override eNotificationRequired() { (eObject as InternalEObject).eNotificationRequired }
    
    /* Unsupported eMethods */
    
    override eAllContents()                                    { throw new AssertionError }
    override eContents()                                       { throw new AssertionError }
    override eCrossReferences()                                { throw new AssertionError }
    override eInvoke(EOperation operation, EList<?> arguments) { throw new AssertionError }
    override eSet(EStructuralFeature feature, Object newValue) { throw new AssertionError }
    override eUnset(EStructuralFeature feature)                { throw new AssertionError }
    override eDeliver()                                        { throw new AssertionError }
    override eSetDeliver(boolean deliver)                      { throw new AssertionError }
    override eNotify(Notification notification)                { throw new AssertionError }
    
    override eBaseStructuralFeatureID(int derivedFeatureID, Class<?> baseClass) { throw new AssertionError }
    override eBasicRemoveFromContainer(NotificationChain notifications) { throw new AssertionError }
    override eBasicSetContainer(InternalEObject newContainer, int newContainerFeatureID, NotificationChain notifications) { throw new AssertionError }
    override eContainerFeatureID() { throw new AssertionError }
    override eDerivedOperationID(int baseOperationID, Class<?> baseClass) { throw new AssertionError }
    override eDerivedStructuralFeatureID(int baseFeatureID, Class<?> baseClass) { throw new AssertionError }
    override eGet(EStructuralFeature eFeature, boolean resolve, boolean coreType) { throw new AssertionError }
    override eGet(int featureID, boolean resolve, boolean coreType) { throw new AssertionError }
    override eInverseAdd(InternalEObject otherEnd, int featureID, Class<?> baseClass, NotificationChain notifications) { throw new AssertionError }
    override eInverseRemove(InternalEObject otherEnd, int featureID, Class<?> baseClass, NotificationChain notifications) { throw new AssertionError }
    override eInvoke(int operationID, EList<?> arguments) throws InvocationTargetException{ throw new AssertionError }
    override eObjectForURIFragmentSegment(String uriFragmentSegment) { throw new AssertionError }
    override eResolveProxy(InternalEObject proxy) { throw new AssertionError }
    override eSet(int featureID, Object newValue) { throw new AssertionError }
    override eSetClass(EClass eClass) { throw new AssertionError }
    override eSetProxyURI(URI uri) { throw new AssertionError }
    override eSetResource(Internal resource, NotificationChain notifications) { throw new AssertionError }
    override eSetStore(EStore store) { throw new AssertionError }
    override eSetting(EStructuralFeature feature) { throw new AssertionError }
    override eStore() { throw new AssertionError }
    override eURIFragmentSegment(EStructuralFeature eFeature, EObject eObject) { throw new AssertionError }
    override eUnset(int featureID) { throw new AssertionError }
    
    /* equals and hashCode, to be safe */
    
    override equals(Object other) {
        if(this === other)
            true
        else if(other instanceof ParameterizedEObject<?>)
            super.equals(other) && eObject == other.eObject
        else
            false
    }
    
    override hashCode() {
        super.hashCode + eObject.hashCode
    }
}
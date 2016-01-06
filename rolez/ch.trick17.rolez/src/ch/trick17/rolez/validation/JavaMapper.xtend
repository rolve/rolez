package ch.trick17.rolez.validation

import ch.trick17.rolez.RolezExtensions
import ch.trick17.rolez.rolez.Boolean
import ch.trick17.rolez.rolez.Char
import ch.trick17.rolez.rolez.Constr
import ch.trick17.rolez.rolez.Double
import ch.trick17.rolez.rolez.GenericClassRef
import ch.trick17.rolez.rolez.Int
import ch.trick17.rolez.rolez.Method
import ch.trick17.rolez.rolez.Null
import ch.trick17.rolez.rolez.PrimitiveType
import ch.trick17.rolez.rolez.RoleType
import ch.trick17.rolez.rolez.TypeParamRef
import ch.trick17.rolez.rolez.Void
import javax.inject.Inject
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.common.types.JvmExecutable
import org.eclipse.xtext.common.types.JvmGenericArrayTypeReference
import org.eclipse.xtext.common.types.JvmType
import org.eclipse.xtext.common.types.JvmTypeParameter
import org.eclipse.xtext.common.types.JvmTypeReference
import org.eclipse.xtext.common.types.access.IJvmTypeProvider

class JavaMapper {
    
    @Inject extension RolezExtensions
    @Inject IJvmTypeProvider.Factory jvmTypesFactory
    
    def checkedExceptionTypes(Method it) {
        if(!isMapped) emptyList
        else jvmMethod.checkedExceptionTypes(it)
    }
    
    def checkedExceptionTypes(Constr it) {
        if(!isMapped) emptyList
        else jvmConstr.checkedExceptionTypes(it)
    }
    
    private def checkedExceptionTypes(JvmExecutable it, EObject context) {
        exceptions.map[type].filter(JvmDeclaredType).filter[
            isSubclassOf("java.lang.Exception", context)
                && !isSubclassOf("java.lang.RuntimeException", context)
        ]
    }
    
    def isSubclassOf(JvmDeclaredType it, String otherName, EObject context) {
        val jvmTypes = jvmTypesFactory.findOrCreateTypeProvider(context.eResource.resourceSet)
        isSubclassOf(jvmTypes.findTypeByName(otherName))
    }
    
    def boolean isSubclassOf(JvmDeclaredType it, JvmType other) {
        // IMPROVE: Find an existing method that does this
        if(it == other) true
        else if(extendedClass == null) false
        else (extendedClass.type as JvmDeclaredType).isSubclassOf(other)
    }
    
    def dispatch boolean mapsTo(PrimitiveType it, JvmTypeReference other) {
        name == other.type.qualifiedName
    }
    
    def dispatch boolean mapsTo(RoleType it, JvmTypeReference other) {
        val base = base
        if(base instanceof GenericClassRef)
            other instanceof JvmGenericArrayTypeReference
                && base.typeArg.mapsTo((other as JvmGenericArrayTypeReference).componentType)
        else
            base.clazz.jvmClass.qualifiedName == other.type.qualifiedName
    }
    
    def dispatch boolean mapsTo(TypeParamRef it, JvmTypeReference other) {
        other.type instanceof JvmTypeParameter && other.type.simpleName == param.name
        // TODO: Check declaring class of param?
    }
    
    def dispatch boolean mapsTo(Null it, JvmTypeReference _) { false }
    def dispatch boolean mapsTo(java.lang.Void it, JvmTypeReference _) { false } // To avoid NPEs
    
    def dispatch jvmWrapperTypeName(    Int _) { "java.lang.Integer"   }
    def dispatch jvmWrapperTypeName( Double _) { "java.lang.Double"    }
    def dispatch jvmWrapperTypeName(Boolean _) { "java.lang.Boolean"   }
    def dispatch jvmWrapperTypeName(   Char _) { "java.lang.Character" }
    def dispatch jvmWrapperTypeName(   Void _) { "java.lang.Void"      }
}
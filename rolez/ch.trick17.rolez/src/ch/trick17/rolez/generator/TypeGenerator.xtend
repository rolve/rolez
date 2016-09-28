package ch.trick17.rolez.generator

import ch.trick17.rolez.RolezExtensions
import ch.trick17.rolez.rolez.Class
import ch.trick17.rolez.rolez.ClassRef
import ch.trick17.rolez.rolez.GenericClassRef
import ch.trick17.rolez.rolez.PrimitiveType
import ch.trick17.rolez.rolez.RoleType
import ch.trick17.rolez.rolez.Type
import ch.trick17.rolez.validation.JavaMapper
import javax.inject.Inject

import static ch.trick17.rolez.Constants.*

import static extension java.util.Objects.requireNonNull

class TypeGenerator {
    
    @Inject extension RolezExtensions
    @Inject extension JavaMapper
    @Inject extension SafeJavaNames
    
    def CharSequence generate(Type it) { switch(it) {
        PrimitiveType: name
        RoleType: base.generate
        default: throw new AssertionError // Null or TypeParamRef
    }}
    
    def generateGeneric(Type it) { switch(it) {
        PrimitiveType: jvmWrapperTypeName
        default: generate
    }}
    
    def generateErased(Type it) { switch(it) {
        RoleType: base.generateErased
        default: generate
    }}
    
    def generate(ClassRef it) { switch(it) {
        GenericClassRef case clazz.        isSliceClass: '''«jvmGuardedSliceClassName»<«typeArg.generate»[]>'''
        GenericClassRef case clazz.        isArrayClass: '''«jvmGuardedArrayClassName»<«typeArg.generate»[]>'''
        GenericClassRef case clazz.       isVectorClass: '''«typeArg.generate»[]'''
        GenericClassRef case clazz.isVectorBuilderClass: '''«jvmGuardedVectorBuilderClassName»<«typeArg.generate»[]>'''
        GenericClassRef: '''«clazz.generateName»<«typeArg.generateGeneric»>'''
        default: clazz.generateName
    }}
    
    private def generateErased(ClassRef it) { switch(it) {
        GenericClassRef case clazz.        isSliceClass: jvmGuardedSliceClassName
        GenericClassRef case clazz.        isArrayClass: jvmGuardedArrayClassName
        GenericClassRef case clazz.       isVectorClass: '''«typeArg.generate»[]'''
        GenericClassRef case clazz.isVectorBuilderClass: jvmGuardedVectorBuilderClassName
        default: clazz.generateName
    }}
    
    private def generateName(Class it) {
        if(mapped && !isSingleton)
            jvmClass.getQualifiedName(".").requireNonNull
        else
            safeQualifiedName
    }
    
    def isGuarded(Type it) {
        it instanceof RoleType && (it as RoleType).base.clazz.isGuarded
    }
    
    private def isGuarded(Class it) {
        if(isObjectClass)
            true // Special case: some subclasses are guarded, some are not
        else
            // Otherwise, classes are guarded, except if pure or mapped to a non-guarded JVM class
            !pure && (!isMapped || jvmClass.isSubclassOf(jvmGuardedClassName, it))
    }
}
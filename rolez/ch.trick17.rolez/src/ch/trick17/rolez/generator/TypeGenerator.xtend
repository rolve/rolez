package ch.trick17.rolez.generator

import ch.trick17.rolez.rolez.Class
import ch.trick17.rolez.rolez.ClassRef
import ch.trick17.rolez.rolez.GenericClassRef
import ch.trick17.rolez.rolez.PrimitiveType
import ch.trick17.rolez.rolez.RoleType
import ch.trick17.rolez.rolez.Type
import ch.trick17.rolez.validation.JavaMapper
import javax.inject.Inject

import static ch.trick17.rolez.Constants.*

import static extension ch.trick17.rolez.RolezExtensions.*
import static extension ch.trick17.rolez.generator.SafeJavaNames.*
import static extension java.util.Objects.requireNonNull

class TypeGenerator {
    
    @Inject extension JavaMapper
    
    def CharSequence generate(Type it) { switch(it) {
        PrimitiveType: name
        RoleType: base.generate
        default: throw new AssertionError // Null or TypeParamRef
        // FIXME: val v = null; triggers the above error!
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
}
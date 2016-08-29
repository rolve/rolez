package ch.trick17.rolez.generator

import ch.trick17.rolez.RolezExtensions
import ch.trick17.rolez.generic.ParameterizedMethod
import ch.trick17.rolez.rolez.BuiltInRole
import ch.trick17.rolez.rolez.Constr
import ch.trick17.rolez.rolez.Field
import ch.trick17.rolez.rolez.Method
import ch.trick17.rolez.rolez.NormalClass
import ch.trick17.rolez.rolez.Param
import ch.trick17.rolez.rolez.Pure
import ch.trick17.rolez.rolez.ReadOnly
import ch.trick17.rolez.rolez.ReadWrite
import ch.trick17.rolez.rolez.ReturnNothing
import ch.trick17.rolez.rolez.Role
import ch.trick17.rolez.rolez.RoleParamRef
import ch.trick17.rolez.rolez.RoleType
import ch.trick17.rolez.rolez.SingletonClass
import ch.trick17.rolez.rolez.TypeParamRef
import ch.trick17.rolez.rolez.Void
import ch.trick17.rolez.validation.cfg.CfgProvider
import ch.trick17.rolez.validation.cfg.InstrNode
import javax.inject.Inject

import static ch.trick17.rolez.Constants.*

import static extension ch.trick17.rolez.generator.InstrGenerator.generate

class ClassGenerator {
    
    @Inject extension RolezExtensions
    
    @Inject extension InstrGenerator
    @Inject extension TypeGenerator
    @Inject extension SafeJavaNames
    @Inject extension CfgProvider
    
    // IMPROVE: Use some kind of import manager (note: the Xtext one is incorrect when using the default pkg)
    
    def dispatch generate(NormalClass it) '''
        «IF !safePackage.isEmpty»
        package «safePackage»;
        
        «ENDIF»
        import static «jvmGuardedClassName».*;
        
        public class «safeSimpleName» extends «generateSuperclassName» {
            « fields.map[gen].join»
            «constrs.map[gen].join»
            «methods.map[gen].join»
            «IF !guardedFields.isEmpty»
            
            @java.lang.Override
            protected java.lang.Iterable<?> guardedRefs() {
                return java.util.Arrays.asList(«guardedFields.map[name].join(", ")»);
            }
            «ENDIF»
        }
    '''
    
    private def generateSuperclassName(NormalClass it) {
        if(superclass.isObjectClass && !pure) jvmGuardedClassName
        else superclassRef.generate
    }
    
    private def guardedFields(NormalClass it) {
        allMembers.filter(Field).filter[type.isGuarded]
    }
    
    def dispatch generate(SingletonClass it) '''
        «IF !safePackage.isEmpty»
        package «safePackage»;
        
        «ENDIF»
        import static «jvmGuardedClassName».*;
        
        public final class «safeSimpleName» extends «superclassRef.generate» {
            
            public static final «safeSimpleName» INSTANCE = new «safeSimpleName»();
            
            private «safeSimpleName»() {}
            « fields.map[genObjectField ].join»
            «methods.map[genObjectMethod].join»
        }
    '''
    
    private def gen(Constr it) {
        val roleAnalysis = new RoleAnalysis(body, CodeKind.CONSTR)
        val guardThis = !(roleAnalysis.dynamicThisRoleAtExit instanceof ReadWrite)
        '''
            
            public «enclosingClass.safeSimpleName»(«params.map[gen].join(", ")») {
                «body.generateWithTryCatch(roleAnalysis, CodeKind.CONSTR, guardThis)»
                «IF guardThis»
                finally {
                    guardReadWrite(this);
                }
                «ENDIF»
            }
        '''
    }
    
    // IMPROVE: Support initializer code that may throw checked exceptions
    private def gen(Field it) '''
        
        public «kind.generate»«type.generate» «safeName»«IF initializer != null» = «initializer.generate(new RoleAnalysis(initializer, CodeKind.FIELD_INITIALIZER), CodeKind.FIELD_INITIALIZER)»«ENDIF»;
    '''
    
    private def gen(Method it) '''
        
        «IF isOverriding»
        @java.lang.Override
        «ENDIF»
        public «genReturnType» «safeName»(«params.map[gen].join(", ")») {
            «body.generateWithTryCatch(new RoleAnalysis(body, CodeKind.METHOD), CodeKind.METHOD, false)»
        }
        «IF isTask»
        
        public «taskClassName»<«type.generateGeneric»> $«name»Task(«params.map[gen].join(", ")») {
            return new «taskClassName»<«type.generateGeneric»>(new Object[]{«genTransitionArgs(ReadWrite)»}, new Object[]{«genTransitionArgs(ReadOnly)»}) {
                @java.lang.Override
                protected «type.generateGeneric» runRolez() {
                    «body.generateWithTryCatch(new RoleAnalysis(body, CodeKind.TASK), CodeKind.TASK, false)»
                    «IF needsReturnNull»
                    return null;
                    «ENDIF»
                }
            };
        }
        «ENDIF»
        «IF isMain»
        
        public static void main(final java.lang.String[] args) {
            «jvmTaskSystemClassName».getDefault().run(«genMainInstance».$«name»Task(«IF !params.isEmpty»«jvmGuardedArrayClassName».<java.lang.String[]>wrap(args)«ENDIF»));
        }
        «ENDIF»
    '''
    
    private def genTransitionArgs(Method it, Class<? extends Role> role) {
        (#[thisType -> "this"] + params.filter[type instanceof RoleType].map[type as RoleType -> safeName])
            .filter[key.needsTransition]
            .filter[role.isInstance(key.role.erased)]
            .map[value].join(", ")
    }
    
    private def needsTransition(RoleType it) {
        isGuarded && !(role.erased instanceof Pure)
    }
    
    private def erased(Role it) { switch(it) {
        BuiltInRole: it
        RoleParamRef: param.upperBound
    }}
    
    private def needsReturnNull(Method it) {
        type instanceof Void && body.controlFlowGraph.exit.predecessors
            .filter(InstrNode).exists[!(instr instanceof ReturnNothing)]
    }
    
    private def genMainInstance(Method it) {
        if(enclosingClass.isSingleton)
            '''INSTANCE'''
        else
            '''new «enclosingClass.safeSimpleName»()'''
    }
    
    // TODO: Disable guarding
    
    private def genObjectField(Field it) { if(isMapped) '''
        
        public «kind.generate»«type.generate» «name» = «enclosingClass.jvmClass.qualifiedName».«name»;
    ''' else gen }
    
    private def genObjectMethod(Method it) { if(isMapped) '''
        
        public «genReturnType» «name»(«params.map[gen].join(", ")») {
            «if(!(type instanceof Void)) "return "»«generateStaticCall»;
        }
    ''' else gen }
    
    private def generateStaticCall(Method it)
        '''«enclosingClass.jvmClass.qualifiedName».«name»(«params.map[safeName].join(", ")»)'''
    
    private def gen(Param it) {
        // Use the boxed version of a primitive param type if any of the "overridden"
        // params is generic, i.e., its type is a type parameter reference (e.g., T)
        // IMPROVE: For some methods, it may make sense to generate primitive version too,
        // to enable efficient passing of primitive values
        val genType =
            if(overridesGenericParam)  type.generateGeneric
            else                       type.generate
        '''«kind.generate»«genType» «safeName»'''
    }
    
    private def boolean overridesGenericParam(Param it) {
        val superMethod = enclosingMethod?.superMethod
        val superParam = 
            if(superMethod instanceof ParameterizedMethod)
                superMethod.genericEObject.params.get(paramIndex)
            else
                superMethod?.params?.get(paramIndex)
        
        superParam?.type instanceof TypeParamRef
            || superParam != null && superParam.overridesGenericParam
    }
    
    private def genReturnType(Method it) {
        if(overridesGenericReturnType) type.generateGeneric
        else                           type.generate
    }
    
    private def boolean overridesGenericReturnType(Method it) {
        val superReturnType = 
            if(superMethod instanceof ParameterizedMethod)
                (superMethod as ParameterizedMethod).genericEObject.type
            else
                superMethod?.type
        
        superReturnType instanceof TypeParamRef
            || superMethod != null && superMethod.overridesGenericReturnType
    }
}

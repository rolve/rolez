package ch.trick17.rolez.generator

import ch.trick17.rolez.RolezUtils
import ch.trick17.rolez.generic.ParameterizedMethod
import ch.trick17.rolez.rolez.Constr
import ch.trick17.rolez.rolez.Field
import ch.trick17.rolez.rolez.Instr
import ch.trick17.rolez.rolez.MemberAccess
import ch.trick17.rolez.rolez.Method
import ch.trick17.rolez.rolez.NormalClass
import ch.trick17.rolez.rolez.Param
import ch.trick17.rolez.rolez.Pure
import ch.trick17.rolez.rolez.ReadOnly
import ch.trick17.rolez.rolez.ReadWrite
import ch.trick17.rolez.rolez.ReturnNothing
import ch.trick17.rolez.rolez.Role
import ch.trick17.rolez.rolez.RoleType
import ch.trick17.rolez.rolez.SingletonClass
import ch.trick17.rolez.rolez.TypeParamRef
import ch.trick17.rolez.rolez.Void
import ch.trick17.rolez.validation.JavaMapper
import ch.trick17.rolez.validation.cfg.CfgProvider
import ch.trick17.rolez.validation.cfg.InstrNode
import java.util.ArrayList
import javax.inject.Inject
import org.eclipse.xtext.common.types.JvmArrayType

import static ch.trick17.rolez.Constants.*
import static ch.trick17.rolez.generator.CodeKind.*

import static extension ch.trick17.rolez.RolezExtensions.*
import static extension ch.trick17.rolez.generator.InstrGenerator.generate

class ClassGenerator {
        
    @Inject extension InstrGenerator
    @Inject extension TypeGenerator
    @Inject extension JavaMapper
    @Inject extension SafeJavaNames
    @Inject extension CfgProvider
    @Inject extension RoleAnalysis.Provider
    @Inject extension RolezUtils
    
    // IMPROVE: Use some kind of import manager (note: the Xtext one is incorrect when using the default pkg)
    
    def dispatch generate(NormalClass it) '''
        «IF !safePackage.isEmpty»
        package «safePackage»;
        
        «ENDIF»
        import static «jvmGuardedClassName».*;
        
        public class «safeSimpleName» extends «generateGuardedSuperclassName» {
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
    
    private def generateGuardedSuperclassName(NormalClass it) {
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
        val roleAnalysis = newRoleAnalysis(it, body.controlFlowGraph)
         '''
            
            public «enclosingClass.safeSimpleName»(«params.map[gen].join(", ")») {
                «body.stmts.head.generate(roleAnalysis) /* super constr call */»
                «IF body.startsTasks»
                final «jvmTasksClassName» $tasks = new «jvmTasksClassName»();
                «ENDIF»
                «body.generateWithTryCatch(roleAnalysis, body.startsTasks)»
                «IF body.startsTasks»
                finally {
                    $tasks.joinAll();
                }
                «ENDIF»
            }
        '''
    }
    
    // IMPROVE: Support initializer code that may throw checked exceptions
    private def gen(Field it) '''
        
        public «kind.generate»«type.generate» «safeName»«IF initializer != null» = «initializer.expr.generate(newRoleAnalysis(initializer, initializer.expr.controlFlowGraph))»«ENDIF»;
    '''
    
    private def gen(Method it) '''
        
        «IF isOverriding»
        @java.lang.Override
        «ENDIF»
        public «genReturnType» «safeName»(«genParams») {
            «IF needsTasksJoin»
            final «jvmTasksClassName» $tasks = new «jvmTasksClassName»();
            «ENDIF»
            «body.generateWithTryCatch(newRoleAnalysis(it, body.controlFlowGraph, METHOD), needsTasksJoin)»
            «IF needsTasksJoin»
            finally {
                $tasks.joinAll();
            }
            «ENDIF»
        }
        «IF isTask»
        
        public «taskClassName»<«type.generateGeneric»> $«name»Task(«genParams») {
            return new «taskClassName»<«type.generateGeneric»>(new Object[]{«genTransitionArgs(ReadWrite)»}, new Object[]{«genTransitionArgs(ReadOnly)»}) {
                @java.lang.Override
                protected «type.generateGeneric» runRolez() {
                    «body.generateWithTryCatch(newRoleAnalysis(it, body.controlFlowGraph, TASK), false)»
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
    
    private def genParams(Method it) {
        var allParams = new ArrayList(params.map[gen])
        if(isAsync)
            allParams += "final " + jvmTasksClassName + " $tasks"
        allParams.join(", ")
    }
    
    private def needsTasksJoin(Method it) {
        !isAsync && body.startsTasks
    }
    
    private def startsTasks(Instr it) {
        all(MemberAccess).exists[isTaskStart || isMethodInvoke && method.isAsync]
    }
    
    private def genTransitionArgs(Method it, Class<? extends Role> role) {
        (#[thisParam.type -> "this"] + params.filter[type instanceof RoleType].map[type as RoleType -> safeName])
            .filter[key.needsTransition]
            .filter[role.isInstance(key.role.erased)]
            .map[value].join(", ")
    }
    
    private def needsTransition(RoleType it) {
        isGuarded && !(role.erased instanceof Pure)
    }
    
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
        
        public «genReturnType» «name»(«params.map[genPlain].join(", ")»)«checkedExceptionTypes.join(" throws ", ", ", "", [qualifiedName])» {
            «if(!(type instanceof Void)) "return "»«generateStaticCall»;
        }
    ''' else gen }
    
    private def genPlain(Param it) {
        val paramType = jvmParam.parameterType.type
        if(paramType instanceof JvmArrayType) {
            val arrayType = paramType.toString.substring(14) // IMPROVE: Yes, magic.
            '''«kind.generate»«arrayType» «name»'''
        }
        else
            gen
    }
    
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

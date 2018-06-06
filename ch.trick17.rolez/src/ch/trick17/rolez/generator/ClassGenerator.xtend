package ch.trick17.rolez.generator

import ch.trick17.rolez.RolezUtils
import ch.trick17.rolez.generic.ParameterizedMethod
import ch.trick17.rolez.rolez.Constr
import ch.trick17.rolez.rolez.Executable
import ch.trick17.rolez.rolez.Field
import ch.trick17.rolez.rolez.Instr
import ch.trick17.rolez.rolez.Member
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
import ch.trick17.rolez.rolez.Slice
import ch.trick17.rolez.rolez.TypeParamRef
import ch.trick17.rolez.rolez.Void
import ch.trick17.rolez.validation.JavaMapper
import ch.trick17.rolez.validation.cfg.CfgProvider
import ch.trick17.rolez.validation.cfg.InstrNode
import java.util.ArrayList
import javax.inject.Inject
import org.eclipse.xtext.common.types.JvmArrayType

import static ch.trick17.rolez.Constants.*
import static ch.trick17.rolez.generator.MethodKind.*

import static extension ch.trick17.rolez.RolezExtensions.*
import static extension ch.trick17.rolez.generator.InstrGenerator.generate
import static extension ch.trick17.rolez.generator.SafeJavaNames.*

/**
 * Generates Java code for Rolez classes. Uses {@link InstrGenerator} to generate
 * the method and constructor bodies and the field initializers.
 */
class ClassGenerator {
    
    @Inject extension InstrGenerator
    @Inject extension TypeGenerator
    @Inject extension JavaMapper
    @Inject extension CfgProvider
    @Inject extension RoleAnalysisProvider
    @Inject extension ChildTasksAnalysisProvider
    @Inject extension RolezUtils
    
    // IMPROVE: Use some kind of import manager (note: the Xtext one is incorrect when using the default pkg)
    
    def dispatch generate(NormalClass it) '''
        «IF !safePackage.isEmpty»
        package «safePackage»;
        
        «ENDIF»
        import static «jvmGuardedClassName».*;
        
        public«IF isSliced» final«ENDIF» class «safeSimpleName» extends «generateGuardedSuperclassName»«IF isSliced» implements «slices.map[interfaceName].join(", ")»«ENDIF» {
            « fields.map[gen].join»
            «constrs.map[gen].join»
            «methods.map[gen].join»
            «IF !allMembers.guardedFields.isEmpty»
            
            @java.lang.Override
            protected java.util.List<?> guardedRefs() {
                return java.util.Arrays.asList(«allMembers.guardedFields.map[name].join(", ")»);
            }
            «ENDIF»
            «IF isSliced»
            
            @java.lang.Override
            public «safeSimpleName» $object() {
                return this;
            }
            
            private final java.util.Map<java.lang.String, «jvmGuardedClassName»> $slices = new java.util.HashMap<java.lang.String, «jvmGuardedClassName»>();
            
            @java.lang.Override
            protected final java.util.Collection<«jvmGuardedClassName»> views() {
                return $slices.values();
            }
            
            @java.lang.Override
            protected final «safeSimpleName» viewLock() {
                return this;
            }
            «FOR slice : slices»
            
            public final «slice.interfaceName» $«slice.safeName»Slice() {
                ensureGuardingInitialized(rolez.lang.Task.currentTask().idBits());
                synchronized(this) {
                    «slice.implName» slice = («slice.implName») $slices.get("«slice.name»");
                    if(slice == null) {
                        slice = new «slice.implName»(this);
                        $slices.put("«slice.name»", slice);
                    }
                    return slice;
                }
            }
            «ENDFOR»
            «ENDIF»
        }
    '''
    
    def generateSlice(Slice it) '''
        «IF !enclosingClass.safePackage.isEmpty»
        package «enclosingClass.safePackage»;
        
        «ENDIF»
        import static «jvmGuardedClassName».*;
        
        public interface «simpleInterfaceName» {
            
            «enclosingClass.safeName» $object();
            «FOR method : members.filter(Method)»
            «FOR kind : #[GUARDED_METHOD, UNGUARDED_METHOD]»
            «method.genReturnType» «method.safeName»«kind.suffix»(«method.genParamsWithExtra»);
            «ENDFOR»
            «ENDFOR»
            
            final class Impl extends «jvmGuardedClassName» implements «simpleInterfaceName» {
                
                final «enclosingClass.safeName» object;
                
                Impl(final «enclosingClass.safeName» object) {
                    super(true);
                    this.object = object;
                }
                «members.filter(Method).map[gen].join»
                
                @java.lang.Override
                public «enclosingClass.safeName» $object() {
                    return object;
                }
                «IF !members.guardedFields.isEmpty»
                
                @java.lang.Override
                protected final java.util.List<?> guardedRefs() {
                    return java.util.Arrays.asList(«members.guardedFields.map["object." + name].join(", ")»);
                }
                «ENDIF»
                
                @java.lang.Override
                protected final java.util.List<«enclosingClass.safeName»> views() {
                    return java.util.Arrays.asList(object);
                }
                
                @java.lang.Override
                protected final «enclosingClass.safeName» viewLock() {
                    return object;
                }
            }
        }
    '''
    
    // IMPROVE: Don't generate two versions for methods in slices
    
    private def generateGuardedSuperclassName(NormalClass it) {
        if(superclass.isObjectClass && !pure) jvmGuardedClassName
        else superclassRef.generate
    }
    
    private def guardedFields(Iterable<Member> it) {
        filter(Field).filter[type.isGuarded]
    }
    
    private def interfaceName(Slice it) { enclosingClass.safeName + "£" + safeName }
    
    private def implName(Slice it) { interfaceName + ".Impl" }
    
    private def simpleInterfaceName(Slice it) {
        enclosingClass.safeSimpleName + "£" + safeName
    }
    
    def dispatch generate(SingletonClass it) '''
        «IF !safePackage.isEmpty»
        package «safePackage»;
        
        «ENDIF»
        import static «jvmGuardedClassName».*;
        
        public final class «safeSimpleName» extends «superclassRef.generate» {
            
            public static final «safeSimpleName» INSTANCE = new «safeSimpleName»();
            
            «IF superclass.isMapped»
            private «safeSimpleName»() {}
            «ELSE»
            private «safeSimpleName»() {
                super(«taskClassName».currentTask().idBits());
            }
            «ENDIF»
            « fields.map[genObjectField ].join»
            «methods.map[genObjectMethod].join»
        }
    '''
    
    private def gen(Constr it) {
        val roleAnalysis = newRoleAnalysis(it)
        val childTasksAnalysis = newChildTasksAnalysis(it)
        '''
        
        public «enclosingClass.safeSimpleName»(«genParamsWithExtra») {
            «body.stmts.head.generate(roleAnalysis, childTasksAnalysis) /* super constr call */»
            «IF body.startsTasks»
            final «jvmTasksClassName» $tasks = new «jvmTasksClassName»();
            «ENDIF»
            «body.generateWithTryCatch(roleAnalysis, childTasksAnalysis, body.startsTasks)»
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
        
        public «kind.generate»«type.generate» «safeName»«IF initializer !== null» = «initializer.expr.generate(newRoleAnalysis(initializer), newChildTasksAnalysis(initializer))»«ENDIF»;
    '''
    
    private def gen(Method it) {
        val roleAnalysis = newRoleAnalysis(it)
        '''
        «FOR kind : #[GUARDED_METHOD, UNGUARDED_METHOD]»
        
        «IF isOverriding && !superMethod.isMapped»
        @java.lang.Override
        «ENDIF»
        public «genReturnType» «safeName»«kind.suffix»(«genParamsWithExtra») {
            «IF needsTasksJoin»
            final «jvmTasksClassName» $tasks = new «jvmTasksClassName»();
            «ENDIF»
            «body.generateWithTryCatch(kind, roleAnalysis, newChildTasksAnalysis(it, kind), needsTasksJoin)»
            «IF needsTasksJoin»
            finally {
                $tasks.joinAll();
            }
            «ENDIF»
        }
        «ENDFOR»
        «IF isOverriding && superMethod.isMapped»
        
        @java.lang.Override
        public «genReturnType» «safeName»(«params.map[gen].join(", ")») {
            «IF !(type instanceof Void)»return «ENDIF»«genBridgeCall»;
        }
        «ENDIF»
        «IF isTask»
        
        public «taskClassName»<«type.generateGeneric»> «safeName»«TASK.suffix»(«params.map[gen].join(", ")») {
            return new «taskClassName»<«type.generateGeneric»>(new Object[]{«genTransitionArgs(ReadWrite)»}, new Object[]{«genTransitionArgs(ReadOnly)»}) {
                @java.lang.Override
                protected «type.generateGeneric» runRolez() {
                    final long $task = idBits();
                    «body.generateWithTryCatch(TASK, roleAnalysis, newChildTasksAnalysis(it, TASK), false)»
                    «IF needsReturnNull»
                    return null;
                    «ENDIF»
                }
            };
        }
        «ENDIF»
        «IF isMain»
        
        public static void main(final java.lang.String[] args) {
            «taskClassName».registerNewRootTask();
            final long $task = «taskClassName».currentTask().idBits();
            «genMainInstance».«name»«UNGUARDED_METHOD.suffix»(«IF !params.isEmpty»«jvmGuardedArrayClassName».<java.lang.String[]>wrap(args), «ENDIF»$task);
            «taskClassName».unregisterRootTask();
        }
        «ENDIF»
        '''
    }
    
    private def genParamsWithExtra(Executable it) {
        val allParams = new ArrayList(params.map[gen])
        if(it instanceof Method && (it as Method).isAsync)
            allParams += '''final «jvmTasksClassName» $tasks'''
        allParams += '''final long $task'''
        allParams.join(", ")
    }
    
    private def needsTasksJoin(Method it) {
        !isAsync && body.startsTasks
    }
    
    private def startsTasks(Instr it) {
        all(MemberAccess).exists[isTaskStart || isMethodInvoke && method.isAsync]
    }
    
    /**
     * Generated for methods that override a mapped method and can therefore be called from Java
     * code that does not pass the current task. The current task is retrieved from a thread-local
     * variable and the real method is called.
     */
    private def genBridgeCall(Method it) {
        val args = params.map[safeName] + #[taskClassName + ".currentTask().idBits()"]
        '''this.«safeName»(«args.join(", ")»)'''
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
            '''new «enclosingClass.safeSimpleName»($task)'''
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
            || superParam !== null && superParam.overridesGenericParam
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
            || superMethod !== null && superMethod.overridesGenericReturnType
    }
}

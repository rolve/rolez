package ch.trick17.rolez.generator

import ch.trick17.rolez.RolezExtensions
import ch.trick17.rolez.RolezUtils
import ch.trick17.rolez.generic.ParameterizedMethod
import ch.trick17.rolez.rolez.ArithmeticBinaryExpr
import ch.trick17.rolez.rolez.Assignment
import ch.trick17.rolez.rolez.BinaryExpr
import ch.trick17.rolez.rolez.Block
import ch.trick17.rolez.rolez.BooleanLiteral
import ch.trick17.rolez.rolez.Cast
import ch.trick17.rolez.rolez.CharLiteral
import ch.trick17.rolez.rolez.Class
import ch.trick17.rolez.rolez.ClassLike
import ch.trick17.rolez.rolez.ClassRef
import ch.trick17.rolez.rolez.Constr
import ch.trick17.rolez.rolez.DoubleLiteral
import ch.trick17.rolez.rolez.EqualityExpr
import ch.trick17.rolez.rolez.Expr
import ch.trick17.rolez.rolez.ExprStmt
import ch.trick17.rolez.rolez.Field
import ch.trick17.rolez.rolez.GenericClassRef
import ch.trick17.rolez.rolez.IfStmt
import ch.trick17.rolez.rolez.IntLiteral
import ch.trick17.rolez.rolez.LocalVarDecl
import ch.trick17.rolez.rolez.LogicalExpr
import ch.trick17.rolez.rolez.MemberAccess
import ch.trick17.rolez.rolez.Method
import ch.trick17.rolez.rolez.Named
import ch.trick17.rolez.rolez.New
import ch.trick17.rolez.rolez.NormalClass
import ch.trick17.rolez.rolez.Null
import ch.trick17.rolez.rolez.NullLiteral
import ch.trick17.rolez.rolez.Param
import ch.trick17.rolez.rolez.Parenthesized
import ch.trick17.rolez.rolez.PrimitiveType
import ch.trick17.rolez.rolez.Program
import ch.trick17.rolez.rolez.Pure
import ch.trick17.rolez.rolez.ReadOnly
import ch.trick17.rolez.rolez.ReadWrite
import ch.trick17.rolez.rolez.RelationalExpr
import ch.trick17.rolez.rolez.ReturnExpr
import ch.trick17.rolez.rolez.ReturnNothing
import ch.trick17.rolez.rolez.Role
import ch.trick17.rolez.rolez.RoleType
import ch.trick17.rolez.rolez.RolezFactory
import ch.trick17.rolez.rolez.SimpleClassRef
import ch.trick17.rolez.rolez.SingletonClass
import ch.trick17.rolez.rolez.Start
import ch.trick17.rolez.rolez.Stmt
import ch.trick17.rolez.rolez.StringLiteral
import ch.trick17.rolez.rolez.SuperConstrCall
import ch.trick17.rolez.rolez.Task
import ch.trick17.rolez.rolez.TaskRef
import ch.trick17.rolez.rolez.The
import ch.trick17.rolez.rolez.This
import ch.trick17.rolez.rolez.Type
import ch.trick17.rolez.rolez.TypeParamRef
import ch.trick17.rolez.rolez.UnaryExpr
import ch.trick17.rolez.rolez.UnaryMinus
import ch.trick17.rolez.rolez.UnaryNot
import ch.trick17.rolez.rolez.VarKind
import ch.trick17.rolez.rolez.VarRef
import ch.trick17.rolez.rolez.Void
import ch.trick17.rolez.rolez.WhileLoop
import ch.trick17.rolez.typesystem.RolezSystem
import ch.trick17.rolez.validation.JavaMapper
import java.io.File
import javax.inject.Inject
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.common.types.JvmArrayType
import org.eclipse.xtext.generator.AbstractGenerator
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGeneratorContext

import static ch.trick17.rolez.Constants.*
import static ch.trick17.rolez.rolez.VarKind.*

import static extension java.util.Objects.requireNonNull
import static extension org.eclipse.xtext.util.Strings.convertToJavaString

class RolezGenerator extends AbstractGenerator {
    
    @Inject extension RolezExtensions
    @Inject extension RolezFactory
    @Inject extension JavaMapper
    @Inject extension RoleAnalysis
    @Inject RolezUtils utils
    @Inject RolezSystem system
    
    // IMPROVE: Use some kind of import manager (note: the Xtext one is incorrect when using the default pkg)
    
    override void doGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext context) {
        val program = resource.contents.head as Program
        for (e : program.classes.filter[!mapped || isSingleton] + program.tasks) {
            val name = e.qualifiedName.segments.join(File.separator) + ".java"
            fsa.generateFile(name, e.generateElement)
        }
    }
    
    /*
     * Class and members
     */
    
    private def dispatch generateElement(NormalClass it) '''
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
        if(superclass.isObjectClass) jvmGuardedClassName
        else superclassRef.gen
    }
    
    private def guardedFields(NormalClass it) {
        allMembers.filter(Field).filter[type.isGuarded]
    }
    
    private def isGuarded(Type it) {
        it instanceof RoleType && (it as RoleType).base.clazz.isGuarded
    }
    
    private def isGuarded(Class it) {
        it instanceof NormalClass && (!isMapped
            || jvmClass.isSubclassOf(jvmGuardedClassName, it)
            || isObjectClass)
    }
    
    private def dispatch generateElement(SingletonClass it) '''
        «IF !safePackage.isEmpty»
        package «safePackage»;
        
        «ENDIF»
        import static «jvmGuardedClassName».*;
        
        public final class «safeSimpleName» extends «superclassRef.gen» {
            
            public static final «safeSimpleName» INSTANCE = new «safeSimpleName»();
            
            private «safeSimpleName»() {}
            « fields.map[genObjectField ].join»
            «methods.map[genObjectMethod].join»
        }
    '''
    
    private def dispatch generateElement(Task it) '''
        «IF !safePackage.isEmpty»
        package «safePackage»;
        
        «ENDIF»
        import static «jvmGuardedClassName».*;
        
        public final class «safeSimpleName» implements java.util.concurrent.Callable<«type.genGeneric»> {
            «IF isMain»
            
            public static void main(final java.lang.String[] args) {
                «jvmTaskSystemClassName».getDefault().run(new «safeSimpleName»(«IF !params.isEmpty»«jvmGuardedArrayClassName».<java.lang.String[]>wrap(args)«ENDIF»));
            }
            «ENDIF»
            «IF !params.isEmpty»
            
            «FOR p : params»
            private final «p.type.gen» «p.safeName»;
            «ENDFOR»
            
            public «safeSimpleName»(«params.map[gen].join(", ")») {
                «FOR p : params.filter[needsTransition]»
                «p.genTransition»
                «ENDFOR»
                «FOR p : params»
                this.«p.safeName» = «p.safeName»;
                «ENDFOR»
            }
            «ENDIF»
            
            public «type.genGeneric» call() {
                «FOR p : params.filter[needsRegisterNewOwner]»
                «p.genRegisterNewOwner»
                «ENDFOR»
                «body.genStmtsWithTryCatch»
                «FOR p : params.filter[needsTransition]»
                «p.genRelease»
                «ENDFOR»
                «IF type instanceof Void»
                return null;
                «ENDIF»
            }
        }
    '''
    
    private def needsTransition(Param it) {
        type.isGuarded && !((type as RoleType).role instanceof Pure)
    }
    
    private def genTransition(Param it) {
        val kind = if((type as RoleType).role instanceof ReadWrite) "pass" else "share"
        if((type as RoleType).base.clazz.isObjectClass)
            '''
            if(«safeName» instanceof «jvmGuardedClassName»)
                ((«jvmGuardedClassName») «safeName»).pass();
            '''
        else
            '''«safeName».«kind»();'''
    }
    
    private def needsRegisterNewOwner(Param it) {
        type.isGuarded && (type as RoleType).role instanceof ReadWrite
    }
    
    private def genRegisterNewOwner(Param it) {
        if((type as RoleType).base.clazz.isObjectClass)
            '''
            if(«safeName» instanceof «jvmGuardedClassName»)
                ((«jvmGuardedClassName») «safeName»).registerNewOwner();
            '''
        else
            '''«safeName».registerNewOwner();'''
    }
    
    private def genRelease(Param it) {
        val kind = if((type as RoleType).role  instanceof ReadWrite) "Passed" else "Shared"
        if((type as RoleType).base.clazz.isObjectClass)
            '''
            if(«safeName» instanceof «jvmGuardedClassName»)
                ((«jvmGuardedClassName») «safeName»).release«kind»();
            '''
        else
            '''«safeName».release«kind»();'''
    }
    
    private def gen(Field it) '''
        
        public «kind.gen»«type.gen» «safeName»«IF initializer != null» = «initializer.gen»«ENDIF»;
    '''
    
    private def gen(Method it) '''
        
        «IF isOverriding»
        @java.lang.Override
        «ENDIF»
        public «genReturnType» «safeName»(«params.map[gen].join(", ")») {
            «body.genStmtsWithTryCatch»
        }
    '''
    
    private def gen(Constr it) {
        val guardThis = !(dynamicThisRoleAtExit instanceof ReadWrite)
        '''
            
            public «enclosingClass.safeSimpleName»(«params.map[gen].join(", ")») {
                «body.genStmtsWithTryCatch(guardThis)»
                «IF guardThis»
                finally {
                    guardReadWrite(this);
                }
                «ENDIF»
            }
        '''
    }
    
    private def genStmtsWithTryCatch(Block it) { genStmtsWithTryCatch(false) }
    
    private def genStmtsWithTryCatch(Block it, boolean forceTry) {
        val exceptionTypes = thrownExceptionTypes
        val isConstr = !stmts.isEmpty && stmts.head instanceof SuperConstrCall
        if(!exceptionTypes.isEmpty || forceTry) '''
            «IF isConstr»
            «stmts.head.gen»
            «ENDIF»
            try {
                «stmts.drop(if(isConstr) 1 else 0).map[gen].join»
            }
            «IF !exceptionTypes.isEmpty»
            catch(«exceptionTypes.map[qualifiedName].join(" | ")» e) {
                throw new java.lang.RuntimeException("ROLEZ EXCEPTION WRAPPER", e);
            }
            «ENDIF»
        '''
        else '''
            «stmts.map[gen].join»
        '''
    }
    
    private def thrownExceptionTypes(Stmt it) {
        val all = eAllContents.toIterable.map[switch(it) {
            MemberAccess case isMethodInvoke: method.checkedExceptionTypes
            New: constr.checkedExceptionTypes
            default: emptyList
        }].flatten.toSet
        
        all.filter[sub |
            !all.exists[supr | sub !== supr && sub.isSubclassOf(supr)]
        ].toSet
    }
    
    private def genObjectField(Field it) { if(isMapped) '''
        
        public «kind.gen»«type.gen» «name» = «enclosingClass.jvmClass.qualifiedName».«name»;
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
            if(overridesGenericParam)  type.genGeneric
            else                       type.gen
        '''«kind.gen»«genType» «safeName»'''
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
        if(overridesGenericReturnType) type.genGeneric
        else                           type.gen
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
    
    private def gen(VarKind it) { if(it == VAL) "final " }
    
    /*
     * Statements
     */
    
    private def CharSequence gen(Stmt it) { generateStmt }
    
    private def dispatch CharSequence genIndent(Block it) { " " + gen }
    private def dispatch CharSequence genIndent(Stmt it)  { "\n    " + gen }
    
    private def dispatch generateStmt(Block it)'''
        {
            «stmts.map[gen].join»
        }
    '''
    
    private def dispatch generateStmt(LocalVarDecl it) {
        val type = system.varType(utils.createEnv(it), variable).value
        '''
            «variable.kind.gen»«type.gen» «variable.safeName»«IF initializer != null» = «initializer.gen»«ENDIF»;
        '''
    }
    
    private def dispatch generateStmt(IfStmt it) '''
        if(«condition.gen»)«thenPart.genIndent»
        else«elsePart.genIndent»
    '''
    
    private def dispatch generateStmt(WhileLoop it) '''
        while(«condition.gen»)«body.genIndent»
    '''
    
    private def dispatch generateStmt(SuperConstrCall it) '''
        super(«args.map[gen].join(", ")»);
    '''
    
    private def dispatch generateStmt(ReturnNothing _) '''
        return;
    '''
    
    private def dispatch generateStmt(ReturnExpr it) '''
        return «expr.gen»;
    '''
    
    /* Java only allows certain kinds of "expression statements", so find
     * the corresponding expressions in the rolez expression tree */
    private def dispatch generateStmt(ExprStmt it) '''
        «findSideFxExpr(expr).map[gen + ";\n"].join»
    '''
    
    private def Iterable<Expr> findSideFxExpr(Expr it) {
        switch(it) {
            case utils.isSideFxExpr(it): #[it]
            BinaryExpr: findSideFxExpr(left) + findSideFxExpr(right)
            UnaryExpr: findSideFxExpr(expr)
            // Special cases for array instantiations and get
            MemberAccess case isMethodInvoke && method.isArrayGet: {
                if(args.size != 1) throw new AssertionError
                findSideFxExpr(target) + findSideFxExpr(args.get(0))
            }
            New: {
                if(args.size != 1) throw new AssertionError
                findSideFxExpr(args.get(0))
            }
            MemberAccess: findSideFxExpr(target)
            default: emptyList
        }
    }
    
    /*
     * Expressions
     */
    
    private def CharSequence gen(Expr it) { generateExpr }
    
    private def dispatch CharSequence genNested(Assignment it) { gen }
    private def dispatch CharSequence genNested(BinaryExpr it) '''(«gen»)'''
    private def dispatch CharSequence genNested( UnaryExpr it) '''(«gen»)'''
    private def dispatch CharSequence genNested(      Cast it) '''(«gen»)'''
    private def dispatch CharSequence genNested(      Expr it) { gen }
    
    private def dispatch generateExpr(Assignment it)
        '''«left.gen» = «right.gen»'''
    
    private def dispatch generateExpr(BinaryExpr it) {
        val op = switch(it) {
            LogicalExpr: op
            EqualityExpr: op
            RelationalExpr: op
            ArithmeticBinaryExpr: op
        }
        '''«left.genNested» «op» «right.genNested»'''
    }
    
    private def dispatch generateExpr(Cast it)
        '''(«type.gen») «expr.genNested»'''
    
    private def dispatch generateExpr(UnaryMinus it)
        '''-«expr.genNested»'''
    
    private def dispatch generateExpr(UnaryNot it)
        '''!«expr.genNested»'''
    
    private def dispatch generateExpr(MemberAccess it) {
        switch(it) {
            case isMethodInvoke && method.isArrayGet:  generateArrayGet
            case isMethodInvoke && method.isArraySet:  generateArraySet
            case isMethodInvoke:                       generateMethodInvoke
            case isFieldAccess && field.isArrayLength: generateArrayLength
            case isFieldAccess:                        generateFieldAccess
        }
    }
    
    private def generateArrayGet(MemberAccess it) {
        '''«target.genGuarded(createReadOnly)».data[«args.get(0).gen»]'''
    }
    
    private def generateArraySet(MemberAccess it) {
        '''«target.genGuarded(createReadWrite)».data[«args.get(0).gen»] = «args.get(1).gen»'''
    }
    
    private def generateArrayLength(MemberAccess it)
        '''«target.genNested».data.length'''
    
    private def generateMethodInvoke(MemberAccess it) {
        if(method.isMapped) {
            val genInvoke = '''«target.genNested».«method.safeName»(«args.map[genCoerced].join(", ")»)'''
            if(method.jvmMethod.returnType.type instanceof JvmArrayType) {
                val componentType = ((method.type as RoleType).base as GenericClassRef).typeArg
                '''«jvmGuardedArrayClassName».<«componentType.gen»[]>wrap(«genInvoke»)'''
            }
            else
                genInvoke
        }
        else
            '''«target.genNested».«method.safeName»(«args.map[gen].join(", ")»)'''
    }
    
    private def genCoerced(Expr it) {
        val paramType = destParam.jvmParam.parameterType.type
        if(paramType instanceof JvmArrayType)
            '''«jvmGuardedArrayClassName».unwrap(«genNested», «paramType.gen».class)'''
        else
            gen
    }
    
    private def gen(JvmArrayType it) { toString.substring(14) } // IMPROVE: A little less magic, a little more robustness, please
    
    private def generateFieldAccess(MemberAccess it) {
        val requiredRole =
            if(isFieldWrite)           createReadWrite
            else if(field.kind == VAR) createReadOnly
            else                       createPure
        '''«target.genGuarded(requiredRole)».«field.safeName»'''
    }
    
    private def isFieldWrite(MemberAccess it) {
        eContainer instanceof Assignment && it === (eContainer as Assignment).left
    }
    
    private def genGuarded(Expr it, Role requiredRole) {
        val type = system.type(utils.createEnv(it), it).value
        val needsGuard = !system.subroleSucceeded(dynamicRole, requiredRole)
        if(type.isGuarded && needsGuard)
            switch(requiredRole) {
                ReadWrite: "guardReadWrite(" + gen + ")"
                ReadOnly : "guardReadOnly("  + gen + ")"
                Pure     : genNested
            }
        else
            genNested
        
        // IMPROVE: Better syntax (and performance?) when using type system
    }
    
    private def dispatch generateExpr(This _) '''this'''
    
    private def dispatch generateExpr(VarRef it) { variable.safeName }
    
    private def dispatch generateExpr(New it) {
        val genArgs = 
            if(classRef.clazz.isArrayClass) {
                val componentType = (classRef as GenericClassRef).typeArg
                '''new «componentType.genErased»[«args.head.gen»]'''
            }
            else if(constr.isMapped)
                args.map[genCoerced].join(", ")
            else
                args.map[gen].join(", ")
        '''new «classRef.gen»(«genArgs»)'''
    }
    
    private def dispatch generateExpr(The it) '''«classRef.gen».INSTANCE'''
    
    private def dispatch generateExpr(Start it)
        '''«jvmTaskSystemClassName».getDefault().start(new «taskRef.gen»(«args.map[gen].join(", ")»))'''
    
    private def int arrayNesting(GenericClassRef it) {
        if(!clazz.isArrayClass)
            throw new AssertionError
        
        val arg = typeArg
        switch(arg) {
            RoleType case arg.base instanceof GenericClassRef:
                arrayNesting(arg.base as GenericClassRef) + 1
            default:
                1
        }
    }
    
    private def dispatch generateExpr(Parenthesized it) { expr.gen }
    
    private def dispatch generateExpr(    IntLiteral it) { value.toString }
    private def dispatch generateExpr( DoubleLiteral it) { value.toString }
    private def dispatch generateExpr(BooleanLiteral it) { value.toString }
    
    private def dispatch generateExpr(StringLiteral it) {
        "\"" + value.convertToJavaString + "\""
    }
    
    private def dispatch generateExpr(CharLiteral it) {
        "'" + value.toString.convertToJavaString + "'"
    }
    
    private def dispatch generateExpr(NullLiteral _) '''null'''
    
    /*
     * Types and class refs
     */
    
    private def CharSequence gen(Type it) { generateType }
    
    private def dispatch generateType(PrimitiveType it) { string }
    
    private def dispatch generateType(RoleType it) { base.gen }
    
    private def dispatch generateType(Null it) {
        throw new AssertionError("Null type usage not checked")
    }
    
    private def dispatch generateType(TypeParamRef _) {
        // So far, only mapped non-singleton classes can declare a type parameter and no code is
        // generated for these classes
        throw new AssertionError
    }
    
    private def gen(ClassRef it) { generateClassRef }
    
    private def dispatch generateClassRef(SimpleClassRef it) {
        clazz.generateName
    }
    
    private def dispatch generateClassRef(GenericClassRef it) {
        if(clazz.isArrayClass)
            '''«jvmGuardedArrayClassName»<«typeArg.gen»[]>'''
        else
            '''«clazz.generateName»<«typeArg.genGeneric»>'''
    }
    
    private def generateName(Class it) {
        if(mapped && !isSingleton)
            jvmClass.getQualifiedName(".").requireNonNull
        else
            safeQualifiedName
    }
    
    private def gen(TaskRef it) { task.safeQualifiedName }
    
    private def dispatch CharSequence genGeneric(PrimitiveType it) { jvmWrapperTypeName }
    private def dispatch CharSequence genGeneric(         Type it) { generateType }
    
    private def dispatch genErased(RoleType it) { base.generateClassRefErased }
    private def dispatch genErased(    Type it) { generateType }
    
    private def dispatch generateClassRefErased(GenericClassRef it) {
        if(clazz.isArrayClass) jvmGuardedArrayClassName
        else clazz.generateName
    }
    private def dispatch generateClassRefErased(ClassRef it) { generateClassRef }
    
    /*
     * Safe Java names
     */
    
    static val javaKeywords = #{
        "abstract", "assert", "boolean", "break", "byte", "case", "catch", 
        "char", "class", "const", "continue", "default", "do", "double", "else",
        "enum", "extends", "final", "finally", "float", "for", "goto", "if",
        "implements", "import", "instanceof", "int", "interface", "long",
        "native", "new", "package", "private", "protected", "public", "return",
        "short", "static", "strictfp", "super", "switch", "synchronized", "this",
        "throw", "throws", "transient", "try", "void", "volatile", "while"}
    
    private def safeName(Named it) { safe(name) }
    
    private def safeQualifiedName(ClassLike it) {
        qualifiedName.segments.map[safe].join(".")
    }
    
    private def safeSimpleName(ClassLike it) {
        safe(qualifiedName.lastSegment)
    }
    
    private def safePackage(ClassLike it) {
        val segments = qualifiedName.segments
        segments.takeWhile[it != segments.last].map[safe].join(".")
    }
    
    private def safe(String name) {
        if(javaKeywords.contains(name))
            "$" + name
        else
            name
    }
}

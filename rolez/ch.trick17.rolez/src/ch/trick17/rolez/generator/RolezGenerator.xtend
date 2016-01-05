package ch.trick17.rolez.generator

import ch.trick17.rolez.RolezExtensions
import ch.trick17.rolez.RolezUtils
import ch.trick17.rolez.rolez.ArithmeticBinaryExpr
import ch.trick17.rolez.rolez.Assignment
import ch.trick17.rolez.rolez.BinaryExpr
import ch.trick17.rolez.rolez.Block
import ch.trick17.rolez.rolez.Boolean
import ch.trick17.rolez.rolez.BooleanLiteral
import ch.trick17.rolez.rolez.Cast
import ch.trick17.rolez.rolez.Char
import ch.trick17.rolez.rolez.CharLiteral
import ch.trick17.rolez.rolez.Class
import ch.trick17.rolez.rolez.ClassLike
import ch.trick17.rolez.rolez.ClassRef
import ch.trick17.rolez.rolez.Constr
import ch.trick17.rolez.rolez.Double
import ch.trick17.rolez.rolez.DoubleLiteral
import ch.trick17.rolez.rolez.EqualityExpr
import ch.trick17.rolez.rolez.Expr
import ch.trick17.rolez.rolez.ExprStmt
import ch.trick17.rolez.rolez.Field
import ch.trick17.rolez.rolez.GenericClassRef
import ch.trick17.rolez.rolez.IfStmt
import ch.trick17.rolez.rolez.Int
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
import ch.trick17.rolez.rolez.RelationalExpr
import ch.trick17.rolez.rolez.ReturnExpr
import ch.trick17.rolez.rolez.ReturnNothing
import ch.trick17.rolez.rolez.RoleType
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
import org.eclipse.xtext.generator.AbstractGenerator
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGeneratorContext

import static ch.trick17.rolez.rolez.Role.*
import static ch.trick17.rolez.rolez.VarKind.*

import static extension java.util.Objects.requireNonNull
import static extension org.eclipse.xtext.util.Strings.convertToJavaString

class RolezGenerator extends AbstractGenerator {
    
    @Inject extension RolezExtensions
    @Inject extension JavaMapper
    @Inject extension RoleAnalysis
    @Inject RolezUtils utils
    @Inject RolezSystem system
    
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
        import static rolez.lang.Guarded.*;
        
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
        if(superclass.isObjectClass) "rolez.lang.Guarded"
        else superclass.generateName
    }
    
    private def guardedFields(NormalClass it) {
        allMembers.filter(Field).filter[type.isGuarded]
    }
    
    private def isGuarded(Type it) {
        it instanceof RoleType && (it as RoleType).base.clazz.isGuarded
    }
    
    private def isGuarded(Class it) {
        it instanceof NormalClass && (!isMapped
            || jvmClass.isSubclassOf("rolez.lang.Guarded", it)
            || isObjectClass)
    }
    
    private def dispatch generateElement(SingletonClass it) '''
        «IF !safePackage.isEmpty»
        package «safePackage»;
        
        «ENDIF»
        import static rolez.lang.Guarded.*;
        
        public final class «safeSimpleName» extends «superclass.generateName» {
            
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
        import static rolez.lang.Guarded.*;
        
        public final class «safeSimpleName» implements java.util.concurrent.Callable<«type.genGeneric»> {
            «IF isMain»
            
            public static void main(final java.lang.String[] args) {
                rolez.lang.TaskSystem.getDefault().run(new «safeSimpleName»(«IF !params.isEmpty»new rolez.lang.ObjectArray<>(args)«ENDIF»));
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
        type.isGuarded && (type as RoleType).role != PURE
    }
    
    private def genTransition(Param it) {
        val kind = if((type as RoleType).role == READWRITE) "pass" else "share"
        if((type as RoleType).base.clazz.isObjectClass)
            '''
            if(«safeName» instanceof rolez.lang.Guarded)
                ((rolez.lang.Guarded) «safeName»).pass();
            '''
        else
            '''«safeName».«kind»();'''
    }
    
    private def needsRegisterNewOwner(Param it) {
        type.isGuarded && (type as RoleType).role == READWRITE
    }
    
    private def genRegisterNewOwner(Param it) {
        if((type as RoleType).base.clazz.isObjectClass)
            '''
            if(«safeName» instanceof rolez.lang.Guarded)
                ((rolez.lang.Guarded) «safeName»).registerNewOwner();
            '''
        else
            '''«safeName».registerNewOwner();'''
    }
    
    private def genRelease(Param it) {
        val kind = if((type as RoleType).role == READWRITE) "Passed" else "Shared"
        if((type as RoleType).base.clazz.isObjectClass)
            '''
            if(«safeName» instanceof rolez.lang.Guarded)
                ((rolez.lang.Guarded) «safeName»).release«kind»();
            '''
        else
            '''«safeName».release«kind»();'''
    }
    
    private def gen(Field it) '''
        
        public «kind.gen»«type.gen» «safeName»«IF initializer != null» = «initializer.gen»«ENDIF»;
    '''
    
    private def gen(Method it) '''
        
        public «type.gen» «safeName»(«params.map[gen].join(", ")») {
            «body.genStmtsWithTryCatch»
        }
    '''
    
    private def gen(Constr it) {
        val guardThis = dynamicThisRoleAtExit != READWRITE
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
        
        public «type.gen» «name»(«params.map[gen].join(", ")») {
            «if(!(type instanceof Void)) "return "»«generateStaticCall»;
        }
    ''' else gen }
    
    private def generateStaticCall(Method it)
        '''«enclosingClass.jvmClass.qualifiedName».«name»(«params.map[safeName].join(", ")»)'''
    
    private def gen(Param it) '''«kind.gen»«type.gen» «safeName»'''
    
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
        val guardedTarget =
            if(system.subroleSucceeded(target.dynamicRole, READONLY))
                target.genNested
            else
                "guardReadOnly(" + target.gen + ")"
        '''«guardedTarget».data[«args.get(0).gen»]'''
    }
    
    private def generateArraySet(MemberAccess it) {
        val guardedTarget =
            if(system.subroleSucceeded(target.dynamicRole, READWRITE))
                target.genNested
            else
                "guardReadWrite(" + target.gen + ")"
        '''«guardedTarget».data[«args.get(0).gen»] = «args.get(1).gen»'''
    }
    
    private def generateArrayLength(MemberAccess it)
        '''«target.genNested».data.length'''
    
    private def generateMethodInvoke(MemberAccess it) {
        val targetType = system.type(utils.createEnv(it), target).value
        val needsGuard = !system.subroleSucceeded(target.dynamicRole, method.thisRole)
        val guardedTarget =
            if(targetType.isGuarded && needsGuard)
                if((targetType as RoleType).base.clazz.isObjectClass)
                    switch(method.thisRole) {
                        case READWRITE: "guardReadWriteIfNeeded(" + target.gen + ")"
                        case  READONLY: "guardReadOnlyIfNeeded("  + target.gen + ")"
                        case      PURE:                     target.genNested
                    }
                else
                    switch(method.thisRole) {
                        case READWRITE: "guardReadWrite(" + target.gen + ")"
                        case  READONLY: "guardReadOnly("  + target.gen + ")"
                        case      PURE:                     target.genNested
                    }
            else 
                target.genNested
        '''«guardedTarget».«method.safeName»(«args.map[gen].join(", ")»)'''
    }
    
    private def generateFieldAccess(MemberAccess it) {
        val targetType = system.type(utils.createEnv(it), target).value
        val requiredRole =
            if(isFieldWrite)           READWRITE
            else if(field.kind == VAR) READONLY
            else                       PURE
        val needsGuard = !system.subroleSucceeded(target.dynamicRole, requiredRole)
        val guardedTarget =
            if(targetType.isGuarded && needsGuard)
                if(requiredRole == READWRITE) "guardReadWrite(" + target.gen + ")"
                else                          "guardReadOnly("  + target.gen + ")"
            else
                target.genNested
        '''«guardedTarget».«field.safeName»'''
    }
    
    private def isFieldWrite(MemberAccess it) {
        eContainer instanceof Assignment && it === (eContainer as Assignment).left
    }
    
    private def dispatch generateExpr(This _) '''this'''
    
    private def dispatch generateExpr(VarRef it) { variable.safeName }
    
    private def dispatch generateExpr(New it)
        '''new «classRef.gen»(«args.map[gen].join(", ")»)'''
    
    private def dispatch generateExpr(The it) '''«classRef.gen».INSTANCE'''
    
    private def dispatch generateExpr(Start it)
        '''rolez.lang.TaskSystem.getDefault().start(new «taskRef.gen»(«args.map[gen].join(", ")»))'''
    
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
    
    private def Type elemType(GenericClassRef it) {
        if(!clazz.isArrayClass)
            throw new AssertionError
        
        val arg = typeArg
        switch(arg) {
            RoleType case arg.base instanceof GenericClassRef:
                elemType(arg.base as GenericClassRef)
            default:
                arg
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
    
    private def CharSequence genGeneric(Type it) {
        if(it instanceof PrimitiveType) jvmWrapperTypeName
        else generateType
    }
    
    private def dispatch generateType(PrimitiveType it) { string }
    
    private def dispatch generateType(RoleType it) { base.gen }
    
    private def dispatch generateType(Null it) {
        throw new AssertionError("Null type usage not checked")
    }
    
    private def dispatch generateType(TypeParamRef _) {
        // TODO
        throw new AssertionError
    }
    
    private def gen(ClassRef it) { generateClassRef }
    
    private def dispatch generateClassRef(SimpleClassRef it) {
        clazz.generateName
    }
    
    private def dispatch generateClassRef(GenericClassRef it) {
        if(clazz.isArrayClass)
            switch(typeArg) {
                Int:     '''rolez.lang.IntArray'''
                Double:  '''rolez.lang.DoubleArray'''
                Char:    '''rolez.lang.CharArray'''
                Boolean: '''rolez.lang.BooleanArray'''
                default: '''rolez.lang.ObjectArray<«typeArg.gen»>'''
            }
        else
            '''«clazz.generateName»<«typeArg.genGeneric»>'''
    }
    
    private def generateName(Class it) {
        if(mapped && !isSingleton)
            jvmClass.qualifiedName.requireNonNull
        else
            safeQualifiedName
    }
    
    private def gen(TaskRef it) { task.safeQualifiedName }
    
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

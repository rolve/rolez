package ch.trick17.rolez.generator

import ch.trick17.rolez.RolezUtils
import ch.trick17.rolez.rolez.ArithmeticBinaryExpr
import ch.trick17.rolez.rolez.Assignment
import ch.trick17.rolez.rolez.BinaryExpr
import ch.trick17.rolez.rolez.Block
import ch.trick17.rolez.rolez.BooleanLiteral
import ch.trick17.rolez.rolez.Cast
import ch.trick17.rolez.rolez.CharLiteral
import ch.trick17.rolez.rolez.DoubleLiteral
import ch.trick17.rolez.rolez.EqualityExpr
import ch.trick17.rolez.rolez.Expr
import ch.trick17.rolez.rolez.ExprStmt
import ch.trick17.rolez.rolez.ForLoop
import ch.trick17.rolez.rolez.GenericClassRef
import ch.trick17.rolez.rolez.IfStmt
import ch.trick17.rolez.rolez.Instr
import ch.trick17.rolez.rolez.IntLiteral
import ch.trick17.rolez.rolez.LocalVarDecl
import ch.trick17.rolez.rolez.LogicalExpr
import ch.trick17.rolez.rolez.MemberAccess
import ch.trick17.rolez.rolez.New
import ch.trick17.rolez.rolez.NullLiteral
import ch.trick17.rolez.rolez.Parenthesized
import ch.trick17.rolez.rolez.PrimitiveType
import ch.trick17.rolez.rolez.Pure
import ch.trick17.rolez.rolez.ReadOnly
import ch.trick17.rolez.rolez.ReadWrite
import ch.trick17.rolez.rolez.RelationalExpr
import ch.trick17.rolez.rolez.ReturnExpr
import ch.trick17.rolez.rolez.ReturnNothing
import ch.trick17.rolez.rolez.Role
import ch.trick17.rolez.rolez.RoleType
import ch.trick17.rolez.rolez.RolezFactory
import ch.trick17.rolez.rolez.Stmt
import ch.trick17.rolez.rolez.StringLiteral
import ch.trick17.rolez.rolez.SuperConstrCall
import ch.trick17.rolez.rolez.The
import ch.trick17.rolez.rolez.This
import ch.trick17.rolez.rolez.UnaryExpr
import ch.trick17.rolez.rolez.UnaryMinus
import ch.trick17.rolez.rolez.UnaryNot
import ch.trick17.rolez.rolez.VarKind
import ch.trick17.rolez.rolez.VarRef
import ch.trick17.rolez.rolez.WhileLoop
import ch.trick17.rolez.typesystem.RolezSystem
import ch.trick17.rolez.validation.JavaMapper
import com.google.inject.Injector
import java.util.regex.Pattern
import javax.inject.Inject
import org.eclipse.xtext.common.types.JvmArrayType

import static ch.trick17.rolez.Constants.*
import static ch.trick17.rolez.RolezUtils.*
import static ch.trick17.rolez.rolez.VarKind.*

import static extension ch.trick17.rolez.RolezExtensions.*
import static extension org.eclipse.xtext.util.Strings.convertToJavaString

class InstrGenerator {
    
    @Inject Injector injector
    
    def generate(Instr it, RoleAnalysis roleAnalysis, CodeKind codeKind) {
        newGenerator(roleAnalysis, codeKind).generate(it)
    }
    
    def generateWithTryCatch(Block it, RoleAnalysis roleAnalysis, CodeKind codeKind, boolean forceTry) {
        newGenerator(roleAnalysis, codeKind).generateWithTryCatch(it, forceTry)
    }
    
    private def newGenerator(RoleAnalysis roleAnalysis, CodeKind codeKind) {
        new Generator(roleAnalysis, codeKind) => [injector.injectMembers(it)]
    }
    
    /**
     * This generator is parameterized with RoleAnalysis. To avoid passing the analysis object
     * around, we have a second class that keeps it in a field.
     */
    private static class Generator {
        
        @Inject extension RolezFactory
        @Inject extension JavaMapper
        @Inject RolezUtils utils
        @Inject RolezSystem system
        
        @Inject extension TypeGenerator
        @Inject extension SafeJavaNames
        
        val RoleAnalysis roleAnalysis
        val CodeKind codeKind
        
        new(RoleAnalysis roleAnalysis, CodeKind codeKind) {
            this.roleAnalysis = roleAnalysis
            this.codeKind = codeKind
        }
        
        /* Stmt */
        
        private def dispatch CharSequence generate(Block it) '''
            {
                «stmts.map[generate].join("\n")»
            }'''
        
        private def dispatch CharSequence generate(LocalVarDecl it) {
            val type = system.varType(createEnv(it), variable).value
            '''«variable.kind.generate»«type.generate» «variable.safeName»«IF initializer != null» = «initializer.generate»«ENDIF»;'''
        }
        
        private def dispatch CharSequence generate(IfStmt it) '''
            if(«condition.generate»)«thenPart.genIndent»
            «IF elsePart != null»
            else«elsePart.genIndent»
            «ENDIF»'''
        
        private def dispatch CharSequence generate(WhileLoop it) '''
            while(«condition.generate»)«body.genIndent»'''
        
        private def dispatch CharSequence generate(ForLoop it) '''
            for(«initializer.generate» «condition.generate»; «step.generate»)«body.genIndent»'''
        
        private def dispatch CharSequence generate(SuperConstrCall it) '''
            super(«args.map[generate].join(", ")»);'''
        
        private def dispatch CharSequence generate(ReturnNothing _) {
            if(codeKind == CodeKind.TASK) '''
                return null;
            '''
            else '''
                return;
            '''
        }
        
        private def dispatch CharSequence generate(ReturnExpr it) '''
            return «expr.generate»;'''
        
        /* Java only allows certain kinds of "expression statements", so find
         * the corresponding expressions in the rolez expression tree */
        private def dispatch CharSequence generate(ExprStmt it) '''
            «findSideFxExpr(expr).map[generate + ";"].join("\n")»'''
        
        private def Iterable<Expr> findSideFxExpr(Expr it) {
            switch(it) {
                case utils.isSideFxExpr(it): #[it]
                BinaryExpr: findSideFxExpr(left) + findSideFxExpr(right)
                UnaryExpr: findSideFxExpr(expr)
                // Special cases for array instantiations and get
                MemberAccess case utils.isArrayGet(it): {
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
        
        private def genIndent(Stmt it) { switch(it) {
            Block: " " + generate
            default: "\n    " + generate
        }}
        
        private def generateWithTryCatch(Block it, boolean forceTry) {
            val exceptionTypes = thrownExceptionTypes
            val isConstr = !stmts.isEmpty && stmts.head instanceof SuperConstrCall
            if(!exceptionTypes.isEmpty || forceTry) '''
                «IF isConstr»
                «stmts.head.generate»
                «ENDIF»
                try {
                    «stmts.drop(if(isConstr) 1 else 0).map[generate].join("\n")»
                }
                «IF !exceptionTypes.isEmpty»
                catch(«exceptionTypes.map[qualifiedName].join(" | ")» e) {
                    throw new java.lang.RuntimeException("ROLEZ EXCEPTION WRAPPER", e);
                }
                «ENDIF»
            '''
            else '''
                «stmts.map[generate].join("\n")»
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
        
        /* Expr */
        
        private def dispatch CharSequence generate(Assignment it)
            '''«left.generate» «op» «right.generate»'''
        
        private def dispatch CharSequence generate(BinaryExpr it) {
            val op = switch(it) {
                LogicalExpr: op
                EqualityExpr: op
                RelationalExpr: op
                ArithmeticBinaryExpr: op
            }
            '''«left.genNested» «op» «right.genNested»'''
        }
        
        private def dispatch CharSequence generate(Cast it)
            '''(«type.generate») «expr.genNested»'''
        
        private def dispatch CharSequence generate(UnaryMinus it)
            '''-«expr.genNested»'''
        
        private def dispatch CharSequence generate(UnaryNot it)
            '''!«expr.genNested»'''
        
        private def dispatch CharSequence generate(MemberAccess it) { switch(it) {
            case utils.isSliceGet(it):         generateSliceGet
            case utils.isSliceSet(it):         generateSliceSet
            case utils.isArrayGet(it):         generateArrayGet
            case utils.isArraySet(it):         generateArraySet
            case utils.isArrayLength(it):      generateArrayLength
            case utils.isVectorGet(it):        generateVectorGet
            case utils.isVectorLength(it):     generateVectorLength
            case utils.isVectorBuilderGet(it): generateArrayGet         // same code as for arrays
            case utils.isVectorBuilderSet(it): generateVectorBuilderSet
            case isFieldAccess:                generateFieldAccess
            case isMethodInvoke:               generateMethodInvoke
            case isTaskStart:                  generateTaskStart
        }}
        
        private def generateSliceGet(MemberAccess it)
            '''«target.genGuarded(createReadOnly)».«genSliceAccess("get")»(«args.get(0).generate»)'''
        
        private def generateSliceSet(MemberAccess it)
            '''«target.genGuarded(createReadWrite)».«genSliceAccess("set")»(«args.get(0).generate», «args.get(1).generate»)'''
        
        private def genSliceAccess(MemberAccess it, String getOrSet) {
            val targetType = system.type(createEnv(it), target).value
            val componentType = ((targetType as RoleType).base as GenericClassRef).typeArg
            switch(componentType) {
                PrimitiveType: getOrSet + componentType.name.toFirstUpper
                case getOrSet == "get": "<" + componentType.generate + ">get"
                case getOrSet == "set": "set"
                default: throw new AssertionError
            }
        }
        
        private def generateArrayGet(MemberAccess it)
            '''«target.genGuarded(createReadOnly)».data[«args.get(0).generate»]'''
        
        private def generateArraySet(MemberAccess it)
            '''«target.genGuarded(createReadWrite)».data[«args.get(0).generate»] = «args.get(1).generate»'''
        
        private def generateArrayLength(MemberAccess it)
            '''«target.genNested».data.length'''
        
        private def generateVectorGet(MemberAccess it)
            '''«target.genNested»[«args.get(0).generate»]'''
        
        private def generateVectorLength(MemberAccess it)
            '''«target.genNested».length'''
        
        private def generateVectorBuilderSet(MemberAccess it) {
            val targetType = system.type(createEnv(it), target).value
            val componentType = ((targetType as RoleType).base as GenericClassRef).typeArg
            val suffix =
                if(componentType instanceof PrimitiveType) componentType.name.toFirstUpper
                else ""
            
            '''«target.genGuarded(createReadWrite)».set«suffix»(«args.get(0).generate», «args.get(1).generate»)'''
        }
        
        private def generateFieldAccess(MemberAccess it) {
            val requiredRole =
                if(isFieldWrite)           createReadWrite
                else if(field.kind == VAR) createReadOnly
                else                       createPure
            '''«target.genGuarded(requiredRole)».«field.safeName»'''
        }
        
        private def generateMethodInvoke(MemberAccess it) {
            if(method.isMapped) {
                // Shorter and more efficient code for access to mapped singletons, like System, Math
                val genTarget = 
                    if(target instanceof The) (target as The).classRef.clazz.jvmClass.getQualifiedName('.')
                    else target.genNested
                val genInvoke = '''«genTarget».«method.safeName»(«args.map[genCoerced].join(", ")»)'''
                if(method.jvmMethod.returnType.type instanceof JvmArrayType) {
                    val componentType = ((method.type as RoleType).base as GenericClassRef).typeArg
                    '''«jvmGuardedArrayClassName».<«componentType.generate»[]>wrap(«genInvoke»)'''
                }
                else
                    genInvoke
            }
            else
                '''«target.genNested».«method.safeName»(«args.map[generate].join(", ")»)'''
        }
        
        private def CharSequence genCoerced(Expr it) {
            val paramType = destParam.jvmParam.parameterType.type
            if(paramType instanceof JvmArrayType) {
                val arrayType = paramType.toString.substring(14) // IMPROVE: A little less magic, a little more robustness, please
                '''«jvmGuardedArrayClassName».unwrap(«genNested», «arrayType».class)'''
            }
            else
                generate
        }
        
        private def isFieldWrite(MemberAccess it) {
            eContainer instanceof Assignment && it === (eContainer as Assignment).left
        }
        
        private def generateTaskStart(MemberAccess it)
            '''«jvmTaskSystemClassName».getDefault().start(«target.genNested».$«method.name»Task(«args.map[generate].join(", ")»))'''
        
        private def dispatch CharSequence generate(This it)  {
            if(codeKind == CodeKind.TASK) '''«enclosingClass.safeSimpleName».this'''
            else '''this'''
        }
        
        private def dispatch CharSequence generate(VarRef it) { variable.safeName }
        
        private def dispatch CharSequence generate(New it) {
            val genArgs = 
                if(classRef.clazz.isArrayClass || classRef.clazz.isVectorBuilderClass) {
                    val componentType = (classRef as GenericClassRef).typeArg
                    val withoutSize = '''new «componentType.generateErased»[]'''
                    val matcher = bracketPattern.matcher(withoutSize)
                    matcher.find
                    withoutSize.substring(0, matcher.start) + args.head.generate + withoutSize.substring(matcher.start)
                }
                else if(constr.isMapped)
                    args.map[genCoerced].join(", ")
                else
                    args.map[generate].join(", ")
            '''new «classRef.generate»(«genArgs»)'''
        }
        
        private static val bracketPattern = Pattern.compile("\\](\\[\\])*$")
        
        private def dispatch CharSequence generate(The it) {
            if(eContainer instanceof MemberAccess && it == (eContainer as MemberAccess).target
                    && classRef.clazz.isMapped)
                classRef.clazz.jvmClass.qualifiedName // more efficient access to static members
            else
                '''«classRef.generate».INSTANCE'''
        }
        
        private def dispatch CharSequence generate(Parenthesized it) { expr.generate }
        
        private def dispatch CharSequence generate(    IntLiteral it) { value.toString }
        private def dispatch CharSequence generate( DoubleLiteral it) { value.toString }
        private def dispatch CharSequence generate(BooleanLiteral it) { value.toString }
        
        private def dispatch CharSequence generate(StringLiteral it) {
            "\"" + value.convertToJavaString + "\""
        }
        
        private def dispatch CharSequence generate(CharLiteral it) {
            "'" + value.toString.convertToJavaString + "'"
        }
        
        private def dispatch CharSequence generate(NullLiteral _) '''null'''
        
        private def CharSequence genNested(Expr it) { switch(it) {
            Assignment: generate
            BinaryExpr: '''(«generate»)'''
            UnaryExpr : '''(«generate»)'''
            default   : generate
        }}
        
        private def genGuarded(Expr it, Role requiredRole) {
            val type = system.type(createEnv(it), it).value
            val needsGuard = !system.subroleSucceeded(roleAnalysis.dynamicRole(it), requiredRole)
            if(type.isGuarded && needsGuard)
                switch(requiredRole) {
                    ReadWrite: "guardReadWrite(" + generate + ")"
                    ReadOnly : "guardReadOnly("  + generate + ")"
                    Pure     : genNested
                }
            else
                genNested
            
            // IMPROVE: Better syntax (and performance?) when using type system
        }
    }
    
    /* VarKind */
    
    static def generate(VarKind it) { if(it == VAL) "final " }
}
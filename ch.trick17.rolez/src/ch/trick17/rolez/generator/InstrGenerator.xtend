package ch.trick17.rolez.generator

import ch.trick17.rolez.RolezUtils
import ch.trick17.rolez.rolez.Argumented
import ch.trick17.rolez.rolez.ArithmeticUnaryExpr
import ch.trick17.rolez.rolez.Assignment
import ch.trick17.rolez.rolez.BinaryExpr
import ch.trick17.rolez.rolez.BitwiseNot
import ch.trick17.rolez.rolez.Block
import ch.trick17.rolez.rolez.BooleanLiteral
import ch.trick17.rolez.rolez.Cast
import ch.trick17.rolez.rolez.CharLiteral
import ch.trick17.rolez.rolez.DoubleLiteral
import ch.trick17.rolez.rolez.Expr
import ch.trick17.rolez.rolez.ExprStmt
import ch.trick17.rolez.rolez.FieldInitializer
import ch.trick17.rolez.rolez.ForLoop
import ch.trick17.rolez.rolez.GenericClassRef
import ch.trick17.rolez.rolez.IfStmt
import ch.trick17.rolez.rolez.Instr
import ch.trick17.rolez.rolez.IntLiteral
import ch.trick17.rolez.rolez.LocalVarDecl
import ch.trick17.rolez.rolez.LogicalNot
import ch.trick17.rolez.rolez.LongLiteral
import ch.trick17.rolez.rolez.MemberAccess
import ch.trick17.rolez.rolez.New
import ch.trick17.rolez.rolez.NullLiteral
import ch.trick17.rolez.rolez.ParallelStmt
import ch.trick17.rolez.rolez.Parenthesized
import ch.trick17.rolez.rolez.Parfor
import ch.trick17.rolez.rolez.PrimitiveType
import ch.trick17.rolez.rolez.ReadOnly
import ch.trick17.rolez.rolez.ReadWrite
import ch.trick17.rolez.rolez.ReturnExpr
import ch.trick17.rolez.rolez.ReturnNothing
import ch.trick17.rolez.rolez.Role
import ch.trick17.rolez.rolez.RoleType
import ch.trick17.rolez.rolez.RolezFactory
import ch.trick17.rolez.rolez.Slicing
import ch.trick17.rolez.rolez.Stmt
import ch.trick17.rolez.rolez.StringLiteral
import ch.trick17.rolez.rolez.Super
import ch.trick17.rolez.rolez.SuperConstrCall
import ch.trick17.rolez.rolez.The
import ch.trick17.rolez.rolez.This
import ch.trick17.rolez.rolez.UnaryExpr
import ch.trick17.rolez.rolez.VarKind
import ch.trick17.rolez.rolez.VarRef
import ch.trick17.rolez.rolez.WhileLoop
import ch.trick17.rolez.typesystem.RolezSystem
import ch.trick17.rolez.validation.JavaMapper
import com.google.inject.Injector
import java.util.ArrayList
import java.util.regex.Pattern
import javax.inject.Inject
import org.eclipse.xtext.common.types.JvmArrayType

import static ch.trick17.rolez.Constants.*
import static ch.trick17.rolez.generator.MethodKind.*
import static ch.trick17.rolez.rolez.OpArithmeticUnary.*
import static ch.trick17.rolez.rolez.VarKind.*

import static extension ch.trick17.rolez.RolezExtensions.*
import static extension ch.trick17.rolez.generator.SafeJavaNames.*
import static extension org.eclipse.xtext.util.Strings.convertToJavaString

/**
 * Generates Java code for Rolez instructions (single or code blocks). Relies on
 * {@link RoleAnalysis} and {@link ChildTasksAnalysis} to decide where to insert
 * guards.
 */
class InstrGenerator {
    
    @Inject extension Injector
    
    /**
     * Generates Java code for the the given Rolez instruction, in the context of a
     * constructor or field initializer.
     */
    def generate(Instr it, RoleAnalysis roleAnalysis, ChildTasksAnalysis childTasksAnalysis) {
        newGenerator(null, roleAnalysis, childTasksAnalysis).generate(it)
    }
    
    /**
     * Generates Java code for a Rolez code block, in the context of a constructor.
     * If the resulting code may throw checked exceptions, it is wrapped in a try-catch
     * block that catches and wraps them in runtime exceptions. If <code>forceTry</code>
     * is true, then the try-block is also generated if no checked exceptions need to be
     * caught.
     */
    def generateWithTryCatch(Block it, RoleAnalysis roleAnalysis,
            ChildTasksAnalysis childTasksAnalysis, boolean forceTry) {
        newGenerator(null, roleAnalysis, childTasksAnalysis).generateWithTryCatch(it, forceTry)
    }
    
    /**
     * Generates Java code for a Rolez code block, in the context of the given kind of method.
     * If the resulting code may throw checked exceptions, it is wrapped in a try-catch
     * block that catches and wraps them in runtime exceptions. If <code>forceTry</code>
     * is true, then the try-block is also generated if no checked exceptions need to be
     * caught.
     */
    def generateWithTryCatch(Block it, MethodKind methodKind, RoleAnalysis roleAnalysis,
            ChildTasksAnalysis childTasksAnalysis, boolean forceTry) {
        newGenerator(methodKind, roleAnalysis, childTasksAnalysis).generateWithTryCatch(it, forceTry)
    }
    
    private def newGenerator(MethodKind mk, RoleAnalysis ra, ChildTasksAnalysis cta) {
        new Generator(mk, ra, cta) => [injectMembers]
    }
    
    /**
     * This generator is parameterized with RoleAnalysis, ChildTasksAnalysis and
     * MethodKind. To avoid passing these around, we have a second class that keeps
     * them in fields. A method kind of <code>null</code> means that the code is
     * not in a method (but in a constructor or field initializer).
     */
    private static class Generator {
        
        @Inject extension RolezFactory
        @Inject extension JavaMapper
        @Inject RolezUtils utils
        @Inject RolezSystem system
        
        @Inject extension TypeGenerator
        
        val RoleAnalysis roleAnalysis
        val ChildTasksAnalysis childTasksAnalysis
        var MethodKind methodKind
        
        private new(MethodKind methodKind, RoleAnalysis roleAnalysis,
                ChildTasksAnalysis childTasksAnalysis) {
            this.methodKind = methodKind
            this.roleAnalysis = roleAnalysis
            this.childTasksAnalysis = childTasksAnalysis
        }
        
        /* Stmt */
        private def dispatch CharSequence generate(Parfor it){
            val canHazChildTask = childTasksAnalysis.childTasksMayExist(it)
            // TODO: generate unguarded version
            
            val params = it.params;
            val paramTypes = params.map[
            	if (variable.type != null) variable.type
            	else system.type(initializer).value
            ]
            val genParamTypes = paramTypes.map[generate];
            val args = params.map[initializer]
            val passed = new ArrayList
            val shared = new ArrayList
            
            // separate arguments for the tasks into shared and passed objects
            for(var i = 0; i < params.size; i++) {
            	val pitype = paramTypes.get(i);
                if(pitype instanceof RoleType) {
                    val role = (pitype as RoleType).role
                    if(role instanceof ReadWrite) 
                        passed.add(i)
                    else if(role instanceof ReadOnly)
                        shared.add(i)
                }
            }
            
            '''
            { /* parfor */
                final java.util.List<java.lang.Object[]> $argsList = new java.util.ArrayList<>();
                for(«initializer.generate» «condition.generate»; «step.generate»)
                    $argsList.add(new java.lang.Object[] {«args.map[generate].join(", ")»});
                
                final java.lang.Object[][] $passed = new java.lang.Object[$argsList.size()][];
                final java.lang.Object[][] $shared = new java.lang.Object[$argsList.size()][];
                for(int $i = 0; $i < $argsList.size(); $i++) {
                    final java.lang.Object[] $args = $argsList.get($i);
                    $passed[$i] = new java.lang.Object[] {«passed.map["$args["+it+"]"].join(", ")»};
                    $shared[$i] = new java.lang.Object[] {«shared.map["$args["+it+"]"].join(", ")»};
                }
                
                final «taskClassName»<?>[] $tasks = new «taskClassName»<?>[$argsList.size()];
                long $tasksBits = 0;
                for(int $i = 0; $i < $tasks.length; $i++) {
                    final java.lang.Object[] $args = $argsList.get($i);
                    $tasks[$i] = new «taskClassName»<java.lang.Void>($passed[$i], $shared[$i], $tasksBits) {
                        @java.lang.Override
                        protected java.lang.Void runRolez() {
                            «(0..<params.size).map[
                            	genParamTypes.get(it) + " " + params.get(it).variable.name +
                            	" = (" + genParamTypes.get(it) + ") $args[" + it + "];"
                            ].join("\n")»
                            «body.generate»
                            return null;
                        }
                    };
                    $tasksBits |= $tasks[$i].idBits();
                }
                
                try {
                    for(int $i = 0; $i < $tasks.length-1; $i++)
                        «jvmTaskSystemClassName».getDefault().start($tasks[$i]);
                    «jvmTaskSystemClassName».getDefault().run($tasks[$tasks.length - 1]);
                } finally {
                    for(«taskClassName»<?> $t : $tasks)
                        $t.get();
                }
            }
            '''
        }
        
        private def dispatch CharSequence generate(ParallelStmt it) {
            val canHazChildTask = childTasksAnalysis.childTasksMayExist(it)
            // TODO: generate unguarded version
            
            val argPrefix1 = "$t1Arg"
            val argPrefix2 = "$t2Arg"
            
            var ma1 = (part1 as ExprStmt).expr as MemberAccess	
            var ma2 = (part2 as ExprStmt).expr as MemberAccess	
            
            val args1 = ma1.allArgs.toList
            val args2 = ma2.allArgs.toList
            val params1 = ma1.method.allParams.toList
            val params2 = ma2.method.allParams.toList
            
            val passed1 = new ArrayList
            val passed2 = new ArrayList
            
            val shared1 = new ArrayList
            val shared2 = new ArrayList
            
            // separate arguments for the tasks into shared and passed objects
            for(var i = 0; i < params1.size; i++){
                if(params1.get(i).type instanceof RoleType) {
                    val role = (params1.get(i).type as RoleType).role
                    if(role instanceof ReadWrite) 
                        passed1.add(i)
                    else if(role instanceof ReadOnly)
                        shared1.add(i)
                }
            }
            
            for(var i = 0; i < params2.size; i++){
                if(params2.get(i).type instanceof RoleType) {
                    val role = (params2.get(i).type as RoleType).role
                    if(role instanceof ReadWrite) 
                        passed2.add(i)
                    else if(role instanceof ReadOnly)
                        shared2.add(i)
                }
            }
            
            val argList1 = (1..<args1.size).map[argPrefix1 + it + ", "].join
            val argList2 = (1..<args2.size).map[argPrefix2 + it + ", "].join
            
            '''
            { /* parallel-and */
                «FOR i : 0..<args1.size»
                final «params1.get(i).type.generate» «argPrefix1»«i» = «args1.get(i).generate»;
                «ENDFOR»
                «FOR i : 0..<args2.size»
                final «params2.get(i).type.generate» «argPrefix2»«i» = «args2.get(i).generate»;
                «ENDFOR»
                
                final java.lang.Object[] $t1Passed = {«passed1.map[argPrefix1 + it].join(", ")»};
                final java.lang.Object[] $t1Shared = {«shared1.map[argPrefix1 + it].join(", ")»};
                final java.lang.Object[] $t2Passed = {«passed2.map[argPrefix2 + it].join(", ")»};
                final java.lang.Object[] $t2Shared = {«shared2.map[argPrefix2 + it].join(", ")»};
                
                final «taskClassName»<?> $t1 = new «taskClassName»<java.lang.Void>($t1Passed, $t1Shared) {
                    @java.lang.Override
                    protected java.lang.Void runRolez() {
                        «argPrefix1»0.«ma1.method.name»«UNGUARDED_METHOD.suffix»(«argList1»idBits());
                        return null;
                    }
                };
                final «taskClassName»<?> $t2 = new «taskClassName»<java.lang.Void>($t2Passed, $t2Shared, $t1.idBits()) {
                    @java.lang.Override
                    protected java.lang.Void runRolez() {
                        «argPrefix2»0.«ma2.method.name»«UNGUARDED_METHOD.suffix»(«argList2»idBits());
                        return null;
                    }
                };
                
                try {
                    «jvmTaskSystemClassName».getDefault().start($t1);
                    «jvmTaskSystemClassName».getDefault().run($t2);
                } finally {
                    $t1.get();
                }
            }
            '''
        }
        
        /*private static def boolean isTaskCall(Stmt body) {
        	if (!(body instanceof ExprStmt))
        		return false;
	        val es = body as ExprStmt
	        if (!(es.expr instanceof MemberAccess))
	        	return false;
	        
	        val ma = es.expr as MemberAccess
	        
	        if (!ma.isMethodInvoke)
	        	return false;
	        
	        return ma.getMethod.declaredTask;
        }*/
        
        private def dispatch CharSequence generate(Block it) '''
            {
                «stmts.map[generate].join("\n")»
            }'''
        
        private def dispatch CharSequence generate(LocalVarDecl it) {
            val type = system.varType(variable).value
            '''«variable.kind.generate»«type.generate» «variable.safeName»«IF initializer !== null» = «initializer.generate»«ENDIF»;'''
        }
        
        private def dispatch CharSequence generate(IfStmt it) '''
            if(«condition.generate»)«thenPart.genIndent»
            «IF elsePart !== null»
            else«elsePart.genIndent»
            «ENDIF»'''
        
        private def dispatch CharSequence generate(WhileLoop it) '''
            while(«condition.generate»)«body.genIndent»'''
        
        private def dispatch CharSequence generate(ForLoop it) '''
            for(«initializer.generate» «condition.generate»; «step.generate»)«body.genIndent»'''
        
        private def dispatch CharSequence generate(SuperConstrCall it) '''
            super(«genArgs»);'''
        
        private def dispatch CharSequence generate(ReturnNothing _) {
            if(methodKind == TASK) '''
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
                    allArgs.map[findSideFxExpr].flatten
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
            // skip super constr call if there is one, it is generated separately, outside of try
            val withoutSuperConstr = stmts.filter[!(it instanceof SuperConstrCall)]
            val exceptionTypes = thrownExceptionTypes
            if(!exceptionTypes.isEmpty || forceTry) '''
                try {
                    «withoutSuperConstr.map[generate].join("\n")»
                }
                «IF !exceptionTypes.isEmpty»
                catch(«exceptionTypes.map[qualifiedName].join(" | ")» e) {
                    throw new java.lang.RuntimeException("ROLEZ EXCEPTION WRAPPER", e);
                }
                «ENDIF»
            '''
            else '''
                «withoutSuperConstr.map[generate].join("\n")»
            '''
        }
        
        private def thrownExceptionTypes(Stmt it) {
            val all = all(Expr).map[switch(it) {
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
        
        private def dispatch CharSequence generate(BinaryExpr it)
            '''«left.genNested» «op» «right.genNested»'''
        
        private def dispatch CharSequence generate(Cast it)
            '''(«type.generate») «expr.genNested»'''
        
        private def dispatch CharSequence generate(ArithmeticUnaryExpr it) {
            if(op == POST_INCREMENT || op == POST_DECREMENT)
                // duplicate enum literals are forbidden, so the POST_* operators contain "post"
                expr.genNested + op.literal.replace("post", "")
            else
                op.literal + expr.genNested
        }
        
        private def dispatch CharSequence generate(LogicalNot it)
            '''!«expr.genNested»'''
        
        private def dispatch CharSequence generate(BitwiseNot it)
            '''~«expr.genNested»'''
        
        private def dispatch CharSequence generate(Slicing it)
            '''«target.generate».$«slice.safeName»Slice()'''
        
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
            '''«target.genGuarded(createReadOnly, true)».«genSliceAccess("get")»(«args.get(0).generate»)'''
        
        private def generateSliceSet(MemberAccess it)
            '''«target.genGuarded(createReadWrite, true)».«genSliceAccess("set")»(«args.get(0).generate», «args.get(1).generate»)'''
        
        private def genSliceAccess(MemberAccess it, String getOrSet) {
            val targetType = system.type(target).value
            val componentType = ((targetType as RoleType).base as GenericClassRef).typeArg
            switch(componentType) {
                PrimitiveType: getOrSet + componentType.name.toFirstUpper
                case getOrSet == "get": "<" + componentType.generate + ">get"
                case getOrSet == "set": "set"
                default: throw new AssertionError
            }
        }
        
        private def generateArrayGet(MemberAccess it)
            '''«target.genGuarded(createReadOnly, true)».data[«args.get(0).generate»]'''
        
        private def generateArraySet(MemberAccess it)
            '''«target.genGuarded(createReadWrite, true)».data[«args.get(0).generate»] = «args.get(1).generate»'''
        
        private def generateArrayLength(MemberAccess it)
            '''«target.genNested».data.length'''
        
        private def generateVectorGet(MemberAccess it)
            '''«target.genNested»[«args.get(0).generate»]'''
        
        private def generateVectorLength(MemberAccess it)
            '''«target.genNested».length'''
        
        private def generateVectorBuilderSet(MemberAccess it) {
            val targetType = system.type(target).value
            val componentType = ((targetType as RoleType).base as GenericClassRef).typeArg
            val suffix =
                if(componentType instanceof PrimitiveType) componentType.name.toFirstUpper
                else ""
            
            '''«target.genGuarded(createReadWrite, true)».set«suffix»(«args.get(0).generate», «args.get(1).generate»)'''
        }
        
        private def generateFieldAccess(MemberAccess it) {
            val requiredRole =
                if(isFieldWrite)           createReadWrite
                else if(field.kind == VAR) createReadOnly
                else                       createPure
            val targetType = system.type(target).value as RoleType
            val redirect = if(targetType.isSliced) ".$object()" else ""
            '''«target.genGuarded(requiredRole, true)»«redirect».«field.safeName»'''
        }
        
        private def generateMethodInvoke(MemberAccess it) {
            if(method.isMapped) {
                // Shorter and more efficient code for access to mapped singletons, like System, Math
                val genTarget = 
                    if(target instanceof The) (target as The).classRef.clazz.jvmClass.getQualifiedName('.')
                    else target.genGuardedMapped(method.original.thisParam.type.role.erased, true)
                val genInvoke = '''«genTarget».«method.safeName»(«genArgs»)'''
                if(method.type.isArrayType && method.jvmMethod.returnType.type instanceof JvmArrayType) {
                    val componentType = ((method.type as RoleType).base as GenericClassRef).typeArg
                    '''«jvmGuardedArrayClassName».<«componentType.generate»[]>wrap(«genInvoke»)'''
                }
                else
                    genInvoke
            }
            else {
                val methodKind =
                    if(childTasksAnalysis.childTasksMayExist(it)) GUARDED_METHOD
                    else UNGUARDED_METHOD
                '''«target.genNested».«method.safeName»«methodKind.suffix»(«genArgs»)'''
            }
        }
        
        private def genGuardedMapped(Expr it, Role requiredRole, boolean nested) {
            val container = eContainer
            val annotated = switch(container) {
                MemberAccess case it === container.target: container.method.jvmMethod
                default: destParam.jvmParam
            }
            
            if(annotated.isSafe) generate else genGuarded(requiredRole, nested)
        }
        
        private def genArgs(Argumented it) {
            val allArgs =
                if(executable.isMapped)
                    new ArrayList(args.map[genCoerced])
                else
                    new ArrayList(args.map[generate])
            
            if(it instanceof MemberAccess && (it as MemberAccess).method.isAsync)
                if(methodKind == TASK)
                    allArgs += jvmTasksClassName + ".NO_OP_INSTANCE"
                else
                    allArgs += "$tasks"
            
            if(!executable.isMapped) {
                if(enclosingExecutable instanceof FieldInitializer)
                    allArgs += taskClassName + ".currentTask().idBits()"
                else
                    allArgs += "$task"
            }
            
            allArgs.join(", ")
        }
        
        private def CharSequence genCoerced(Expr it) {
            val paramType = destParam.type
            val originalParamType = destParam.original.type
            val jvmParamType = destParam.jvmParam.parameterType.type
            val reqRole =
                if(originalParamType instanceof RoleType) originalParamType.role.erased
                else if(paramType instanceof RoleType)    paramType.role
            if(paramType.isArrayType && jvmParamType instanceof JvmArrayType) {
                val arrayType = jvmParamType.toString.substring(14) // IMPROVE: A little less magic, a little more robustness, please
                '''«jvmGuardedArrayClassName».unwrap(«genGuardedMapped(reqRole, false)», «arrayType».class)'''
            }
            else
                genGuardedMapped(reqRole, false)
        }
        
        private def generateTaskStart(MemberAccess it) {
            val start = '''«jvmTaskSystemClassName».getDefault().start(«target.genNested».«method.name»«TASK.suffix»(«args.map[generate].join(", ")»))'''
            if(methodKind == TASK)
                start
            else
                '''$tasks.addInline(«start»)'''
        }
        
        private def dispatch CharSequence generate(This it)  {
            if(methodKind == TASK) '''«enclosingClass.safeSimpleName».this'''
            else '''this'''
        }
        
        private def dispatch CharSequence generate(Super it)  {
            if(methodKind == TASK) '''«enclosingClass.safeSimpleName».super'''
            else '''super'''
        }
        
        private def dispatch CharSequence generate(VarRef it) { variable.safeName }
        
        private def dispatch CharSequence generate(New it) {
            val generatedArgs = 
                if(classRef.clazz.isArrayClass || classRef.clazz.isVectorBuilderClass) {
                    val componentType = (classRef as GenericClassRef).typeArg
                    val withoutSize = '''new «componentType.generateErased»[]'''
                    val matcher = bracketPattern.matcher(withoutSize)
                    matcher.find
                    withoutSize.substring(0, matcher.start) + args.head.generate + withoutSize.substring(matcher.start)
                }
                else
                    genArgs
            '''new «classRef.generate»(«generatedArgs»)'''
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
        
        private def dispatch CharSequence generate( DoubleLiteral it) { value }
        private def dispatch CharSequence generate(   LongLiteral it) { value + "L" }
        private def dispatch CharSequence generate(    IntLiteral it) { value.toString }
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
        
        private def genGuarded(Expr it, Role requiredRole, boolean nested) {
            val type = system.type(it).value
            val needsGuard = childTasksAnalysis.childTasksMayExist(it)
                    && !system.subroleSucceeded(roleAnalysis.dynamicRole(it), requiredRole)
            if(utils.isGuarded(type) && needsGuard) {
                val slice = if((type as RoleType).isSliced) "Slice" else ""
                switch(requiredRole) {
                    ReadWrite: "guardReadWrite" + slice + "(" + generate + ", $task)"
                    ReadOnly : "guardReadOnly"  + slice + "(" + generate + ", $task)"
                    default  : throw new AssertionError("unexpected role: " + requiredRole)
                }
            }
            else
                if(nested) genNested else generate
            
            // IMPROVE: Better syntax (and performance?) when using type system
        }
    }
    
    /* VarKind */
    
    static def generate(VarKind it) { if(it == VAL) "final " }
}
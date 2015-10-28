package ch.trick17.rolez.lang.generator

import ch.trick17.rolez.lang.RolezExtensions
import ch.trick17.rolez.lang.rolez.ArithmeticBinaryExpr
import ch.trick17.rolez.lang.rolez.Assignment
import ch.trick17.rolez.lang.rolez.BinaryExpr
import ch.trick17.rolez.lang.rolez.Block
import ch.trick17.rolez.lang.rolez.BooleanLiteral
import ch.trick17.rolez.lang.rolez.Cast
import ch.trick17.rolez.lang.rolez.CharLiteral
import ch.trick17.rolez.lang.rolez.Class
import ch.trick17.rolez.lang.rolez.ClassRef
import ch.trick17.rolez.lang.rolez.Constr
import ch.trick17.rolez.lang.rolez.DoubleLiteral
import ch.trick17.rolez.lang.rolez.EqualityExpr
import ch.trick17.rolez.lang.rolez.Expr
import ch.trick17.rolez.lang.rolez.ExprStmt
import ch.trick17.rolez.lang.rolez.Field
import ch.trick17.rolez.lang.rolez.GenericClassRef
import ch.trick17.rolez.lang.rolez.IfStmt
import ch.trick17.rolez.lang.rolez.IntLiteral
import ch.trick17.rolez.lang.rolez.LocalVarDecl
import ch.trick17.rolez.lang.rolez.LogicalExpr
import ch.trick17.rolez.lang.rolez.MemberAccess
import ch.trick17.rolez.lang.rolez.Method
import ch.trick17.rolez.lang.rolez.New
import ch.trick17.rolez.lang.rolez.Null
import ch.trick17.rolez.lang.rolez.NullLiteral
import ch.trick17.rolez.lang.rolez.Param
import ch.trick17.rolez.lang.rolez.Parenthesized
import ch.trick17.rolez.lang.rolez.PrimitiveType
import ch.trick17.rolez.lang.rolez.Program
import ch.trick17.rolez.lang.rolez.RelationalExpr
import ch.trick17.rolez.lang.rolez.ReturnExpr
import ch.trick17.rolez.lang.rolez.ReturnNothing
import ch.trick17.rolez.lang.rolez.RoleType
import ch.trick17.rolez.lang.rolez.SimpleClassRef
import ch.trick17.rolez.lang.rolez.Start
import ch.trick17.rolez.lang.rolez.Stmt
import ch.trick17.rolez.lang.rolez.StringLiteral
import ch.trick17.rolez.lang.rolez.SuperConstrCall
import ch.trick17.rolez.lang.rolez.This
import ch.trick17.rolez.lang.rolez.Type
import ch.trick17.rolez.lang.rolez.TypeParamRef
import ch.trick17.rolez.lang.rolez.UnaryExpr
import ch.trick17.rolez.lang.rolez.UnaryMinus
import ch.trick17.rolez.lang.rolez.UnaryNot
import ch.trick17.rolez.lang.rolez.VarRef
import ch.trick17.rolez.lang.rolez.WhileLoop
import ch.trick17.rolez.lang.typesystem.RolezUtils
import java.io.File
import javax.inject.Inject
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.generator.IFileSystemAccess
import org.eclipse.xtext.generator.IGenerator

import static ch.trick17.rolez.lang.Constants.*
import static ch.trick17.rolez.lang.rolez.VarKind.VAL

import static extension org.eclipse.xtext.util.Strings.convertToJavaString
import ch.trick17.rolez.lang.rolez.VarKind

class RolezGenerator implements IGenerator {
    
    @Inject extension RolezExtensions
    @Inject RolezUtils utils
    
    public static val mappedClasses = #{
        objectClassName -> "java.lang.Object",
        stringClassName -> "java.lang.String",
        arrayClassName  -> null
    }
    
    override void doGenerate(Resource resource, IFileSystemAccess fsa) {
        val program = resource.contents.head as Program
        for (c : program.classes.filter[!mapped]) {
            val name = c.qualifiedName.segments.join(File.separator) + ".java"
            fsa.generateFile(name, c.generate(program))
        }
    }
    
    /*
     * Class and members
     */
    
    private def generate(Class it, Program p) {'''
        «if(!package.isEmpty)
        '''
        package «package»;
        
        '''»
        public class «simpleName» extends «actualSuperclass?.generateName?:"java.lang.Object"» {
            «fields.map[gen].join»
            «constrs.map[gen].join»
            «methods.map[gen].join»
        }
    '''}
    
    private def gen(Field it) {'''
        public «kind.gen»«type.gen» «name»;
    '''}
    
    private def gen(Constr it) {'''
        
        public «enclosingClass.simpleName»(«params.map[gen].join(", ")»)«body.gen»
    '''}
    
    private def gen(Method it) {'''
        
        public «type.gen» «name»(«params.map[gen].join(", ")»)«body.gen»
    '''}
    
    private def gen(Param it) {'''«kind.gen»«type.gen» «name»'''}
    
    private def gen(VarKind it) { if(it == VAL) "final " else "" }
    
    /*
     * Statements
     */
    
    private def CharSequence gen(Stmt it) { generateStmt }
    
    private def dispatch CharSequence genIndent(Block it) { gen }
    private def dispatch CharSequence genIndent(Stmt it) {
        '''
        
            «gen»
        '''
    }
    
    private def dispatch generateStmt(Block it) {'''
         {
            «stmts.map[gen].join»
        }
    '''}
    
    private def dispatch generateStmt(LocalVarDecl it) {
        if(initializer == null) '''
            «variable.kind.gen»«variable.type.gen» «variable.name»;
        ''' else '''
            «variable.kind.gen»«variable.type.gen» «variable.name» = «initializer.gen»;
        '''
    }
    
    private def dispatch generateStmt(IfStmt it) {'''
        if(«condition.gen»)«thenPart.genIndent»
        «if(elsePart != null)
        '''else«elsePart.genIndent»'''»
    '''}
    
    private def dispatch generateStmt(WhileLoop it) {'''
        while(«condition.gen»)«body.genIndent»
    '''}
    
    private def dispatch generateStmt(SuperConstrCall it) {'''
        super(«args.map[gen].join(", ")»);
    '''}
    
    private def dispatch generateStmt(ReturnNothing _) {'''
        return;
    '''}
    
    private def dispatch generateStmt(ReturnExpr it) {'''
        return «expr.gen»;
    '''}
    
    /* Java only allows certain kinds of "expression statements", so find
     * the corresponding expressions in the rolez expression tree */
    private def dispatch generateStmt(ExprStmt it) {'''
        «findSideFxExpr(expr).map[gen + ";\n"].join»
    '''}
    
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
    private def dispatch CharSequence genNested(BinaryExpr it) { "(" + gen + ")" }
    private def dispatch CharSequence genNested( UnaryExpr it) { "(" + gen + ")" }
    private def dispatch CharSequence genNested(      Cast it) { "(" + gen + ")" }
    private def dispatch CharSequence genNested(      Expr it) { gen }
    
    private def dispatch generateExpr(Assignment it) {
        '''«left.gen» = «right.gen»'''
    }
    
    private def dispatch generateExpr(BinaryExpr it) {
        val op = switch(it) {
            LogicalExpr: op
            EqualityExpr: op
            RelationalExpr: op
            ArithmeticBinaryExpr: op
        }
        '''«left.genNested» «op» «right.genNested»'''
    }
    
    private def dispatch generateExpr(Cast it) {
        '''(«type.gen») «expr.genNested»'''
    }
    
    private def dispatch generateExpr(UnaryMinus it) {
        '''-«expr.genNested»'''
    }
    
    private def dispatch generateExpr(UnaryNot it) {
        '''!«expr.genNested»'''
    }
    
    private def dispatch generateExpr(MemberAccess it) {
        // TODO: guard
        switch(it) {
            case isMethodInvoke && method.isArrayGet:
                '''«target.gen»[«args.get(0).gen»]'''
            case isMethodInvoke && method.isArraySet:
                '''«target.gen»[«args.get(0).gen»] = «args.get(1).gen»'''
            case isMethodInvoke:
                '''«target.genNested».«method.name»(«args.map[gen].join(", ")»)'''
            case isFieldAccess:
                '''«target.genNested».«field.name»'''
        }
    }
    
    private def dispatch generateExpr(This _) {'''this'''}
    
    private def dispatch generateExpr(VarRef it) { variable.name }
    
    private def dispatch generateExpr(New it) {
        if(classRef.clazz.qualifiedName == arrayClassName) {
            if(args.size != 1) throw new AssertionError
            
            val ref = classRef as GenericClassRef
            val emptyBrackets = (1 ..< arrayNesting(ref)).map["[]"].join
            '''new «elemType(ref).gen»[«args.head.gen»]«emptyBrackets»'''
        }
        else
            '''new «classRef.gen»(«args.map[gen].join(", ")»)'''
    }
    
    private def int arrayNesting(GenericClassRef it) {
        if(clazz.qualifiedName != arrayClassName)
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
        if(clazz.qualifiedName != arrayClassName)
            throw new AssertionError
        
        val arg = typeArg
        switch(arg) {
            RoleType case arg.base instanceof GenericClassRef:
                elemType(arg.base as GenericClassRef)
            default:
                arg
        }
    }
    
    private def dispatch generateExpr(Start it) {
        '''null /* TODO */'''
    }
    
    private def dispatch generateExpr(Parenthesized it) {'''«expr.gen»'''}
    
    private def dispatch generateExpr(    IntLiteral it) { value.toString }
    private def dispatch generateExpr( DoubleLiteral it) { value.toString }
    private def dispatch generateExpr(BooleanLiteral it) { value.toString }
    
    private def dispatch generateExpr(StringLiteral it) {
        "\"" + value.convertToJavaString + "\""
    }
    
    private def dispatch generateExpr(CharLiteral it) {
        "'" + value.toString.convertToJavaString.replace("\\\"", "\"").replace("'", "\\'") + "'"
    }
    
    private def dispatch generateExpr(NullLiteral _) {'''null'''}
    
    /*
     * Types and class refs
     */
    
    private def CharSequence gen(Type it) { generateType }
    
    private def dispatch generateType(PrimitiveType it) { string }
    
    private def dispatch generateType(RoleType it) { base.gen }
    
    private def dispatch generateType(Null it) {
        throw new AssertionError("Null type usage not checked")
    }
    
    private def dispatch generateType(TypeParamRef _) { throw new AssertionError }
    
    private def gen(ClassRef it) { generateClassRef }
    
    private def dispatch generateClassRef(SimpleClassRef it) {
        clazz.generateName
    }
    
    private def dispatch generateClassRef(GenericClassRef it) {
        if(clazz.qualifiedName == arrayClassName)
            '''«typeArg.gen»[]'''
        else
            '''«clazz.generateName»<«typeArg.gen»>'''
    }
    
    private def generateName(Class it) {
        if(mapped) {
            val name = mappedClasses.get(qualifiedName)
            if(name == null) throw new AssertionError
            name
        }
        else
            qualifiedName.toString
    }
}

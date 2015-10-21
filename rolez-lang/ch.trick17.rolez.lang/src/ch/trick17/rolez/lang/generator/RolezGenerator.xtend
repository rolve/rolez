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
import ch.trick17.rolez.lang.rolez.FieldSelector
import ch.trick17.rolez.lang.rolez.GenericClassRef
import ch.trick17.rolez.lang.rolez.IfStmt
import ch.trick17.rolez.lang.rolez.IntLiteral
import ch.trick17.rolez.lang.rolez.LocalVarDecl
import ch.trick17.rolez.lang.rolez.LogicalExpr
import ch.trick17.rolez.lang.rolez.MemberAccess
import ch.trick17.rolez.lang.rolez.Method
import ch.trick17.rolez.lang.rolez.MethodSelector
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

class RolezGenerator implements IGenerator {
    
    @Inject extension RolezExtensions
    @Inject RolezUtils utils
    
    private static val specialClassesMap = #{
        objectClassName -> "java.lang.Object",
        stringClassName -> "java.lang.String",
        arrayClassName  -> null
    }
    
    override void doGenerate(Resource resource, IFileSystemAccess fsa) {
        val program = resource.contents.head as Program
        val filtered = program.classes.filter[
            !specialClassesMap.containsKey(qualifiedName)
        ]
        
        for (c : filtered) {
            val name = c.qualifiedName.segments.join(File.separator) + ".java"
            fsa.generateFile(name, c.generate(program))
        }
    }
    
    /*
     * Class and members
     */
    
    private def generate(Class it, Program p) {'''
        «if(!package.isEmpty) '''package «package»;'''»
        
        «p.imports.map[importedNamespace].join('''
        ''')»
        public class «simpleName» extends «actualSuperclass?.generateName?:"java.lang.Object"» {
            
            «fields.map[gen].join»
            
            «constructors.map[gen].join("\n")»
            
            «methods.map[gen].join("\n")»
        }
    '''}
    
    private def gen(Field it) {'''
        public «type.gen» «name»;
    '''}
    
    private def gen(Constr it) {'''
        public «enclosingClass.simpleName»(«params.map[gen].join(", ")») «body.gen»
    '''}
    
    private def gen(Method it) {'''
        public «type.gen» «name»(«params.map[gen].join(", ")») «body.gen»
    '''}
    
    private def gen(Param it) {'''«type.gen» «name»'''}
    
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
            «variable.type.gen» «variable.name»;
        ''' else '''
            «variable.type.gen» «variable.name» = «initializer.gen»;
        '''
    }
    
    private def dispatch generateStmt(IfStmt it) {'''
        if(«condition.gen») «thenPart.genIndent»
        «if(elsePart != null) '''else «elsePart.genIndent»'''»
    '''}
    
    private def dispatch generateStmt(WhileLoop it) {'''
        while(«condition.gen») «body.genIndent»
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
            // Special case for array instantiations:
            New: {
                if(args.size != 1) throw new AssertionError
                findSideFxExpr(args.head)
            }
            default: emptyList
        }
    }
    
    /*
     * Expressions
     */
    
    private def CharSequence gen(Expr it) { generateExpr }
    
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
        '''«left.gen» «op» («right.gen»)'''
    }
    
    private def dispatch generateExpr(Cast it) {
        '''((«type.gen») «expr.gen»)'''
    }
    
    private def dispatch generateExpr(UnaryMinus it) {
        '''-(«expr.gen»)'''
    }
    
    private def dispatch generateExpr(UnaryNot it) {
        '''!(«expr.gen»)'''
    }
    
    private def dispatch generateExpr(MemberAccess it) {
        // TODO: guard
        // TODO: access to special classes
        '''«target.gen».«selector.generateSelector»'''
    }
    
    private def dispatch generateSelector( FieldSelector it) { field.name }
    
    private def dispatch generateSelector(MethodSelector it) {
        '''«method.name»(«args.map[gen].join(", ")»)'''
    }
    
    private def dispatch generateExpr(This _) {'''this'''}
    
    private def dispatch generateExpr(VarRef it) { variable.name }
    
    private def dispatch generateExpr(New it) {
        if(classRef.clazz.qualifiedName == arrayClassName) {
            if(args.size != 1) throw new AssertionError
            
            val ref = classRef as GenericClassRef
            val emptyBrackets = (1 .. arrayNesting(ref)).map["[]"].join
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
        '''"«value.replace("\\", "\\\\").replace("\"", "\\\"")»"'''
    }
    
    private def dispatch generateExpr(CharLiteral it) {
        ''''«value.toString.replace("\\", "\\\\").replace("'", "\\'")»' '''
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
        val name = specialClassesMap.getOrDefault(qualifiedName, qualifiedName.toString)
        if(name == null) throw new AssertionError
        name
    }
}

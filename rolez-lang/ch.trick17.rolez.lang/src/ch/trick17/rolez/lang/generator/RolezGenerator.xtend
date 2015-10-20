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
import ch.trick17.rolez.lang.rolez.UnaryMinus
import ch.trick17.rolez.lang.rolez.UnaryNot
import ch.trick17.rolez.lang.rolez.VarRef
import ch.trick17.rolez.lang.rolez.WhileLoop
import java.io.File
import javax.inject.Inject
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.generator.IFileSystemAccess
import org.eclipse.xtext.generator.IGenerator

import static ch.trick17.rolez.lang.Constants.*

class RolezGenerator implements IGenerator {
    
    @Inject extension RolezExtensions
    
    override void doGenerate(Resource resource, IFileSystemAccess fsa) {
        val program = resource.contents.head as Program
        for (c : program.classes) {
            val name = c.qualifiedName.segments.join(File.separator) + ".java"
            fsa.generateFile(name, generate(c, program))
        }
    }
    
    /*
     * Class and members
     */
    
    private def generate(Class it, Program p) {'''
        «if(!package.isEmpty) '''package «package»;'''»
        
        «p.imports.map[importedNamespace].join('''
        ''')»
        public class «simpleName» extends «actualSuperclass?.qualifiedName?:"java.lang.Object"» {
            
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
        if(«condition.gen») «thenPart.gen»
        «if(elsePart != null) '''else «elsePart.gen»'''»
    '''}
    
    private def dispatch generateStmt(WhileLoop it) {'''
        while(«condition.gen») «body.gen»
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
    
    private def dispatch generateStmt(ExprStmt it) {'''
        «expr.gen»;
    '''}
    
    /*
     * Expressions
     */
    
    private def CharSequence gen(Expr it) { generateExpr }
    
    private def dispatch generateExpr(Assignment it) {
        '''«left.gen» = («right.gen»)'''
    }
    
    private def dispatch generateExpr(BinaryExpr it) {
        val op = switch(it) {
            LogicalExpr: op
            EqualityExpr: op
            RelationalExpr: op
            ArithmeticBinaryExpr: op
        }
        '''(«left.gen») «op» «right.gen»'''
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
        '''«target.gen».«selector.generateSelector»'''
    }
    
    private def dispatch generateSelector( FieldSelector it) { field.name }
    
    private def dispatch generateSelector(MethodSelector it) {
        '''«method.name»(«args.map[gen].join(", ")»)'''
    }
    
    private def dispatch generateExpr(This _) {'''this'''}
    
    private def dispatch generateExpr(VarRef it) { variable.name }
    
    private def dispatch generateExpr(New it) {
        '''new «classRef.gen»(«args.map[gen].join(", ")»)'''
    }
    
    private def dispatch generateExpr(Start it) {
        '''null /* TODO */'''
    }
    
    private def dispatch generateExpr(Parenthesized it) {'''(«expr.gen»)'''}
    
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
    private def dispatch generateType(     RoleType it) { base.gen }
    private def dispatch generateType(         Null it) { "java.lang.Void" }
    
    private def gen(ClassRef it) { generateClassRef }
    
    private def dispatch generateClassRef(SimpleClassRef it) {
        clazz.qualifiedName.toString
    }
    
    private def dispatch generateClassRef(GenericClassRef it) {
        if(clazz.qualifiedName == arrayClassName)
            '''«typeArg.gen»[]'''
        else
            '''«clazz.qualifiedName»<«typeArg.gen»>'''
    }
}

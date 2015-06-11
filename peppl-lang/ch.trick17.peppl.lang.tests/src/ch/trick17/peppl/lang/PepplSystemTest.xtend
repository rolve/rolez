package ch.trick17.peppl.lang

import ch.trick17.peppl.lang.peppl.Boolean
import ch.trick17.peppl.lang.peppl.Expression
import ch.trick17.peppl.lang.peppl.ExpressionStatement
import ch.trick17.peppl.lang.peppl.Int
import ch.trick17.peppl.lang.peppl.PepplPackage
import ch.trick17.peppl.lang.peppl.Program
import ch.trick17.peppl.lang.peppl.Role
import ch.trick17.peppl.lang.peppl.RoleType
import ch.trick17.peppl.lang.peppl.Type
import ch.trick17.peppl.lang.typesystem.PepplSystem
import ch.trick17.peppl.lang.typesystem.PepplTypeUtils
import javax.inject.Inject
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.eclipse.xtext.junit4.validation.ValidationTestHelper
import org.junit.Test
import org.junit.runner.RunWith

import static ch.trick17.peppl.lang.typesystem.PepplSystem.*
import static org.hamcrest.Matchers.*

import static extension org.hamcrest.MatcherAssert.assertThat

@RunWith(XtextRunner)
@InjectWith(PepplInjectorProvider)
class PepplSystemTest {
    
    val PepplPackage peppl = PepplPackage.eINSTANCE
    
    @Inject extension ParseHelper<Program>
    @Inject extension ValidationTestHelper
    @Inject extension PepplSystem system
    @Inject extension PepplTypeUtils
    
    @Test
    def testTAssignment() {
        val program = parse('''
            class Object
            class A
            class B extends A
            main {
                var a: readwrite A;
                a = new B;
            }
        ''')
        
        val type = program.mainExpr.type.asRoleType
        type.role.assertThat(is(Role.READWRITE))
        type.base.assertThat(is(program.classes.findFirst[name=="A"]))
    }
    
    @Test
    def void testTAssignmentErrorInOp() {
        parse("main { !5 = 5; }")
            .assertError(peppl.intLiteral, SUBTYPEEXPRESSION, "int", "boolean")
        
        parse('''
            main {
                var x: int;
                x = !5;
            }
        ''').assertError(peppl.intLiteral, SUBTYPEEXPRESSION, "int", "boolean")
    }
    
    @Test
    def void testTAssignmentNotAssignable() {
        parse('''
            main {
                val x: int;
                x = 5;
            }
        ''').assertError(peppl.variableRef, AVARIABLEREF, "assign", "value")
    }
    
    @Test
    def void testTAssignmentTypeMismatch() {
        parse('''
            main {
                var x: int;
                x = true;
            }
        ''').assertError(peppl.booleanLiteral, SUBTYPEEXPRESSION, "int", "boolean")
    }
    
    @Test
    def testTBooleanExpression() {
        parse("main { true || false; }").mainExpr.type.assertThat(instanceOf(Boolean))
        parse("main { true && false; }").mainExpr.type.assertThat(instanceOf(Boolean))
    }
    
    @Test
    def void testTBooleanExpressionErrorInOp() {
        parse("main { !5 || false; }")
            .assertError(peppl.intLiteral, SUBTYPEEXPRESSION, "int", "boolean")
        parse("main { true || !5; }")
            .assertError(peppl.intLiteral, SUBTYPEEXPRESSION, "int", "boolean")
    }
    
    @Test
    def void testTBooleanExpressionInvalidType() {
        parse("main { 5 || false; }")
            .assertError(peppl.intLiteral, SUBTYPEEXPRESSION, "int", "boolean")
        parse("main { true || 5; }")
            .assertError(peppl.intLiteral, SUBTYPEEXPRESSION, "int", "boolean")
    }
    
    @Test
    def testTEqualityExpression() {
        parse("main { true == false; }").mainExpr.type.assertThat(instanceOf(Boolean))
        parse("main { 5 != 3; }").mainExpr.type.assertThat(instanceOf(Boolean))

        parse('''
            class Object
            class A
            main {
                new Object == new A;
                new A == new Object;
                new A == new A;
            }
        ''').assertNoErrors
    }
    
    @Test
    def void testTEqualityExpressionErrorInOp() {
        parse("main { !5 == false; }")
            .assertError(peppl.intLiteral, SUBTYPEEXPRESSION, "int", "boolean")
        parse("main { true != !5; }")
            .assertError(peppl.intLiteral, SUBTYPEEXPRESSION, "int", "boolean")
    }
    
    @Test
    def testTEqualityExpressionIncompatibleTypes() {
        parse('''
            class Object
            class A
            class B
            main { new A == new B; }
        ''').assertError(peppl.equalityExpression, null, "compare", "A", "B")
        // IMPROVE: Find a way to include an issue code for explicit failures?
        
        parse("main { 42 != false; }")
            .assertError(peppl.equalityExpression, null, "compare", "int", "boolean")
    }
    
    @Test
    def testTRelationalExpression() {
        parse("main {   5 <    6; }").mainExpr.type.assertThat(instanceOf(Boolean))
        parse("main {  -1 <= -10; }").mainExpr.type.assertThat(instanceOf(Boolean))
        parse("main { 'a' >  ' '; }").mainExpr.type.assertThat(instanceOf(Boolean))
        parse("main { 3+4 >=   0; }").mainExpr.type.assertThat(instanceOf(Boolean))
    }
    
    @Test
    def void testTRelationalExpressionErrorInOp() {
        parse("main { -true < 0; }")
            .assertError(peppl.booleanLiteral, SUBTYPEEXPRESSION, "int", "boolean")
        parse("main { 100 <= -false; }")
            .assertError(peppl.booleanLiteral, SUBTYPEEXPRESSION, "int", "boolean")
        parse("main { -'a' > 0; }")
            .assertError(peppl.charLiteral, SUBTYPEEXPRESSION, "int", "char")
        parse("main { 100 >= -false; }")
            .assertError(peppl.booleanLiteral, SUBTYPEEXPRESSION, "int", "boolean")
    }
    
    @Test
    def testTRelationalExpressionIncompatibleTypes() {
        parse('''
            class Object
            main { new Object < new Object; }
        ''').assertError(peppl.relationalExpression, null, "compare", "Object")
        
        parse("main { true <= false; }")
            .assertError(peppl.relationalExpression, null, "compare", "boolean")
        parse("main { null > null; }")
            .assertError(peppl.relationalExpression, null, "compare", "null")
        parse("main { 5 > '5'; }")
            .assertError(peppl.relationalExpression, null, "compare", "int", "char")
        parse("main { true > '5'; }")
            .assertError(peppl.relationalExpression, null, "compare", "boolean", "char")
    }
    
    @Test
    def testTArithmeticExpression() {
        parse("main {   4 +  4; }").mainExpr.type.assertThat(instanceOf(Int))
        parse("main {   0 -  0; }").mainExpr.type.assertThat(instanceOf(Int))
        parse("main {   3 *  2; }").mainExpr.type.assertThat(instanceOf(Int))
        parse("main { 100 / -1; }").mainExpr.type.assertThat(instanceOf(Int))
        parse("main { -99 %  3; }").mainExpr.type.assertThat(instanceOf(Int))
        
        parse('''
            class Object
            class String
            main { "Hi" + " World"; }
        ''').mainExpr.type.asRoleType.base.name.assertThat(is("String"))
        parse('''
            class Object
            class String
            main { "" + '5'; }
        ''').mainExpr.type.asRoleType.base.name.assertThat(is("String"))
        parse('''
            class Object
            class String
            main { null + " "; }
        ''').mainExpr.type.asRoleType.base.name.assertThat(is("String"))
    }
    
    @Test
    def void testTArithmeticExpressionErrorInOp() {
        parse("main { !'a' + 0; }")
            .assertError(peppl.charLiteral, SUBTYPEEXPRESSION, "char", "boolean")
        parse("main { 100 - -false; }")
            .assertError(peppl.booleanLiteral, SUBTYPEEXPRESSION, "int", "boolean")
        parse("main { -'a' * 0; }")
            .assertError(peppl.charLiteral, SUBTYPEEXPRESSION, "int", "char")
        parse("main { 100 / -true; }")
            .assertError(peppl.booleanLiteral, SUBTYPEEXPRESSION, "int", "boolean")
        parse("main { (3*3) % !42; }")
            .assertError(peppl.intLiteral, SUBTYPEEXPRESSION, "int", "boolean")
    }
    
    @Test
    def testTArtithmeticExpressionInvalidType() {
        parse('''
            class Object
            main { new Object + new Object; }
        ''').assertError(peppl.arithmeticExpression, null, "operator", "undefined", "object")
        parse('''
            class Object
            class A
            class B
            main { new A - new B; }
        ''').assertError(peppl.arithmeticExpression, null, "operator", "undefined", "A", "B")
        
        parse('''
            class Object
            class String
            main { "Hello" - "World"; }
        ''').assertError(peppl.arithmeticExpression, null, "operator", "undefined", "String")
        parse('''
            class Object
            class String
            main { "Hello" * new Object; }
        ''').assertError(peppl.arithmeticExpression, null, "operator", "undefined", "String", "Object")
        parse('''
            class Object
            class String
            main { 5 / "World"; }
        ''').assertError(peppl.arithmeticExpression, null, "operator", "undefined", "int", "String")
        parse('''
            class Object
            class String
            main { null % "World"; }
        ''').assertError(peppl.arithmeticExpression, null, "operator", "undefined", "null", "String")
        
        parse("main { 'a' * 'b'; }")
            .assertError(peppl.arithmeticExpression, null, "operator", "undefined", "char")
        parse("main { null / null; }")
            .assertError(peppl.arithmeticExpression, null, "operator", "undefined", "null")
        parse("main { 5 % '5'; }")
            .assertError(peppl.arithmeticExpression, null, "operator", "undefined", "int", "char")
        parse("main { true % '5'; }")
            .assertError(peppl.arithmeticExpression, null, "operator", "undefined", "boolean", "char")
    }
    
    @Test
    def testTUnaryMinus() {
        parse("main { -2; }").mainExpr.type.assertThat(instanceOf(Int))
        parse("main { val a: int = 5; -a; }").mainExpr.type.assertThat(instanceOf(Int))
        parse("main { -(4-4); }").mainExpr.type.assertThat(instanceOf(Int))
    }
    
    @Test
    def void testTUnaryMinusErrorInOp() {
        parse("main { -!5; }")
            .assertError(peppl.intLiteral, SUBTYPEEXPRESSION, "int", "boolean")
        parse("main { -(-'a'); }")
            .assertError(peppl.charLiteral, SUBTYPEEXPRESSION, "char", "int")
    }
    
    @Test
    def testTUnaryMinusInvalidType() {
        parse('''
            class Object
            main { -new Object; }
        ''').assertError(peppl.^new, SUBTYPEEXPRESSION, "Object", "int")
        parse('''
            class Object
            class String
            main { -"Hello"; }
        ''').assertError(peppl.stringLiteral, SUBTYPEEXPRESSION, "String", "int")
        
        parse("main { -'a'; }")
            .assertError(peppl.charLiteral, SUBTYPEEXPRESSION, "char", "int")
        parse("main { -true; }")
            .assertError(peppl.booleanLiteral, SUBTYPEEXPRESSION, "boolean", "int")
        parse("main { -null; }")
            .assertError(peppl.nullLiteral, SUBTYPEEXPRESSION, "null", "int")
    }
    
    @Test
    def testTUnaryNot() {
        parse("main { !true; }").mainExpr.type.assertThat(instanceOf(Boolean))
        parse("main { val a: boolean = false; !a; }").mainExpr.type.assertThat(instanceOf(Boolean))
        parse("main { !(true || false); }").mainExpr.type.assertThat(instanceOf(Boolean))
    }
    
    @Test
    def void testTUnaryNotErrorInOp() {
        parse("main { !(-'a'); }")
            .assertError(peppl.charLiteral, SUBTYPEEXPRESSION, "char", "int")
        parse("main { !(!5); }")
            .assertError(peppl.intLiteral, SUBTYPEEXPRESSION, "int", "boolean")
    }
    
    @Test
    def testTUnaryNotInvalidType() {
        parse('''
            class Object
            main { !new Object; }
        ''').assertError(peppl.^new, SUBTYPEEXPRESSION, "Object", "boolean")
        parse('''
            class Object
            class String
            main { !"Hello"; }
        ''').assertError(peppl.stringLiteral, SUBTYPEEXPRESSION, "String", "boolean")
        
        parse("main { !'a'; }")
            .assertError(peppl.charLiteral, SUBTYPEEXPRESSION, "char", "boolean")
        parse("main { !5; }")
            .assertError(peppl.intLiteral, SUBTYPEEXPRESSION, "int", "boolean")
        parse("main { !null; }")
            .assertError(peppl.nullLiteral, SUBTYPEEXPRESSION, "null", "boolean")
    }
    
    @Test
    def testTNew() {
        val program = parse('''
            class Object
            class A
            main { new A; }
        ''')
        
        val type = program.mainExpr.type.asRoleType
        type.role.assertThat(is(Role.READWRITE))
        type.base.assertThat(is(program.classes.findFirst[name=="A"]))
    }
    
    def mainExpr(Program program) {
        program.assertNoErrors
        program.main.body.statements.filter(ExpressionStatement).head.expr
    }
    
    def type(Expression expr) {
        val result = system.type(expr)
        result.failed.assertThat(is(false))
        result.value
    }
    
    def asRoleType(Type type) {
        type.assertThat(instanceOf(RoleType))
        type as RoleType
    }
}
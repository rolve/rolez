package ch.trick17.rolez.typesystem

import ch.trick17.rolez.TestUtils
import ch.trick17.rolez.rolez.Boolean
import ch.trick17.rolez.rolez.Char
import ch.trick17.rolez.rolez.Double
import ch.trick17.rolez.rolez.GenericClassRef
import ch.trick17.rolez.rolez.Int
import ch.trick17.rolez.rolez.Long
import ch.trick17.rolez.rolez.Short
import ch.trick17.rolez.rolez.Byte
import ch.trick17.rolez.rolez.Null
import ch.trick17.rolez.rolez.Program
import ch.trick17.rolez.rolez.Pure
import ch.trick17.rolez.rolez.ReadOnly
import ch.trick17.rolez.rolez.ReadWrite
import ch.trick17.rolez.rolez.RoleType
import ch.trick17.rolez.rolez.RolezFactory
import ch.trick17.rolez.tests.RolezInjectorProvider
import javax.inject.Inject
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.eclipse.xtext.junit4.validation.ValidationTestHelper
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import rolez.lang.Guarded

import static ch.trick17.rolez.Constants.*
import static ch.trick17.rolez.rolez.RolezPackage.Literals.*
import static ch.trick17.rolez.typesystem.RolezSystem.*
import static org.hamcrest.Matchers.*

import static extension org.hamcrest.MatcherAssert.assertThat

@RunWith(XtextRunner)
@InjectWith(RolezInjectorProvider)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class RolezTypeSystemTest {
    
    @Inject RolezSystem system
    @Inject extension RolezFactory
    @Inject extension TestUtils
    @Inject extension ParseHelper<Program>
    @Inject extension ValidationTestHelper
    
    @Test def void testTAssignment() {
        var program = parse('''
            class rolez.lang.Object mapped to java.lang.Object
            pure class rolez.lang.String mapped to java.lang.String
            class A {
                var d: double
                var l: long
                var i: int
            }
            class B extends A 
            class App {
                task pure main: {
                    var a: readwrite A;
                    a = new B;
                    a.d -= 42.0;
                    a.d *= 42;
                    a.l += 4L;
                    a.l /= 9;
                    a.i += 1;
                    a.i += 1 as short;
                    a.i += 1 as byte;
                    var sh: short;
                    sh = 1 as short;
                    var by: byte;
                    by = 1 as byte;
                    var b = true;
                    b &= false;
                    var s = "Hello";
                    s += " World";
                }
            }
        ''')
        program.task.expr(0).type.assertRoleType(ReadWrite, "A")
        program.task.expr(1).type.assertInstanceOf(Double)
        program.task.expr(2).type.assertInstanceOf(Double)
        program.task.expr(3).type.assertInstanceOf(Long)
        program.task.expr(4).type.assertInstanceOf(Long)
        program.task.expr(5).type.assertInstanceOf(Int)
        program.task.expr(6).type.assertInstanceOf(Int)
        program.task.expr(7).type.assertInstanceOf(Int)
        program.task.expr(8).type.assertInstanceOf(Short)
        program.task.expr(9).type.assertInstanceOf(Byte)
        program.task.expr(10).type.assertInstanceOf(Boolean)
        program.task.expr(11).type.assertRoleType(ReadWrite, stringClassName) // Slight inconsistency in the type system, but it is safe
    }
    
    @Test def testTAssignmentErrorInOp() {
        parse("!5 = 5;".withFrame).assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
        
        parse('''
            var i: int;
            i = !5;
        '''.withFrame).assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
        
        parse('''
            var i = 0;
            i += -false;
        '''.withFrame).assertError(ARITHMETIC_UNARY_EXPR, null, "operator", "-", "undefined", "boolean")
    }
    
    @Test def testTAssignmentNotAssignable() {
        parse("5 =  3;".withFrame).assertError(INT_LITERAL, AEXPR, "assign")
        parse("5 += 3;".withFrame).assertError(INT_LITERAL, AEXPR, "assign")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def pure foo: {}
            }
            class App {
                task pure main: {
                    new A.foo = 3;
                }
            }
        ''').assertError(MEMBER_ACCESS, AMEMBERACCESS, "assign")
        
        parse('''
            val x: int = 5;
            x = 5;
        '''.withFrame).assertError(VAR_REF, AVARREF, "assign", "value")
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                val x: int
                def readwrite foo: {
                    this.x = 4;
                }
            }
        ''').assertError(MEMBER_ACCESS, null, "assign", "value field")
    }
    
    @Test def testTAssignmentTypeMismatch() {
        parse('''
            var i: int;
            i = true;
        '''.withFrame).assertError(BOOLEAN_LITERAL, SUBTYPEEXPR, "int", "boolean")
        parse('''
            var i: int = 0;
            var s: short;
            s = i;
        '''.withFrame).assertError(VAR_REF, SUBTYPEEXPR, "short", "int")
        parse('''
            var i: int;
            i /= true;
        '''.withFrame).assertError(ASSIGNMENT, null, "operator", "undefined", "int", "boolean")
        
        parse('''
            var o = new Object;
            o += new Object;
        '''.withFrame).assertError(ASSIGNMENT, null, "operator", "undefined", "Object")
        parse('''
            var o = new Object;
            o /= new Object;
        '''.withFrame).assertError(ASSIGNMENT, null, "operator", "undefined", "Object")
    }
    
    @Test def testTLogicalExpr() {
        parse("true || false;".withFrame).task.lastExpr.type.assertThat(instanceOf(Boolean))
        parse("true && false;".withFrame).task.lastExpr.type.assertThat(instanceOf(Boolean))
    }
    
    @Test def testTLogicalExprErrorInOp() {
        parse("  !5 || false;".withFrame).assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
        parse("true ||    !5;".withFrame).assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test def testTLogicalExprTypeMismatch() {
        parse("5 || false;".withFrame).assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
        parse("true  || 5;".withFrame).assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test def testTBitwiseExpr() {
        parse("4 | 2;".withFrame).task.lastExpr.type.assertThat(instanceOf(Int))
        parse("4 ^ 2;".withFrame).task.lastExpr.type.assertThat(instanceOf(Int))
        parse("4 & 2;".withFrame).task.lastExpr.type.assertThat(instanceOf(Int))
        
        parse("4 <<  2;".withFrame).task.lastExpr.type.assertThat(instanceOf(Int))
        parse("4 >>  2;".withFrame).task.lastExpr.type.assertThat(instanceOf(Int))
        parse("4 >>> 2;".withFrame).task.lastExpr.type.assertThat(instanceOf(Int))
        
        parse("4L <<  2 ;".withFrame).task.lastExpr.type.assertThat(instanceOf(Long))
        parse("4  >>  2L;".withFrame).task.lastExpr.type.assertThat(instanceOf(Long))
        parse("4L >>> 2L;".withFrame).task.lastExpr.type.assertThat(instanceOf(Long))
        
        // type is int if operands are of type short, byte or char
        parse("'a' | 'b';".withFrame).task.lastExpr.type.assertThat(instanceOf(Int))
        parse("'a' |  2 ;".withFrame).task.lastExpr.type.assertThat(instanceOf(Int))
        parse(" 4  | 'b';".withFrame).task.lastExpr.type.assertThat(instanceOf(Int))
        parse(" 4  | 'b';".withFrame).task.lastExpr.type.assertThat(instanceOf(Int))
        
        parse("4  | 2 as short;".withFrame).task.lastExpr.type.assertThat(instanceOf(Int))
        parse("4 as byte |   2;".withFrame).task.lastExpr.type.assertThat(instanceOf(Int))
    }
    
    @Test def testTBitwiseExprErrorInOp() {
        parse("  !5 | false;".withFrame).assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
        parse("true |    !5;".withFrame).assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test def testTBitwiseExprTypeMismatch() {
        parse("5 | false;".withFrame)
            .assertError(BITWISE_EXPR, null, "operator", "|", "undefined", "boolean")
        parse("true | 5;".withFrame)
            .assertError(BITWISE_EXPR, null, "operator", "|", "undefined", "boolean")
        parse("true | false;".withFrame)
            .assertError(BITWISE_EXPR, null, "operator", "|", "undefined", "boolean")
    }
    
    @Test def testTEqualityExpr() {
        parse("true == false;".withFrame).task.lastExpr.type.assertThat(instanceOf(Boolean))
        parse("   5 !=     3;".withFrame).task.lastExpr.type.assertThat(instanceOf(Boolean))

        parse('''
            new Object == new A;
            new A == new Object;
            new A == new A;
        '''.withFrame).assertNoErrors
    }
    
    @Test def testTEqualityExprErrorInOp() {
        parse("!5 == false;".withFrame).assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
        parse("true != !5; ".withFrame).assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test def testTEqualityExprIncompatibleTypes() {
        parse("new A == new B;".withFrame).assertError(EQUALITY_EXPR, null, "compare", "A", "B")
        // IMPROVE: Test issue code once supported for explicit failures
        
        parse("42 != false;".withFrame).assertError(EQUALITY_EXPR, null, "compare", "int", "boolean")
    }
    
    @Test def testTRelationalExpr() {
        parse("  5 <    6;".withFrame).task.lastExpr.type.assertThat(instanceOf(Boolean))
        parse(" -1 <= -10;".withFrame).task.lastExpr.type.assertThat(instanceOf(Boolean))
        parse("3+4 >=   0;".withFrame).task.lastExpr.type.assertThat(instanceOf(Boolean))
        
        parse("'a' >  ' ';".withFrame).task.lastExpr.type.assertThat(instanceOf(Boolean))
        
        parse(" 5.0 <  6.0;".withFrame).task.lastExpr.type.assertThat(instanceOf(Boolean))
        parse("-1.0 <= -10;".withFrame).task.lastExpr.type.assertThat(instanceOf(Boolean))
        parse("-1 <= -10.0;".withFrame).task.lastExpr.type.assertThat(instanceOf(Boolean))
    }
    
    @Test def testTRelationalExprErrorInOp() {
        parse("-true < 0;".withFrame)
            .assertError(ARITHMETIC_UNARY_EXPR, null, "operator", "-", "undefined", "boolean")
        parse("100 <= -false;".withFrame)
            .assertError(ARITHMETIC_UNARY_EXPR, null, "operator", "-", "undefined", "boolean")
        parse("100 >= -false;".withFrame)
            .assertError(ARITHMETIC_UNARY_EXPR, null, "operator", "-", "undefined", "boolean")
    }
    
    @Test def testTRelationalExprIncompatibleTypes() {
        parse("new Object < new Object;".withFrame).assertError(RELATIONAL_EXPR, null, "compare", "Object")
        
        parse("true <= false;".withFrame).assertError(RELATIONAL_EXPR, null, "compare", "boolean")
        parse("null >   null;".withFrame).assertError(RELATIONAL_EXPR, null, "compare", "null")
        parse("true >    '5';".withFrame).assertError(RELATIONAL_EXPR, null, "compare", "boolean", "char")
        parse("true >    5.0;".withFrame).assertError(RELATIONAL_EXPR, null, "compare", "boolean", "double")
    }
    
    @Test def testTArithmeticExpr() {
        parse("4.0 +  4.0;".withFrame).task.lastExpr.type.assertThat(instanceOf(Double))
        parse("0.0 -    0;".withFrame).task.lastExpr.type.assertThat(instanceOf(Double))
        parse("3.0 *    2;".withFrame).task.lastExpr.type.assertThat(instanceOf(Double))
        parse("100 / -1.0;".withFrame).task.lastExpr.type.assertThat(instanceOf(Double))
        parse("-99.0 %  3;".withFrame).task.lastExpr.type.assertThat(instanceOf(Double))
        
        parse("4L + 4 ;".withFrame).task.lastExpr.type.assertThat(instanceOf(Long))
        parse("0  - 0L;".withFrame).task.lastExpr.type.assertThat(instanceOf(Long))
        parse("4L / 2L;".withFrame).task.lastExpr.type.assertThat(instanceOf(Long))
        
        parse("  4 +  4;".withFrame).task.lastExpr.type.assertThat(instanceOf(Int))
        parse("  0 -  0;".withFrame).task.lastExpr.type.assertThat(instanceOf(Int))
        parse("  3 *  2;".withFrame).task.lastExpr.type.assertThat(instanceOf(Int))
        parse("100 / -1;".withFrame).task.lastExpr.type.assertThat(instanceOf(Int))
        parse("-99 %  3;".withFrame).task.lastExpr.type.assertThat(instanceOf(Int))
        
        parse("1 as short + 2;".withFrame).task.lastExpr.type.assertThat(instanceOf(Int))
        parse("1 +  2 as byte;".withFrame).task.lastExpr.type.assertThat(instanceOf(Int))
        parse("'H' +      'W';".withFrame).task.lastExpr.type.assertThat(instanceOf(Int))
        
        parse(''' "Hi" + " World";'''.withFrame).task.lastExpr.type
            .assertRoleType(ReadOnly, stringClassName)
        
        parse('''  "" + '5';'''.withFrame).task.lastExpr.type.assertRoleType(ReadOnly, stringClassName)
        parse('''  "" + 5.0;'''.withFrame).task.lastExpr.type.assertRoleType(ReadOnly, stringClassName)
        parse('''null + " ";'''.withFrame).task.lastExpr.type.assertRoleType(ReadOnly, stringClassName)
    }
    
    @Test def testTArithmeticExprErrorInOp() {
        parse("!'a' + 0;    ".withFrame).assertError(CHAR_LITERAL, SUBTYPEEXPR, "char", "boolean")
        parse("100 - -false;".withFrame).assertError(ARITHMETIC_UNARY_EXPR, null, "operator", "-", "undefined", "boolean")
        parse("100 / -true; ".withFrame).assertError(ARITHMETIC_UNARY_EXPR, null, "operator", "-", "undefined", "boolean")
        parse("(3*3) % !42; ".withFrame).assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test def testTArtithmeticExprTypeMismatch() {
        parse("new Object + new Object;".withFrame)
            .assertError(ARITHMETIC_BINARY_EXPR, null, "operator", "undefined", "Object")
        parse("new A - new B;".withFrame)
            .assertError(ARITHMETIC_BINARY_EXPR, null, "operator", "undefined", "A", "B")
        
        parse('''"Hello" - "World";'''.withFrame)
            .assertError(ARITHMETIC_BINARY_EXPR, null, "operator", "undefined", "String")
        parse('''"Hello" * new Object;'''.withFrame)
            .assertError(ARITHMETIC_BINARY_EXPR, null, "operator", "undefined", "String", "Object")
        parse('''5 / "World";'''.withFrame)
            .assertError(ARITHMETIC_BINARY_EXPR, null, "operator", "undefined", "int", "String")
        parse('''null % "World";'''.withFrame)
            .assertError(ARITHMETIC_BINARY_EXPR, null, "operator", "undefined", "null", "String")
        
        parse("null / null;".withFrame)
            .assertError(ARITHMETIC_BINARY_EXPR, null, "operator", "undefined", "null")
        parse("true % '5';".withFrame)
            .assertError(ARITHMETIC_BINARY_EXPR, null, "operator", "undefined", "boolean", "char")
    }
    
    @Test def testTCast() {
        // Redundant casts
        parse("5 as int;".withFrame).task.lastExpr.type.assertThat(instanceOf(Int))
        parse("true as boolean;".withFrame).task.lastExpr.type.assertThat(instanceOf(Boolean))
        parse("5.0 as double;".withFrame).task.lastExpr.type.assertThat(instanceOf(Double))
        
        parse("new Object as readwrite Object;".withFrame).task.lastExpr.type.assertRoleType(ReadWrite, objectClassName)
        
        // Upcasts
        var program = parse('''
            new A as readwrite Object;
            new A as readonly A;
            new A as pure A;
            null as readwrite A;
            null as readonly A;
            new Array[int](3) as readonly Array[int];
            new Array[pure A](0) as readonly Array[pure A];
        '''.withFrame)
        program.task.expr(0).type.assertRoleType(ReadWrite, objectClassName)
        program.task.expr(1).type.assertRoleType(ReadOnly , "A")
        program.task.expr(2).type.assertRoleType(Pure     , "A")
        program.task.expr(3).type.assertRoleType(ReadWrite, "A")
        program.task.expr(4).type.assertRoleType(ReadOnly, "A")
        program.task.expr(5).type.assertRoleType(ReadOnly, arrayClassName, Int)
        program.task.expr(6).type.assertInstanceOf(RoleType) => [
            role.assertThat(instanceOf(ReadOnly))
            base.assertInstanceOf(GenericClassRef) => [
                clazz.name.assertThat(is(arrayClassName.toString))
                typeArg.assertRoleType(Pure, "A")
            ]
        ]
        
        // Downcasts
        parse("new Object as readwrite A;".withFrame).task.lastExpr.type.assertRoleType(ReadWrite, "A")
    }
    
    @Test def testTCastErrorInOp() {
        parse("!5 as boolean;".withFrame).assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test def testTCastIllegal() {
        parse("5 as boolean;  ".withFrame).assertError(CAST, null, "cast", "int", "boolean")
        parse("false as int;  ".withFrame).assertError(CAST, null, "cast", "boolean", "int")
        parse("null as int;   ".withFrame).assertError(CAST, null, "cast", "null", "int")
        parse("null as double;".withFrame).assertError(CAST, null, "cast", "null", "double")
        parse("5 as ;         ".withFrame).assertError(CAST, null, "cast", "int", "void")
        
        parse("5 as readwrite Object;".withFrame)
            .assertError(CAST, null, "cast", "readwrite rolez.lang.Object", "int")
        parse("new Object as int;".withFrame)
            .assertError(CAST, null, "cast", "readwrite rolez.lang.Object", "int")
        parse("new A as readonly A as readwrite A;".withFrame)
            .assertError(CAST, null, "cast", "readwrite A", "readonly A")
        
        parse("new Array[boolean] as readwrite Array[int];".withFrame)
            .assertError(CAST, null, "cast", "readwrite rolez.lang.Array[boolean]", "readwrite rolez.lang.Array[int]")
        parse("new Array[pure Object] as readwrite Array[pure A];".withFrame)
            .assertError(CAST, null, "cast", "readwrite rolez.lang.Array[pure rolez.lang.Object]", "readwrite rolez.lang.Array[pure A]")
        parse("new Array[readwrite A] as readwrite Array[pure A];".withFrame)
            .assertError(CAST, null, "cast", "readwrite rolez.lang.Array[readwrite A]", "readwrite rolez.lang.Array[pure A]")
        parse("new Array[pure A] as readwrite Array[readwrite A];".withFrame)
            .assertError(CAST, null, "cast", "readwrite rolez.lang.Array[pure A]", "readwrite rolez.lang.Array[readwrite A]")
    }
    
    @Test def testTArithmeticUnaryExpr() {        
        parse("-2.0;           ".withFrame).task.lastExpr.type.assertThat(instanceOf(Double))
        parse("val d = 5.0; -d;".withFrame).task.lastExpr.type.assertThat(instanceOf(Double))
        parse("-(4-4.0);       ".withFrame).task.lastExpr.type.assertThat(instanceOf(Double))
    
        parse("-          2L;".withFrame).task.lastExpr.type.assertThat(instanceOf(Long))
        parse("-           2;".withFrame).task.lastExpr.type.assertThat(instanceOf(Int))
        parse("-(2 as short);".withFrame).task.lastExpr.type.assertThat(instanceOf(Int))
        parse("-(2 as  byte);".withFrame).task.lastExpr.type.assertThat(instanceOf(Int))
        parse("-         'a';".withFrame).task.lastExpr.type.assertThat(instanceOf(Int))
        
        parse("var d = 0.0; d++;".withFrame).task.lastExpr.type.assertThat(instanceOf(Double))
        parse("var l = 0L ; l--;".withFrame).task.lastExpr.type.assertThat(instanceOf(Long))
        parse("var i = 0  ; ++i;".withFrame).task.lastExpr.type.assertThat(instanceOf(Int))
        parse("var s = 0 as short; --s;".withFrame).task.lastExpr.type.assertThat(instanceOf(Short))
        parse("var y = 0 as byte; y++;".withFrame).task.lastExpr.type.assertThat(instanceOf(Byte))
        parse("var c = 'a'; c++;".withFrame).task.lastExpr.type.assertThat(instanceOf(Char))
    }
    
    @Test def testTArithmeticUnaryExprErrorInOp() {
        parse("-!5;    ".withFrame).assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test def testTArithmeticUnaryExprTypeMismatch() {
        parse('''-new Object;'''.withFrame)
            .assertError(ARITHMETIC_UNARY_EXPR, null, "operator", "-", "undefined", "Object")
        parse('''-"Hello";'''.withFrame)
            .assertError(ARITHMETIC_UNARY_EXPR, null, "operator", "-", "undefined", "String")
        
        parse("-true;".withFrame).assertError(ARITHMETIC_UNARY_EXPR, null, "operator", "-", "undefined", "boolean")
        parse("-null;".withFrame).assertError(ARITHMETIC_UNARY_EXPR, null, "operator", "-", "undefined", "null")
    }
    
    @Test def testTArithmeticUnaryExprNotAssignable() {
        parse("1++;".withFrame).assertError(INT_LITERAL, AEXPR, "assign")
        parse("val i = 1; i--;".withFrame).assertError(VAR_REF, AVARREF, "assign")
        parse("var i = 1; --(++i);".withFrame).assertError(PARENTHESIZED, AEXPR, "assign")
    }
    
    @Test def testTLogicalNot() {
        parse("!true;            ".withFrame).task.lastExpr.type.assertThat(instanceOf(Boolean))
        parse("val f = false; !f;".withFrame).task.lastExpr.type.assertThat(instanceOf(Boolean))
        parse("!(true || false); ".withFrame).task.lastExpr.type.assertThat(instanceOf(Boolean))
    }
    
    @Test def testTLogicalNotErrorInOp() {
        parse("!(!5);  ".withFrame).assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test def testTLogicalNotTypeMismatch() {
        parse('''!new Object;'''.withFrame).assertError(NEW, SUBTYPEEXPR, "Object", "boolean")
        parse('''!"Hello";   '''.withFrame).assertError(STRING_LITERAL, SUBTYPEEXPR, "String", "boolean")
        
        parse("!'a'; ".withFrame).assertError(  CHAR_LITERAL, SUBTYPEEXPR, "char",   "boolean")
        parse("!5;   ".withFrame).assertError(   INT_LITERAL, SUBTYPEEXPR, "int",    "boolean")
        parse("!5.0; ".withFrame).assertError(DOUBLE_LITERAL, SUBTYPEEXPR, "double", "boolean")
        parse("!null;".withFrame).assertError(  NULL_LITERAL, SUBTYPEEXPR, "null",   "boolean")
    }
    
    @Test def testTBitwiseNot() {
        parse("~          2L;".withFrame).task.lastExpr.type.assertThat(instanceOf(Long))
        parse("~           2;".withFrame).task.lastExpr.type.assertThat(instanceOf(Int))
        parse("~(2 as short);".withFrame).task.lastExpr.type.assertThat(instanceOf(Int))
        parse("~(2 as  byte);".withFrame).task.lastExpr.type.assertThat(instanceOf(Int))
        parse("~         'a';".withFrame).task.lastExpr.type.assertThat(instanceOf(Int))
    }
    
    @Test def testTBitwiseNotErrorInOp() {
        parse("~(!5);  ".withFrame).assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test def testTBitwiseNotTypeMismatch() {
        parse('''~new Object;'''.withFrame)
            .assertError(BITWISE_NOT, null, "operator", "~", "undefined", "rolez.lang.Object")
        parse('''~"Hello";   '''.withFrame)
            .assertError(BITWISE_NOT, null, "operator", "~", "undefined", "rolez.lang.String")
        
        parse("~5.0; ".withFrame).assertError(BITWISE_NOT, null, "operator", "~", "undefined", "double")
        parse("~true;".withFrame).assertError(BITWISE_NOT, null, "operator", "~", "undefined", "boolean")
        parse("~null;".withFrame).assertError(BITWISE_NOT, null, "operator", "~", "undefined", "Null")
    }
    
    @Test def testTSlicingErrorInTarget() {
        parse("(!5) slice s".withFrame).assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test def testTSlicing() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                slice a {
                    var i: int
                }
            }
            class App {
                task pure main: { new A slice a; }
            }
        ''').task.lastExpr.type.assertRoleType(ReadWrite, "A", "a")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                slice a {
                    var i: int
                }
            }
            class App {
                task pure main: { (new A as readonly A) slice a; }
            }
        ''').task.lastExpr.type.assertRoleType(ReadOnly, "A", "a")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                slice a {
                    var i: int
                }
            }
            class App {
                task pure main: { (new A as pure A) slice a; }
            }
        ''').task.lastExpr.type.assertRoleType(Pure, "A", "a")
    }
    
    @Test def testTMemberAccessErrorInTarget() {
        parse("(!5).a;".withFrame).assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test def testTMemberAccessField() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                var i: int
            }
            class App {
                task pure main: { new A.i; }
            }
        ''').task.lastExpr.type.assertThat(instanceOf(Int))
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                var i: int
            }
            class App {
                task pure main: {
                    val a: readonly A = new A;
                    a.i;
                }
            }
        ''').task.lastExpr.type.assertThat(instanceOf(Int))
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                val i: int = 0
            }
            class App {
                task pure main: {
                    val a: pure A = new A;
                    a.i;
                }
            }
        ''').task.lastExpr.type.assertThat(instanceOf(Int))
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                var a: readwrite A
            }
            class App {
                task pure main: {
                    val a = new A;
                    a.a;
                }
            }
        ''').task.lastExpr.type.assertRoleType(ReadWrite, "A")
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                var a: readwrite A
            }
            class App {
                task pure main: {
                    val a: readonly A = new A;
                    a.a;
                }
            }
        ''').task.lastExpr.type.assertRoleType(ReadOnly, "A")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                var a: readonly A
            }
            class App {
                task pure main: {
                    val a = new A;
                    a.a;
                }
            }
        ''').task.lastExpr.type.assertRoleType(ReadOnly, "A")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                var a: pure A
            }
            class App {
                task pure main: {
                    val a = new A;
                    a.a;
                }
            }
        ''').task.lastExpr.type.assertRoleType(Pure, "A")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                var a: pure A
            }
            class App {
                task pure main: {
                    val a: readonly A = new A;
                    a.a;
                }
            }
        ''').task.lastExpr.type.assertRoleType(Pure, "A")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                val a: readwrite A = null
            }
            class App {
                task pure main: {
                    val a: pure A = new A;
                    a.a;
                }
            }
        ''').task.lastExpr.type.assertRoleType(Pure, "A")
    }
    
    @Test def testTMemberAccessFieldGeneric() {
        var program = parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A
            class Container[E] mapped to «Container.canonicalName» {
                mapped var e: E
            }
            class App {
                task pure main: {
                    new Container[int].e;
                    new Container[readwrite A].e;
                    new Container[readonly A].e;
                    (new Container[readwrite A] as readonly Container[readwrite A]).e;
                }
            }
        ''')
        program.task.expr(0).type.assertThat(instanceOf(Int))
        program.task.expr(1).type.assertRoleType(ReadWrite, "A")
        program.task.expr(2).type.assertRoleType(ReadOnly, "A")
        program.task.expr(3).type.assertRoleType(ReadOnly, "A")
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class Container[E] mapped to «Container.canonicalName» {
                mapped var e: E
            }
            class A extends Container[int] {
                def readonly foo: int {
                    return this.e;
                }
            }
            class App {
                task pure main: { new A.e; }
            }
        ''').task.lastExpr.type.assertThat(instanceOf(Int))
    }
    
    static class Container<E> extends Guarded {
        public var E e = null
        new() {}
        new(E e) { this.e = e }
        def E get() { e }
        def void set(E e) { this.e = e }
    }
    
    static class SubContainer<E> extends Container<E> {}
    
    @Test def testTMemberAccessFieldRoleMismatch() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                var x: int
            }
            class App {
                task pure main: {
                    val a: pure A = new A;
                    a.x;
                }
            }
        ''').assertError(VAR_REF, null,
                "Role", "mismatch", "field", "pure")
    }
    
    /* More member access tests in RolezLinkingTest */
    
    @Test def testTMemberAccessMethod() {
        val roles = #[createReadWrite, createReadOnly, createPure]
        for(expected : roles) {
            for(actual : roles.filter[system.subroleSucceeded(it, expected)]) {
                parse('''
                    class rolez.lang.Object mapped to java.lang.Object
                    class A {
                        def «expected.name» x: int { return 42; }
                    }
                    class App {
                        task pure main: {
                            val a: «actual.name» A = new A;
                            a.x;
                        }
                    }
                ''').task.lastExpr.type.assertThat(instanceOf(Int))
            }
        }
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def readwrite a: readonly A { return null; }
            }
            class App {
                task pure main: { new A.a; }
            }
        ''').task.lastExpr.type.assertRoleType(ReadOnly, "A")
    }
    
    @Test def testTMemberAccessMethodErrorInArg() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def pure foo(i: int): {}
                def pure bar: { this.foo(!5); }
            }
        ''').assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
        
        // Apparently, when the method is defined in another resource,
        // linking somehow works differently...
        val set = newResourceSet.with('''
            class rolez.lang.Object mapped to java.lang.Object
            class A { def pure foo(i: int): {} }
        ''')
        parse('''
            class B {
                def pure bar: { new A.foo(!5); }
            }
        ''', set).assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test def testTMemberAccessTaskClassNotDefined() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class App {
                task pure foo: {}
                task pure main: { this start foo; }
            }
        ''').assertError(MEMBER_ACCESS, TMEMBERACCESS, "task class", "not defined")
    }
    
    @Test def testTMemberAccessMethodRoleMismatch() {
        val roles = #[createReadWrite, createReadOnly, createPure]
        for(expected : roles) {
            for(actual : roles.filter[!system.subroleSucceeded(it, expected)]) {
                parse('''
                    class rolez.lang.Object mapped to java.lang.Object
                    class A {
                        def «expected.name» x: int { return 42; }
                    }
                    class App {
                        task pure main: {
                            val a: «actual.name» A = new A;
                            a.x;
                        }
                    }
                ''').assertError(MEMBER_ACCESS, null, "role mismatch", "method", actual.name)
            }
        }
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def readwrite foo: {}
                def readonly  bar: { this.foo; }
            }
        ''').assertError(MEMBER_ACCESS, null, "role mismatch", "method", "readonly")
    }
    
    @Test def testTMemberAccessMethodRoleBoundMismatch() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A
            class AContainer {
                var a: readwrite A
                def r get[r includes readonly]: r A { return this.a; }
            }
            class App {
                task pure main: { new AContainer.get[pure]; }
            }
        ''').assertError(MEMBER_ACCESS, null, "bound mismatch", "pure", "r includes readonly")
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A
            class AContainer {
                var a: readwrite A
                def r get[r includes readonly]: r A { return this.a; }
            }
            class App {
                task pure main: { (new AContainer as pure AContainer).get; }
            }
        ''').assertError(MEMBER_ACCESS, null, "bound mismatch", "pure", "r includes readonly")
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Slice[T] mapped to rolez.lang.Slice {
                mapped def r get[r includes readonly](index: int): T with r
            }
            class App {
                def pure foo(s: pure Slice[int]): int { return s.get(0); }
            }
        ''').assertError(MEMBER_ACCESS, null, "bound mismatch", "pure", "r includes readonly")
        
        // with start
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A
            class AContainer {
                var a: readwrite A
                task r get[r includes readonly]: r A { return this.a; }
            }
            class App {
                task pure main: { (new AContainer as pure AContainer) start get; }
            }
        ''').assertError(MEMBER_ACCESS, null, "bound mismatch", "pure", "r includes readonly")
    }
    
    @Test def testTMemberAccessMethodGeneric() {
        val lib = newResourceSet.with('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Array[T] mapped to rolez.lang.Array {
                mapped new(length: int)
            }
            pure class rolez.lang.String mapped to java.lang.String
            class Container[E] mapped to «Container.canonicalName» {
                mapped var e: E
                mapped def r get[r includes readonly]: E with r
                mapped def readwrite set(e: E):
            }
            class SubContainer[E] mapped to «SubContainer.canonicalName» extends Container[E]
        ''')
        
        parse('''
            val array = new Array[int](1);
            array.set(0, 42);
            array.get(0);
        '''.withFrame).task.lastExpr.type.assertThat(instanceOf(Int))
        parse('''
            class App {
                task pure main: {
                    val s = new SubContainer[int];
                    s.get;
                }
            }
        ''', lib).task.lastExpr.type.assertThat(instanceOf(Int))
        parse("new Array[pure A](1).get[readwrite](0);".withFrame).task.lastExpr.type
            .assertRoleType(Pure, "A")
        
        var program = parse('''
            val array = new Array[readwrite A](1);
            array.set(0, new A);
            array.get(0);
            (array as readonly Array[readwrite A]).get(0);
        '''.withFrame)
        program.task.expr(1).type.assertRoleType(ReadWrite, "A")
        program.task.expr(2).type.assertRoleType(ReadOnly , "A")
        
        parse('''
            class IntContainer extends Container[int]
            class App {
                task pure main: {
                    val c = new IntContainer;
                    c.set(42);
                    c.get;
                }
            }
        ''', lib).task.lastExpr.type.assertThat(instanceOf(Int))
        program = parse('''
            class ObjectContainer extends Container[readwrite Object]
            class App {
                task pure main: {
                    val c = new ObjectContainer;
                    c.set(new Object);
                    c.get;
                    (c as readonly ObjectContainer).get;
                }
            }
        ''', lib)
        program.task.expr(1).type.assertRoleType(ReadWrite, objectClassName)
        program.task.expr(2).type.assertRoleType(ReadOnly , objectClassName)
        
        val task = parse('''
            val array = new Array[int](10);
            array.partition(null, 5).get(0);
            (array as readonly Array[int]).partition(null, 5).get(0);
        '''.withFrame).task
        task.expr(0).type
            .assertInstanceOf(RoleType) => [
                role.assertThat(instanceOf(ReadWrite)) // slice is readwrite
                base.assertInstanceOf(GenericClassRef) => [
                    clazz.name.assertThat(is(sliceClassName.toString))
                    typeArg.assertInstanceOf(Int)
                ]
            ]
        task.expr(1).type
            .assertInstanceOf(RoleType) => [
                role.assertThat(instanceOf(ReadOnly)) // slice is readonly
                base.assertInstanceOf(GenericClassRef) => [
                    clazz.name.assertThat(is(sliceClassName.toString))
                    typeArg.assertInstanceOf(Int)
                ]
            ]
        
        parse('''
            class IntContainer extends Container[int] {
                def readonly myGet: int { return this.get; }
            }
        ''', lib).assertNoErrors
        parse('''
            class StringContainer extends Container[readwrite String] {
                def r myGet[r includes readonly]: r String { return this.get; }
            }
        ''', lib).assertNoErrors
    }
    
    @Test def testTThis() {
        val roles = #[createReadWrite, createReadOnly, createPure]
        for(expected : roles)
            parse('''
                class rolez.lang.Object mapped to java.lang.Object
                class A {
                    def «expected.name» foo: { this; }
                }
            ''').findClass("A").findMethod("foo").lastExpr.type.assertRoleType(expected.class, "A")
        
        for(expected : roles)
            parse('''
                class rolez.lang.Object mapped to java.lang.Object
                class A {
                    task «expected.name» foo: { this; }
                }
            ''').findClass("A").findMethod("foo").lastExpr.type.assertRoleType(expected.class, "A")
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                new { this; }
            }
        ''').findNormalClass("A").constrs.head.lastExpr.type.assertRoleType(ReadWrite, "A")
    }
    
    @Test def testTSuper() {
        val roles = #[createReadWrite, createReadOnly, createPure]
        for(expected : roles)
            parse('''
                class rolez.lang.Object mapped to java.lang.Object
                class A
                class B extends A {
                    def «expected.name» foo: { super; }
                }
            ''').findClass("B").findMethod("foo").lastExpr.type.assertRoleType(expected.class, "A")
        
        for(expected : roles)
            parse('''
                class rolez.lang.Object mapped to java.lang.Object
                class A
                class B extends A {
                    task «expected.name» foo: { super; }
                }
            ''').findClass("B").findMethod("foo").lastExpr.type.assertRoleType(expected.class, "A")
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A
            class B extends A {
                new { super; }
            }
        ''').findNormalClass("B").constrs.head.lastExpr.type.assertRoleType(ReadWrite, "A")
    }
    
    @Test def testTVarRef() {
        parse('''
            val i = 5;
            i;
        '''.withFrame).task.lastExpr.type.assertThat(instanceOf(Int))
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def pure foo(i: int): {
                    i;
                }
            }
        ''').findClass("A").methods.head.lastExpr.type.assertThat(instanceOf(Int))
        
        parse('''
            val foo: readonly A = new A;
            foo;
        '''.withFrame).task.lastExpr.type.assertRoleType(ReadOnly, "A")
    }
    
    /* More "new" tests in RolezLinkingTest */
    
    @Test def void testTNew() {
        parse("new A;".withFrame).task.lastExpr.type.assertRoleType(ReadWrite, "A")
        
        parse("new Array[int](100);".withFrame).task.lastExpr.type
            .assertRoleType(ReadWrite, arrayClassName, Int)
        
        parse("new Array[readonly A](10);".withFrame).task.lastExpr.type
            .assertInstanceOf(RoleType) => [
                role.assertThat(instanceOf(ReadWrite))
                base.assertInstanceOf(GenericClassRef) => [
                    clazz.name.assertThat(is(arrayClassName.toString))
                    typeArg.assertRoleType(ReadOnly, "A")
                ]
            ]
        
        parse("new Array[pure Array[readwrite A]](1000);".withFrame).task.lastExpr.type
            .assertInstanceOf(RoleType) => [
                role.assertThat(instanceOf(ReadWrite))
                base.assertInstanceOf(GenericClassRef) => [
                    clazz.name.assertThat(is(arrayClassName.toString))
                    typeArg.assertInstanceOf(RoleType) => [
                        role.assertThat(instanceOf(Pure))
                        base.assertInstanceOf(GenericClassRef) => [
                            clazz.name.assertThat(is(arrayClassName.toString))
                            typeArg.assertRoleType(ReadWrite, "A")
                        ]
                    ]
                ]
            ]
    }
    
    @Test def testTNewErrorInArg() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                new(i: int) {}
                def pure foo(i: int): { new A(!5); }
            }
        ''').assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test def void testTNewGeneric() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            pure class rolez.lang.String mapped to java.lang.String
            class Container[E] mapped to «Container.canonicalName» {
                mapped new(e: E)
            }
            class App {
                task pure main: { new Container[int](42); }
            }
        ''').task.lastExpr.type
            .assertRoleType(ReadWrite, "Container", Int)
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            pure class rolez.lang.String mapped to java.lang.String
            class Container[E] mapped to «Container.canonicalName» {
                mapped new(e: E)
            }
            class App {
                task pure main: { new Container[readonly String]("Hello World!"); }
            }
        ''').task.lastExpr.type.assertInstanceOf(RoleType) => [
            role.assertThat(instanceOf(ReadWrite))
            base.assertInstanceOf(GenericClassRef) => [
                clazz.name.assertThat(is("Container"))
                typeArg.assertRoleType(ReadOnly, stringClassName)
            ]
        ]
    }
    
    @Test def testTThe() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            object A
            class App {
                task pure main: { the A; }
            }
        ''').task.lastExpr.type.assertRoleType(ReadOnly, "A")
    }
    
    @Test def testTParenthesized() {
        val program = parse('''
            (5);
            ('c');
            (new A);
            (new A as pure A);
        '''.withFrame)
        program.task.expr(0).type.assertThat(instanceOf(Int))
        program.task.expr(1).type.assertThat(instanceOf(Char))
        program.task.expr(2).type.assertRoleType(ReadWrite, "A")
        program.task.expr(3).type.assertRoleType(Pure, "A")
    }
    
    @Test def testTParenthesizedErrorInExpr() {
        parse("(!5);".withFrame).assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test def testTStringLiteral() {
        parse('''"Hi";'''.withFrame).task.lastExpr.type.assertRoleType(ReadWrite, stringClassName)
    }
    
    @Test def testTStringLiteralStringClassNotDefined() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class App {
                task pure main: { "Hi"; }
            }
        ''').assertError(STRING_LITERAL, TSTRINGLITERAL, "rolez.lang.String class", "not defined")
    }
    
    @Test def testTNullLiteral() {
        parse("null;".withFrame).task.lastExpr.type.assertThat(instanceOf(Null))
    }
    
    @Test def testTIntLiteral() {
        parse("5;".withFrame).task.lastExpr.type.assertThat(instanceOf(Int))
    }
    
    @Test def testTDoubleLiteral() {
        parse("5.0;".withFrame).task.lastExpr.type.assertThat(instanceOf(Double))
    }
    
    @Test def testTBooleanLiteral() {
        parse("true;".withFrame).task.lastExpr.type.assertThat(instanceOf(Boolean))
    }
    
    @Test def testTCharLiteral() {
        parse("'c';".withFrame).task.lastExpr.type.assertThat(instanceOf(Char))
    }
    
    @Test def testVParam() {
        var program = parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A
            class App {
                task pure foo(i: int, a: readwrite A, o: pure Object): {}
            }
        ''')
        program.task.params.get(0).varType.assertThat(instanceOf(Int))
        program.task.params.get(1).varType.assertRoleType(ReadWrite, "A")
        program.task.params.get(2).varType.assertRoleType(Pure, objectClassName)
    }
    
    @Test def testVLocalVar() {
        var program = parse('''
            var i: int;
            var aa: readwrite A;
            var o: pure Object;
        '''.withFrame)
        program.task.variable(0).varType.assertThat(instanceOf(Int))
        program.task.variable(1).varType.assertRoleType(ReadWrite, "A")
        program.task.variable(2).varType.assertRoleType(Pure, objectClassName)
        
        program = parse('''
            var aa: readonly A = new A;
            var o: pure Object = null;
        '''.withFrame)
        program.task.variable(0).varType.assertRoleType(ReadOnly, "A")
        program.task.variable(1).varType.assertRoleType(Pure, objectClassName)
        
        program = parse('''
            val i = 1;
            val aa = new A;
            val n = null;
        '''.withFrame)
        program.task.variable(0).varType.assertThat(instanceOf(Int))
        program.task.variable(1).varType.assertRoleType(ReadWrite, "A")
        program.task.variable(2).varType.assertThat(instanceOf(Null))
        
        program = parse('''
            var i = 1;
            var aa = new A;
            var n = null;
        '''.withFrame)
        program.task.variable(0).varType.assertThat(instanceOf(Int))
        program.task.variable(1).varType.assertRoleType(ReadWrite, "A")
        program.task.variable(2).varType.assertThat(instanceOf(Null))
        
        parse("var i;".withFrame).assertError(LOCAL_VAR, VLOCALVAR)
    }
    
    @Test def testWBlock() {
        parse('''
            new Object;
            {
                {
                    new Object;
                    new Object;
                }
                new Object;
                {
                    new Object;
                    {}
                    {{{}}}
                }
                new Object;
            }
        '''.withFrame).assertNoErrors
        
        parse("false as int;".withFrame).assertError(CAST, null, "cannot cast", "boolean", "int")
        parse('''
            {
                new Object;
                {
                    new Object;
                    false as int;
                    new Object;
                    {}
                }
            }
        '''.withFrame).assertError(CAST, null, "cannot cast", "boolean", "int")
    }
    
    @Test def testWLocalVarDecl() {
        parse('''
            val i: int = 1;
            val aa: readwrite A = new A;
            val o: pure Object = new A;
            val c: readwrite Object = null;
        '''.withFrame).assertNoErrors
        
        parse("val i: int = false;".withFrame)
            .assertError(BOOLEAN_LITERAL, SUBTYPEEXPR, "boolean", "int")
        parse("val o: readwrite Object = new Object as pure Object;".withFrame)
            .assertError(CAST, SUBTYPEEXPR, "pure rolez.lang.Object", "readwrite rolez.lang.Object")
        parse("val o: pure A = new A as pure Object;".withFrame)
            .assertError(CAST, SUBTYPEEXPR, "pure rolez.lang.Object", "pure A")
    }
    
    @Test def testWIfStmt() {
        parse('''
            if(true)
                new Object;
            else
                new Object;
            
            if(false) {
                new Object;
                new Object;
            }
            else
                new Object;
            
            if(1 == 1)
                new Object;
        '''.withFrame).assertNoErrors
        
        parse('''
            if(5)
                new Object;
        '''.withFrame).assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
        parse('''
            if(true)
                false as int;
        '''.withFrame).assertError(CAST, null, "cannot cast", "boolean", "int")
        parse('''
            if(true) {}
            else
                false as int;
        '''.withFrame).assertError(CAST, null, "cannot cast", "boolean", "int")
    }
    
    @Test def testWWhileLoop() {
        parse('''
            while(true)
                new Object;
            
            while(3 == 2) {
                new Object;
                new Object;
            }
        '''.withFrame).assertNoErrors
        
        parse('''
            while(5)
                new Object;
        '''.withFrame).assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
        parse('''
            while(true)
                false as int;
        '''.withFrame).assertError(CAST, null, "cannot cast", "boolean", "int")
    }
    
    @Test def testWForLoop() {
        parse('''
            for(var i = 0; i < 10; i++)
                new Object;
            
            for(var i = 0; true; true) {
                new Object;
                new Object;
            }
        '''.withFrame).assertNoErrors
        
        parse('''
            for(var i = 0; 5; true)
                new Object;
        '''.withFrame).assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test def testWForLoopErrorInChild() {
        parse("for(var i = false as int; true; true) true;".withFrame)
            .assertError(CAST, null, "cannot cast", "boolean", "int")
        parse("for(var i = 0; false as int; true) true;".withFrame)
            .assertError(CAST, null, "cannot cast", "boolean", "int")
        parse("for(var i = 0; true; false as int) true;".withFrame)
            .assertError(CAST, null, "cannot cast", "boolean", "int")
        parse("for(var i = 0; true; true) false as int;".withFrame)
            .assertError(CAST, null, "cannot cast", "boolean", "int")
    }
    
    /* More super constr tests in RolezLinkingTest */
    
    @Test def testWSuperConstrCallErrorInArg() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                new(i: int) {}
            }
            class B extends A {
                new { super(!5); }
            }
        ''').assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test def testWReturn() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def pure a:                  {}
                def pure b:                  { return; }
                def pure c:              int { return 5; }
                def pure d: readwrite Object { return new Object; }
                def pure e:      pure Object { return new A; }
                def pure f:       readonly A { return new A as readonly A; }
            }
        ''').assertNoErrors
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def pure a: { return 1; }
            }
        ''').assertError(INT_LITERAL, SUBTYPEEXPR, "int", "void")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                task pure foo: { return 1; }
            }
        ''').assertError(INT_LITERAL, SUBTYPEEXPR, "int", "void")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def pure a: int { return false; }
            }
        ''').assertError(BOOLEAN_LITERAL, SUBTYPEEXPR, "boolean", "int")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def pure a: readwrite A { return new A as pure A; }
            }
        ''').assertError(CAST, SUBTYPEEXPR, "pure A", "readwrite A")
    }
    
    @Test def testSubtype() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Slice[T] mapped to rolez.lang.Slice {
                mapped def r partition[r](p: pure Partitioner, n: int): readonly Array[r Slice[T]]
            }
            class rolez.lang.Array[T] mapped to rolez.lang.Array extends Slice[T] {
                mapped new(length: int)
            }
            class rolez.lang.Partitioner mapped to rolez.lang.Partitioner
            class A
            class S {
                slice a { var i: int }
                slice b { var j: int }
            }
            class App {
                task pure main: {
                    val i: int = 5;
                    val j: boolean = false;
                    val k: char = 'c';
                    
                    var a: readwrite A = new A;
                    a = null;
                    var b: readonly A = new A;
                    b = new A as readonly A;
                    b = null;
                    var c: pure A = new A;
                    c = new A as readonly A;
                    c = new A as pure A;
                    c = null;
                    var o: pure Object = new A;
                    o = new A as pure A;
                    o = new A as readwrite Object;
                    o = new Array[int](2);
                    
                    var ia: pure Array[int] = new Array[int](2);
                    ia = null;
                    var oa: readwrite Array[pure Object] = new Array[pure Object](1);
                    oa = null;
                    val slices: readonly Array[readwrite Slice[pure Object]] = oa.partition(null, 1);
                    
                    var s: readwrite S = new S;
                    var sa: readwrite S\a = s slice a;
                    var sb: readwrite S\b = s slice b;
                }
            }
        ''').assertNoErrors
        
        // IMPROVE: Test type params, once supported outside mapped classes
    }
    
    @Test def testSubtypePrimitiveMismatch() {
        parse("val i: int = false;".withFrame).assertError(  BOOLEAN_LITERAL, SUBTYPEEXPR, "boolean", "int")
        parse("val i: int = 'c';".withFrame).assertError(       CHAR_LITERAL, SUBTYPEEXPR, "char", "int")
        parse("val bool: boolean = 1;".withFrame).assertError(   INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
        parse("val bool: boolean = 'c';".withFrame).assertError(CHAR_LITERAL, SUBTYPEEXPR, "char", "boolean")
        // I think we get the picture...
    }
    
    @Test def testSubtypeSimpleClassMismatch() {
        parse("val array: readwrite A = new Object;".withFrame)
            .assertError(NEW, SUBTYPEEXPR, "readwrite rolez.lang.Object", "readwrite A")
    }
    
    @Test def testSubtypeGenericClassMismatch() {
        parse("val array: pure Array[int] = new Array[boolean](0);".withFrame)
            .assertError(NEW, SUBTYPEEXPR, "readwrite rolez.lang.Array[boolean]", "pure rolez.lang.Array[int]")
        parse("val array: pure Array[int] = new Array[pure Object](0);".withFrame)
            .assertError(NEW, SUBTYPEEXPR, "readwrite rolez.lang.Array[pure rolez.lang.Object]", "pure rolez.lang.Array[int]")
        parse("val array: pure Array[pure Object] = new Array[int](0);".withFrame)
            .assertError(NEW, SUBTYPEEXPR, "readwrite rolez.lang.Array[int]", "pure rolez.lang.Array[pure rolez.lang.Object]")
        parse("val array: pure Array[pure Object] = new Array[pure A](0);".withFrame)
            .assertError(NEW, SUBTYPEEXPR, "readwrite rolez.lang.Array[pure A]", "pure rolez.lang.Array[pure rolez.lang.Object]")
        parse("val array: pure Array[pure A] = new Array[pure Object](0);".withFrame)
            .assertError(NEW, SUBTYPEEXPR, "readwrite rolez.lang.Array[pure rolez.lang.Object]", "pure rolez.lang.Array[pure A]")
        parse("val array: pure Array[pure A] = new Array[readwrite A](0);".withFrame)
            .assertError(NEW, SUBTYPEEXPR, "readwrite rolez.lang.Array[readwrite A]", "pure rolez.lang.Array[pure A]")
        parse("val array: pure Array[readwrite A] = new Array[pure A](0);".withFrame)
            .assertError(NEW, SUBTYPEEXPR, "readwrite rolez.lang.Array[pure A]", "pure rolez.lang.Array[readwrite A]")
    }
    
    @Test def testSubtypeRoleMismatch() {
        parse("val a: readwrite A = new A as readonly A;".withFrame)
            .assertError(CAST, SUBTYPEEXPR, "readonly A", "readwrite A")
        parse("val a: readwrite A = new A as pure A;".withFrame)
            .assertError(CAST, SUBTYPEEXPR, "pure A", "readwrite A")
        parse("val a: readonly A = new A as pure A;".withFrame)
            .assertError(CAST, SUBTYPEEXPR, "pure A", "readonly A")
    }
    
    @Test def testSubtypeSlice() {
        parse("val s: readwrite S\\a = new S slice a;".withFrame).assertNoErrors
    }
    
    @Test def testSubtypeSliceMismatch() {
        parse("val s: readwrite S\\a = new S slice b;".withFrame)
            .assertError(SLICING, SUBTYPEEXPR, "S\\a", "S\\b")
    }
}
					
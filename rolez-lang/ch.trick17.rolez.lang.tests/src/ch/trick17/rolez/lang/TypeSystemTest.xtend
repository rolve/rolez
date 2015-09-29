package ch.trick17.rolez.lang

import ch.trick17.rolez.lang.rolez.Boolean
import ch.trick17.rolez.lang.rolez.Char
import ch.trick17.rolez.lang.rolez.Double
import ch.trick17.rolez.lang.rolez.Int
import ch.trick17.rolez.lang.rolez.Null
import ch.trick17.rolez.lang.rolez.Program
import ch.trick17.rolez.lang.rolez.Role
import ch.trick17.rolez.lang.typesystem.RolezSystem
import ch.trick17.rolez.lang.typesystem.RolezUtils
import javax.inject.Inject
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.eclipse.xtext.junit4.validation.ValidationTestHelper
import org.junit.Test
import org.junit.runner.RunWith

import static ch.trick17.rolez.lang.Constants.*
import static ch.trick17.rolez.lang.rolez.Role.*
import static ch.trick17.rolez.lang.rolez.RolezPackage.Literals.*
import static ch.trick17.rolez.lang.typesystem.RolezSystem.*
import static ch.trick17.rolez.lang.validation.RolezValidator.*
import static org.eclipse.xtext.diagnostics.Diagnostic.*
import static org.hamcrest.Matchers.*

import static extension org.hamcrest.MatcherAssert.assertThat

@RunWith(XtextRunner)
@InjectWith(RolezInjectorProvider)
class TypeSystemTest {
    
    @Inject RolezSystem system
    @Inject extension RolezExtensions
    @Inject extension RolezUtils
    @Inject extension TestUtilz
    @Inject extension ParseHelper<Program>
    @Inject extension ValidationTestHelper
    
    @Test
    def testTAssignment() {
        val program = parse('''
            class rolez.lang.Object
            class A
            class B extends A 
            task Main: {
                var a: readwrite A;
                a = new B;
            }
        ''')
        program.main.lastExpr.type
            .assertThat(isRoleType(READWRITE, newClassRef(program.findClass("A"))))
    }
    
    @Test
    def testTAssignmentErrorInOp() {
        parse("task Main: { !5 = 5; }")
            .assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
        
        parse('''
            task Main: {
                var x: int;
                x = !5;
            }
        ''').assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test
    def testTAssignmentNotAssignable() {
        parse('''
            task Main: {
                5 = 3;
            }
        ''').assertError(INT_LITERAL, AEXPR, "assign", "5")
        parse('''
            class rolez.lang.Object
            class A {
                def pure foo: {}
            }
            task Main: {
                new A.foo() = 3;
            }
        ''').assertError(MEMBER_ACCESS, AMEMBERACCESS, "assign", "foo()")
        
        parse('''
            task Main: {
                val x: int;
                x = 5;
            }
        ''').assertError(VAR_REF, AVARREF, "assign", "value")
        
        parse('''
            class rolez.lang.Object
            class A {
                val x: int
                def pure foo: {
                    this.x = 4;
                }
            }
        ''').assertError(MEMBER_ACCESS, null, "assign", "value field")
    }
    
    @Test
    def testTAssignmentTypeMismatch() {
        parse('''
            task Main: {
                var x: int;
                x = true;
            }
        ''').assertError(BOOLEAN_LITERAL, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test
    def testTBooleanExpr() {
        parse("task Main: { true || false; }").main.lastExpr.type.assertThat(instanceOf(Boolean))
        parse("task Main: { true && false; }").main.lastExpr.type.assertThat(instanceOf(Boolean))
    }
    
    @Test
    def testTBooleanExprErrorInOp() {
        parse("task Main: { !5 || false; }")
            .assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
        parse("task Main: { true || !5; }")
            .assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test
    def testTBooleanExprTypeMismatch() {
        parse("task Main: { 5 || false; }")
            .assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
        parse("task Main: { true || 5; }")
            .assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test
    def testTEqualityExpr() {
        parse("task Main: { true == false; }").main.lastExpr.type.assertThat(instanceOf(Boolean))
        parse("task Main: { 5 != 3; }").main.lastExpr.type.assertThat(instanceOf(Boolean))

        parse('''
            class rolez.lang.Object
            class A
            task Main: {
                new Object == new A;
                new A == new Object;
                new A == new A;
            }
        ''').assertNoErrors
    }
    
    @Test
    def testTEqualityExprErrorInOp() {
        parse("task Main: { !5 == false; }")
            .assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
        parse("task Main: { true != !5; }")
            .assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test
    def testTEqualityExprIncompatibleTypes() {
        parse('''
            class rolez.lang.Object
            class A
            class B
            task Main: { new A == new B; }
        ''').assertError(EQUALITY_EXPR, null, "compare", "A", "B")
        // IMPROVE: Find a way to include an issue code for explicit failures?
        
        parse("task Main: { 42 != false; }")
            .assertError(EQUALITY_EXPR, null, "compare", "int", "boolean")
    }
    
    @Test
    def testTRelationalExpr() {
        parse("task Main: {   5 <    6; }").main.lastExpr.type.assertThat(instanceOf(Boolean))
        parse("task Main: {  -1 <= -10; }").main.lastExpr.type.assertThat(instanceOf(Boolean))
        parse("task Main: { 'a' >  ' '; }").main.lastExpr.type.assertThat(instanceOf(Boolean))
        parse("task Main: { 3+4 >=   0; }").main.lastExpr.type.assertThat(instanceOf(Boolean))
    }
    
    @Test
    def testTRelationalExprErrorInOp() {
        parse("task Main: { -true < 0; }")
            .assertError(BOOLEAN_LITERAL, SUBTYPEEXPR, "int", "boolean")
        parse("task Main: { 100 <= -false; }")
            .assertError(BOOLEAN_LITERAL, SUBTYPEEXPR, "int", "boolean")
        parse("task Main: { -'a' > 0; }")
            .assertError(CHAR_LITERAL, SUBTYPEEXPR, "int", "char")
        parse("task Main: { 100 >= -false; }")
            .assertError(BOOLEAN_LITERAL, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test
    def testTRelationalExprIncompatibleTypes() {
        parse('''
            class rolez.lang.Object
            task Main: { new Object < new Object; }
        ''').assertError(RELATIONAL_EXPR, null, "compare", "Object")
        
        parse("task Main: { true <= false; }")
            .assertError(RELATIONAL_EXPR, null, "compare", "boolean")
        parse("task Main: { null > null; }")
            .assertError(RELATIONAL_EXPR, null, "compare", "null")
        parse("task Main: { 5 > '5'; }")
            .assertError(RELATIONAL_EXPR, null, "compare", "int", "char")
        parse("task Main: { true > '5'; }")
            .assertError(RELATIONAL_EXPR, null, "compare", "boolean", "char")
    }
    
    @Test
    def testTArithmeticExpr() {
        parse("task Main: {   4 +  4; }").main.lastExpr.type.assertThat(instanceOf(Int))
        parse("task Main: {   0 -  0; }").main.lastExpr.type.assertThat(instanceOf(Int))
        parse("task Main: {   3 *  2; }").main.lastExpr.type.assertThat(instanceOf(Int))
        parse("task Main: { 100 / -1; }").main.lastExpr.type.assertThat(instanceOf(Int))
        parse("task Main: { -99 %  3; }").main.lastExpr.type.assertThat(instanceOf(Int))
        
        var program = parse('''
            class rolez.lang.Object
            class rolez.lang.String
            task Main: { "Hi" + " World"; }
        ''')
        program.main.lastExpr.type.assertThat(
            isRoleType(READWRITE, newClassRef(program.findClass(stringClassName))))
            
        program = parse('''
            class rolez.lang.Object
            class rolez.lang.String
            task Main: { "" + '5'; }
        ''')
        program.main.lastExpr.type.assertThat(
            isRoleType(READWRITE, newClassRef(program.findClass(stringClassName))))
            
        program = parse('''
            class rolez.lang.Object
            class rolez.lang.String
            task Main: { null + " "; }
        ''')
        program.main.lastExpr.type.assertThat(
            isRoleType(READWRITE, newClassRef(program.findClass(stringClassName))))
    }
    
    @Test
    def testTArithmeticExprErrorInOp() {
        parse("task Main: { !'a' + 0; }")
            .assertError(CHAR_LITERAL, SUBTYPEEXPR, "char", "boolean")
        parse("task Main: { 100 - -false; }")
            .assertError(BOOLEAN_LITERAL, SUBTYPEEXPR, "int", "boolean")
        parse("task Main: { -'a' * 0; }")
            .assertError(CHAR_LITERAL, SUBTYPEEXPR, "int", "char")
        parse("task Main: { 100 / -true; }")
            .assertError(BOOLEAN_LITERAL, SUBTYPEEXPR, "int", "boolean")
        parse("task Main: { (3*3) % !42; }")
            .assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test
    def testTArtithmeticExprTypeMismatch() {
        parse('''
            class rolez.lang.Object
            task Main: { new Object + new Object; }
        ''').assertError(ARITHMETIC_BINARY_EXPR, null, "operator", "undefined", "object")
        parse('''
            class rolez.lang.Object
            class A
            class B
            task Main: { new A - new B; }
        ''').assertError(ARITHMETIC_BINARY_EXPR, null, "operator", "undefined", "A", "B")
        
        parse('''
            class rolez.lang.Object
            class rolez.lang.String
            task Main: { "Hello" - "World"; }
        ''').assertError(ARITHMETIC_BINARY_EXPR, null, "operator", "undefined", "String")
        parse('''
            class rolez.lang.Object
            class rolez.lang.String
            task Main: { "Hello" * new Object; }
        ''').assertError(ARITHMETIC_BINARY_EXPR, null, "operator", "undefined", "String", "Object")
        parse('''
            class rolez.lang.Object
            class rolez.lang.String
            task Main: { 5 / "World"; }
        ''').assertError(ARITHMETIC_BINARY_EXPR, null, "operator", "undefined", "int", "String")
        parse('''
            class rolez.lang.Object
            class rolez.lang.String
            task Main: { null % "World"; }
        ''').assertError(ARITHMETIC_BINARY_EXPR, null, "operator", "undefined", "null", "String")
        
        parse("task Main: { 'a' * 'b'; }")
            .assertError(ARITHMETIC_BINARY_EXPR, null, "operator", "undefined", "char")
        parse("task Main: { null / null; }")
            .assertError(ARITHMETIC_BINARY_EXPR, null, "operator", "undefined", "null")
        parse("task Main: { 5 % '5'; }")
            .assertError(ARITHMETIC_BINARY_EXPR, null, "operator", "undefined", "int", "char")
        parse("task Main: { true % '5'; }")
            .assertError(ARITHMETIC_BINARY_EXPR, null, "operator", "undefined", "boolean", "char")
    }
    
    @Test
    def testCast() {
        // Redundant casts
        parse("task Main: { 5 as int; }").main.lastExpr.type.assertThat(instanceOf(Int))
        parse("task Main: { true as boolean; }").main.lastExpr.type.assertThat(instanceOf(Boolean))
        
        var program = parse('''
            class rolez.lang.Object
            task Main: { new Object as readwrite Object; }
        ''')
        program.main.lastExpr.type.assertThat(isRoleType(READWRITE, newClassRef(program.findClass(objectClassName))))
        
        // Upcasts
        program = parse('''
            class rolez.lang.Object
            class rolez.lang.Array
            class A
            task Main: {
                new A as readwrite Object;
                new A as readonly A;
                new A as pure A;
                null as readwrite A;
                null as readonly A;
                new Array[int] as readonly Array[int];
                new Array[pure A] as readonly Array[pure A];
            }
        ''')
        program.main.expr(0).type.assertThat(isRoleType(READWRITE, newClassRef(program.findClass(objectClassName))))
        program.main.expr(1).type.assertThat(isRoleType(READONLY,  newClassRef(program.findClass("A"))))
        program.main.expr(2).type.assertThat(isRoleType(PURE,      newClassRef(program.findClass("A"))))
        program.main.expr(3).type.assertThat(isRoleType(READWRITE, newClassRef(program.findClass("A"))))
        program.main.expr(4).type.assertThat(isRoleType(READONLY,  newClassRef(program.findClass("A"))))
        program.main.expr(5).type.assertThat(isRoleType(READONLY,
                newClassRef(program.findClass(arrayClassName), newIntType)))
        program.main.expr(6).type.assertThat(isRoleType(READONLY,
            newClassRef(program.findClass(arrayClassName), newRoleType(PURE, newClassRef(program.findClass("A"))))))
        
        // Downcasts
        program = parse('''
            class rolez.lang.Object
            class A
            task Main: { new Object as readwrite A; }
        ''')
        program.main.lastExpr.type.assertThat(isRoleType(READWRITE, newClassRef(program.findClass("A"))))
    }
    
    @Test
    def testTCastErrorInOp() {
        parse("task Main: { !5 as boolean; }")
            .assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test
    def testTCastIllegal() {
        parse("task Main: { 5 as boolean; }")
            .assertError(CAST, null, "cast", "int", "boolean")
        parse("task Main: { false as int; }")
            .assertError(CAST, null, "cast", "boolean", "int")
        parse("task Main: { null as int; }")
            .assertError(CAST, null, "cast", "null", "int")
        parse("task Main: { 5 as ; }")
            .assertError(CAST, null, "cast", "int", "void")
        
        parse('''
            class rolez.lang.Object
            task Main: { 5 as readwrite Object; }
        ''').assertError(CAST, null, "cast", "readwrite rolez.lang.Object", "int")
        parse('''
            class rolez.lang.Object
            task Main: { new Object as int; }
        ''').assertError(CAST, null, "cast", "readwrite rolez.lang.Object", "int")
        parse('''
            class rolez.lang.Object
            class A
            task Main: { new A as readonly A as readwrite A; }
        ''').assertError(CAST, null, "cast", "readwrite A", "readonly A")
        
        parse('''
            class rolez.lang.Object
            class rolez.lang.Array
            task Main: { new Array[boolean] as readwrite Array[int]; }
        ''').assertError(CAST, null, "cast", "readwrite rolez.lang.Array[boolean]", "readwrite rolez.lang.Array[int]")
        parse('''
            class rolez.lang.Object
            class rolez.lang.Array
            class A
            task Main: { new Array[pure Object] as readwrite Array[pure A]; }
        ''').assertError(CAST, null, "cast", "readwrite rolez.lang.Array[pure rolez.lang.Object]", "readwrite rolez.lang.Array[pure A]")
        parse('''
            class rolez.lang.Object
            class rolez.lang.Array
            class A
            task Main: { new Array[readwrite A] as readwrite Array[pure A]; }
        ''').assertError(CAST, null, "cast", "readwrite rolez.lang.Array[readwrite A]", "readwrite rolez.lang.Array[pure A]")
        parse('''
            class rolez.lang.Object
            class rolez.lang.Array
            class A
            task Main: { new Array[pure A] as readwrite Array[readwrite A]; }
        ''').assertError(CAST, null, "cast", "readwrite rolez.lang.Array[pure A]", "readwrite rolez.lang.Array[readwrite A]")
    }
    
    @Test
    def testTUnaryMinus() {
        parse("task Main: { -2; }").main.lastExpr.type.assertThat(instanceOf(Int))
        parse("task Main: { val a: int = 5; -a; }").main.lastExpr.type.assertThat(instanceOf(Int))
        parse("task Main: { -(4-4); }").main.lastExpr.type.assertThat(instanceOf(Int))
    }
    
    @Test
    def testTUnaryMinusErrorInOp() {
        parse("task Main: { -!5; }")
            .assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
        parse("task Main: { -(-'a'); }")
            .assertError(CHAR_LITERAL, SUBTYPEEXPR, "char", "int")
    }
    
    @Test
    def testTUnaryMinusTypeMismatch() {
        parse('''
            class rolez.lang.Object
            task Main: { -new Object; }
        ''').assertError(NEW, SUBTYPEEXPR, "Object", "int")
        parse('''
            class rolez.lang.Object
            class rolez.lang.String
            task Main: { -"Hello"; }
        ''').assertError(STRING_LITERAL, SUBTYPEEXPR, "String", "int")
        
        parse("task Main: { -'a'; }")
            .assertError(CHAR_LITERAL, SUBTYPEEXPR, "char", "int")
        parse("task Main: { -true; }")
            .assertError(BOOLEAN_LITERAL, SUBTYPEEXPR, "boolean", "int")
        parse("task Main: { -null; }")
            .assertError(NULL_LITERAL, SUBTYPEEXPR, "null", "int")
    }
    
    @Test
    def testTUnaryNot() {
        parse("task Main: { !true; }").main.lastExpr.type
            .assertThat(instanceOf(Boolean))
        parse("task Main: { val a: boolean = false; !a; }").main.lastExpr.type
            .assertThat(instanceOf(Boolean))
        parse("task Main: { !(true || false); }").main.lastExpr.type
            .assertThat(instanceOf(Boolean))
    }
    
    @Test
    def testTUnaryNotErrorInOp() {
        parse("task Main: { !(-'a'); }")
            .assertError(CHAR_LITERAL, SUBTYPEEXPR, "char", "int")
        parse("task Main: { !(!5); }")
            .assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test
    def testTUnaryNotTypeMismatch() {
        parse('''
            class rolez.lang.Object
            task Main: { !new Object; }
        ''').assertError(NEW, SUBTYPEEXPR, "Object", "boolean")
        parse('''
            class rolez.lang.Object
            class rolez.lang.String
            task Main: { !"Hello"; }
        ''').assertError(STRING_LITERAL, SUBTYPEEXPR, "String", "boolean")
        
        parse("task Main: { !'a'; }")
            .assertError(CHAR_LITERAL, SUBTYPEEXPR, "char", "boolean")
        parse("task Main: { !5; }")
            .assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
        parse("task Main: { !null; }")
            .assertError(NULL_LITERAL, SUBTYPEEXPR, "null", "boolean")
    }
    
    @Test
    def testTMemberAccessErrorInTarget() {
        parse("task Main: { (!5).a; }")
            .assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
        parse("task Main: { (!5).foo(); }")
            .assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test
    def testTMemberAccessIllegalTarget() {
        parse('''task Main: { 5.5.a; }''')
            .assertError(DOUBLE_LITERAL, null, "Illegal", "target", "access")
        parse('''task Main: { false.foo(); }''')
            .assertError(BOOLEAN_LITERAL, null, "Illegal", "target", "access")
    }
    
    @Test
    def testTMemberAccessField() {
        parse('''
            class rolez.lang.Object
            class A { var x: int }
            task Main: { new A.x; }
        ''').main.lastExpr.type.assertThat(instanceOf(Int))
        parse('''
            class rolez.lang.Object
            class A { var x: int }
            task Main: {
                val a: readonly A = new A;
                a.x;
            }
        ''').main.lastExpr.type.assertThat(instanceOf(Int))
        
        val program = parse('''
            class rolez.lang.Object
            class A { var a: readwrite A }
            task Main: {
                val a: readwrite A = new A;
                a.a;
            }
        ''')
        program.main.lastExpr.type
            .assertThat(isRoleType(READWRITE, newClassRef(program.findClass("A"))))
        
        parse('''
            class rolez.lang.Object
            class A { var a: readwrite A }
            task Main: {
                val a: readonly A = new A;
                a.a;
            }
        ''').main.lastExpr.type.asRoleType.role.assertThat(is(READONLY))
        parse('''
            class rolez.lang.Object
            class A { var a: readonly A }
            task Main: {
                val a: readwrite A = new A;
                a.a;
            }
        ''').main.lastExpr.type.asRoleType.role.assertThat(is(READONLY))
        parse('''
            class rolez.lang.Object
            class A { var a: pure A }
            task Main: {
                val a: readwrite A = new A;
                a.a;
            }
        ''').main.lastExpr.type.asRoleType.role.assertThat(is(PURE))
        parse('''
            class rolez.lang.Object
            class A { var a: pure A }
            task Main: {
                val a: readonly A = new A;
                a.a;
            }
        ''').main.lastExpr.type.asRoleType.role.assertThat(is(PURE))
    }
    
    @Test
    def testTMemberAccessFieldRoleMismatch() {
        parse('''
            class rolez.lang.Object
            class A { var x: int }
            task Main: {
                val a: pure A = new A;
                a.x;
            }
        ''').assertError(VAR_REF, null,
                "Role", "mismatch", "field", "pure")
    }
    
    @Test
    def testTMemberAccessMethod() {
        for(expected : Role.values) {
            for(actual : Role.values.filter[system.subroleSucceeded(it, expected)]) {
                parse('''
                    class rolez.lang.Object
                    class A {
                        def «expected» x: int { return 42; }
                    }
                    task Main: {
                        val a: «actual» A = new A;
                        a.x();
                    }
                ''').main.lastExpr.type.assertThat(instanceOf(Int))
            }
        }
        
        val program = parse('''
            class rolez.lang.Object
            class A {
                def readwrite a: readonly A { return null; }
            }
            task Main: { new A.a(); }
        ''')
        program.main.lastExpr.type
            .assertThat(isRoleType(READONLY, newClassRef(program.findClass("A"))))
        
        parse('''
            class rolez.lang.Object
            class A
            class B {
                def readwrite foo(val a: readonly A, val b: readwrite B,
                        val c: readwrite C, val d: int): {}
            }
            class C extends B
            task Main: { new C.foo(new A, new C, null, 5); }
        ''').assertNoErrors
    }
    
    @Test
    def testTMemberAccessMethodErrorInArg() {
        parse('''
            class rolez.lang.Object
            class A {
                def pure foo(val i: int): {}
                def pure bar: { foo(!5); }
            }
        ''').assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test
    def testTMemberAccessMethodRoleMismatch() {
        for(expected : Role.values) {
            for(actual : Role.values.filter[!system.subroleSucceeded(it, expected)]) {
                parse('''
                    class rolez.lang.Object
                    class A {
                        def «expected» x: int { return 42; }
                    }
                    task Main: {
                        val a: «actual» A = new A;
                        a.x();
                    }
                ''').assertError(VAR_REF, null,
                        "Role", "mismatch", "method", actual.toString)
            }
        }
    }
    
    @Test
    def testTMemberAccessMethodTypeMismatch() {
        parse('''
            class rolez.lang.Object
            class A { def readwrite foo: {} }
            task Main: { new A.foo(5); }
        ''').assertError(METHOD_SELECTOR, LINKING_DIAGNOSTIC, "method", "foo")
        parse('''
            class rolez.lang.Object
            class A { def readwrite foo(val c: char): {} }
            task Main: { new A.foo(5, false); }
        ''').assertError(METHOD_SELECTOR, LINKING_DIAGNOSTIC, "method", "foo")
        parse('''
            class rolez.lang.Object
            class A { def readwrite foo(val i: int): {} }
            task Main: { new A.foo(); }
        ''').assertError(METHOD_SELECTOR, LINKING_DIAGNOSTIC, "method", "foo")
        parse('''
            class rolez.lang.Object
            class A { def readwrite foo(val i: int, val a: readwrite A): {} }
            task Main: { new A.foo(false); }
        ''').assertError(METHOD_SELECTOR, LINKING_DIAGNOSTIC, "method", "foo")
        
        parse('''
            class rolez.lang.Object
            class A { def readwrite foo(val i: int): {} }
            task Main: { new A.foo(false); }
        ''').assertError(METHOD_SELECTOR, LINKING_DIAGNOSTIC, "method", "foo")
        parse('''
            class rolez.lang.Object
            class A { def readwrite foo(val a: readwrite A): {} }
            task Main: { new A.foo(new Object); }
        ''').assertError(METHOD_SELECTOR, LINKING_DIAGNOSTIC, "method", "foo")
        parse('''
            class rolez.lang.Object
            class A { def readwrite foo(val a: readwrite A): {} }
            task Main: { new A.foo(new A as readonly A); }
        ''').assertError(METHOD_SELECTOR, LINKING_DIAGNOSTIC, "method", "foo")
    }
    
    @Test
    def testTMemberAccessMethodOverloading() {
        var program = parse('''
            class rolez.lang.Object
            class A {
                def readwrite foo(val a: int): int { return 0; }
                def readwrite foo(val a: boolean): boolean { return false; }
            }
            task Main: {
                new A.foo(4);
                new A.foo(true);
            }
        ''')
        program.main.expr(0).type.assertThat(instanceOf(Int))
        program.main.expr(1).type.assertThat(instanceOf(Boolean))
        
        program = parse('''
            class rolez.lang.Object
            class A {
                def readwrite foo(val a: readwrite A): int { return 0; }
                def readwrite foo(val a: readonly  A): boolean { return false; }
            }
            task Main: {
                new A.foo(new A);
                new A.foo(new A as readonly A);
            }
        ''')
        program.main.expr(0).type.assertThat(instanceOf(Int))
        program.main.expr(1).type.assertThat(instanceOf(Boolean))
        
        // (Switch order of declaration to rule out accidental selection of the correct one)
        program = parse('''
            class rolez.lang.Object
            class A {
                def readwrite foo(val a: readonly  A): boolean { return false; }
                def readwrite foo(val a: readwrite A): int { return 0; }
            }
            task Main: {
                new A.foo(new A);
                new A.foo(new A as readonly A);
            }
        ''')
        program.main.expr(0).type.assertThat(instanceOf(Int))
        program.main.expr(1).type.assertThat(instanceOf(Boolean))
        
        program = parse('''
            class rolez.lang.Object
            class A {
                def readwrite foo(val a: readonly  A, val b: readonly  A): boolean { return false; }
                def readwrite foo(val a: readwrite A, val b: readwrite A): int { return 0; }
            }
            task Main: {
                new A.foo(new A, new A);
                new A.foo(new A, new A as readonly A);
                new A.foo(new A, new A as readonly A);
                new A.foo(new A as readonly A, new A as readonly A);
            }
        ''')
        program.main.expr(0).type.assertThat(instanceOf(Int))
        program.main.expr(1).type.assertThat(instanceOf(Boolean))
        program.main.expr(1).type.assertThat(instanceOf(Boolean))
        program.main.expr(1).type.assertThat(instanceOf(Boolean))
        
        program = parse('''
            class rolez.lang.Object
            class A {
                def readwrite foo(val a: readwrite A, val b: readwrite A): int { return 0; }
                def readwrite foo(val a: readonly  A, val b: readonly  A): boolean { return false; }
            }
            task Main: {
                new A.foo(new A, new A);
                new A.foo(new A as readonly A, new A);
                new A.foo(new A, new A as readonly A);
                new A.foo(new A as readonly A, new A as readonly A);
            }
        ''')
        program.main.expr(0).type.assertThat(instanceOf(Int))
        program.main.expr(1).type.assertThat(instanceOf(Boolean))
        program.main.expr(1).type.assertThat(instanceOf(Boolean))
        program.main.expr(1).type.assertThat(instanceOf(Boolean))
    }
    
    @Test
    def testTMemberAccessMethodAmbiguous() {
        parse('''
            class rolez.lang.Object
            class A {
                def readwrite foo(val a: readonly  A, val b: readwrite A): {}
                def readwrite foo(val a: readwrite A, val b: readonly  A): {}
            }
            task Main: {
                new A.foo(new A, new A);
            }
        ''').assertError(METHOD_SELECTOR, AMBIGUOUS_CALL)
        
        parse('''
            class rolez.lang.Object
            class A {
                def readwrite foo(val a: readwrite Object, val b: readwrite A): {}
                def readwrite foo(val a: readwrite A, val b: readwrite Object): {}
            }
            task Main: {
                new A.foo(new A, new A);
            }
        ''').assertError(METHOD_SELECTOR, AMBIGUOUS_CALL)
    }
    
    @Test 
    def testTThis() {
        for(expected : Role.values) {
            val program = parse('''
                class rolez.lang.Object
                class A {
                    def «expected» foo: { this; }
                }
            ''')
            program.findClass("A").findMethod("foo").lastExpr.type
                .assertThat(isRoleType(expected, newClassRef(program.findClass("A"))))
        }
        
        val program = parse('''
            class rolez.lang.Object
            class A {
                new { this; }
            }
        ''')
        program.findClass("A").constructors.head.lastExpr.type
            .assertThat(isRoleType(READWRITE, newClassRef(program.findClass("A"))))
    }
    
    @Test
    def testTThisTask() {
        parse('''
            task Main: {
                this;
            }
        ''').assertError(THIS, TTHIS)
    }
    
    @Test
    def testTVarRef() {
        parse('''
            task Main: {
                val i: int = 5;
                i;
            }
        ''').main.lastExpr.type.assertThat(instanceOf(Int))
        parse('''
            class rolez.lang.Object
            class A {
                def pure foo(val i: int): {
                    i;
                }
            }
            task Main: {}
        ''').findClass("A").methods.head.lastExpr.type.assertThat(instanceOf(Int))
        
        var program = parse('''
            class rolez.lang.Object
            class A
            task Main: {
                val a: readonly A = new A;
                a;
            }
        ''')
        program.main.lastExpr.type
            .assertThat(isRoleType(READONLY, newClassRef(program.findClass("A"))))
    }
    
    @Test
    def testTNew() {
        var program = parse('''
            class rolez.lang.Object
            class A
            task Main: { new A; }
        ''')
        program.main.lastExpr.type
            .assertThat(isRoleType(READWRITE, newClassRef(program.findClass("A"))))
        
        program = parse('''
            class rolez.lang.Object
            class rolez.lang.Array
            task Main: { new Array[int]; }
        ''')
        program.main.lastExpr.type
            .assertThat(isRoleType(READWRITE, newClassRef(program.findClass(arrayClassName), newIntType)))
        
        program = parse('''
            class rolez.lang.Object
            class A
            class rolez.lang.Array
            task Main: { new Array[readonly A]; }
        ''')
        program.main.lastExpr.type
            .assertThat(isRoleType(READWRITE, newClassRef(program.findClass(arrayClassName),
                newRoleType(READONLY, newClassRef(program.findClass("A"))))))
        
        program = parse('''
            class rolez.lang.Object
            class A
            class rolez.lang.Array
            task Main: { new Array[pure Array[readwrite A]]; }
        ''')
        val array = program.findClass(arrayClassName)
        program.main.lastExpr.type
            .assertThat(isRoleType(READWRITE, newClassRef(array,
                newRoleType(PURE, newClassRef(array,
                    newRoleType(READWRITE, newClassRef(program.findClass("A"))))))))
    }
    
    @Test
    def testTNewErrorInArg() {
        parse('''
            class rolez.lang.Object
            class A {
                new(val i: int) {}
                def pure foo(val i: int): { new A(!5); }
            }
        ''').assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test
    def testTNewTypeMismatch() {
        parse('''
            class rolez.lang.Object
            class A
            task Main: { new A(5); }
        ''').assertError(NEW, null, "no suitable constructor")
        parse('''
            class rolez.lang.Object
            class A { new {} }
            task Main: { new A(5); }
        ''').assertError(NEW, null, "no suitable constructor")
        parse('''
            class rolez.lang.Object
            class A { new(val c: char) {} }
            task Main: { new A(5, false); }
        ''').assertError(NEW, null, "no suitable constructor")
        parse('''
            class rolez.lang.Object
            class A { new(val i: int) {} }
            task Main: { new A; }
        ''').assertError(NEW, null, "no suitable constructor")
        
        parse('''
            class rolez.lang.Object
            class A { new(val i: int) {} }
            task Main: { new A(false); }
        ''').assertError(NEW, null, "no suitable constructor")
        parse('''
            class rolez.lang.Object
            class A { new(val a: readwrite A) {} }
            task Main: { new A(new Object); }
        ''').assertError(NEW, null, "no suitable constructor")
    }
    
    @Test
    def testTNewOverloading() {
        parse('''
            class rolez.lang.Object
            class A {
                new(val a: int) {}
                new(val a: boolean) {}
            }
            task Main: {
                new A(4);
                new A(true);
            }
        ''').assertNoErrors
        
        // TODO: For the following, test that the right constructor is chosen,
        // either by linking something or after code generation.
        parse('''
            class rolez.lang.Object
            class A
            class B {
                new(val a: readwrite A) {}
                new(val a: readonly  A) {}
            }
            task Main: {
                new B(new A);
                new B(new A as readonly A);
            }
        ''').assertNoErrors
        
        // (Switch order of declaration to rule out accidental selection of the correct one)
        parse('''
            class rolez.lang.Object
            class A
            class B {
                new(val a: readonly  A) {}
                new(val a: readwrite A) {}
            }
            task Main: {
                new B(new A);
                new B(new A as readonly A);
            }
        ''').assertNoErrors
    }
    
    @Test
    def testTNewAmbiguous() {
        parse('''
            class rolez.lang.Object
            class A
            class B {
                new(val a: readonly  A, val b: readwrite A) {}
                new(val a: readwrite A, val b: readonly  A) {}
            }
            task Main: {
                new B(new A, new A);
            }
        ''').assertError(NEW, null, "constructor", "ambiguous")
        
        parse('''
            class rolez.lang.Object
            class A
            class B {
                new(val a: readwrite Object, val b: readwrite A) {}
                new(val a: readwrite A, val b: readwrite Object) {}
            }
            task Main: {
                new B(new A, new A);
            }
        ''').assertError(NEW, null, "constructor", "ambiguous")
    }
    
    @Test
    def testTStart() {
        var program = parse('''
            class rolez.lang.Object
            class rolez.lang.Task
            task T: int { return 0; }
            task Main: { start T; }
        ''')
        program.main.lastExpr.type
            .assertThat(isRoleType(PURE, newClassRef(program.findClass(taskClassName), newIntType)))
        
        program = parse('''
            class rolez.lang.Object
            class rolez.lang.Task
            task T: {}
            task Main: { start T; }
        ''')
        program.main.lastExpr.type
            .assertThat(isRoleType(PURE, newClassRef(program.findClass(taskClassName), newVoidType)))
        
        program = parse('''
            class rolez.lang.Object
            class rolez.lang.Task
            class A
            task T: readwrite A { return null; }
            task Main: { start T; }
        ''')
        program.main.lastExpr.type
            .assertThat(isRoleType(PURE, newClassRef(program.findClass(taskClassName),
                newRoleType(READWRITE, newClassRef(program.findClass("A"))))))
        
        parse('''
            class rolez.lang.Object
            class rolez.lang.Task
            task T(val i: int): {}
            task Main: { start T(5); }
        ''').assertNoErrors
        parse('''
            class rolez.lang.Object
            class rolez.lang.Task
            class A
            task T(val a: pure A): {}
            task Main: {
                start T(new A);
                start T(new A as readonly A);
                start T(new A as pure A);
                start T(null);
            }
        ''').assertNoErrors
        parse('''
            class rolez.lang.Object
            class rolez.lang.Task
            class A
            task T(val i: int, val c: char, val a: readwrite A): {}
            task Main: {
                start T(0, 'c', new A);
            }
        ''').assertNoErrors
    }
    
    @Test
    def testTStartTaskClassNotDefined() {
        parse('''
            class rolez.lang.Object
            task T: {}
            task Main: { start T; }
        ''').assertError(START, TSTART, "task class", "not defined")
    }
    
    @Test
    def testTStartErrorInArg() {
        parse('''
            class rolez.lang.Object
            class rolez.lang.Task
            task T(val i: int): {}
            task Main: { start T(!5); }
        ''').assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test
    def testTStartTypeMismatch() {
        parse('''
            class rolez.lang.Object
            class rolez.lang.Task
            task T: int {}
            task Main: { start T(5); }
        ''').assertError(START, null, "too many arguments")
        parse('''
            class rolez.lang.Object
            class rolez.lang.Task
            task T(val i: int): int {}
            task Main: { start T; }
        ''').assertError(START, null, "too few arguments")
        
        parse('''
            class rolez.lang.Object
            class rolez.lang.Task
            task T(val i: int): int {}
            task Main: { start T(true); }
        ''').assertError(BOOLEAN_LITERAL, SUBTYPEEXPR, "boolean", "int")
    }
    
    @Test
    def testTParenthesized() {
        val program = parse('''
            class rolez.lang.Object
            class A
            task Main: {
                (5);
                ('c');
                (new A);
                (new A as pure A);
            }
        ''')
        program.main.expr(0).type.assertThat(instanceOf(Int))
        program.main.expr(1).type.assertThat(instanceOf(Char))
        program.main.expr(2).type.assertThat(
            isRoleType(READWRITE, newClassRef(program.findClass("A"))))
        program.main.expr(3).type.assertThat(
            isRoleType(PURE, newClassRef(program.findClass("A"))))
    }
    
    @Test
    def testTParenthesizedErrorInExpr() {
        parse('''
            task Main: { (!5); }
        ''').assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test
    def testTStringLiteral() {
        val program = parse('''
            class rolez.lang.Object
            class rolez.lang.String
            task Main: { "Hi"; }
        ''')
        program.main.lastExpr.type.assertThat(
            isRoleType(READWRITE, newClassRef(program.findClass(stringClassName))))
    }
    
    @Test
    def testTStringLiteralStringClassNotDefined() {
        parse('''
            class rolez.lang.Object
            task Main: { "Hi"; }
        ''').assertError(STRING_LITERAL, TSTRINGLITERAL, "rolez.lang.String class", "not defined")
    }
    
    @Test
    def testTNullLiteral() {
        parse('''
            task Main: { null; }
        ''').main.lastExpr.type.assertThat(instanceOf(Null))
    }
    
    @Test
    def testTIntLiteral() {
        parse('''
            task Main: { 5; }
        ''').main.lastExpr.type.assertThat(instanceOf(Int))
    }
    
    @Test
    def testTDoubleLiteral() {
        parse('''
            task Main: { 5.0; }
        ''').main.lastExpr.type.assertThat(instanceOf(Double))
    }
    
    @Test
    def testTBooleanLiteral() {
        parse('''
            task Main: { true; }
        ''').main.lastExpr.type.assertThat(instanceOf(Boolean))
    }
    
    @Test
    def testTCharLiteral() {
        parse('''
            task Main: { 'c'; }
        ''').main.lastExpr.type.assertThat(instanceOf(Char))
    }
    
    @Test
    def testWBlock() {
        parse('''
            class rolez.lang.Object
            task Main: {
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
            }
        ''').assertNoErrors
        
        parse('''
            task Main: {
                false as int;
            }
        ''').assertError(CAST, null, "cannot cast", "boolean", "int")
        parse('''
            class rolez.lang.Object
            task Main: {
                {
                    new Object;
                    {
                        new Object;
                        false as int;
                        new Object;
                        {}
                    }
                }
            }
        ''').assertError(CAST, null, "cannot cast", "boolean", "int")
    }
    
    @Test
    def testWLocalVarDecl() {
        parse('''
            class rolez.lang.Object
            class A
            task Main: {
                val i: int = 1;
                val a: readwrite A = new A;
                val b: pure Object = new A;
                val c: readwrite Object = null;
            }
        ''').assertNoErrors
        
        parse('''
            task Main: {
                val i: int = false;
            }
        ''').assertError(BOOLEAN_LITERAL, SUBTYPEEXPR, "boolean", "int")
        parse('''
            class rolez.lang.Object
            task Main: {
                val o: readwrite Object = new Object as pure Object;
            }
        ''').assertError(CAST, SUBTYPEEXPR, "pure rolez.lang.Object", "readwrite rolez.lang.Object")
        parse('''
            class rolez.lang.Object
            class A
            task Main: {
                val o: pure A = new A as pure Object;
            }
        ''').assertError(CAST, SUBTYPEEXPR, "pure rolez.lang.Object", "pure A")
    }
    
    @Test
    def testWIfStmt() {
        parse('''
            class rolez.lang.Object
            task Main: {
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
            }
        ''').assertNoErrors
        
        parse('''
            class rolez.lang.Object
            task Main: {
                if(5)
                    new Object;
            }
        ''').assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
        parse('''
            task Main: {
                if(true)
                    false as int;
            }
        ''').assertError(CAST, null, "cannot cast", "boolean", "int")
        parse('''
            task Main: {
                if(true) {}
                else
                    false as int;
            }
        ''').assertError(CAST, null, "cannot cast", "boolean", "int")
    }
    
    @Test
    def testWWhileLoop() {
        parse('''
            class rolez.lang.Object
            task Main: {
                while(true)
                    new Object;
                
                while(3 == 2) {
                    new Object;
                    new Object;
                }
            }
        ''').assertNoErrors
        
        parse('''
            class rolez.lang.Object
            task Main: {
                while(5)
                    new Object;
            }
        ''').assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
        parse('''
            task Main: {
                while(true)
                    false as int;
            }
        ''').assertError(CAST, null, "cannot cast", "boolean", "int")
    }
    
    @Test
    def testWSuperConstrCall() {
        parse('''
            class rolez.lang.Object
            class A
            class B extends A {
                new {}
            }
            class C extends B
            class D extends C {
                new(val i: int) { 2; }
            }
            class E extends D {
                new {
                    super(0);
                    5;
                }
                new(val a: readonly A, val b: pure B) { super(1); }
            }
            class F extends E {
                new(val a: readwrite A) { super(a, new B); }
            }
            class G {
                new { super(); }
            }
        ''').assertNoErrors
    }
    
    @Test
    def testWSuperConstrCallErrorInArg() {
        parse('''
            class rolez.lang.Object
            class A {
                new(val i: int) {}
            }
            class B extends A {
                new { super(!5); }
            }
        ''').assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test
    def testWSuperConstrCallTypeMismatch() {
        parse('''
            class rolez.lang.Object
            class A
            class B extends A {
                new { super(5); }
            }
        ''').assertError(SUPER_CONSTR_CALL, null, "no suitable super constructor")
        parse('''
            class rolez.lang.Object
            class A {
                new { super(5); }
            }
        ''').assertError(SUPER_CONSTR_CALL, null, "no suitable super constructor")
        parse('''
            class rolez.lang.Object
            class A {
                new {}
            }
            class B extends A {
                new { super(5); }
            }
        ''').assertError(SUPER_CONSTR_CALL, null, "no suitable super constructor")
        parse('''
            class rolez.lang.Object
            class A {
                new(val c: char) {}
            }
            class B extends A {
                new { super(5, false); }
            }
        ''').assertError(SUPER_CONSTR_CALL, null, "no suitable super constructor")
        parse('''
            class rolez.lang.Object
            class A {
                new(val i: int) {}
            }
            class B extends A { 
                new { super(); }
            }
        ''').assertError(SUPER_CONSTR_CALL, null, "no suitable super constructor")
        
        parse('''
            class rolez.lang.Object
            class A {
                new(val i: int) {}
            }
            class B extends A {
                new { super(false); }
            }
        ''').assertError(SUPER_CONSTR_CALL, null, "no suitable super constructor")
        parse('''
            class rolez.lang.Object
            class A {
                new(val a: readwrite A) {}
            }
            class B extends A {
                new { super(new Object); }
            }
        ''').assertError(SUPER_CONSTR_CALL, null, "no suitable super constructor")
    }
    
    @Test
    def testWSuperConstrCallOverloading() {
        parse('''
            class rolez.lang.Object
            class A {
                new(val a: int) {}
                new(val a: boolean) {}
            }
            class B extends A {
                new { super(4); }
                new(val b: boolean) { super(b); }
            }
        ''').assertNoErrors
        
        // TODO: For the following, test that the right constructor is chosen,
        // either by linking something or after code generation.
        parse('''
            class rolez.lang.Object
            class A
            class B {
                new(val a: readwrite A) {}
                new(val a: readonly  A) {}
            }
            class C extends B {
                new { super(new A); }
                new(val i: int) { super(new A as readonly A); }
            }
        ''').assertNoErrors
        
        // (Switch order of declaration to rule out accidental selection of the correct one)
        parse('''
            class rolez.lang.Object
            class A
            class B {
                new(val a: readonly  A) {}
                new(val a: readwrite A) {}
            }
            class C extends B {
                new { super(new A); }
                new(val i: int) { super(new A as readonly A); }
            }
        ''').assertNoErrors
    }
    
    @Test
    def testWSuperConstrCallAmbiguous() {
        parse('''
            class rolez.lang.Object
            class A
            class B {
                new(val a: readonly  A, val b: readwrite A) {}
                new(val a: readwrite A, val b: readonly  A) {}
            }
            class C extends B {
                new { super(new A, new A); }
            }
        ''').assertError(SUPER_CONSTR_CALL, null, "constructor", "ambiguous")
        
        parse('''
            class rolez.lang.Object
            class A
            class B {
                new(val a: readwrite Object, val b: readwrite A) {}
                new(val a: readwrite A, val b: readwrite Object) {}
            }
            class C extends B {
                new { super(new A, new A); }
            }
        ''').assertError(SUPER_CONSTR_CALL, null, "constructor", "ambiguous")
    }
    
    @Test
    def testWReturn() {
        parse('''
            class rolez.lang.Object
            class A {
                def pure a: {}
                def pure b: {
                    return;
                }
                def pure c: int {
                    return 5;
                }
                def pure d: readwrite Object {
                    return new Object;
                }
                def pure e: pure Object {
                    return new A;
                }
                def pure f: readonly A {
                    return new A as readonly A;
                }
            }
        ''').assertNoErrors
        
        parse('''
            class rolez.lang.Object
            class A {
                def pure a: {
                    return 1;
                }
            }
        ''').assertError(INT_LITERAL, SUBTYPEEXPR, "int", "void")
        parse('''
            task T: {
                return 1;
            }
        ''').assertError(INT_LITERAL, SUBTYPEEXPR, "int", "void")
        parse('''
            class rolez.lang.Object
            class A {
                def pure a: int {
                    return false;
                }
            }
        ''').assertError(BOOLEAN_LITERAL, SUBTYPEEXPR, "boolean", "int")
        parse('''
            class rolez.lang.Object
            class A {
                def pure a: readwrite A {
                    return new A as pure A;
                }
            }
        ''').assertError(CAST, SUBTYPEEXPR, "pure A", "readwrite A")
    }
    
    @Test
    def testSubtype() {
        parse('''
            class rolez.lang.Object
            class rolez.lang.Array
            class A
            task Main: {
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
                
                var ia: pure Array[int] = new Array[int];
                ia = null;
                var oa: readwrite Array[pure Object] = new Array[pure Object];
                oa = null;
            }
        ''').assertNoErrors
    }
    
    @Test
    def testSubtypePrimitiveMismatch() {
        parse('''
            task Main: {
                val i: int = false;
            }
        ''').assertError(BOOLEAN_LITERAL, SUBTYPEEXPR, "boolean", "int")
        parse('''
            task Main: {
                val i: int = 'c';
            }
        ''').assertError(CHAR_LITERAL, SUBTYPEEXPR, "char", "int")
        parse('''
            task Main: {
                val b: boolean = 1;
            }
        ''').assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
        parse('''
            task Main: {
                val b: boolean = 'c';
            }
        ''').assertError(CHAR_LITERAL, SUBTYPEEXPR, "char", "boolean")
        // I think we get the picture...
    }
    
    @Test
    def testSubtypeSimpleClassMismatch() {
        parse('''
            class rolez.lang.Object
            class A
            task Main: {
                val a: readwrite A = new Object;
            }
        ''').assertError(NEW, SUBTYPEEXPR, "readwrite rolez.lang.Object", "readwrite A")
    }
    
    @Test
    def testSubtypeGenericClassMismatch() {
        parse('''
            class rolez.lang.Object
            class rolez.lang.Array
            task Main: {
                val a: pure Array[int] = new Array[boolean];
            }
        ''').assertError(NEW, SUBTYPEEXPR, "readwrite rolez.lang.Array[boolean]", "pure rolez.lang.Array[int]")
        parse('''
            class rolez.lang.Object
            class rolez.lang.Array
            task Main: {
                val a: pure Array[int] = new Array[pure Object];
            }
        ''').assertError(NEW, SUBTYPEEXPR, "readwrite rolez.lang.Array[pure rolez.lang.Object]", "pure rolez.lang.Array[int]")
        parse('''
            class rolez.lang.Object
            class rolez.lang.Array
            task Main: {
                val a: pure Array[pure Object] = new Array[int];
            }
        ''').assertError(NEW, SUBTYPEEXPR, "readwrite rolez.lang.Array[int]", "pure rolez.lang.Array[pure rolez.lang.Object]")
        parse('''
            class rolez.lang.Object
            class rolez.lang.Array
            class A
            task Main: {
                val a: pure Array[pure Object] = new Array[pure A];
            }
        ''').assertError(NEW, SUBTYPEEXPR, "readwrite rolez.lang.Array[pure A]", "pure rolez.lang.Array[pure rolez.lang.Object]")
        parse('''
            class rolez.lang.Object
            class rolez.lang.Array
            class A
            task Main: {
                val a: pure Array[pure A] = new Array[pure Object];
            }
        ''').assertError(NEW, SUBTYPEEXPR, "readwrite rolez.lang.Array[pure rolez.lang.Object]", "pure rolez.lang.Array[pure A]")
        parse('''
            class rolez.lang.Object
            class rolez.lang.Array
            class A
            task Main: {
                val a: pure Array[pure A] = new Array[readwrite A];
            }
        ''').assertError(NEW, SUBTYPEEXPR, "readwrite rolez.lang.Array[readwrite A]", "pure rolez.lang.Array[pure A]")
        parse('''
            class rolez.lang.Object
            class rolez.lang.Array
            class A
            task Main: {
                val a: pure Array[readwrite A] = new Array[pure A];
            }
        ''').assertError(NEW, SUBTYPEEXPR, "readwrite rolez.lang.Array[pure A]", "pure rolez.lang.Array[readwrite A]")
    }
    
    @Test
    def testSubtypeRoleMismatch() {
        parse('''
            class rolez.lang.Object
            class A
            task Main: {
                val a: readwrite A = new A as readonly A;
            }
        ''').assertError(CAST, SUBTYPEEXPR, "readonly A", "readwrite A")
        parse('''
            class rolez.lang.Object
            class A
            task Main: {
                val a: readwrite A = new A as pure A;
            }
        ''').assertError(CAST, SUBTYPEEXPR, "pure A", "readwrite A")
        parse('''
            class rolez.lang.Object
            class A
            task Main: {
                val a: readonly A = new A as pure A;
            }
        ''').assertError(CAST, SUBTYPEEXPR, "pure A", "readonly A")
    }
}
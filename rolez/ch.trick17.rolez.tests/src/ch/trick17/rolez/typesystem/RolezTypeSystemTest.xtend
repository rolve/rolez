package ch.trick17.rolez.typesystem

import ch.trick17.rolez.RolezExtensions
import ch.trick17.rolez.TestUtils
import ch.trick17.rolez.rolez.Boolean
import ch.trick17.rolez.rolez.Char
import ch.trick17.rolez.rolez.Double
import ch.trick17.rolez.rolez.GenericClassRef
import ch.trick17.rolez.rolez.Int
import ch.trick17.rolez.rolez.Null
import ch.trick17.rolez.rolez.Program
import ch.trick17.rolez.rolez.Pure
import ch.trick17.rolez.rolez.ReadOnly
import ch.trick17.rolez.rolez.ReadWrite
import ch.trick17.rolez.rolez.RoleType
import ch.trick17.rolez.rolez.RolezFactory
import ch.trick17.rolez.rolez.SimpleClassRef
import ch.trick17.rolez.rolez.Void
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
    @Inject extension RolezExtensions
    @Inject extension TestUtils
    @Inject extension ParseHelper<Program>
    @Inject extension ValidationTestHelper
    
    @Test def testTAssignment() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A
            class B extends A 
            task Main: {
                var a: readwrite A;
                a = new B;
            }
        ''').main.lastExpr.type.assertRoleType(ReadWrite, "A")
    }
    
    @Test def testTAssignmentErrorInOp() {
        parse("task Main: { !5 = 5; }").assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
        
        parse('''
            task Main: {
                var x: int;
                x = !5;
            }
        ''').assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test def testTAssignmentNotAssignable() {
        parse('''
            task Main: {
                5 = 3;
            }
        ''').assertError(INT_LITERAL, AEXPR, "assign", "5")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def pure foo: {}
            }
            task Main: {
                new A.foo = 3;
            }
        ''').assertError(MEMBER_ACCESS, AMEMBERACCESS, "assign", "foo")
        
        parse('''
            task Main: {
                val x: int = 5;
                x = 5;
            }
        ''').assertError(VAR_REF, AVARREF, "assign", "value")
        
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
            task Main: {
                var x: int;
                x = true;
            }
        ''').assertError(BOOLEAN_LITERAL, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test def testTBooleanExpr() {
        parse("task Main: { true || false; }").main.lastExpr.type.assertThat(instanceOf(Boolean))
        parse("task Main: { true && false; }").main.lastExpr.type.assertThat(instanceOf(Boolean))
    }
    
    @Test def testTBooleanExprErrorInOp() {
        parse("task Main: { !5 || false; }")
            .assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
        parse("task Main: { true || !5; }")
            .assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test def testTBooleanExprTypeMismatch() {
        parse("task Main: { 5 || false; }").assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
        parse("task Main: { true  || 5; }").assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test def testTEqualityExpr() {
        parse("task Main: { true == false; }").main.lastExpr.type.assertThat(instanceOf(Boolean))
        parse("task Main: {    5 !=     3; }").main.lastExpr.type.assertThat(instanceOf(Boolean))

        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A
            task Main: {
                new Object == new A;
                new A == new Object;
                new A == new A;
            }
        ''').assertNoErrors
    }
    
    @Test def testTEqualityExprErrorInOp() {
        parse("task Main: { !5 == false; }")
            .assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
        parse("task Main: { true != !5; }")
            .assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test def testTEqualityExprIncompatibleTypes() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A
            class B
            task Main: { new A == new B; }
        ''').assertError(EQUALITY_EXPR, null, "compare", "A", "B")
        // IMPROVE: Test issue code once supported for explicit failures
        
        parse("task Main: { 42 != false; }")
            .assertError(EQUALITY_EXPR, null, "compare", "int", "boolean")
    }
    
    @Test def testTRelationalExpr() {
        parse("task Main: {   5 <    6; }").main.lastExpr.type.assertThat(instanceOf(Boolean))
        parse("task Main: {  -1 <= -10; }").main.lastExpr.type.assertThat(instanceOf(Boolean))
        parse("task Main: { 'a' >  ' '; }").main.lastExpr.type.assertThat(instanceOf(Boolean))
        parse("task Main: { 3+4 >=   0; }").main.lastExpr.type.assertThat(instanceOf(Boolean))
    }
    
    @Test def testTRelationalExprErrorInOp() {
        parse("task Main: { -true < 0; }")
            .assertError(BOOLEAN_LITERAL, SUBTYPEEXPR, "int", "boolean")
        parse("task Main: { 100 <= -false; }")
            .assertError(BOOLEAN_LITERAL, SUBTYPEEXPR, "int", "boolean")
        parse("task Main: { -'a' > 0; }")
            .assertError(CHAR_LITERAL, SUBTYPEEXPR, "int", "char")
        parse("task Main: { 100 >= -false; }")
            .assertError(BOOLEAN_LITERAL, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test def testTRelationalExprIncompatibleTypes() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
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
    
    @Test def testTArithmeticExpr() {
        parse("task Main: {   4 +  4; }").main.lastExpr.type.assertThat(instanceOf(Int))
        parse("task Main: {   0 -  0; }").main.lastExpr.type.assertThat(instanceOf(Int))
        parse("task Main: {   3 *  2; }").main.lastExpr.type.assertThat(instanceOf(Int))
        parse("task Main: { 100 / -1; }").main.lastExpr.type.assertThat(instanceOf(Int))
        parse("task Main: { -99 %  3; }").main.lastExpr.type.assertThat(instanceOf(Int))
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.String mapped to java.lang.String
            task Main: { "Hi" + " World"; }
        ''').main.lastExpr.type.assertRoleType(ReadOnly, stringClassName)
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.String mapped to java.lang.String
            task Main: { "" + '5'; }
        ''').main.lastExpr.type.assertRoleType(ReadOnly, stringClassName)
            
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.String mapped to java.lang.String
            task Main: { null + " "; }
        ''').main.lastExpr.type.assertRoleType(ReadOnly, stringClassName)
    }
    
    @Test def testTArithmeticExprErrorInOp() {
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
    
    @Test def testTArtithmeticExprTypeMismatch() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            task Main: { new Object + new Object; }
        ''').assertError(ARITHMETIC_BINARY_EXPR, null, "operator", "undefined", "object")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A
            class B
            task Main: { new A - new B; }
        ''').assertError(ARITHMETIC_BINARY_EXPR, null, "operator", "undefined", "A", "B")
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.String mapped to java.lang.String
            task Main: { "Hello" - "World"; }
        ''').assertError(ARITHMETIC_BINARY_EXPR, null, "operator", "undefined", "String")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.String mapped to java.lang.String
            task Main: { "Hello" * new Object; }
        ''').assertError(ARITHMETIC_BINARY_EXPR, null, "operator", "undefined", "String", "Object")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.String mapped to java.lang.String
            task Main: { 5 / "World"; }
        ''').assertError(ARITHMETIC_BINARY_EXPR, null, "operator", "undefined", "int", "String")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.String mapped to java.lang.String
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
    
    @Test def testTCast() {
        // Redundant casts
        parse("task Main: { 5 as int; }").main.lastExpr.type.assertThat(instanceOf(Int))
        parse("task Main: { true as boolean; }").main.lastExpr.type.assertThat(instanceOf(Boolean))
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            task Main: { new Object as readwrite Object; }
        ''').main.lastExpr.type.assertRoleType(ReadWrite, objectClassName)
        
        // Upcasts
        var program = parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Array[T] mapped to rolez.lang.Array {
                mapped new(length: int)
            }
            class A
            task Main: {
                new A as readwrite Object;
                new A as readonly A;
                new A as pure A;
                null as readwrite A;
                null as readonly A;
                new Array[int](3) as readonly Array[int];
                new Array[pure A](0) as readonly Array[pure A];
            }
        ''')
        program.main.expr(0).type.assertRoleType(ReadWrite, objectClassName)
        program.main.expr(1).type.assertRoleType(ReadOnly , "A")
        program.main.expr(2).type.assertRoleType(Pure     , "A")
        program.main.expr(3).type.assertRoleType(ReadWrite, "A")
        program.main.expr(4).type.assertRoleType(ReadOnly, "A")
        program.main.expr(5).type.assertRoleType(ReadOnly, arrayClassName, Int)
        program.main.expr(6).type.assertInstanceOf(RoleType) => [
            role.assertThat(instanceOf(ReadOnly))
            base.assertInstanceOf(GenericClassRef) => [
                clazz.name.assertThat(is(arrayClassName.toString))
                typeArg.assertInstanceOf(RoleType) => [
                    role.assertThat(instanceOf(Pure))
                    base.assertInstanceOf(SimpleClassRef) => [ clazz.name.assertThat(is("A"))]
                ]
            ]
        ]
        
        // Downcasts
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A
            task Main: { new Object as readwrite A; }
        ''').main.lastExpr.type.assertRoleType(ReadWrite, "A")
    }
    
    @Test def testTCastErrorInOp() {
        parse("task Main: { !5 as boolean; }")
            .assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test def testTCastIllegal() {
        parse("task Main: { 5 as boolean; }")
            .assertError(CAST, null, "cast", "int", "boolean")
        parse("task Main: { false as int; }")
            .assertError(CAST, null, "cast", "boolean", "int")
        parse("task Main: { null as int; }")
            .assertError(CAST, null, "cast", "null", "int")
        parse("task Main: { 5 as ; }")
            .assertError(CAST, null, "cast", "int", "void")
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            task Main: { 5 as readwrite Object; }
        ''').assertError(CAST, null, "cast", "readwrite rolez.lang.Object", "int")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            task Main: { new Object as int; }
        ''').assertError(CAST, null, "cast", "readwrite rolez.lang.Object", "int")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A
            task Main: { new A as readonly A as readwrite A; }
        ''').assertError(CAST, null, "cast", "readwrite A", "readonly A")
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Array[T] mapped to rolez.lang.Array
            task Main: { new Array[boolean] as readwrite Array[int]; }
        ''').assertError(CAST, null, "cast", "readwrite rolez.lang.Array[boolean]", "readwrite rolez.lang.Array[int]")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Array[T] mapped to rolez.lang.Array
            class A
            task Main: { new Array[pure Object] as readwrite Array[pure A]; }
        ''').assertError(CAST, null, "cast", "readwrite rolez.lang.Array[pure rolez.lang.Object]", "readwrite rolez.lang.Array[pure A]")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Array[T] mapped to rolez.lang.Array
            class A
            task Main: { new Array[readwrite A] as readwrite Array[pure A]; }
        ''').assertError(CAST, null, "cast", "readwrite rolez.lang.Array[readwrite A]", "readwrite rolez.lang.Array[pure A]")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Array[T] mapped to rolez.lang.Array
            class A
            task Main: { new Array[pure A] as readwrite Array[readwrite A]; }
        ''').assertError(CAST, null, "cast", "readwrite rolez.lang.Array[pure A]", "readwrite rolez.lang.Array[readwrite A]")
    }
    
    @Test def testTUnaryMinus() {
        parse("task Main: { -2; }").main.lastExpr.type.assertThat(instanceOf(Int))
        parse("task Main: { val a: int = 5; -a; }").main.lastExpr.type.assertThat(instanceOf(Int))
        parse("task Main: { -(4-4); }").main.lastExpr.type.assertThat(instanceOf(Int))
    }
    
    @Test def testTUnaryMinusErrorInOp() {
        parse("task Main: { -!5; }")
            .assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
        parse("task Main: { -(-'a'); }")
            .assertError(CHAR_LITERAL, SUBTYPEEXPR, "char", "int")
    }
    
    @Test def testTUnaryMinusTypeMismatch() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            task Main: { -new Object; }
        ''').assertError(NEW, SUBTYPEEXPR, "Object", "int")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.String mapped to java.lang.String
            task Main: { -"Hello"; }
        ''').assertError(STRING_LITERAL, SUBTYPEEXPR, "String", "int")
        
        parse("task Main: { -'a'; }")
            .assertError(CHAR_LITERAL, SUBTYPEEXPR, "char", "int")
        parse("task Main: { -true; }")
            .assertError(BOOLEAN_LITERAL, SUBTYPEEXPR, "boolean", "int")
        parse("task Main: { -null; }")
            .assertError(NULL_LITERAL, SUBTYPEEXPR, "null", "int")
    }
    
    @Test def testTUnaryNot() {
        parse("task Main: { !true; }").main.lastExpr.type
            .assertThat(instanceOf(Boolean))
        parse("task Main: { val a = false; !a; }").main.lastExpr.type
            .assertThat(instanceOf(Boolean))
        parse("task Main: { !(true || false); }").main.lastExpr.type
            .assertThat(instanceOf(Boolean))
    }
    
    @Test def testTUnaryNotErrorInOp() {
        parse("task Main: { !(-'a'); }")
            .assertError(CHAR_LITERAL, SUBTYPEEXPR, "char", "int")
        parse("task Main: { !(!5); }")
            .assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test def testTUnaryNotTypeMismatch() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            task Main: { !new Object; }
        ''').assertError(NEW, SUBTYPEEXPR, "Object", "boolean")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.String mapped to java.lang.String
            task Main: { !"Hello"; }
        ''').assertError(STRING_LITERAL, SUBTYPEEXPR, "String", "boolean")
        
        parse("task Main: { !'a'; }")
            .assertError(CHAR_LITERAL, SUBTYPEEXPR, "char", "boolean")
        parse("task Main: { !5; }")
            .assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
        parse("task Main: { !null; }")
            .assertError(NULL_LITERAL, SUBTYPEEXPR, "null", "boolean")
    }
    
    @Test def testTMemberAccessErrorInTarget() {
        parse("task Main: { (!5).a; }")
            .assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test def testTMemberAccessField() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                var i: int
            }
            task Main: { new A.i; }
        ''').main.lastExpr.type.assertThat(instanceOf(Int))
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                var i: int
            }
            task Main: {
                val a: readonly A = new A;
                a.i;
            }
        ''').main.lastExpr.type.assertThat(instanceOf(Int))
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                val i: int = 0
            }
            task Main: {
                val a: pure A = new A;
                a.i;
            }
        ''').main.lastExpr.type.assertThat(instanceOf(Int))
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                var a: readwrite A
            }
            task Main: {
                val a = new A;
                a.a;
            }
        ''').main.lastExpr.type.assertRoleType(ReadWrite, "A")
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                var a: readwrite A
            }
            task Main: {
                val a: readonly A = new A;
                a.a;
            }
        ''').main.lastExpr.type.assertRoleType(ReadOnly, "A")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                var a: readonly A
            }
            task Main: {
                val a = new A;
                a.a;
            }
        ''').main.lastExpr.type.assertRoleType(ReadOnly, "A")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                var a: pure A
            }
            task Main: {
                val a = new A;
                a.a;
            }
        ''').main.lastExpr.type.assertRoleType(Pure, "A")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                var a: pure A
            }
            task Main: {
                val a: readonly A = new A;
                a.a;
            }
        ''').main.lastExpr.type.assertRoleType(Pure, "A")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                val a: readwrite A = null
            }
            task Main: {
                val a: pure A = new A;
                a.a;
            }
        ''').main.lastExpr.type.assertRoleType(Pure, "A")
    }
    
    @Test def testTMemberAccessFieldGeneric() {
        var program = parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A
            class Container[E] mapped to «Container.canonicalName» {
                mapped var e: E
            }
            task Main: {
                new Container[int].e;
                new Container[readwrite A].e;
                new Container[readonly A].e;
                (new Container[readwrite A] as readonly Container[readwrite A]).e;
            }
        ''')
        program.main.expr(0).type.assertThat(instanceOf(Int))
        program.main.expr(1).type.assertRoleType(ReadWrite, "A")
        program.main.expr(2).type.assertRoleType(ReadOnly, "A")
        program.main.expr(2).type.assertRoleType(ReadOnly, "A")
        
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
            task Main: {
                new A.e;
            }
        ''').main.lastExpr.type.assertThat(instanceOf(Int))
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
            class A { var x: int }
            task Main: {
                val a: pure A = new A;
                a.x;
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
                    task Main: {
                        val a: «actual.name» A = new A;
                        a.x;
                    }
                ''').main.lastExpr.type.assertThat(instanceOf(Int))
            }
        }
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def readwrite a: readonly A { return null; }
            }
            task Main: { new A.a; }
        ''').main.lastExpr.type.assertRoleType(ReadOnly, "A")
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
    
    @Test def testTMemberAccessMethodRoleMismatch() {
        val roles = #[createReadWrite, createReadOnly, createPure]
        for(expected : roles) {
            for(actual : roles.filter[!system.subroleSucceeded(it, expected)]) {
                parse('''
                    class rolez.lang.Object mapped to java.lang.Object
                    class A {
                        def «expected.name» x: int { return 42; }
                    }
                    task Main: {
                        val a: «actual.name» A = new A;
                        a.x;
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
            task Main: {
                new AContainer.get[pure];
            }
        ''').assertError(MEMBER_ACCESS, null, "bound mismatch", "pure", "r includes readonly")
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A
            class AContainer {
                var a: readwrite A
                def r get[r includes readonly]: r A { return this.a; }
            }
            task Main: {
                (new AContainer as pure AContainer).get;
            }
        ''').assertError(MEMBER_ACCESS, null, "bound mismatch", "pure", "r includes readonly")
    }
    
    @Test def testTMemberAccessMethodGeneric() {
        val lib = newResourceSet.with('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.String mapped to java.lang.String
            class rolez.lang.Array[T] mapped to rolez.lang.Array {
                mapped new(length: int)
                mapped def r get[r includes readonly](i: int): T with r
                mapped def readwrite set(i: int, o: T):
            }
            class Container[E] mapped to «Container.canonicalName» {
                mapped var e: E
                mapped def r get[r includes readonly]: E with r
                mapped def readwrite set(e: E):
            }
            class SubContainer[E] mapped to «SubContainer.canonicalName» extends Container[E]
        ''')
        
        parse('''
            task Main: {
                val a = new Array[int](1);
                a.set(0, 42);
                a.get(0);
            }
        ''', lib).main.expr(1).type.assertThat(instanceOf(Int))
        parse('''
            task Main: {
                val a = new SubContainer[int];
                a.get;
            }
        ''', lib).main.lastExpr.type.assertThat(instanceOf(Int))
        parse('''
            class A
            task Main: {
                new Array[pure A](1).get[readwrite](0);
            }
        ''', lib).main.lastExpr.type.assertRoleType(Pure, "A")
        
        var program = parse('''
            class A
            task Main: {
                val a = new Array[readwrite A](1);
                a.set(0, new A);
                a.get(0);
                (a as readonly Array[readwrite A]).get(0);
            }
        ''', lib)
        program.main.expr(1).type.assertRoleType(ReadWrite, "A")
        program.main.expr(2).type.assertRoleType(ReadOnly , "A")
        
        parse('''
            class IntContainer extends Container[int]
            task Main: {
                val c = new IntContainer;
                c.set(42);
                c.get;
            }
        ''', lib).main.lastExpr.type.assertThat(instanceOf(Int))
        val p = parse('''
            class ObjectContainer extends Container[readwrite Object]
            task Main: {
                val c = new ObjectContainer;
                c.set(new Object);
                c.get;
                (c as readonly ObjectContainer).get;
            }
        ''', lib)
        p.main.expr(1).type.assertRoleType(ReadWrite, objectClassName)
        p.main.expr(2).type.assertRoleType(ReadOnly , objectClassName)
        
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
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                new { this; }
            }
        ''').findNormalClass("A").constrs.head.lastExpr.type.assertRoleType(ReadWrite, "A")
    }
    
    @Test def testTThisTask() {
        parse('''
            task Main: {
                this;
            }
        ''').assertError(THIS, TTHIS)
    }
    
    @Test def testTVarRef() {
        parse('''
            task Main: {
                val i = 5;
                i;
            }
        ''').main.lastExpr.type.assertThat(instanceOf(Int))
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def pure foo(i: int): {
                    i;
                }
            }
            task Main: {}
        ''').findClass("A").methods.head.lastExpr.type.assertThat(instanceOf(Int))
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A
            task Main: {
                val a: readonly A = new A;
                a;
            }
        ''').main.lastExpr.type.assertRoleType(ReadOnly, "A")
    }
    
    /* More "new" tests in RolezLinkingTest */
    
    @Test def void testTNew() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A
            task Main: { new A; }
        ''').main.lastExpr.type.assertRoleType(ReadWrite, "A")
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Array[T] mapped to rolez.lang.Array {
                mapped new(length: int)
            }
            task Main: { new Array[int](100); }
        ''').main.lastExpr.type.assertRoleType(ReadWrite, arrayClassName, Int)
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A
            class rolez.lang.Array[T] mapped to rolez.lang.Array {
                mapped new(length: int)
            }
            task Main: { new Array[readonly A](10); }
        ''').main.lastExpr.type.assertInstanceOf(RoleType) => [
            role.assertThat(instanceOf(ReadWrite))
            base.assertInstanceOf(GenericClassRef) => [
                clazz.name.assertThat(is(arrayClassName.toString))
                typeArg.assertInstanceOf(RoleType) => [
                    role.assertThat(instanceOf(ReadOnly))
                    base.assertInstanceOf(SimpleClassRef) => [ clazz.name.assertThat(is("A"))]
                ]
            ]
        ]
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A
            class rolez.lang.Array[T] mapped to rolez.lang.Array {
                mapped new(length: int)
            }
            task Main: { new Array[pure Array[readwrite A]](1000); }
        ''').main.lastExpr.type.assertInstanceOf(RoleType) => [
            role.assertThat(instanceOf(ReadWrite))
            base.assertInstanceOf(GenericClassRef) => [
                clazz.name.assertThat(is(arrayClassName.toString))
                typeArg.assertInstanceOf(RoleType) => [
                    role.assertThat(instanceOf(Pure))
                    base.assertInstanceOf(GenericClassRef) => [
                        clazz.name.assertThat(is(arrayClassName.toString))
                        typeArg.assertInstanceOf(RoleType) => [
                            role.assertInstanceOf(ReadWrite)
                            base.assertInstanceOf(SimpleClassRef) => [ clazz.name.assertThat(is("A"))]
                        ]
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
            class Container[E] mapped to «Container.canonicalName» {
                mapped new(e: E)
            }
            task Main: { new Container[int](42); }
        ''').main.lastExpr.type.assertRoleType(ReadWrite, "Container", Int)
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.String mapped to java.lang.String
            class Container[E] mapped to «Container.canonicalName» {
                mapped new(e: E)
            }
            task Main: { new Container[readonly String]("Hello World!"); }
        ''').main.lastExpr.type.assertInstanceOf(RoleType) => [
            role.assertThat(instanceOf(ReadWrite))
            base.assertInstanceOf(GenericClassRef) => [
                clazz.name.assertThat(is("Container"))
                typeArg.assertInstanceOf(RoleType) => [
                    role.assertThat(instanceOf(ReadOnly))
                    base.assertInstanceOf(SimpleClassRef) => [
                        clazz.name.assertThat(is(stringClassName.toString))
                    ]
                ]
            ]
        ]
    }
    
    @Test def testTThe() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            object A
            task Main: {
                the A;
            }
        ''').main.lastExpr.type.assertRoleType(ReadOnly, "A")
    }
    
    @Test def testTStart() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Task
            task T: int { return 0; }
            task Main: { start T; }
        ''').main.lastExpr.type.assertRoleType(Pure, taskClassName, Int)
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Task
            task T: {}
            task Main: { start T; }
        ''').main.lastExpr.type.assertRoleType(Pure, taskClassName, Void)
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Task
            class A
            task T: readwrite A { return null; }
            task Main: { start T; }
        ''').main.lastExpr.type.assertInstanceOf(RoleType) => [
            role.assertThat(instanceOf(Pure))
            base.assertInstanceOf(GenericClassRef) => [
                clazz.name.assertThat(is(taskClassName.toString))
                typeArg.assertInstanceOf(RoleType) => [
                    role.assertThat(instanceOf(ReadWrite))
                    base.assertInstanceOf(SimpleClassRef) => [ clazz.name.assertThat(is("A"))]
                ]
            ]
        ]
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Task
            task T(i: int): {}
            task Main: { start T(5); }
        ''').assertNoErrors
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Task
            class A
            task T(a: pure A): {}
            task Main: {
                start T(new A);
                start T(new A as readonly A);
                start T(new A as pure A);
                start T(null);
            }
        ''').assertNoErrors
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Task
            class A
            task T(i: int, c: char, a: readwrite A): {}
            task Main: {
                start T(0, 'c', new A);
            }
        ''').assertNoErrors
    }
    
    @Test def testTStartTaskClassNotDefined() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            task T: {}
            task Main: { start T; }
        ''').assertError(START, TSTART, "task class", "not defined")
    }
    
    @Test def testTStartErrorInArg() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Task
            task T(i: int): {}
            task Main: { start T(!5); }
        ''').assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test def testTStartTypeMismatch() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Task
            task T: int {}
            task Main: { start T(5); }
        ''').assertError(START, null, "too many arguments")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Task
            task T(i: int): int {}
            task Main: { start T; }
        ''').assertError(START, null, "too few arguments")
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Task
            task T(i: int): int {}
            task Main: { start T(true); }
        ''').assertError(BOOLEAN_LITERAL, SUBTYPEEXPR, "boolean", "int")
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Task[V] mapped to rolez.lang.Task
            task A(o: readwrite Object): {}
            task Main: {
                start A(new Object as readonly Object);
            }
        ''').assertError(CAST, SUBTYPEEXPR, "readonly rolez.lang.Object", "readwrite rolez.lang.Object")
    }
    
    @Test def testTParenthesized() {
        val program = parse('''
            class rolez.lang.Object mapped to java.lang.Object
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
        program.main.expr(2).type.assertRoleType(ReadWrite, "A")
        program.main.expr(3).type.assertRoleType(Pure, "A")
    }
    
    @Test def testTParenthesizedErrorInExpr() {
        parse('''
            task Main: { (!5); }
        ''').assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test def testTStringLiteral() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.String mapped to java.lang.String
            task Main: { "Hi"; }
        ''').main.lastExpr.type.assertRoleType(ReadWrite, stringClassName)
    }
    
    @Test def testTStringLiteralStringClassNotDefined() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            task Main: { "Hi"; }
        ''').assertError(STRING_LITERAL, TSTRINGLITERAL, "rolez.lang.String class", "not defined")
    }
    
    @Test def testTNullLiteral() {
        parse('''
            task Main: { null; }
        ''').main.lastExpr.type.assertThat(instanceOf(Null))
    }
    
    @Test def testTIntLiteral() {
        parse('''
            task Main: { 5; }
        ''').main.lastExpr.type.assertThat(instanceOf(Int))
    }
    
    @Test def testTDoubleLiteral() {
        parse('''
            task Main: { 5.0; }
        ''').main.lastExpr.type.assertThat(instanceOf(Double))
    }
    
    @Test def testTBooleanLiteral() {
        parse('''
            task Main: { true; }
        ''').main.lastExpr.type.assertThat(instanceOf(Boolean))
    }
    
    @Test def testTCharLiteral() {
        parse('''
            task Main: { 'c'; }
        ''').main.lastExpr.type.assertThat(instanceOf(Char))
    }
    
    @Test def testVParam() {
        var program = parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A
            task Main(i: int, a: readwrite A, o: pure Object): {}
        ''')
        program.main.params.get(0).varType.assertThat(instanceOf(Int))
        program.main.params.get(1).varType.assertRoleType(ReadWrite, "A")
        program.main.params.get(2).varType.assertRoleType(Pure, objectClassName)
    }
    
    @Test def testVLocalVar() {
        var program = parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A
            task Main: {
                var i: int;
                var a: readwrite A;
                var o: pure Object;
            }
        ''')
        program.main.variable(0).varType.assertThat(instanceOf(Int))
        program.main.variable(1).varType.assertRoleType(ReadWrite, "A")
        program.main.variable(2).varType.assertRoleType(Pure, objectClassName)
        
        program = parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A
            task Main: {
                var a: readonly A = new A;
                var o: pure Object = null;
            }
        ''')
        program.main.variable(0).varType.assertRoleType(ReadOnly, "A")
        program.main.variable(1).varType.assertRoleType(Pure, objectClassName)
        
        program = parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A
            task Main: {
                val i = 1;
                val a = new A;
                val n = null;
            }
        ''')
        program.main.variable(0).varType.assertThat(instanceOf(Int))
        program.main.variable(1).varType.assertRoleType(ReadWrite, "A")
        program.main.variable(2).varType.assertThat(instanceOf(Null))
        
        program = parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A
            task Main: {
                var i = 1;
                var a = new A;
                var n = null;
            }
        ''')
        program.main.variable(0).varType.assertThat(instanceOf(Int))
        program.main.variable(1).varType.assertRoleType(ReadWrite, "A")
        program.main.variable(2).varType.assertThat(instanceOf(Null))
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A
            task Main: {
                var i;
            }
        ''').assertError(LOCAL_VAR, VLOCALVAR)
    }
    
    @Test def testWBlock() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
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
            class rolez.lang.Object mapped to java.lang.Object
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
    
    @Test def testWLocalVarDecl() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
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
            class rolez.lang.Object mapped to java.lang.Object
            task Main: {
                val o: readwrite Object = new Object as pure Object;
            }
        ''').assertError(CAST, SUBTYPEEXPR, "pure rolez.lang.Object", "readwrite rolez.lang.Object")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A
            task Main: {
                val o: pure A = new A as pure Object;
            }
        ''').assertError(CAST, SUBTYPEEXPR, "pure rolez.lang.Object", "pure A")
    }
    
    @Test def testWIfStmt() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
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
            class rolez.lang.Object mapped to java.lang.Object
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
    
    @Test def testWWhileLoop() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
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
            class rolez.lang.Object mapped to java.lang.Object
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
            class rolez.lang.Object mapped to java.lang.Object
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
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def pure a: int {
                    return false;
                }
            }
        ''').assertError(BOOLEAN_LITERAL, SUBTYPEEXPR, "boolean", "int")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def pure a: readwrite A {
                    return new A as pure A;
                }
            }
        ''').assertError(CAST, SUBTYPEEXPR, "pure A", "readwrite A")
    }
    
    @Test def testSubtype() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Array[T] mapped to rolez.lang.Array {
                mapped new(length: int)
            }
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
                o = new Array[int](2);
                
                var ia: pure Array[int] = new Array[int](2);
                ia = null;
                var oa: readwrite Array[pure Object] = new Array[pure Object](1);
                oa = null;
            }
        ''').assertNoErrors
        
        // IMPROVE: Test type params, once supported outside mapped classes
    }
    
    @Test def testSubtypePrimitiveMismatch() {
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
    
    @Test def testSubtypeSimpleClassMismatch() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A
            task Main: {
                val a: readwrite A = new Object;
            }
        ''').assertError(NEW, SUBTYPEEXPR, "readwrite rolez.lang.Object", "readwrite A")
    }
    
    @Test def testSubtypeGenericClassMismatch() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Array[T] mapped to rolez.lang.Array {
                mapped new(length: int)
            }
            task Main: {
                val a: pure Array[int] = new Array[boolean](0);
            }
        ''').assertError(NEW, SUBTYPEEXPR, "readwrite rolez.lang.Array[boolean]", "pure rolez.lang.Array[int]")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Array[T] mapped to rolez.lang.Array {
                mapped new(length: int)
            }
            task Main: {
                val a: pure Array[int] = new Array[pure Object](0);
            }
        ''').assertError(NEW, SUBTYPEEXPR, "readwrite rolez.lang.Array[pure rolez.lang.Object]", "pure rolez.lang.Array[int]")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Array[T] mapped to rolez.lang.Array {
                mapped new(length: int)
            }
            task Main: {
                val a: pure Array[pure Object] = new Array[int](0);
            }
        ''').assertError(NEW, SUBTYPEEXPR, "readwrite rolez.lang.Array[int]", "pure rolez.lang.Array[pure rolez.lang.Object]")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Array[T] mapped to rolez.lang.Array {
                mapped new(length: int)
            }
            class A
            task Main: {
                val a: pure Array[pure Object] = new Array[pure A](0);
            }
        ''').assertError(NEW, SUBTYPEEXPR, "readwrite rolez.lang.Array[pure A]", "pure rolez.lang.Array[pure rolez.lang.Object]")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Array[T] mapped to rolez.lang.Array {
                mapped new(length: int)
            }
            class A
            task Main: {
                val a: pure Array[pure A] = new Array[pure Object](0);
            }
        ''').assertError(NEW, SUBTYPEEXPR, "readwrite rolez.lang.Array[pure rolez.lang.Object]", "pure rolez.lang.Array[pure A]")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Array[T] mapped to rolez.lang.Array {
                mapped new(length: int)
            }
            class A
            task Main: {
                val a: pure Array[pure A] = new Array[readwrite A](0);
            }
        ''').assertError(NEW, SUBTYPEEXPR, "readwrite rolez.lang.Array[readwrite A]", "pure rolez.lang.Array[pure A]")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Array[T] mapped to rolez.lang.Array {
                mapped new(length: int)
            }
            class A
            task Main: {
                val a: pure Array[readwrite A] = new Array[pure A](0);
            }
        ''').assertError(NEW, SUBTYPEEXPR, "readwrite rolez.lang.Array[pure A]", "pure rolez.lang.Array[readwrite A]")
    }
    
    @Test def testSubtypeRoleMismatch() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A
            task Main: {
                val a: readwrite A = new A as readonly A;
            }
        ''').assertError(CAST, SUBTYPEEXPR, "readonly A", "readwrite A")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A
            task Main: {
                val a: readwrite A = new A as pure A;
            }
        ''').assertError(CAST, SUBTYPEEXPR, "pure A", "readwrite A")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A
            task Main: {
                val a: readonly A = new A as pure A;
            }
        ''').assertError(CAST, SUBTYPEEXPR, "pure A", "readonly A")
    }
}
					
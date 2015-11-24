package ch.trick17.rolez.lang

import ch.trick17.rolez.lang.rolez.Boolean
import ch.trick17.rolez.lang.rolez.Char
import ch.trick17.rolez.lang.rolez.Double
import ch.trick17.rolez.lang.rolez.Int
import ch.trick17.rolez.lang.rolez.New
import ch.trick17.rolez.lang.rolez.NormalClass
import ch.trick17.rolez.lang.rolez.Null
import ch.trick17.rolez.lang.rolez.Program
import ch.trick17.rolez.lang.rolez.Role
import ch.trick17.rolez.lang.rolez.RoleType
import ch.trick17.rolez.lang.rolez.SuperConstrCall
import ch.trick17.rolez.lang.typesystem.RolezSystem
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
    
    @Test def testTAssignment() {
        val program = parse('''
            mapped class rolez.lang.Object
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
    
    @Test def testTAssignmentErrorInOp() {
        parse("task Main: { !5 = 5; }")
            .assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
        
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
            mapped class rolez.lang.Object
            class A {
                def pure foo: {}
            }
            task Main: {
                new A.foo = 3;
            }
        ''').assertError(MEMBER_ACCESS, AMEMBERACCESS, "assign", "foo")
        
        parse('''
            task Main: {
                val x: int;
                x = 5;
            }
        ''').assertError(VAR_REF, AVARREF, "assign", "value")
        
        parse('''
            mapped class rolez.lang.Object
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
        parse("task Main: { 5 || false; }")
            .assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
        parse("task Main: { true || 5; }")
            .assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test def testTEqualityExpr() {
        parse("task Main: { true == false; }").main.lastExpr.type.assertThat(instanceOf(Boolean))
        parse("task Main: { 5 != 3; }").main.lastExpr.type.assertThat(instanceOf(Boolean))

        parse('''
            mapped class rolez.lang.Object
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
            mapped class rolez.lang.Object
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
            mapped class rolez.lang.Object
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
        
        var program = parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.String
            task Main: { "Hi" + " World"; }
        ''')
        program.main.lastExpr.type.assertThat(
            isRoleType(READWRITE, newClassRef(program.findClass(stringClassName))))
            
        program = parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.String
            task Main: { "" + '5'; }
        ''')
        program.main.lastExpr.type.assertThat(
            isRoleType(READWRITE, newClassRef(program.findClass(stringClassName))))
            
        program = parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.String
            task Main: { null + " "; }
        ''')
        program.main.lastExpr.type.assertThat(
            isRoleType(READWRITE, newClassRef(program.findClass(stringClassName))))
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
            mapped class rolez.lang.Object
            task Main: { new Object + new Object; }
        ''').assertError(ARITHMETIC_BINARY_EXPR, null, "operator", "undefined", "object")
        parse('''
            mapped class rolez.lang.Object
            class A
            class B
            task Main: { new A - new B; }
        ''').assertError(ARITHMETIC_BINARY_EXPR, null, "operator", "undefined", "A", "B")
        
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.String
            task Main: { "Hello" - "World"; }
        ''').assertError(ARITHMETIC_BINARY_EXPR, null, "operator", "undefined", "String")
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.String
            task Main: { "Hello" * new Object; }
        ''').assertError(ARITHMETIC_BINARY_EXPR, null, "operator", "undefined", "String", "Object")
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.String
            task Main: { 5 / "World"; }
        ''').assertError(ARITHMETIC_BINARY_EXPR, null, "operator", "undefined", "int", "String")
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.String
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
    
    @Test def testCast() {
        // Redundant casts
        parse("task Main: { 5 as int; }").main.lastExpr.type.assertThat(instanceOf(Int))
        parse("task Main: { true as boolean; }").main.lastExpr.type.assertThat(instanceOf(Boolean))
        
        var program = parse('''
            mapped class rolez.lang.Object
            task Main: { new Object as readwrite Object; }
        ''')
        program.main.lastExpr.type.assertThat(isRoleType(READWRITE, newClassRef(program.findClass(objectClassName))))
        
        // Upcasts
        program = parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.Array[T] {
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
        program.main.expr(0).type.assertThat(isRoleType(READWRITE, newClassRef(program.findClass(objectClassName))))
        program.main.expr(1).type.assertThat(isRoleType(READONLY,  newClassRef(program.findClass("A"))))
        program.main.expr(2).type.assertThat(isRoleType(PURE,      newClassRef(program.findClass("A"))))
        program.main.expr(3).type.assertThat(isRoleType(READWRITE, newClassRef(program.findClass("A"))))
        program.main.expr(4).type.assertThat(isRoleType(READONLY,  newClassRef(program.findClass("A"))))
        program.main.expr(5).type.assertThat(isRoleType(READONLY,
                newClassRef(program.findNormalClass(arrayClassName), newIntType)))
        program.main.expr(6).type.assertThat(isRoleType(READONLY,
            newClassRef(program.findNormalClass(arrayClassName), newRoleType(PURE, newClassRef(program.findClass("A"))))))
        
        // Downcasts
        program = parse('''
            mapped class rolez.lang.Object
            class A
            task Main: { new Object as readwrite A; }
        ''')
        program.main.lastExpr.type.assertThat(isRoleType(READWRITE, newClassRef(program.findClass("A"))))
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
            mapped class rolez.lang.Object
            task Main: { 5 as readwrite Object; }
        ''').assertError(CAST, null, "cast", "readwrite rolez.lang.Object", "int")
        parse('''
            mapped class rolez.lang.Object
            task Main: { new Object as int; }
        ''').assertError(CAST, null, "cast", "readwrite rolez.lang.Object", "int")
        parse('''
            mapped class rolez.lang.Object
            class A
            task Main: { new A as readonly A as readwrite A; }
        ''').assertError(CAST, null, "cast", "readwrite A", "readonly A")
        
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.Array[T]
            task Main: { new Array[boolean] as readwrite Array[int]; }
        ''').assertError(CAST, null, "cast", "readwrite rolez.lang.Array[boolean]", "readwrite rolez.lang.Array[int]")
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.Array[T]
            class A
            task Main: { new Array[pure Object] as readwrite Array[pure A]; }
        ''').assertError(CAST, null, "cast", "readwrite rolez.lang.Array[pure rolez.lang.Object]", "readwrite rolez.lang.Array[pure A]")
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.Array[T]
            class A
            task Main: { new Array[readwrite A] as readwrite Array[pure A]; }
        ''').assertError(CAST, null, "cast", "readwrite rolez.lang.Array[readwrite A]", "readwrite rolez.lang.Array[pure A]")
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.Array[T]
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
            mapped class rolez.lang.Object
            task Main: { -new Object; }
        ''').assertError(NEW, SUBTYPEEXPR, "Object", "int")
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.String
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
        parse("task Main: { val a: boolean = false; !a; }").main.lastExpr.type
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
            mapped class rolez.lang.Object
            task Main: { !new Object; }
        ''').assertError(NEW, SUBTYPEEXPR, "Object", "boolean")
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.String
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
    
    @Test def testTMemberAccessIllegalTarget() {
        parse('''task Main: { 5.5.a; }''')
            .assertError(DOUBLE_LITERAL, null, "Illegal", "target", "access")
    }
    
    @Test def testTMemberAccessField() {
        parse('''
            mapped class rolez.lang.Object
            class A {
                var x: int
            }
            task Main: { new A.x; }
        ''').main.lastExpr.type.assertThat(instanceOf(Int))
        parse('''
            mapped class rolez.lang.Object
            class A {
                var x: int
            }
            task Main: {
                val a: readonly A = new A;
                a.x;
            }
        ''').main.lastExpr.type.assertThat(instanceOf(Int))
        
        val program = parse('''
            mapped class rolez.lang.Object
            class A {
                var a: readwrite A
            }
            task Main: {
                val a: readwrite A = new A;
                a.a;
            }
        ''')
        program.main.lastExpr.type
            .assertThat(isRoleType(READWRITE, newClassRef(program.findClass("A"))))
        
        parse('''
            mapped class rolez.lang.Object
            class A {
                var a: readwrite A
            }
            task Main: {
                val a: readonly A = new A;
                a.a;
            }
        ''').main.lastExpr.type.asRoleType.role.assertThat(is(READONLY))
        parse('''
            mapped class rolez.lang.Object
            class A {
                var a: readonly A
            }
            task Main: {
                val a: readwrite A = new A;
                a.a;
            }
        ''').main.lastExpr.type.asRoleType.role.assertThat(is(READONLY))
        parse('''
            mapped class rolez.lang.Object
            class A {
                var a: pure A
            }
            task Main: {
                val a: readwrite A = new A;
                a.a;
            }
        ''').main.lastExpr.type.asRoleType.role.assertThat(is(PURE))
        parse('''
            mapped class rolez.lang.Object
            class A {
                var a: pure A
            }
            task Main: {
                val a: readonly A = new A;
                a.a;
            }
        ''').main.lastExpr.type.asRoleType.role.assertThat(is(PURE))
    }
    
    @Test def testTMemberAccessFieldRoleMismatch() {
        parse('''
            mapped class rolez.lang.Object
            class A { var x: int }
            task Main: {
                val a: pure A = new A;
                a.x;
            }
        ''').assertError(VAR_REF, null,
                "Role", "mismatch", "field", "pure")
    }
    
    @Test def testTMemberAccessMethod() {
        for(expected : Role.values) {
            for(actual : Role.values.filter[system.subroleSucceeded(it, expected)]) {
                parse('''
                    mapped class rolez.lang.Object
                    class A {
                        def «expected» x: int { return 42; }
                    }
                    task Main: {
                        val a: «actual» A = new A;
                        a.x;
                    }
                ''').main.lastExpr.type.assertThat(instanceOf(Int))
            }
        }
        
        var program = parse('''
            mapped class rolez.lang.Object
            class A {
                def readwrite a: readonly A { return null; }
            }
            task Main: { new A.a; }
        ''')
        program.main.lastExpr.type
            .assertThat(isRoleType(READONLY, newClassRef(program.findClass("A"))))
        
        val lib = newResourceSet.with('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.Array[T] {
                mapped new(length: int)
                mapped def readonly  get(i: int): T
                mapped def readwrite set(i: int, o: T):
            }
        ''')
        parse('''
            task Main: {
                val a: readwrite Array[int] = new Array[int](1);
                a.set(0, 42);
                a.get(0);
            }
        ''', lib).main.lastExpr.type.assertThat(instanceOf(Int))
        program = parse('''
            class A
            task Main: {
                val a: readwrite Array[readwrite A] = new Array[readwrite A](1);
                a.set(0, new A);
                a.get(0);
            }
        ''', lib)
        program.main.lastExpr.type
            .assertThat(isRoleType(READWRITE, newClassRef(program.findClass("A"))))
        program = parse('''
            class A
            task Main: {
                val a: readwrite Array[pure A] = new Array[pure A](1);
                a.set(0, new A);
                a.get(0);
            }
        ''', lib)
        program.main.lastExpr.type
            .assertThat(isRoleType(READWRITE, newClassRef(program.findClass("A"))))
        
        parse('''
            mapped class rolez.lang.Object
            class A
            class B {
                def readwrite foo(a: readonly A, b: readwrite B,
                        c: readwrite C, d: int): {}
            }
            class C extends B
            task Main: { new C.foo(new A, new C, null, 5); }
        ''').assertNoErrors
        
        parse('''
            mapped class rolez.lang.Object
            class A {
                def readwrite foo: {}
            }
            class B extends A {
                override readwrite foo: {}
            }
            task Main: { new B.foo; }
        ''').assertNoErrors
    }
    
    @Test def testTMemberAccessMethodErrorInArg() {
        parse('''
            mapped class rolez.lang.Object
            class A {
                def pure foo(i: int): {}
                def pure bar: { this.foo(!5); }
            }
        ''').assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
        
        // Apparently, when the method is defined in another resource,
        // linking somehow works differently...
        val set = newResourceSet.with("class A { def pure foo(i: int): {} }")
        parse('''
            mapped class rolez.lang.Object
            class B {
                def pure bar: { new A.foo(!5); }
            }
        ''', set).assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test def testTMemberAccessMethodRoleMismatch() {
        for(expected : Role.values) {
            for(actual : Role.values.filter[!system.subroleSucceeded(it, expected)]) {
                parse('''
                    mapped class rolez.lang.Object
                    class A {
                        def «expected» x: int { return 42; }
                    }
                    task Main: {
                        val a: «actual» A = new A;
                        a.x;
                    }
                ''').assertError(MEMBER_ACCESS, null,
                        "Role", "mismatch", "method", actual.toString)
            }
        }
        
        parse('''
            mapped class rolez.lang.Object
            class A {
                def readwrite foo: {}
                def readonly  bar: { this.foo; }
            }
        ''').assertError(MEMBER_ACCESS, null, "Role", "mismatch", "method", "readonly")
    }
    
    @Test def testTMemberAccessMethodTypeMismatch() {
        parse('''
            mapped class rolez.lang.Object
            class A { def readwrite foo: {} }
            task Main: { new A.foo(5); }
        ''').assertError(MEMBER_ACCESS, LINKING_DIAGNOSTIC, "foo")
        parse('''
            mapped class rolez.lang.Object
            class A { def readwrite foo(c: char): {} }
            task Main: { new A.foo(5, false); }
        ''').assertError(MEMBER_ACCESS, LINKING_DIAGNOSTIC, "foo")
        parse('''
            mapped class rolez.lang.Object
            class A { def readwrite foo(i: int): {} }
            task Main: { new A.foo(); }
        ''').assertError(MEMBER_ACCESS, LINKING_DIAGNOSTIC, "foo")
        parse('''
            mapped class rolez.lang.Object
            class A { def readwrite foo(i: int, a: readwrite A): {} }
            task Main: { new A.foo(false); }
        ''').assertError(MEMBER_ACCESS, LINKING_DIAGNOSTIC, "foo")
        
        parse('''
            mapped class rolez.lang.Object
            class A { def readwrite foo(i: int): {} }
            task Main: { new A.foo(false); }
        ''').assertError(MEMBER_ACCESS, LINKING_DIAGNOSTIC, "foo")
        parse('''
            mapped class rolez.lang.Object
            class A { def readwrite foo(a: readwrite A): {} }
            task Main: { new A.foo(new Object); }
        ''').assertError(MEMBER_ACCESS, LINKING_DIAGNOSTIC, "foo")
        parse('''
            mapped class rolez.lang.Object
            class A { def readwrite foo(a: readwrite A): {} }
            task Main: { new A.foo(new A as readonly A); }
        ''').assertError(MEMBER_ACCESS, LINKING_DIAGNOSTIC, "foo")
        
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.Array[T] {
                mapped new(i: int)
                mapped def readwrite set(i: int, o: T):
            }
            task Main: {
                new Array[int](1).set(0, true);
            }
        ''').assertError(MEMBER_ACCESS, LINKING_DIAGNOSTIC, "set")
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.Array[T] {
                mapped new(i: int)
                mapped def readwrite set(i: int, o: T):
            }
            class A
            class B
            task Main: {
                new Array[pure A](1).set(0, new B);
            }
        ''').assertError(MEMBER_ACCESS, LINKING_DIAGNOSTIC, "set")
    }
    
    @Test def testTMemberAccessOverloading() {
        var program = parse('''
            mapped class rolez.lang.Object
            class A {
                def readwrite foo: int { return 0; }
                def readwrite foo(a: boolean): boolean { return false; }
            }
            task Main: {
                new A.foo;
                new A.foo(true);
            }
        ''')
        program.main.expr(0).type.assertThat(instanceOf(Int))
        program.main.expr(1).type.assertThat(instanceOf(Boolean))
        
        program = parse('''
            mapped class rolez.lang.Object
            class A {
                var foo: int
                def readwrite foo(a: boolean): boolean { return false; }
            }
            task Main: {
                new A.foo;
                new A.foo(true);
            }
        ''')
        program.main.expr(0).type.assertThat(instanceOf(Int))
        program.main.expr(1).type.assertThat(instanceOf(Boolean))
        
        program = parse('''
            mapped class rolez.lang.Object
            class A {
                def readwrite foo(a: int): int { return 0; }
                def readwrite foo(a: boolean): boolean { return false; }
            }
            task Main: {
                new A.foo(4);
                new A.foo(true);
            }
        ''')
        program.main.expr(0).type.assertThat(instanceOf(Int))
        program.main.expr(1).type.assertThat(instanceOf(Boolean))
        
        program = parse('''
            mapped class rolez.lang.Object
            class A {
                def readwrite foo(a: readwrite A): int { return 0; }
                def readwrite foo(a: readonly  A): boolean { return false; }
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
            mapped class rolez.lang.Object
            class A {
                def readwrite foo(a: readonly  A): boolean { return false; }
                def readwrite foo(a: readwrite A): int { return 0; }
            }
            task Main: {
                new A.foo(new A);
                new A.foo(new A as readonly A);
            }
        ''')
        program.main.expr(0).type.assertThat(instanceOf(Int))
        program.main.expr(1).type.assertThat(instanceOf(Boolean))
        
        program = parse('''
            mapped class rolez.lang.Object
            class A {
                def readwrite foo(a: readonly  A, b: readonly  A): boolean { return false; }
                def readwrite foo(a: readwrite A, b: readwrite A): int { return 0; }
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
            mapped class rolez.lang.Object
            class A {
                def readwrite foo(a: readwrite A, b: readwrite A): int { return 0; }
                def readwrite foo(a: readonly  A, b: readonly  A): boolean { return false; }
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
        
        // IMPROVE: test generic methods, once supported outside of array class
    }
    
    @Test def testTMemberAccessMethodAmbiguous() {
        parse('''
            mapped class rolez.lang.Object
            class A {
                def readwrite foo(a: readonly  A, b: readwrite A): {}
                def readwrite foo(a: readwrite A, b: readonly  A): {}
            }
            task Main: {
                new A.foo(new A, new A);
            }
        ''').assertError(MEMBER_ACCESS, AMBIGUOUS_CALL)
        
        parse('''
            mapped class rolez.lang.Object
            class A {
                def readwrite foo(a: readwrite Object, b: readwrite A): {}
                def readwrite foo(a: readwrite A, b: readwrite Object): {}
            }
            task Main: {
                new A.foo(new A, new A);
            }
        ''').assertError(MEMBER_ACCESS, AMBIGUOUS_CALL)
        
        // IMPROVE: test generic methods, once supported outside of array class
    }
    
    @Test def testTThis() {
        for(expected : Role.values) {
            val program = parse('''
                mapped class rolez.lang.Object
                class A {
                    def «expected» foo: { this; }
                }
            ''')
            program.findClass("A").findMethod("foo").lastExpr.type
                .assertThat(isRoleType(expected, newClassRef(program.findClass("A"))))
        }
        
        val program = parse('''
            mapped class rolez.lang.Object
            class A {
                new { this; }
            }
        ''')
        program.findNormalClass("A").constrs.head.lastExpr.type
            .assertThat(isRoleType(READWRITE, newClassRef(program.findClass("A"))))
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
                val i: int = 5;
                i;
            }
        ''').main.lastExpr.type.assertThat(instanceOf(Int))
        parse('''
            mapped class rolez.lang.Object
            class A {
                def pure foo(i: int): {
                    i;
                }
            }
            task Main: {}
        ''').findClass("A").methods.head.lastExpr.type.assertThat(instanceOf(Int))
        
        var program = parse('''
            mapped class rolez.lang.Object
            class A
            task Main: {
                val a: readonly A = new A;
                a;
            }
        ''')
        program.main.lastExpr.type
            .assertThat(isRoleType(READONLY, newClassRef(program.findClass("A"))))
    }
    
    @Test def testTNew() {
        var program = parse('''
            mapped class rolez.lang.Object
            class A
            task Main: { new A; }
        ''')
        program.main.lastExpr.type
            .assertThat(isRoleType(READWRITE, newClassRef(program.findClass("A"))))
        
        program = parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.Array[T] {
                mapped new(length: int)
            }
            task Main: { new Array[int](100); }
        ''')
        program.main.lastExpr.type
            .assertThat(isRoleType(READWRITE, newClassRef(program.findNormalClass(arrayClassName), newIntType)))
        
        program = parse('''
            mapped class rolez.lang.Object
            class A
            mapped class rolez.lang.Array[T] {
                mapped new(length: int)
            }
            task Main: { new Array[readonly A](10); }
        ''')
        program.main.lastExpr.type
            .assertThat(isRoleType(READWRITE, newClassRef(program.findNormalClass(arrayClassName),
                newRoleType(READONLY, newClassRef(program.findClass("A"))))))
        
        program = parse('''
            mapped class rolez.lang.Object
            class A
            mapped class rolez.lang.Array[T] {
                mapped new(length: int)
            }
            task Main: { new Array[pure Array[readwrite A]](1000); }
        ''')
        val array = program.findNormalClass(arrayClassName)
        program.main.lastExpr.type
            .assertThat(isRoleType(READWRITE, newClassRef(array,
                newRoleType(PURE, newClassRef(array,
                    newRoleType(READWRITE, newClassRef(program.findClass("A"))))))))
        
        // IMPROVE: test generic constructors, once  supported outside of the array class
    }
    
    @Test def testTNewErrorInArg() {
        parse('''
            mapped class rolez.lang.Object
            class A {
                new(i: int) {}
                def pure foo(i: int): { new A(!5); }
            }
        ''').assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test def testTNewTypeMismatch() {
        parse('''
            mapped class rolez.lang.Object
            class A
            task Main: { new A(5); }
        ''').assertError(NEW, LINKING_DIAGNOSTIC)
        parse('''
            mapped class rolez.lang.Object
            class A { new {} }
            task Main: { new A(5); }
        ''').assertError(NEW, LINKING_DIAGNOSTIC)
        parse('''
            mapped class rolez.lang.Object
            class A { new(c: char) {} }
            task Main: { new A(5, false); }
        ''').assertError(NEW, LINKING_DIAGNOSTIC)
        parse('''
            mapped class rolez.lang.Object
            class A { new(i: int) {} }
            task Main: { new A; }
        ''').assertError(NEW, LINKING_DIAGNOSTIC)
        
        parse('''
            mapped class rolez.lang.Object
            class A { new(i: int) {} }
            task Main: { new A(false); }
        ''').assertError(NEW, LINKING_DIAGNOSTIC)
        parse('''
            mapped class rolez.lang.Object
            class A { new(a: readwrite A) {} }
            task Main: { new A(new Object); }
        ''').assertError(NEW, LINKING_DIAGNOSTIC)
        
        // IMPROVE: test generic constructors, once  supported outside of the array class
    }
    
    @Test def testTNewOverloading() {
        var program = parse('''
            mapped class rolez.lang.Object
            class A {
                new(a: int) {}
                new(a: boolean) {}
            }
            task Main: {
                new A(4);
                new A(true);
            }
        ''')
        (program.main.expr(0) as New).target.params.head.type.assertThat(instanceOf(Int))
        (program.main.expr(1) as New).target.params.head.type.assertThat(instanceOf(Boolean))
        
        program = parse('''
            mapped class rolez.lang.Object
            class A
            class B {
                new(a: readwrite A) {}
                new(a: readonly  A) {}
            }
            task Main: {
                new B(new A);
                new B(new A as readonly A);
            }
        ''')
        ((program.main.expr(0) as New).target.params.head.type as RoleType).role.assertThat(is(READWRITE))
        ((program.main.expr(1) as New).target.params.head.type as RoleType).role.assertThat(is(READONLY))
        
        // (Switch order of declaration to rule out accidental selection of the correct one)
        program = parse('''
            mapped class rolez.lang.Object
            class A
            class B {
                new(a: readonly  A) {}
                new(a: readwrite A) {}
            }
            task Main: {
                new B(new A);
                new B(new A as readonly A);
            }
        ''')
        ((program.main.expr(0) as New).target.params.head.type as RoleType).role.assertThat(is(READWRITE))
        ((program.main.expr(1) as New).target.params.head.type as RoleType).role.assertThat(is(READONLY))
        
        // IMPROVE: test generic constructors, once  supported outside of the array class
    }
    
    @Test def testTNewAmbiguous() {
        parse('''
            mapped class rolez.lang.Object
            class A
            class B {
                new(a: readonly  A, b: readwrite A) {}
                new(a: readwrite A, b: readonly  A) {}
            }
            task Main: {
                new B(new A, new A);
            }
        ''').assertError(NEW, AMBIGUOUS_CALL)
        
        parse('''
            mapped class rolez.lang.Object
            class A
            class B {
                new(a: readwrite Object, b: readwrite A) {}
                new(a: readwrite A, b: readwrite Object) {}
            }
            task Main: {
                new B(new A, new A);
            }
        ''').assertError(NEW, AMBIGUOUS_CALL)
        
        // IMPROVE: test generic constructors, once  supported outside of the array class
    }
    
    @Test def testTThe() {
        val program = parse('''
            mapped class rolez.lang.Object
            mapped object rolez.lang.System
            task Main: {
                the System;
            }
        ''')
        program.main.lastExpr.type.assertThat(isRoleType(READONLY, newClassRef(program.findClass(systemClassName))))
    }
    
    @Test def testTStart() {
        var program = parse('''
            mapped class rolez.lang.Object
            class rolez.lang.Task
            task T: int { return 0; }
            task Main: { start T; }
        ''')
        program.main.lastExpr.type
            .assertThat(isRoleType(PURE, newClassRef(program.findNormalClass(taskClassName), newIntType)))
        
        program = parse('''
            mapped class rolez.lang.Object
            class rolez.lang.Task
            task T: {}
            task Main: { start T; }
        ''')
        program.main.lastExpr.type
            .assertThat(isRoleType(PURE, newClassRef(program.findNormalClass(taskClassName), newVoidType)))
        
        program = parse('''
            mapped class rolez.lang.Object
            class rolez.lang.Task
            class A
            task T: readwrite A { return null; }
            task Main: { start T; }
        ''')
        program.main.lastExpr.type
            .assertThat(isRoleType(PURE, newClassRef(program.findNormalClass(taskClassName),
                newRoleType(READWRITE, newClassRef(program.findClass("A"))))))
        
        parse('''
            mapped class rolez.lang.Object
            class rolez.lang.Task
            task T(i: int): {}
            task Main: { start T(5); }
        ''').assertNoErrors
        parse('''
            mapped class rolez.lang.Object
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
            mapped class rolez.lang.Object
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
            mapped class rolez.lang.Object
            task T: {}
            task Main: { start T; }
        ''').assertError(START, TSTART, "task class", "not defined")
    }
    
    @Test def testTStartErrorInArg() {
        parse('''
            mapped class rolez.lang.Object
            class rolez.lang.Task
            task T(i: int): {}
            task Main: { start T(!5); }
        ''').assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test def testTStartTypeMismatch() {
        parse('''
            mapped class rolez.lang.Object
            class rolez.lang.Task
            task T: int {}
            task Main: { start T(5); }
        ''').assertError(START, null, "too many arguments")
        parse('''
            mapped class rolez.lang.Object
            class rolez.lang.Task
            task T(i: int): int {}
            task Main: { start T; }
        ''').assertError(START, null, "too few arguments")
        
        parse('''
            mapped class rolez.lang.Object
            class rolez.lang.Task
            task T(i: int): int {}
            task Main: { start T(true); }
        ''').assertError(BOOLEAN_LITERAL, SUBTYPEEXPR, "boolean", "int")
    }
    
    @Test def testTParenthesized() {
        val program = parse('''
            mapped class rolez.lang.Object
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
    
    @Test def testTParenthesizedErrorInExpr() {
        parse('''
            task Main: { (!5); }
        ''').assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test def testTStringLiteral() {
        val program = parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.String
            task Main: { "Hi"; }
        ''')
        program.main.lastExpr.type.assertThat(
            isRoleType(READWRITE, newClassRef(program.findClass(stringClassName))))
    }
    
    @Test def testTStringLiteralStringClassNotDefined() {
        parse('''
            mapped class rolez.lang.Object
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
    
    @Test def testWBlock() {
        parse('''
            mapped class rolez.lang.Object
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
            mapped class rolez.lang.Object
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
            mapped class rolez.lang.Object
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
            mapped class rolez.lang.Object
            task Main: {
                val o: readwrite Object = new Object as pure Object;
            }
        ''').assertError(CAST, SUBTYPEEXPR, "pure rolez.lang.Object", "readwrite rolez.lang.Object")
        parse('''
            mapped class rolez.lang.Object
            class A
            task Main: {
                val o: pure A = new A as pure Object;
            }
        ''').assertError(CAST, SUBTYPEEXPR, "pure rolez.lang.Object", "pure A")
    }
    
    @Test def testWIfStmt() {
        parse('''
            mapped class rolez.lang.Object
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
            mapped class rolez.lang.Object
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
            mapped class rolez.lang.Object
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
            mapped class rolez.lang.Object
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
    
    @Test def testWSuperConstrCall() {
        parse('''
            mapped class rolez.lang.Object
            class A
            class B extends A {
                new {}
            }
            class C extends B
            class D extends C {
                new(i: int) { 2; }
            }
            class E extends D {
                new {
                    super(0);
                    5;
                }
                new(a: readonly A, b: pure B) { super(1); }
            }
            class F extends E {
                new(a: readwrite A) { super(a, new B); }
            }
            class G {
                new { super; }
            }
        ''').assertNoErrors
    }
    
    @Test def testWSuperConstrCallErrorInArg() {
        parse('''
            mapped class rolez.lang.Object
            class A {
                new(i: int) {}
            }
            class B extends A {
                new { super(!5); }
            }
        ''').assertError(INT_LITERAL, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test def testWSuperConstrCallTypeMismatch() {
        parse('''
            mapped class rolez.lang.Object
            class A
            class B extends A {
                new { super(5); }
            }
        ''').assertError(SUPER_CONSTR_CALL, LINKING_DIAGNOSTIC)
        parse('''
            mapped class rolez.lang.Object
            class A {
                new { super(5); }
            }
        ''').assertError(SUPER_CONSTR_CALL, LINKING_DIAGNOSTIC)
        parse('''
            mapped class rolez.lang.Object
            class A {
                new {}
            }
            class B extends A {
                new { super(5); }
            }
        ''').assertError(SUPER_CONSTR_CALL, LINKING_DIAGNOSTIC)
        parse('''
            mapped class rolez.lang.Object
            class A {
                new(c: char) {}
            }
            class B extends A {
                new { super(5, false); }
            }
        ''').assertError(SUPER_CONSTR_CALL, LINKING_DIAGNOSTIC)
        parse('''
            mapped class rolez.lang.Object
            class A {
                new(i: int) {}
            }
            class B extends A { 
                new { super(); }
            }
        ''').assertError(SUPER_CONSTR_CALL, LINKING_DIAGNOSTIC)
        
        parse('''
            mapped class rolez.lang.Object
            class A {
                new(i: int) {}
            }
            class B extends A {
                new { super(false); }
            }
        ''').assertError(SUPER_CONSTR_CALL, LINKING_DIAGNOSTIC)
        parse('''
            mapped class rolez.lang.Object
            class A {
                new(a: readwrite A) {}
            }
            class B extends A {
                new { super(new Object); }
            }
        ''').assertError(SUPER_CONSTR_CALL, LINKING_DIAGNOSTIC)
    }
    
    @Test def testWSuperConstrCallOverloading() {
        val classB = (parse('''
            mapped class rolez.lang.Object
            class A {
                new(a: int) {}
                new(a: boolean) {}
            }
            class B extends A {
                new             { super(4); }
                new(b: boolean) { super(b); }
            }
        ''').classes.findFirst[name == "B"] as NormalClass)
        (classB.constrs.findFirst[params.size == 0].body.stmts.head as SuperConstrCall)
            .target.params.head.type.assertThat(instanceOf(Int))
        (classB.constrs.findFirst[params.size == 1].body.stmts.head as SuperConstrCall)
            .target.params.head.type.assertThat(instanceOf(Boolean))
        
        var classC = (parse('''
            mapped class rolez.lang.Object
            class A
            class B {
                new(a: readwrite A) {}
                new(a: readonly  A) {}
            }
            class C extends B {
                new         { super(new A); }
                new(i: int) { super(new A as readonly A); }
            }
        ''').classes.findFirst[name == "C"] as NormalClass)
        ((classC.constrs.findFirst[params.size == 0].body.stmts.head as SuperConstrCall)
            .target.params.head.type as RoleType).role.assertThat(is(READWRITE))
        ((classC.constrs.findFirst[params.size == 1].body.stmts.head as SuperConstrCall)
            .target.params.head.type as RoleType).role.assertThat(is(READONLY))
        
        // (Switch order of declaration to rule out accidental selection of the correct one)
        classC = (parse('''
            mapped class rolez.lang.Object
            class A
            class B {
                new(a: readonly  A) {}
                new(a: readwrite A) {}
            }
            class C extends B {
                new         { super(new A); }
                new(i: int) { super(new A as readonly A); }
            }
        ''').classes.findFirst[name == "C"] as NormalClass)
        ((classC.constrs.findFirst[params.size == 0].body.stmts.head as SuperConstrCall)
            .target.params.head.type as RoleType).role.assertThat(is(READWRITE))
        ((classC.constrs.findFirst[params.size == 1].body.stmts.head as SuperConstrCall)
            .target.params.head.type as RoleType).role.assertThat(is(READONLY))
    }
    
    @Test def testWSuperConstrCallAmbiguous() {
        parse('''
            mapped class rolez.lang.Object
            class A
            class B {
                new(a: readonly  A, b: readwrite A) {}
                new(a: readwrite A, b: readonly  A) {}
            }
            class C extends B {
                new { super(new A, new A); }
            }
        ''').assertError(SUPER_CONSTR_CALL, AMBIGUOUS_CALL)
        
        parse('''
            mapped class rolez.lang.Object
            class A
            class B {
                new(a: readwrite Object, b: readwrite A) {}
                new(a: readwrite A, b: readwrite Object) {}
            }
            class C extends B {
                new { super(new A, new A); }
            }
        ''').assertError(SUPER_CONSTR_CALL, AMBIGUOUS_CALL)
    }
    
    @Test def testWReturn() {
        parse('''
            mapped class rolez.lang.Object
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
            mapped class rolez.lang.Object
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
            mapped class rolez.lang.Object
            class A {
                def pure a: int {
                    return false;
                }
            }
        ''').assertError(BOOLEAN_LITERAL, SUBTYPEEXPR, "boolean", "int")
        parse('''
            mapped class rolez.lang.Object
            class A {
                def pure a: readwrite A {
                    return new A as pure A;
                }
            }
        ''').assertError(CAST, SUBTYPEEXPR, "pure A", "readwrite A")
    }
    
    @Test def testSubtype() {
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.Array[T] {
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
        
        // IMPROVE: Test type params, once they're supported outside the array class
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
            mapped class rolez.lang.Object
            class A
            task Main: {
                val a: readwrite A = new Object;
            }
        ''').assertError(NEW, SUBTYPEEXPR, "readwrite rolez.lang.Object", "readwrite A")
    }
    
    @Test def testSubtypeGenericClassMismatch() {
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.Array[T]
            task Main: {
                val a: pure Array[int] = new Array[boolean];
            }
        ''').assertError(NEW, SUBTYPEEXPR, "readwrite rolez.lang.Array[boolean]", "pure rolez.lang.Array[int]")
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.Array[T]
            task Main: {
                val a: pure Array[int] = new Array[pure Object];
            }
        ''').assertError(NEW, SUBTYPEEXPR, "readwrite rolez.lang.Array[pure rolez.lang.Object]", "pure rolez.lang.Array[int]")
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.Array[T]
            task Main: {
                val a: pure Array[pure Object] = new Array[int];
            }
        ''').assertError(NEW, SUBTYPEEXPR, "readwrite rolez.lang.Array[int]", "pure rolez.lang.Array[pure rolez.lang.Object]")
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.Array[T]
            class A
            task Main: {
                val a: pure Array[pure Object] = new Array[pure A];
            }
        ''').assertError(NEW, SUBTYPEEXPR, "readwrite rolez.lang.Array[pure A]", "pure rolez.lang.Array[pure rolez.lang.Object]")
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.Array[T]
            class A
            task Main: {
                val a: pure Array[pure A] = new Array[pure Object];
            }
        ''').assertError(NEW, SUBTYPEEXPR, "readwrite rolez.lang.Array[pure rolez.lang.Object]", "pure rolez.lang.Array[pure A]")
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.Array[T]
            class A
            task Main: {
                val a: pure Array[pure A] = new Array[readwrite A];
            }
        ''').assertError(NEW, SUBTYPEEXPR, "readwrite rolez.lang.Array[readwrite A]", "pure rolez.lang.Array[pure A]")
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.Array[T]
            class A
            task Main: {
                val a: pure Array[readwrite A] = new Array[pure A];
            }
        ''').assertError(NEW, SUBTYPEEXPR, "readwrite rolez.lang.Array[pure A]", "pure rolez.lang.Array[readwrite A]")
    }
    
    @Test def testSubtypeRoleMismatch() {
        parse('''
            mapped class rolez.lang.Object
            class A
            task Main: {
                val a: readwrite A = new A as readonly A;
            }
        ''').assertError(CAST, SUBTYPEEXPR, "readonly A", "readwrite A")
        parse('''
            mapped class rolez.lang.Object
            class A
            task Main: {
                val a: readwrite A = new A as pure A;
            }
        ''').assertError(CAST, SUBTYPEEXPR, "pure A", "readwrite A")
        parse('''
            mapped class rolez.lang.Object
            class A
            task Main: {
                val a: readonly A = new A as pure A;
            }
        ''').assertError(CAST, SUBTYPEEXPR, "pure A", "readonly A")
    }
}
package ch.trick17.peppl.lang

import ch.trick17.peppl.lang.peppl.Boolean
import ch.trick17.peppl.lang.peppl.Class
import ch.trick17.peppl.lang.peppl.Int
import ch.trick17.peppl.lang.peppl.PepplPackage
import ch.trick17.peppl.lang.peppl.Program
import ch.trick17.peppl.lang.peppl.Role
import ch.trick17.peppl.lang.peppl.RoleType
import ch.trick17.peppl.lang.peppl.Type
import ch.trick17.peppl.lang.peppl.WithBlock
import ch.trick17.peppl.lang.typesystem.PepplSystem
import ch.trick17.peppl.lang.typesystem.PepplTypeUtils
import javax.inject.Inject
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.eclipse.xtext.junit4.validation.ValidationTestHelper
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith

import static ch.trick17.peppl.lang.peppl.Role.*
import static ch.trick17.peppl.lang.typesystem.PepplSystem.*
import static ch.trick17.peppl.lang.validation.PepplValidator.*
import static org.eclipse.xtext.diagnostics.Diagnostic.*
import static org.hamcrest.Matchers.*

import static extension org.hamcrest.MatcherAssert.assertThat
import ch.trick17.peppl.lang.peppl.Expr
import ch.trick17.peppl.lang.peppl.ExprStmt

@RunWith(XtextRunner)
@InjectWith(PepplInjectorProvider)
class PepplSystemTest {
    
    val PepplPackage peppl = PepplPackage.eINSTANCE
    
    @Inject extension ParseHelper<Program>
    @Inject extension ValidationTestHelper
    @Inject extension PepplSystem system
    @Inject extension PepplTypeUtils
    
    @Test
    def void testTAssignment() {
        val program = parse('''
            class Object
            class A
            class B extends A 
            main {
                var a: readwrite A;
                a = new B;
            }
        ''')
        program.main.lastExpr.type
            .assertThat(roleType(READWRITE,program.findClass("A")))
    }
    
    @Test
    def void testTAssignmentErrorInOp() {
        parse("main { !5 = 5; }")
            .assertError(peppl.intLiteral, SUBTYPEEXPR, "int", "boolean")
        
        parse('''
            main {
                var x: int;
                x = !5;
            }
        ''').assertError(peppl.intLiteral, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test
    def void testTAssignmentNotAssignable() {
        parse('''
            main {
                val x: int;
                x = 5;
            }
        ''').assertError(peppl.varRef, AVARREF, "assign", "value")
    }
    
    @Test
    def void testTAssignmentTypeMismatch() {
        parse('''
            main {
                var x: int;
                x = true;
            }
        ''').assertError(peppl.booleanLiteral, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test
    def void testTBooleanExpr() {
        parse("main { true || false; }").main.lastExpr.type.assertThat(instanceOf(Boolean))
        parse("main { true && false; }").main.lastExpr.type.assertThat(instanceOf(Boolean))
    }
    
    @Test
    def void testTBooleanExprErrorInOp() {
        parse("main { !5 || false; }")
            .assertError(peppl.intLiteral, SUBTYPEEXPR, "int", "boolean")
        parse("main { true || !5; }")
            .assertError(peppl.intLiteral, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test
    def void testTBooleanExprTypeMismatch() {
        parse("main { 5 || false; }")
            .assertError(peppl.intLiteral, SUBTYPEEXPR, "int", "boolean")
        parse("main { true || 5; }")
            .assertError(peppl.intLiteral, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test
    def void testTEqualityExpr() {
        parse("main { true == false; }").main.lastExpr.type.assertThat(instanceOf(Boolean))
        parse("main { 5 != 3; }").main.lastExpr.type.assertThat(instanceOf(Boolean))

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
    def void testTEqualityExprErrorInOp() {
        parse("main { !5 == false; }")
            .assertError(peppl.intLiteral, SUBTYPEEXPR, "int", "boolean")
        parse("main { true != !5; }")
            .assertError(peppl.intLiteral, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test
    def void testTEqualityExprIncompatibleTypes() {
        parse('''
            class Object
            class A
            class B
            main { new A == new B; }
        ''').assertError(peppl.equalityExpr, null, "compare", "A", "B")
        // IMPROVE: Find a way to include an issue code for explicit failures?
        
        parse("main { 42 != false; }")
            .assertError(peppl.equalityExpr, null, "compare", "int", "boolean")
    }
    
    @Test
    def void testTRelationalExpr() {
        parse("main {   5 <    6; }").main.lastExpr.type.assertThat(instanceOf(Boolean))
        parse("main {  -1 <= -10; }").main.lastExpr.type.assertThat(instanceOf(Boolean))
        parse("main { 'a' >  ' '; }").main.lastExpr.type.assertThat(instanceOf(Boolean))
        parse("main { 3+4 >=   0; }").main.lastExpr.type.assertThat(instanceOf(Boolean))
    }
    
    @Test
    def void testTRelationalExprErrorInOp() {
        parse("main { -true < 0; }")
            .assertError(peppl.booleanLiteral, SUBTYPEEXPR, "int", "boolean")
        parse("main { 100 <= -false; }")
            .assertError(peppl.booleanLiteral, SUBTYPEEXPR, "int", "boolean")
        parse("main { -'a' > 0; }")
            .assertError(peppl.charLiteral, SUBTYPEEXPR, "int", "char")
        parse("main { 100 >= -false; }")
            .assertError(peppl.booleanLiteral, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test
    def void testTRelationalExprIncompatibleTypes() {
        parse('''
            class Object
            main { new Object < new Object; }
        ''').assertError(peppl.relationalExpr, null, "compare", "Object")
        
        parse("main { true <= false; }")
            .assertError(peppl.relationalExpr, null, "compare", "boolean")
        parse("main { null > null; }")
            .assertError(peppl.relationalExpr, null, "compare", "null")
        parse("main { 5 > '5'; }")
            .assertError(peppl.relationalExpr, null, "compare", "int", "char")
        parse("main { true > '5'; }")
            .assertError(peppl.relationalExpr, null, "compare", "boolean", "char")
    }
    
    @Test
    def void testTArithmeticExpr() {
        parse("main {   4 +  4; }").main.lastExpr.type.assertThat(instanceOf(Int))
        parse("main {   0 -  0; }").main.lastExpr.type.assertThat(instanceOf(Int))
        parse("main {   3 *  2; }").main.lastExpr.type.assertThat(instanceOf(Int))
        parse("main { 100 / -1; }").main.lastExpr.type.assertThat(instanceOf(Int))
        parse("main { -99 %  3; }").main.lastExpr.type.assertThat(instanceOf(Int))
        
        parse('''
            class Object
            class String
            main { "Hi" + " World"; }
        ''').main.lastExpr.type.asRoleType.base.name.assertThat(is("String"))
        parse('''
            class Object
            class String
            main { "" + '5'; }
        ''').main.lastExpr.type.asRoleType.base.name.assertThat(is("String"))
        parse('''
            class Object
            class String
            main { null + " "; }
        ''').main.lastExpr.type.asRoleType.base.name.assertThat(is("String"))
    }
    
    @Test
    def void testTArithmeticExprErrorInOp() {
        parse("main { !'a' + 0; }")
            .assertError(peppl.charLiteral, SUBTYPEEXPR, "char", "boolean")
        parse("main { 100 - -false; }")
            .assertError(peppl.booleanLiteral, SUBTYPEEXPR, "int", "boolean")
        parse("main { -'a' * 0; }")
            .assertError(peppl.charLiteral, SUBTYPEEXPR, "int", "char")
        parse("main { 100 / -true; }")
            .assertError(peppl.booleanLiteral, SUBTYPEEXPR, "int", "boolean")
        parse("main { (3*3) % !42; }")
            .assertError(peppl.intLiteral, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test
    def void testTArtithmeticExprTypeMismatch() {
        parse('''
            class Object
            main { new Object + new Object; }
        ''').assertError(peppl.arithmeticExpr, null, "operator", "undefined", "object")
        parse('''
            class Object
            class A
            class B
            main { new A - new B; }
        ''').assertError(peppl.arithmeticExpr, null, "operator", "undefined", "A", "B")
        
        parse('''
            class Object
            class String
            main { "Hello" - "World"; }
        ''').assertError(peppl.arithmeticExpr, null, "operator", "undefined", "String")
        parse('''
            class Object
            class String
            main { "Hello" * new Object; }
        ''').assertError(peppl.arithmeticExpr, null, "operator", "undefined", "String", "Object")
        parse('''
            class Object
            class String
            main { 5 / "World"; }
        ''').assertError(peppl.arithmeticExpr, null, "operator", "undefined", "int", "String")
        parse('''
            class Object
            class String
            main { null % "World"; }
        ''').assertError(peppl.arithmeticExpr, null, "operator", "undefined", "null", "String")
        
        parse("main { 'a' * 'b'; }")
            .assertError(peppl.arithmeticExpr, null, "operator", "undefined", "char")
        parse("main { null / null; }")
            .assertError(peppl.arithmeticExpr, null, "operator", "undefined", "null")
        parse("main { 5 % '5'; }")
            .assertError(peppl.arithmeticExpr, null, "operator", "undefined", "int", "char")
        parse("main { true % '5'; }")
            .assertError(peppl.arithmeticExpr, null, "operator", "undefined", "boolean", "char")
    }
    
    @Test
    def void testCast() {
        // Redundant casts
        parse("main { (int) 5; }").main.lastExpr.type.assertThat(instanceOf(Int))
        parse("main { (boolean) true; }").main.lastExpr.type.assertThat(instanceOf(Boolean))
        
        var program = parse('''
            class Object
            main { (readwrite Object) new Object; }
        ''')
        program.main.lastExpr.type.assertThat(roleType(READWRITE, program.findClass("Object")))
        
        // Upcasts
        program = parse('''
            class Object
            class A
            main {
                (readwrite Object) new A;
                (readonly A) new A;
                (readwrite A) null;
            }
        ''')
        program.main.expr(0).type.assertThat(roleType(READWRITE, program.findClass("Object")))
        program.main.expr(1).type.assertThat(roleType(READONLY,  program.findClass("A")))
        program.main.expr(2).type.assertThat(roleType(READWRITE, program.findClass("A")))
        
        // Downcasts
        program = parse('''
            class Object
            class A
            main { (readwrite A) new Object; }
        ''')
        program.main.lastExpr.type.assertThat(roleType(READWRITE, program.findClass("A")))
    }
    
    @Test
    def void testTCastErrorInOp() {
        parse("main { (boolean) !5; }")
            .assertError(peppl.intLiteral, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test
    def void testTCastIllegal() {
        parse("main { (boolean) 5; }")
            .assertError(peppl.cast, null, "cast", "int", "boolean")
        parse("main { (int) false; }")
            .assertError(peppl.cast, null, "cast", "int", "boolean")
        parse("main { (int) null; }")
            .assertError(peppl.cast, null, "cast", "int", "null")
        
        parse('''
            class Object
            main { (readwrite Object) 5; }
        ''').assertError(peppl.cast, null, "cast", "readwrite Object", "int")
        parse('''
            class Object
            main { (int) new Object; }
        ''').assertError(peppl.cast, null, "cast", "readwrite Object", "int")
        parse('''
            class Object
            class A
            main { (readwrite A) (readonly A) new A; }
        ''').assertError(peppl.cast, null, "cast", "readwrite A", "readonly A")
    }
    
    @Test
    def void testTUnaryMinus() {
        parse("main { -2; }").main.lastExpr.type.assertThat(instanceOf(Int))
        parse("main { val a: int = 5; -a; }").main.lastExpr.type.assertThat(instanceOf(Int))
        parse("main { -(4-4); }").main.lastExpr.type.assertThat(instanceOf(Int))
    }
    
    @Test
    def void testTUnaryMinusErrorInOp() {
        parse("main { -!5; }")
            .assertError(peppl.intLiteral, SUBTYPEEXPR, "int", "boolean")
        parse("main { -(-'a'); }")
            .assertError(peppl.charLiteral, SUBTYPEEXPR, "char", "int")
    }
    
    @Test
    def void testTUnaryMinusTypeMismatch() {
        parse('''
            class Object
            main { -new Object; }
        ''').assertError(peppl.^new, SUBTYPEEXPR, "Object", "int")
        parse('''
            class Object
            class String
            main { -"Hello"; }
        ''').assertError(peppl.stringLiteral, SUBTYPEEXPR, "String", "int")
        
        parse("main { -'a'; }")
            .assertError(peppl.charLiteral, SUBTYPEEXPR, "char", "int")
        parse("main { -true; }")
            .assertError(peppl.booleanLiteral, SUBTYPEEXPR, "boolean", "int")
        parse("main { -null; }")
            .assertError(peppl.nullLiteral, SUBTYPEEXPR, "null", "int")
    }
    
    @Test
    def void testTUnaryNot() {
        parse("main { !true; }").main.lastExpr.type
            .assertThat(instanceOf(Boolean))
        parse("main { val a: boolean = false; !a; }").main.lastExpr.type
            .assertThat(instanceOf(Boolean))
        parse("main { !(true || false); }").main.lastExpr.type
            .assertThat(instanceOf(Boolean))
    }
    
    @Test
    def void testTUnaryNotErrorInOp() {
        parse("main { !(-'a'); }")
            .assertError(peppl.charLiteral, SUBTYPEEXPR, "char", "int")
        parse("main { !(!5); }")
            .assertError(peppl.intLiteral, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test
    def void testTUnaryNotTypeMismatch() {
        parse('''
            class Object
            main { !new Object; }
        ''').assertError(peppl.^new, SUBTYPEEXPR, "Object", "boolean")
        parse('''
            class Object
            class String
            main { !"Hello"; }
        ''').assertError(peppl.stringLiteral, SUBTYPEEXPR, "String", "boolean")
        
        parse("main { !'a'; }")
            .assertError(peppl.charLiteral, SUBTYPEEXPR, "char", "boolean")
        parse("main { !5; }")
            .assertError(peppl.intLiteral, SUBTYPEEXPR, "int", "boolean")
        parse("main { !null; }")
            .assertError(peppl.nullLiteral, SUBTYPEEXPR, "null", "boolean")
    }
    
    @Test
    def void testTMemberAccessErrorInTarget() {
        parse("main { (!5).a; }")
            .assertError(peppl.intLiteral, SUBTYPEEXPR, "int", "boolean")
        parse("main { (!5).foo(); }")
            .assertError(peppl.intLiteral, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test
    def void testTMemberAccessIllegalTarget() {
        parse('''main { 5.a; }''')
            .assertError(peppl.intLiteral, null, "Illegal", "target", "access")
        parse('''main { false.foo(); }''')
            .assertError(peppl.booleanLiteral, null, "Illegal", "target", "access")
    }
    
    @Test
    def void testTMemberAccessField() {
        parse('''
            class Object
            class A { var x: int }
            main { new A.x; }
        ''').main.lastExpr.type.assertThat(instanceOf(Int))
        parse('''
            class Object
            class A { var x: int }
            main {
                val a: readonly A = new A;
                a.x;
            }
        ''').main.lastExpr.type.assertThat(instanceOf(Int))
        
        val program = parse('''
            class Object
            class A { var a: readwrite A }
            main {
                val a: readwrite A = new A;
                a.a;
            }
        ''')
        program.main.lastExpr.type
            .assertThat(roleType(READWRITE, program.findClass("A")))
        
        parse('''
            class Object
            class A { var a: readwrite A }
            main {
                val a: readonly A = new A;
                a.a;
            }
        ''').main.lastExpr.type.asRoleType.role.assertThat(is(READONLY))
        parse('''
            class Object
            class A { var a: readonly A }
            main {
                val a: readwrite A = new A;
                a.a;
            }
        ''').main.lastExpr.type.asRoleType.role.assertThat(is(READONLY))
        parse('''
            class Object
            class A { var a: pure A }
            main {
                val a: readwrite A = new A;
                a.a;
            }
        ''').main.lastExpr.type.asRoleType.role.assertThat(is(PURE))
        parse('''
            class Object
            class A { var a: pure A }
            main {
                val a: readonly A = new A;
                a.a;
            }
        ''').main.lastExpr.type.asRoleType.role.assertThat(is(PURE))
    }
    
    @Test
    def void testTMemberAccessFieldRoleMismatch() {
        parse('''
            class Object
            class A { var x: int }
            main {
                val a: pure A = new A;
                a.x;
            }
        ''').assertError(peppl.varRef, null,
                "Role", "mismatch", "field", "pure")
    }
    
    @Test
    def void testTMemberAccessMethod() {
        for(expected : Role.values) {
            for(actual : Role.values.filter[subroleSucceeded(it, expected)]) {
                parse('''
                    class Object
                    class A {
                        def «expected» x: int { return 42; }
                    }
                    main {
                        val a: «actual» A = new A;
                        a.x();
                    }
                ''').main.lastExpr.type.assertThat(instanceOf(Int))
            }
        }
        
        val program = parse('''
            class Object
            class A {
                def readwrite a: readonly A { return null; }
            }
            main { new A.a(); }
        ''')
        program.main.lastExpr.type
            .assertThat(roleType(READONLY, program.findClass("A")))
        
        parse('''
            class Object
            class A
            class B {
                def readwrite foo(val a: readonly A, val b: readwrite B,
                        val c: readwrite C, val d: int): void {}
            }
            class C extends B
            main { new C.foo(new A, new C, null, 5); }
        ''').assertNoErrors
    }
    
    @Test
    def void testTMemberAccessMethodRoleMismatch() {
        for(expected : Role.values) {
            for(actual : Role.values.filter[!subroleSucceeded(it, expected)]) {
                parse('''
                    class Object
                    class A {
                        def «expected» x: int { return 42; }
                    }
                    main {
                        val a: «actual» A = new A;
                        a.x();
                    }
                ''').assertError(peppl.varRef, null,
                        "Role", "mismatch", "method", actual.toString)
            }
        }
    }
    
    @Test
    def void testTMemberAccessMethodTypeMismatch() {
        parse('''
            class Object
            class A { def readwrite foo: void {} }
            main { new A.foo(5); }
        ''').assertError(peppl.methodSelector, LINKING_DIAGNOSTIC, "method", "foo")
        parse('''
            class Object
            class A { def readwrite foo(val c: char): void {} }
            main { new A.foo(5, false); }
        ''').assertError(peppl.methodSelector, LINKING_DIAGNOSTIC, "method", "foo")
        parse('''
            class Object
            class A { def readwrite foo(val i: int): void {} }
            main { new A.foo(); }
        ''').assertError(peppl.methodSelector, LINKING_DIAGNOSTIC, "method", "foo")
        parse('''
            class Object
            class A { def readwrite foo(val i: int, val a: readwrite A): void {} }
            main { new A.foo(false); }
        ''').assertError(peppl.methodSelector, LINKING_DIAGNOSTIC, "method", "foo")
        
        parse('''
            class Object
            class A { def readwrite foo(val i: int): void {} }
            main { new A.foo(false); }
        ''').assertError(peppl.methodSelector, LINKING_DIAGNOSTIC, "method", "foo")
        parse('''
            class Object
            class A { def readwrite foo(val a: readwrite A): void {} }
            main { new A.foo(new Object); }
        ''').assertError(peppl.methodSelector, LINKING_DIAGNOSTIC, "method", "foo")
        parse('''
            class Object
            class A { def readwrite foo(val a: readwrite A): void {} }
            main {
                val a: readonly A = new A;
                new A.foo(a);
            }
        ''').assertError(peppl.methodSelector, LINKING_DIAGNOSTIC, "method", "foo")
    }
    
    @Test
    def void testTMemberAccessMethodOverloading() {
        var program = parse('''
            class Object
            class A {
                def readwrite foo(val a: int): int {}
                def readwrite foo(val a: boolean): boolean {}
            }
            main {
                new A.foo(4);
                new A.foo(true);
            }
        ''')
        program.main.expr(0).type.assertThat(instanceOf(Int))
        program.main.expr(1).type.assertThat(instanceOf(Boolean))
        
        program = parse('''
            class Object
            class A {
                def readwrite foo(val a: readwrite A): int {}
                def readwrite foo(val a: readonly  A): boolean {}
            }
            main {
                new A.foo(new A);
                new A.foo((readonly A) new A);
            }
        ''')
        program.main.expr(0).type.assertThat(instanceOf(Int))
        program.main.expr(1).type.assertThat(instanceOf(Boolean))
        
        // (Switch order of declaration to rule out accidental selection of the correct one)
        program = parse('''
            class Object
            class A {
                def readwrite foo(val a: readonly  A): boolean {}
                def readwrite foo(val a: readwrite A): int {}
            }
            main {
                new A.foo(new A);
                new A.foo((readonly A) new A);
            }
        ''')
        program.main.expr(0).type.assertThat(instanceOf(Int))
        program.main.expr(1).type.assertThat(instanceOf(Boolean))
        
        program = parse('''
            class Object
            class A {
                def readwrite foo(val a: readonly  A, val b: readonly  A): boolean {}
                def readwrite foo(val a: readwrite A, val b: readwrite A): int {}
            }
            main {
                new A.foo(new A, new A);
                new A.foo((readonly A) new A, new A);
                new A.foo(new A, (readonly A) new A);
                new A.foo((readonly A) new A, (readonly A) new A);
            }
        ''')
        program.main.expr(0).type.assertThat(instanceOf(Int))
        program.main.expr(1).type.assertThat(instanceOf(Boolean))
        program.main.expr(1).type.assertThat(instanceOf(Boolean))
        program.main.expr(1).type.assertThat(instanceOf(Boolean))
        
        program = parse('''
            class Object
            class A {
                def readwrite foo(val a: readwrite A, val b: readwrite A): int {}
                def readwrite foo(val a: readonly  A, val b: readonly  A): boolean {}
            }
            main {
                new A.foo(new A, new A);
                new A.foo((readonly A) new A, new A);
                new A.foo(new A, (readonly A) new A);
                new A.foo((readonly A) new A, (readonly A) new A);
            }
        ''')
        program.main.expr(0).type.assertThat(instanceOf(Int))
        program.main.expr(1).type.assertThat(instanceOf(Boolean))
        program.main.expr(1).type.assertThat(instanceOf(Boolean))
        program.main.expr(1).type.assertThat(instanceOf(Boolean))
    }
    
    @Test
    def void testTMemberAccessMethodAmbiguous() {
        parse('''
            class Object
            class A {
                def readwrite foo(val a: readonly  A, val b: readwrite A): void {}
                def readwrite foo(val a: readwrite A, val b: readonly  A): void {}
            }
            main {
                new A.foo(new A, new A);
            }
        ''').assertError(peppl.methodSelector, AMBIGUOUS_CALL)
        
        parse('''
            class Object
            class A {
                def readwrite foo(val a: readwrite Object, val b: readwrite A): void {}
                def readwrite foo(val a: readwrite A, val b: readwrite Object): void {}
            }
            main {
                new A.foo(new A, new A);
            }
        ''').assertError(peppl.methodSelector, AMBIGUOUS_CALL)
    }
    
    @Test 
    def void testTThis() {
        for(expected : Role.values) {
            val program = parse('''
                class Object
                class A {
                    def «expected» foo: void { this; }
                }
            ''')
            program.findClass("A").findMethod("foo").lastExpr.type
                .assertThat(roleType(expected, program.findClass("A")))
        }
    }
    
    @Test
    def void testTThisMain() {
        parse('''
            main {
                this;
            }
        ''').assertError(peppl.this, TTHIS)
    }
    
    @Test
    def void testTVariableRef() {
        parse('''
            main {
                val i: int = 5;
                i;
            }
        ''').main.lastExpr.type.assertThat(instanceOf(Int))
        val program = parse('''
            class Object
            class A
            main {
                val a: readonly A = new A;
                a;
            }
        ''')
        program.main.lastExpr.type
            .assertThat(roleType(READONLY, program.findClass("A")))
    }
    
    @Test
    def testTNew() {
        val program = parse('''
            class Object
            class A
            main { new A; }
        ''')
        
        val type = program.main.lastExpr.type.asRoleType
        type.role.assertThat(is(READWRITE))
        type.base.assertThat(is(program.findClass("A")))
    }
    
    def findClass(Program program, String name) {
        program.assertNoErrors
        val result = program.classes.findFirst[it.name == name]
        result.assertThat(notNullValue)
        result
    }
    
    def findMethod(Class clazz, String name) {
        val result = clazz.methods.findFirst[it.name == name]
        result.assertThat(notNullValue)
        result
    }
    
    def expr(WithBlock b, int i) {
        b.assertNoErrors;
        b.body.stmts.filter(ExprStmt).get(i).expr
    }
    
    def lastExpr(WithBlock b) {
        b.assertNoErrors;
        b.body.stmts.filter(ExprStmt).last.expr
    }
    
    def type(Expr expr) {
        val result = system.type(envFor(expr), expr)
        result.failed.assertThat(is(false))
        result.value
    }
    
    def asRoleType(Type type) {
        type.assertThat(instanceOf(RoleType))
        type as RoleType
    }
    
    def Matcher<Type> roleType(Role role, Class clazz) {
        new RoleTypeMatcher(system, role, clazz)
    }
    
    static class RoleTypeMatcher extends BaseMatcher<Type> {
        
        extension PepplSystem system
        val Role role
        val Class clazz
    
        new(PepplSystem system, Role role, Class clazz) {
            this.system = system;
            this.role = role
            this.clazz = clazz
        }
        
        override matches(Object item) {
            if(item instanceof RoleType)
                item.role.equals(role) && item.base.equals(clazz)
            else
                false
        }
        
        override describeTo(Description description) {
            description.appendText(role.literal + " " + clazz.name)
        }
        
        override describeMismatch(Object item, Description description) {
            description.appendText(item.stringRep)
        }
        
    }
}
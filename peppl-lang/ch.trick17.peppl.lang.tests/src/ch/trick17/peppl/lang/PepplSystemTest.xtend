package ch.trick17.peppl.lang

import ch.trick17.peppl.lang.peppl.Block
import ch.trick17.peppl.lang.peppl.Boolean
import ch.trick17.peppl.lang.peppl.Class
import ch.trick17.peppl.lang.peppl.ClassRef
import ch.trick17.peppl.lang.peppl.ElemWithBody
import ch.trick17.peppl.lang.peppl.Expr
import ch.trick17.peppl.lang.peppl.ExprStmt
import ch.trick17.peppl.lang.peppl.GenericClassRef
import ch.trick17.peppl.lang.peppl.Int
import ch.trick17.peppl.lang.peppl.PepplPackage
import ch.trick17.peppl.lang.peppl.Program
import ch.trick17.peppl.lang.peppl.Role
import ch.trick17.peppl.lang.peppl.RoleType
import ch.trick17.peppl.lang.peppl.SimpleClassRef
import ch.trick17.peppl.lang.peppl.Type
import ch.trick17.peppl.lang.typesystem.PepplSystem
import ch.trick17.peppl.lang.typesystem.PepplUtils
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
import ch.trick17.peppl.lang.peppl.PrimitiveType

@RunWith(XtextRunner)
@InjectWith(PepplInjectorProvider)
class PepplSystemTest {
    
    val PepplPackage peppl = PepplPackage.eINSTANCE
    
    @Inject extension ParseHelper<Program>
    @Inject extension ValidationTestHelper
    @Inject extension PepplUtils
    @Inject extension PepplSystem system
    
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
        program.main.lastExpr.type
            .assertThat(isRoleType(READWRITE, classRef(program.findClass("A"))))
    }
    
    @Test
    def testTAssignmentErrorInOp() {
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
    def testTAssignmentNotAssignable() {
        parse('''
            main {
                val x: int;
                x = 5;
            }
        ''').assertError(peppl.varRef, AVARREF, "assign", "value")
    }
    
    @Test
    def testTAssignmentTypeMismatch() {
        parse('''
            main {
                var x: int;
                x = true;
            }
        ''').assertError(peppl.booleanLiteral, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test
    def testTBooleanExpr() {
        parse("main { true || false; }").main.lastExpr.type.assertThat(instanceOf(Boolean))
        parse("main { true && false; }").main.lastExpr.type.assertThat(instanceOf(Boolean))
    }
    
    @Test
    def testTBooleanExprErrorInOp() {
        parse("main { !5 || false; }")
            .assertError(peppl.intLiteral, SUBTYPEEXPR, "int", "boolean")
        parse("main { true || !5; }")
            .assertError(peppl.intLiteral, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test
    def testTBooleanExprTypeMismatch() {
        parse("main { 5 || false; }")
            .assertError(peppl.intLiteral, SUBTYPEEXPR, "int", "boolean")
        parse("main { true || 5; }")
            .assertError(peppl.intLiteral, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test
    def testTEqualityExpr() {
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
    def testTEqualityExprErrorInOp() {
        parse("main { !5 == false; }")
            .assertError(peppl.intLiteral, SUBTYPEEXPR, "int", "boolean")
        parse("main { true != !5; }")
            .assertError(peppl.intLiteral, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test
    def testTEqualityExprIncompatibleTypes() {
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
    def testTRelationalExpr() {
        parse("main {   5 <    6; }").main.lastExpr.type.assertThat(instanceOf(Boolean))
        parse("main {  -1 <= -10; }").main.lastExpr.type.assertThat(instanceOf(Boolean))
        parse("main { 'a' >  ' '; }").main.lastExpr.type.assertThat(instanceOf(Boolean))
        parse("main { 3+4 >=   0; }").main.lastExpr.type.assertThat(instanceOf(Boolean))
    }
    
    @Test
    def testTRelationalExprErrorInOp() {
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
    def testTRelationalExprIncompatibleTypes() {
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
    def testTArithmeticExpr() {
        parse("main {   4 +  4; }").main.lastExpr.type.assertThat(instanceOf(Int))
        parse("main {   0 -  0; }").main.lastExpr.type.assertThat(instanceOf(Int))
        parse("main {   3 *  2; }").main.lastExpr.type.assertThat(instanceOf(Int))
        parse("main { 100 / -1; }").main.lastExpr.type.assertThat(instanceOf(Int))
        parse("main { -99 %  3; }").main.lastExpr.type.assertThat(instanceOf(Int))
        
        parse('''
            class Object
            class String
            main { "Hi" + " World"; }
        ''').main.lastExpr.type.asRoleType.base.clazz.name.assertThat(is("String"))
        parse('''
            class Object
            class String
            main { "" + '5'; }
        ''').main.lastExpr.type.asRoleType.base.clazz.name.assertThat(is("String"))
        parse('''
            class Object
            class String
            main { null + " "; }
        ''').main.lastExpr.type.asRoleType.base.clazz.name.assertThat(is("String"))
        // IMPROVE: check rest of the type as well
    }
    
    @Test
    def testTArithmeticExprErrorInOp() {
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
    def testTArtithmeticExprTypeMismatch() {
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
    def testCast() {
        // Redundant casts
        parse("main { (int) 5; }").main.lastExpr.type.assertThat(instanceOf(Int))
        parse("main { (boolean) true; }").main.lastExpr.type.assertThat(instanceOf(Boolean))
        
        var program = parse('''
            class Object
            main { (readwrite Object) new Object; }
        ''')
        program.main.lastExpr.type.assertThat(isRoleType(READWRITE, classRef(program.findClass("Object"))))
        
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
        program.main.expr(0).type.assertThat(isRoleType(READWRITE, classRef(program.findClass("Object"))))
        program.main.expr(1).type.assertThat(isRoleType(READONLY,  classRef(program.findClass("A"))))
        program.main.expr(2).type.assertThat(isRoleType(READWRITE, classRef(program.findClass("A"))))
        
        // Downcasts
        program = parse('''
            class Object
            class A
            main { (readwrite A) new Object; }
        ''')
        program.main.lastExpr.type.assertThat(isRoleType(READWRITE, classRef(program.findClass("A"))))
    }
    
    @Test
    def testTCastErrorInOp() {
        parse("main { (boolean) !5; }")
            .assertError(peppl.intLiteral, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test
    def testTCastIllegal() {
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
    def testTUnaryMinus() {
        parse("main { -2; }").main.lastExpr.type.assertThat(instanceOf(Int))
        parse("main { val a: int = 5; -a; }").main.lastExpr.type.assertThat(instanceOf(Int))
        parse("main { -(4-4); }").main.lastExpr.type.assertThat(instanceOf(Int))
    }
    
    @Test
    def testTUnaryMinusErrorInOp() {
        parse("main { -!5; }")
            .assertError(peppl.intLiteral, SUBTYPEEXPR, "int", "boolean")
        parse("main { -(-'a'); }")
            .assertError(peppl.charLiteral, SUBTYPEEXPR, "char", "int")
    }
    
    @Test
    def testTUnaryMinusTypeMismatch() {
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
    def testTUnaryNot() {
        parse("main { !true; }").main.lastExpr.type
            .assertThat(instanceOf(Boolean))
        parse("main { val a: boolean = false; !a; }").main.lastExpr.type
            .assertThat(instanceOf(Boolean))
        parse("main { !(true || false); }").main.lastExpr.type
            .assertThat(instanceOf(Boolean))
    }
    
    @Test
    def testTUnaryNotErrorInOp() {
        parse("main { !(-'a'); }")
            .assertError(peppl.charLiteral, SUBTYPEEXPR, "char", "int")
        parse("main { !(!5); }")
            .assertError(peppl.intLiteral, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test
    def testTUnaryNotTypeMismatch() {
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
    def testTMemberAccessErrorInTarget() {
        parse("main { (!5).a; }")
            .assertError(peppl.intLiteral, SUBTYPEEXPR, "int", "boolean")
        parse("main { (!5).foo(); }")
            .assertError(peppl.intLiteral, SUBTYPEEXPR, "int", "boolean")
    }
    
    @Test
    def testTMemberAccessIllegalTarget() {
        parse('''main { 5.a; }''')
            .assertError(peppl.intLiteral, null, "Illegal", "target", "access")
        parse('''main { false.foo(); }''')
            .assertError(peppl.booleanLiteral, null, "Illegal", "target", "access")
    }
    
    @Test
    def testTMemberAccessField() {
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
            .assertThat(isRoleType(READWRITE, classRef(program.findClass("A"))))
        
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
    def testTMemberAccessFieldRoleMismatch() {
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
    def testTMemberAccessMethod() {
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
            .assertThat(isRoleType(READONLY, classRef(program.findClass("A"))))
        
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
    def testTMemberAccessMethodRoleMismatch() {
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
    def testTMemberAccessMethodTypeMismatch() {
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
    def testTMemberAccessMethodOverloading() {
        var program = parse('''
            class Object
            class A {
                def readwrite foo(val a: int): int { return 0; }
                def readwrite foo(val a: boolean): boolean { return false; }
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
                def readwrite foo(val a: readwrite A): int { return 0; }
                def readwrite foo(val a: readonly  A): boolean { return false; }
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
                def readwrite foo(val a: readonly  A): boolean { return false; }
                def readwrite foo(val a: readwrite A): int { return 0; }
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
                def readwrite foo(val a: readonly  A, val b: readonly  A): boolean { return false; }
                def readwrite foo(val a: readwrite A, val b: readwrite A): int { return 0; }
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
                def readwrite foo(val a: readwrite A, val b: readwrite A): int { return 0; }
                def readwrite foo(val a: readonly  A, val b: readonly  A): boolean { return false; }
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
    def testTMemberAccessMethodAmbiguous() {
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
    def testTThis() {
        for(expected : Role.values) {
            val program = parse('''
                class Object
                class A {
                    def «expected» foo: void { this; }
                }
            ''')
            program.findClass("A").findMethod("foo").lastExpr.type
                .assertThat(isRoleType(expected, classRef(program.findClass("A"))))
        }
    }
    
    @Test
    def testTThisMain() {
        parse('''
            main {
                this;
            }
        ''').assertError(peppl.this, TTHIS)
    }
    
    @Test
    def testTVariableRef() {
        parse('''
            main {
                val i: int = 5;
                i;
            }
        ''').main.lastExpr.type.assertThat(instanceOf(Int))
        parse('''
            class Object
            class A {
                def pure foo(val i: int): void {
                    i;
                }
            }
            main {}
        ''').findClass("A").methods.head.lastExpr.type.assertThat(instanceOf(Int))
        
        var program = parse('''
            class Object
            class A
            main {
                val a: readonly A = new A;
                a;
            }
        ''')
        program.main.lastExpr.type
            .assertThat(isRoleType(READONLY, classRef(program.findClass("A"))))
        
        parse('''
            main {
                i;
                val i: int = 0;
            }
        ''').assertError(peppl.varRef, LINKING_DIAGNOSTIC, "var", "i")
        parse('''
            main {
                {
                    val i: int = 0;
                }
                i;
            }
        ''').assertError(peppl.varRef, LINKING_DIAGNOSTIC, "var", "i")
    }
    
    @Test
    def testTNew() {
        var program = parse('''
            class Object
            class A
            main { new A; }
        ''')
        program.main.lastExpr.type
            .assertThat(isRoleType(READWRITE, classRef(program.findClass("A"))))
        
        program = parse('''
            class Object
            class Array
            main { new Array[int]; }
        ''')
        program.main.lastExpr.type
            .assertThat(isRoleType(READWRITE, classRef(program.findClass("Array"), intType)))
        
        program = parse('''
            class Object
            class A
            class Array
            main { new Array[readonly A]; }
        ''')
        program.main.lastExpr.type
            .assertThat(isRoleType(READWRITE, classRef(program.findClass("Array"),
                roleType(READONLY, classRef(program.findClass("A"))))))
        
        program = parse('''
            class Object
            class A
            class Array
            main { new Array[pure Array[readwrite A]]; }
        ''')
        val array = program.findClass("Array")
        program.main.lastExpr.type
            .assertThat(isRoleType(READWRITE, classRef(array,
                roleType(PURE, classRef(array,
                    roleType(READWRITE, classRef(program.findClass("A"))))))))
    }
    
    @Test
    def testWBlock() {
        parse('''
            class Object
            main {
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
            main {
                (int) false;
            }
        ''').assertError(peppl.cast, null, "cannot cast", "boolean", "int")
        parse('''
            main {
                {
                    new Object;
                    {
                        new Object;
                        (int) false;
                        new Object;
                        {}
                    }
                }
            }
        ''').assertError(peppl.cast, null, "cannot cast", "boolean", "int")
    }
    
    @Test
    def testWLocalVarDecl() {
        parse('''
            class Object
            class A
            main {
                val i: int = 1;
                val a: readwrite A = new A;
                val b: pure Object = new A;
                val c: readwrite Object = null;
            }
        ''').assertNoErrors
        
        parse('''
            main {
                val i: int = false;
            }
        ''').assertError(peppl.booleanLiteral, SUBTYPEEXPR, "boolean", "int")
        parse('''
            class Object
            main {
                val o: readwrite Object = (pure Object) new Object;
            }
        ''').assertError(peppl.cast, SUBTYPEEXPR, "pure Object", "readwrite Object")
        parse('''
            class Object
            class A
            main {
                val o: pure A = (pure Object) new A;
            }
        ''').assertError(peppl.cast, SUBTYPEEXPR, "pure Object", "pure A")
    }
    
    @Test
    def testWIfStmt() {
        parse('''
            class Object
            main {
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
            class Object
            main {
                if(5)
                    new Object;
            }
        ''').assertError(peppl.intLiteral, SUBTYPEEXPR, "int", "boolean")
        parse('''
            main {
                if(true)
                    (int) false;
            }
        ''').assertError(peppl.cast, null, "cannot cast", "boolean", "int")
        parse('''
            main {
                if(true) {}
                else
                    (int) false;
            }
        ''').assertError(peppl.cast, null, "cannot cast", "boolean", "int")
    }
    
    @Test
    def testWWhileLoop() {
        parse('''
            class Object
            main {
                while(true)
                    new Object;
                
                while(3 == 2) {
                    new Object;
                    new Object;
                }
            }
        ''').assertNoErrors
        
        parse('''
            class Object
            main {
                while(5)
                    new Object;
            }
        ''').assertError(peppl.intLiteral, SUBTYPEEXPR, "int", "boolean")
        parse('''
            main {
                while(true)
                    (int) false;
            }
        ''').assertError(peppl.cast, null, "cannot cast", "boolean", "int")
    }
    
    @Test
    def testWReturn() {
        parse('''
            class Object
            class A {
                def pure a: void {}
                def pure b: void {
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
                    return (readonly A) new A;
                }
            }
        ''').assertNoErrors
        
        parse('''
            class Object
            class A {
                def pure a: void {
                    return 1;
                }
            }
        ''').assertError(peppl.intLiteral, SUBTYPEEXPR, "int", "void")
        parse('''
            class Object
            class A {
                def pure a: int {
                    return false;
                }
            }
        ''').assertError(peppl.booleanLiteral, SUBTYPEEXPR, "boolean", "int")
        parse('''
            class Object
            class A {
                def pure a: readwrite A {
                    return (pure A) new A;
                }
            }
        ''').assertError(peppl.cast, SUBTYPEEXPR, "pure A", "readwrite A")
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
    
    def expr(ElemWithBody b, int i) { expr(b.body, i) }
    
    def expr(Block b, int i) {
        b.assertNoErrors;
        b.stmts.filter(ExprStmt).get(i).expr
    }
    
    def lastExpr(ElemWithBody b) { lastExpr(b.body) }
    
    def lastExpr(Block b) {
        b.assertNoErrors;
        b.stmts.filter(ExprStmt).last.expr
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
    
    def Matcher<Type> isRoleType(Role role, ClassRef base) {
        new RoleTypeMatcher(system, roleType(role, base))
    }
    
    static class RoleTypeMatcher extends BaseMatcher<Type> {
        
        extension PepplSystem system
        val RoleType expected
    
        new(PepplSystem system, RoleType expected) {
            this.system = system
            this.expected = expected
        }
        
        override matches(Object actual) {
            expected.equalTo(actual)
        }
        
        private def dispatch boolean equalTo(RoleType _, Object __) { false }
        
        private def dispatch boolean equalTo(RoleType it, RoleType other) {
            role.equals(other.role)
            base.equalTo(other.base)
        }
        
        private def dispatch boolean equalTo(PrimitiveType it, PrimitiveType other) {
            class == other.class
        }
        
        private def dispatch boolean equalTo(ClassRef _, Object __) { false }
        
        private def dispatch boolean equalTo(SimpleClassRef it, SimpleClassRef other) {
            clazz.equals(other.clazz)
        }
        
        private def dispatch boolean equalTo(GenericClassRef it, GenericClassRef other) {
            clazz.equals(other.clazz)
            typeArg.equalTo(other.typeArg)
        }
        
        override describeTo(Description description) {
            description.appendText(expected.stringRep)
        }
        
        override describeMismatch(Object actual, Description description) {
            description.appendText(actual.stringRep)
        }
        
    }
}
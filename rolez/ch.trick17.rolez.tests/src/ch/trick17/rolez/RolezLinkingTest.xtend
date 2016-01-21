package ch.trick17.rolez

import ch.trick17.rolez.rolez.Int
import ch.trick17.rolez.rolez.MemberAccess
import ch.trick17.rolez.rolez.New
import ch.trick17.rolez.rolez.NormalClass
import ch.trick17.rolez.rolez.Program
import ch.trick17.rolez.rolez.ReadOnly
import ch.trick17.rolez.rolez.ReadWrite
import ch.trick17.rolez.rolez.RoleType
import ch.trick17.rolez.rolez.SuperConstrCall
import ch.trick17.rolez.tests.RolezInjectorProvider
import javax.inject.Inject
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.eclipse.xtext.junit4.validation.ValidationTestHelper
import org.junit.Test
import org.junit.runner.RunWith

import static ch.trick17.rolez.Constants.*
import static ch.trick17.rolez.rolez.RolezPackage.Literals.*
import static ch.trick17.rolez.scoping.RolezScopeProvider.AMBIGUOUS_CALL
import static org.eclipse.xtext.diagnostics.Diagnostic.*
import static org.hamcrest.Matchers.*

import static extension org.hamcrest.MatcherAssert.assertThat

@RunWith(XtextRunner)
@InjectWith(RolezInjectorProvider)
class RolezLinkingTest {
    
    @Inject extension RolezExtensions
    @Inject extension ParseHelper<Program>
    @Inject extension ValidationTestHelper
    @Inject extension TestUtils
    
    @Test def testMultipleResources() {
        val set = newResourceSet.with("class rolez.lang.Object mapped to java.lang.Object").with("class A")
        parse("class B extends A", set).assertNoErrors
    }
    
    @Test def testPackagesAndImports() {
        // "Unpackaged" classes are visible from everywhere
        var set = newResourceSet.with("class rolez.lang.Object mapped to java.lang.Object").with("class A")
        parse('''
            package foo.bar
            class B extends A
        ''', set).assertNoErrors
        
        // Classes in same package are visible
        set = newResourceSet.with("class rolez.lang.Object mapped to java.lang.Object").with('''
            package foo.bar
            class A
        ''')
        parse('''
            package foo.bar
            class B extends A
        ''', set).assertNoErrors
        
        // Classes can specify package directly in declaration
        set = newResourceSet.with("class rolez.lang.Object mapped to java.lang.Object").with('''
            class foo.bar.A
        ''')
        parse('''
            package foo.bar
            class B extends A
        ''', set).assertNoErrors
        
        // Also partially
        set = newResourceSet.with("class rolez.lang.Object mapped to java.lang.Object").with('''
            package foo
            class bar.A
        ''')
        parse('''
            package foo.bar
            class B extends A
        ''', set).assertNoErrors
        
        // Classes can be referred to using their fully qualified name
        set = newResourceSet.with("class rolez.lang.Object mapped to java.lang.Object").with('''
            package foo.bar
            class A
        ''')
        parse('''
            package a.b
            class B extends foo.bar.A {
                def pure foo: {
                    var a: pure foo.bar.A;
                }
            }
        ''', set).assertNoErrors
        
        // Classes can be imported
        set = newResourceSet.with("class rolez.lang.Object mapped to java.lang.Object").with('''
            package foo.bar
            class A
        ''')
        parse('''
            package a.b
            import foo.bar.A
            class B extends A
        ''', set).assertNoErrors
        
        // Also with wildcards
        set = newResourceSet.with("class rolez.lang.Object mapped to java.lang.Object").with('''
            package foo.bar
            class A
        ''')
        parse('''
            package a.b
            import foo.bar.*
            class B extends A
        ''', set).assertNoErrors
        
        // Class in same package is chosen, not "unpackaged" class
        set = newResourceSet.with("class rolez.lang.Object mapped to java.lang.Object").with('''
            class A
        ''').with('''
            package foo.bar
            class A {
                def pure foo: {}
            }
        ''')
        parse('''
            package foo.bar
            task B: {
                new A.foo;
            }
        ''', set).assertNoErrors
        
        // Classes in rolez.lang are always visible
        set = newResourceSet.with('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.A
        ''')
        parse('''
            package foo.bar
            class B extends A
            class C extends rolez.lang.A
        ''', set).assertNoErrors
        
        set = newResourceSet.with("class rolez.lang.Object mapped to java.lang.Object").with('''
            package foo.bar
            class A
        ''')
        parse('''
            package a.b
            class B extends A
        ''', set).assertError(SIMPLE_CLASS_REF, LINKING_DIAGNOSTIC)
        set = newResourceSet.with("class rolez.lang.Object mapped to java.lang.Object").with('''
            package foo.bar
            class A
        ''')
        parse('''
            package a.b
            class B extends A
        ''', set).assertError(SIMPLE_CLASS_REF, LINKING_DIAGNOSTIC)
    }
    
    @Test def testSuperClass() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A extends B
        ''').assertError(SIMPLE_CLASS_REF, LINKING_DIAGNOSTIC)
        parse('''
            class A
        ''').assertError(SIMPLE_CLASS_REF, LINKING_DIAGNOSTIC)
    }
    
    @Test def testSuperConstrCall() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            
            class A1
            class B1 extends A1
            
            class A2 { new {} }
            class B2 extends A2
            
            class A3 { new(i: int) {} }
            class B3 extends A3 { new { super(0); } }
            
            class A4 { new(a: readwrite A4) {} }
            class B4 extends A4 { new { super(new A4(null)); }
        ''').assertNoErrors
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
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
    
    @Test def testSuperConstrCallOverloading() {
        val classB = (parse('''
            class rolez.lang.Object mapped to java.lang.Object
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
            .constr.params.head.type.assertThat(instanceOf(Int))
        (classB.constrs.findFirst[params.size == 1].body.stmts.head as SuperConstrCall)
            .constr.params.head.type.assertThat(instanceOf(Boolean))
        
        var classC = (parse('''
            class rolez.lang.Object mapped to java.lang.Object
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
            .constr.params.head.type as RoleType).role.assertThat(instanceOf(ReadWrite))
        ((classC.constrs.findFirst[params.size == 1].body.stmts.head as SuperConstrCall)
            .constr.params.head.type as RoleType).role.assertThat(instanceOf(ReadOnly))
        
        // (Switch order of declaration to rule out accidental selection of the correct one)
        classC = (parse('''
            class rolez.lang.Object mapped to java.lang.Object
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
            .constr.params.head.type as RoleType).role.assertThat(instanceOf(ReadWrite))
        ((classC.constrs.findFirst[params.size == 1].body.stmts.head as SuperConstrCall)
            .constr.params.head.type as RoleType).role.assertThat(instanceOf(ReadOnly))
    }
    
    @Test def testSuperConstrCallTypeMismatch() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A
            class B extends A {
                new { super(5); }
            }
        ''').assertError(SUPER_CONSTR_CALL, LINKING_DIAGNOSTIC)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                new { super(5); }
            }
        ''').assertError(SUPER_CONSTR_CALL, LINKING_DIAGNOSTIC)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                new {}
            }
            class B extends A {
                new { super(5); }
            }
        ''').assertError(SUPER_CONSTR_CALL, LINKING_DIAGNOSTIC)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                new(c: char) {}
            }
            class B extends A {
                new { super(5, false); }
            }
        ''').assertError(SUPER_CONSTR_CALL, LINKING_DIAGNOSTIC)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                new(i: int) {}
            }
            class B extends A { 
                new { super(); }
            }
        ''').assertError(SUPER_CONSTR_CALL, LINKING_DIAGNOSTIC)
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                new(i: int) {}
            }
            class B extends A {
                new { super(false); }
            }
        ''').assertError(SUPER_CONSTR_CALL, LINKING_DIAGNOSTIC)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                new(a: readwrite A) {}
            }
            class B extends A {
                new { super(new Object); }
            }
        ''').assertError(SUPER_CONSTR_CALL, LINKING_DIAGNOSTIC)
    }
    
    @Test def testSuperConstrCallTypeMismatchImplicit() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                new(i: int) {}
            }
            class B extends A
        ''').assertError(SUPER_CONSTR_CALL, LINKING_DIAGNOSTIC)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                new(i: int) {}
            }
            class B extends A {
                new {}
            }
        ''').assertError(SUPER_CONSTR_CALL, LINKING_DIAGNOSTIC)
    }
    
    @Test def testSuperConstrCallAmbiguous() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
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
            class rolez.lang.Object mapped to java.lang.Object
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
    
    @Test def testMemberAccess() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                val i: int
                def pure foo: {}
            }
            task Main: {
                new A.i;
                new A.foo;
            }
        ''').assertNoErrors
    }
    
    @Test def testMemberAccessOverloading() {
        var program = parse('''
            class rolez.lang.Object mapped to java.lang.Object
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
            class rolez.lang.Object mapped to java.lang.Object
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
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def readwrite foo(a: readwrite A): int { return 0; }
                def readwrite foo(a: readwrite Object): boolean { return false; }
            }
            task Main: {
                new A.foo(new A);
                new A.foo(new A as readwrite Object);
            }
        ''')
        program.main.expr(0).type.assertThat(instanceOf(Int))
        program.main.expr(1).type.assertThat(instanceOf(Boolean))
        
        // (Switch order of declaration to rule out accidental selection of the correct one)
        program = parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def readwrite foo(a: readwrite Object): boolean { return false; }
                def readwrite foo(a: readwrite A): int { return 0; }
            }
            task Main: {
                new A.foo(new A);
                new A.foo(new A as readwrite Object);
            }
        ''')
        program.main.expr(0).type.assertThat(instanceOf(Int))
        program.main.expr(1).type.assertThat(instanceOf(Boolean))
        
        program = parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def readwrite foo(a: pure Object, b: pure Object): boolean { return false; }
                def readwrite foo(a: pure      A, b: pure      A): int { return 0; }
            }
            task Main: {
                new A.foo(new A, new A);
                new A.foo(new A, new A as readwrite Object);
                new A.foo(new A, new A as readwrite Object);
                new A.foo(new A as readwrite Object, new A as readwrite Object);
            }
        ''')
        program.main.expr(0).type.assertThat(instanceOf(Int))
        program.main.expr(1).type.assertThat(instanceOf(Boolean))
        program.main.expr(1).type.assertThat(instanceOf(Boolean))
        program.main.expr(1).type.assertThat(instanceOf(Boolean))
        
        program = parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def readwrite foo(a: pure      A, b: pure      A): int { return 0; }
                def readwrite foo(a: pure Object, b: pure Object): boolean { return false; }
            }
            task Main: {
                new A.foo(new A, new A);
                new A.foo(new A, new A as readwrite Object);
                new A.foo(new A, new A as readwrite Object);
                new A.foo(new A as readwrite Object, new A as readwrite Object);
            }
        ''')
        program.main.expr(0).type.assertThat(instanceOf(Int))
        program.main.expr(1).type.assertThat(instanceOf(Boolean))
        program.main.expr(1).type.assertThat(instanceOf(Boolean))
        program.main.expr(1).type.assertThat(instanceOf(Boolean))
        
        // TODO: test generic methods
    }
    
    @Test def testMemberAccessFieldAndMethod() {
        var program = parse('''
            class rolez.lang.Object mapped to java.lang.Object
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
    }
    
    @Test def testMemberAccessMethodFromSuperclass() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A
            class B {
                def readwrite foo(a: readonly A, b: readwrite B, c: readwrite C, d: int): {}
            }
            class C extends B
            task Main: { new C.foo(new A, new C, null, 5); }
        ''').assertNoErrors
    }
    
    @Test def testMemberAccessMethodOverride() {
       (parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def readwrite foo: {}
            }
            class B extends A {
                override readwrite foo: {}
            }
            task Main: { new B.foo; }
        ''').main.lastExpr as MemberAccess).method.enclosingClass.name.assertThat(is("A"))
     }
    
    @Test def testMemberAccessMethodTypeMismatch() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A { def readwrite foo: {} }
            task Main: { new A.foo(5); }
        ''').assertError(MEMBER_ACCESS, LINKING_DIAGNOSTIC, "foo")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A { def readwrite foo(c: char): {} }
            task Main: { new A.foo(5, false); }
        ''').assertError(MEMBER_ACCESS, LINKING_DIAGNOSTIC, "foo")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A { def readwrite foo(i: int): {} }
            task Main: { new A.foo(); }
        ''').assertError(MEMBER_ACCESS, LINKING_DIAGNOSTIC, "foo")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A { def readwrite foo(i: int, a: readwrite A): {} }
            task Main: { new A.foo(false); }
        ''').assertError(MEMBER_ACCESS, LINKING_DIAGNOSTIC, "foo")
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A { def readwrite foo(i: int): {} }
            task Main: { new A.foo(false); }
        ''').assertError(MEMBER_ACCESS, LINKING_DIAGNOSTIC, "foo")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A { def readwrite foo(a: readwrite A): {} }
            task Main: { new A.foo(new Object); }
        ''').assertError(MEMBER_ACCESS, LINKING_DIAGNOSTIC, "foo")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A { def readwrite foo(a: readwrite A): {} }
            task Main: { new A.foo(new A as readonly A); }
        ''').assertError(MEMBER_ACCESS, LINKING_DIAGNOSTIC, "foo")
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Array[T] mapped to rolez.lang.Array {
                mapped new(i: int)
                mapped def readwrite set(i: int, o: T):
            }
            task Main: {
                new Array[int](1).set(0, true);
            }
        ''').assertError(MEMBER_ACCESS, LINKING_DIAGNOSTIC, "set")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Array[T] mapped to rolez.lang.Array {
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
    
    @Test def testMemberAccessMethodAmbiguous() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def readwrite foo(a: readonly  A, b: readwrite A): {}
                def readwrite foo(a: readwrite A, b: readonly  A): {}
            }
            task Main: {
                new A.foo(new A, new A);
            }
        ''').assertError(MEMBER_ACCESS, AMBIGUOUS_CALL)
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def readwrite foo(a: readwrite Object, b: readwrite A): {}
                def readwrite foo(a: readwrite A, b: readwrite Object): {}
            }
            task Main: {
                new A.foo(new A, new A);
            }
        ''').assertError(MEMBER_ACCESS, AMBIGUOUS_CALL)
        
        // TODO: test generic methods
    }
    
    @Test def testNew() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A
            class B { new: {} }
            class C { new(i: int) {} }
            class D { new(a: readonly A, b: readwrite B) {} }
            task Main: {
                new A;
                new B;
                new C(0);
                new D(new A, new B);
            }
        ''').assertNoErrors
    }
    
    @Test def testNewGeneric() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Array[T] mapped to rolez.lang.Array {
                mapped new(length: int)
            }
            task Main: {
                new Array[int](10);
                new Array[readonly Object](0);
            }
        ''').assertNoErrors
    }
    
    @Test def testNewOverloading() {
        var program = parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                new(a: int) {}
                new(a: boolean) {}
            }
            task Main: {
                new A(4);
                new A(true);
            }
        ''')
        (program.main.expr(0) as New).constr.params.head.type.assertThat(instanceOf(Int))
        (program.main.expr(1) as New).constr.params.head.type.assertThat(instanceOf(Boolean))
        
        program = parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A
            class B {
                new(a: readwrite Object) {}
                new(a: readwrite A) {}
            }
            task Main: {
                new B(new A);
                new B(new A as readwrite Object);
            }
        ''')
        ((program.main.expr(0) as New).constr.params.head.type as RoleType).base.clazz.name.assertThat(is("A"))
        ((program.main.expr(1) as New).constr.params.head.type as RoleType).base.clazz.name.assertThat(is(objectClassName.toString))
        
        // (Switch order of declaration to rule out accidental selection of the correct one)
        program = parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A
            class B {
                new(a: readwrite A) {}
                new(a: readwrite Object) {}
            }
            task Main: {
                new B(new A);
                new B(new A as readwrite Object);
            }
        ''')
        ((program.main.expr(0) as New).constr.params.head.type as RoleType).base.clazz.name.assertThat(is("A"))
        ((program.main.expr(1) as New).constr.params.head.type as RoleType).base.clazz.name.assertThat(is(objectClassName.toString))
        
        // IMPROVE: test generic constructors, once  supported outside of the array class
    }
    
    @Test def testNewTypeMismatch() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A
            task Main: { new A(5); }
        ''').assertError(NEW, LINKING_DIAGNOSTIC)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A { new {} }
            task Main: { new A(5); }
        ''').assertError(NEW, LINKING_DIAGNOSTIC)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A { new(c: char) {} }
            task Main: { new A(5, false); }
        ''').assertError(NEW, LINKING_DIAGNOSTIC)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A { new(i: int) {} }
            task Main: { new A; }
        ''').assertError(NEW, LINKING_DIAGNOSTIC)
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A { new(i: int) {} }
            task Main: { new A(false); }
        ''').assertError(NEW, LINKING_DIAGNOSTIC)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A { new(a: readwrite A) {} }
            task Main: { new A(new Object); }
        ''').assertError(NEW, LINKING_DIAGNOSTIC)
        
        // TODO: test generic constructors
    }
    
    @Test def testNewAmbiguous() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
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
            class rolez.lang.Object mapped to java.lang.Object
            class A
            class B {
                new(a: readwrite Object, b: readwrite A) {}
                new(a: readwrite A, b: readwrite Object) {}
            }
            task Main: {
                new B(new A, new A);
            }
        ''').assertError(NEW, AMBIGUOUS_CALL)
        
        // TODO: test generic constructors
    }
    
    @Test def testNewObjectClassRef() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            object A
            task Main: {
                new A;
            }
        ''').assertError(NEW, LINKING_DIAGNOSTIC)
    }
    
    @Test def testVarRef() {
        parse('''
            task Main: {
                val i = 5;
                i;
            }
        ''').assertNoErrors
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def pure foo(i: int): {
                    i;
                }
            }
            task Main: {}
        ''').assertNoErrors
        
        parse('''
            task Main: {
                i;
                val i = 0;
            }
        ''').assertError(VAR_REF, LINKING_DIAGNOSTIC, "var", "i")
        parse('''
            task Main: {
                {
                    val i = 0;
                }
                i;
            }
        ''').assertError(VAR_REF, LINKING_DIAGNOSTIC, "var", "i")
    }
    
    @Test def testSuperMethod() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {                def readwrite foo: {} }
            class B extends A { override readwrite foo: {} }
        ''').assertNoErrors
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {                def readwrite foo(i: int): {} }
            class B extends A { override readwrite foo(i: int): {} }
        ''').assertNoErrors
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {                def readwrite foo(i: int): int { return 0; } }
            class B extends A { override readwrite foo(j: int): int { return 0; } }
        ''').assertNoErrors
    }
    
    @Test def testSuperMethodDifferentReturnType() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {                def readwrite foo: readwrite A { return null; } }
            class B extends A { override readwrite foo: readwrite B { return null; } }
        ''').assertNoErrors
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {                def readwrite foo: readonly  A { return null; } }
            class B extends A { override readwrite foo: readwrite A { return null; } }
        ''').assertNoErrors
    }
    
    @Test def testSuperMethodDifferentThisRole() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {                def readwrite foo: {} }
            class B extends A { override readonly  foo: {} }
        ''').assertNoErrors
    }
    
    @Test def testSuperMethodDifferentParamRoles() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {                def pure foo(o: readwrite Object): {} }
            class B extends A { override pure foo(o: readonly  Object): {} }
        ''').assertNoErrors
    }
    
    @Test def testSuperMethodGeneric() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class GenericClass[T] mapped to «GenericClass.canonicalName» {
                mapped new(t: T)
                mapped def pure foo(t: T):
                mapped def pure foo(t1: T, t2: T):
                mapped def pure foo: T
            }
            class A extends GenericClass[int] {
                new(i: int) { super(i); }
                def      pure foo(t: double): {}
                override pure foo(t: int   ): {}
                override pure foo(t1: int, t2: int): {}
                override pure foo: int { return 0; }
            }
            class B extends GenericClass[readwrite Object] {
                new(o: readwrite Object) { super(o); }
                def      pure foo(t: readwrite A): {}
                override pure foo(t: readwrite Object): {}
                override pure foo(t1: readwrite Object, t2: readwrite Object): {}
                override pure foo: readwrite Object { return null; }
            }
            class C extends GenericClass[readonly Object] {
                new(o: readonly Object) { super(o); }
                override pure foo: readwrite Object { return null; }
            }
        ''').assertNoErrors
    }
    
    @Test def testSuperMethodFail() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {                def readwrite foo: {} }
            class B extends A { override readwrite foo(i: int): {} }
        ''').assertError(METHOD, LINKING_DIAGNOSTIC)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {                def readwrite foo(i: int): {} }
            class B extends A { override readwrite foo(c: char): {} }
        ''').assertError(METHOD, LINKING_DIAGNOSTIC)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {                def readwrite foo(a: readwrite A): {} }
            class B extends A { override readwrite foo(a: readwrite B): {} }
        ''').assertError(METHOD, LINKING_DIAGNOSTIC)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {                def readwrite foo(a: readwrite B): {} }
            class B extends A { override readwrite foo(a: readwrite A): {} }
        ''').assertError(METHOD, LINKING_DIAGNOSTIC)
    }
    
    @Test def testSuperMethodGenericFail() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class GenericClass[T] mapped to «GenericClass.canonicalName» {
                mapped new(t: T)
                mapped def pure foo(t: T):
            }
            class A extends GenericClass[int] {
                override pure foo(t: double): {}
            }
        ''').assertError(METHOD, LINKING_DIAGNOSTIC)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class GenericClass[T] mapped to «GenericClass.canonicalName» {
                mapped new(t: T)
                mapped def pure foo(t: T):
            }
            class A extends GenericClass[readonly Object] {
                override pure foo(t: readonly A): {}
            }
        ''').assertError(METHOD, LINKING_DIAGNOSTIC)
    }
    
    static class GenericClass<T> {
        new(T t) {}
        new(T t, int i) {}
        def void foo(T t) {}
        def void foo(T t1, T t2) {}
        def T foo() { null }
    }
    
    @Test def testTypeParam() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class GenericClass[T] mapped to «GenericClass.canonicalName» {
                mapped new(t: T)
            }
        ''').assertNoErrors
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class GenericClass[T] mapped to «GenericClass.canonicalName» {
                mapped new(t: T)
            }
            class A {
                def pure foo: T { return null; }
            }
        ''').assertError(TYPE_PARAM_REF, LINKING_DIAGNOSTIC)
    }
    
    @Test def testRoleParam() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def [r] r getThis: r A { return this; }
            }
        ''').assertNoErrors
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def [r] r getThis: r A { return this; }
                def     r getThat: r A { return this; }
            }
        ''').assertError(ROLE_PARAM_REF, LINKING_DIAGNOSTIC)
    }
    
    @Test def testJvmClass() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
        ''').assertNoErrors
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Objectionable
        ''').assertError(CLASS, LINKING_DIAGNOSTIC)
    }
    
    @Test def testJvmField() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.String mapped to java.lang.String
            class rolez.io.PrintStream mapped to java.io.PrintStream {
                mapped new(s: readonly String)
            }
            object rolez.lang.System mapped to java.lang.System {
                mapped val out: readonly rolez.io.PrintStream
            }
        ''').assertNoErrors
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object {
                mapped val thisFieldDoesNotExist: int
            }
        ''').assertError(FIELD, LINKING_DIAGNOSTIC)
    }
    
    @Test def testJvmMethod() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object {
                mapped def readonly hashCode: int
            }
        ''').assertNoErrors
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object {
                mapped def readonly someMethodThatCertainlyDoesNotExist: int
            }
        ''').assertError(METHOD, LINKING_DIAGNOSTIC)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Array[T] mapped to rolez.lang.Array {
                mapped new(i: int)
                mapped def readwrite set(i: int, component: readonly Object):
            }
        ''').assertError(METHOD, LINKING_DIAGNOSTIC)
    }
    
    @Test def testJvmConstr() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
        ''').assertNoErrors
        parse('''
            class rolez.lang.Object mapped to java.lang.Object {
                mapped new
            }
            class rolez.lang.String mapped to java.lang.String {
                mapped new(s: readonly String)
            }
        ''').assertNoErrors
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object {
                mapped new(i: int)
            }
        ''').assertError(CONSTR, LINKING_DIAGNOSTIC)
        
        // TODO: Test implicit constructor with a Java class that doesn't have a no-arg constructor
    }
    
    @Test def testArrayClass() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Array[T] mapped to rolez.lang.Array {
                mapped val length: int
                mapped new(length: int)
                mapped def readonly  get(i: int): T
                mapped def readwrite set(i: int, component: T):
            }
        ''').assertNoErrors
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Array[T] mapped to rolez.lang.Array {
                mapped new(length: int)
                mapped val foo: int
            }
        ''').assertError(FIELD, LINKING_DIAGNOSTIC)
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Array[T] mapped to rolez.lang.Array {
                mapped new(length: int)
                mapped def pure foo: int
            }
        ''').assertError(METHOD, LINKING_DIAGNOSTIC)
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Array[T] mapped to rolez.lang.Array {
                mapped new {}
            }
        ''').assertError(CONSTR, LINKING_DIAGNOSTIC)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Array[T] mapped to rolez.lang.Array {
                mapped new(i: int, j: int) {}
            }
        ''').assertError(CONSTR, LINKING_DIAGNOSTIC)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Array[T] mapped to rolez.lang.Array {
                mapped new(i: double) {}
            }
        ''').assertError(CONSTR, LINKING_DIAGNOSTIC)
    }
}

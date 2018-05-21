package ch.trick17.rolez

import ch.trick17.rolez.rolez.Boolean
import ch.trick17.rolez.rolez.Int
import ch.trick17.rolez.rolez.MemberAccess
import ch.trick17.rolez.rolez.New
import ch.trick17.rolez.rolez.NormalClass
import ch.trick17.rolez.rolez.Program
import ch.trick17.rolez.rolez.Pure
import ch.trick17.rolez.rolez.ReadOnly
import ch.trick17.rolez.rolez.ReadWrite
import ch.trick17.rolez.rolez.Ref
import ch.trick17.rolez.rolez.RoleType
import ch.trick17.rolez.rolez.SuperConstrCall
import ch.trick17.rolez.rolez.TypeParamRef
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

import static ch.trick17.rolez.Constants.*
import static ch.trick17.rolez.rolez.RolezPackage.Literals.*
import static ch.trick17.rolez.scoping.RolezScopeProvider.AMBIGUOUS_CALL
import static org.eclipse.xtext.diagnostics.Diagnostic.*
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertTrue

import static extension ch.trick17.rolez.RolezExtensions.*
import static extension org.hamcrest.MatcherAssert.assertThat

@RunWith(XtextRunner)
@InjectWith(RolezInjectorProvider)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class RolezLinkingTest {
    
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
            class B {
                task pure main: { new A.foo; }
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
            class B4 extends A4 { new { super(new A4(null)); } }
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
            class rolez.lang.Task
            class A {
                var i: int
                def pure foo: {}
                task pure bar: {}
            }
            class App {
                task pure main: {
                    new A.i;
                    new A.foo;
                    new A.bar;
                    new A start bar;
                }
            }
        ''').assertNoErrors
    }
    
    @Test def testMemberAccessNoMember() {
        parse("       5.5.a;".withFrame).assertError(MEMBER_ACCESS, LINKING_DIAGNOSTIC)
        parse("new Object.a;".withFrame).assertError(MEMBER_ACCESS, LINKING_DIAGNOSTIC)
        parse("new Object start a;".withFrame).assertError(MEMBER_ACCESS, LINKING_DIAGNOSTIC)
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                var i: int
            }
            class App {
                task pure main: { new A start i; }
            }
        ''').assertError(MEMBER_ACCESS, LINKING_DIAGNOSTIC)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def foo: {}
            }
            class App {
                task pure main: { new A start foo; }
            }
        ''').assertError(MEMBER_ACCESS, LINKING_DIAGNOSTIC)
    }
    
    @Test def testMemberAccessMethodRoleGeneric() {
        var program = parse('''
            class rolez.lang.Object mapped to java.lang.Object
            pure class rolez.lang.String mapped to java.lang.String
            class StringContainer {
                var s: readwrite String
                def r get[r includes readonly]: r String { return this.s; }
            }
            class App {
                task pure main: {
                    new StringContainer.get[readonly ];
                    new StringContainer.get[readwrite];
                }
            }
        ''')
        program.task.expr(0).type.assertRoleType(ReadOnly , stringClassName)
        program.task.expr(1).type.assertRoleType(ReadWrite, stringClassName)
        
        program = parse('''
            class rolez.lang.Object mapped to java.lang.Object
            pure class rolez.lang.String mapped to java.lang.String
            class StringContainer {
                var s: readwrite String
                def r get[r includes readonly]: r String { return this.s; }
            }
            class StringContainerGetter {
                def pure getFrom[r includes readonly](c: r StringContainer): r String {
                    return c.get[r];
                }
            }
            class App {
                task pure main: {
                    new StringContainerGetter.getFrom[readonly ](new StringContainer);
                    new StringContainerGetter.getFrom[readwrite](new StringContainer);
                }
            }
        ''')
        program.task.expr(0).type.assertRoleType(ReadOnly , stringClassName)
        program.task.expr(1).type.assertRoleType(ReadWrite, stringClassName)
    }
    
    @Test def testMemberAccessMethodRoleInference() {
        var program = parse('''
            class rolez.lang.Object mapped to java.lang.Object
            pure class rolez.lang.String mapped to java.lang.String
            class StringContainer {
                var s: readwrite String
                def r get[r includes readonly]: r String { return this.s; }
            }
            class App {
                task pure main: {
                    new StringContainer.get;
                    (new StringContainer as readonly StringContainer).get;
                }
            }
        ''')
        program.task.expr(0).type.assertRoleType(ReadWrite, stringClassName)
        program.task.expr(1).type.assertRoleType(ReadOnly , stringClassName)
        
        program = parse('''
            class rolez.lang.Object mapped to java.lang.Object
            pure class rolez.lang.String mapped to java.lang.String
            class StringContainer {
                var s: readwrite String
                def r get[r includes readonly]: r String { return this.s; }
            }
            class StringContainerGetter {
                def pure getFrom[r includes readonly](c: r StringContainer): r String {
                    return c.get;
                }
            }
            class App {
                task pure main: {
                    new StringContainerGetter.getFrom(new StringContainer);
                    new StringContainerGetter.getFrom(new StringContainer as readonly StringContainer);
                }
            }
        ''')
        program.task.expr(0).type.assertRoleType(ReadWrite, stringClassName)
        program.task.expr(1).type.assertRoleType(ReadOnly , stringClassName)
        
        program = parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def r better[r includes readonly](other: r A): r A { return null; }
            }
            class App {
                task pure main: {
                    new A.better(new A);
                    new A.better(new A as readonly A);
                    (new A as readonly A).better(new A);
                    (new A as readonly A).better(new A as readonly A);
                }
            }
        ''')
        program.task.expr(0).type.assertRoleType(ReadWrite, "A")
        program.task.expr(1).type.assertRoleType(ReadOnly , "A")
        program.task.expr(2).type.assertRoleType(ReadOnly , "A")
        program.task.expr(3).type.assertRoleType(ReadOnly , "A")
    }
    
    @Test def testMemberAccessMethodWrongNumberOfRoleArgs() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def pure foo[r]: {}
            }
            class App {
                task pure main: { new A.foo[pure, pure]; }
            }
        ''').assertError(MEMBER_ACCESS, LINKING_DIAGNOSTIC)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def pure foo[r1, r2]: {}
            }
            class App {
                task pure main: { new A.foo[pure]; }
            }
        ''').assertError(MEMBER_ACCESS, LINKING_DIAGNOSTIC)
    }
    
    @Test def testMemberAccessOverloading() {
        var program = parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def readwrite foo: int { return 0; }
                def readwrite foo(a: boolean): boolean { return false; }
            }
            class App {
                task pure main: {
                    new A.foo;
                    new A.foo(true);
                }
            }
        ''')
        program.task.expr(0).type.assertThat(instanceOf(Int))
        program.task.expr(1).type.assertThat(instanceOf(Boolean))
        
        program = parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def readwrite foo(a: int): int { return 0; }
                def readwrite foo(a: boolean): boolean { return false; }
            }
            class App {
                task pure main: {
                    new A.foo(4);
                    new A.foo(true);
                }
            }
        ''')
        program.task.expr(0).type.assertThat(instanceOf(Int))
        program.task.expr(1).type.assertThat(instanceOf(Boolean))
        
        program = parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def readwrite foo(a: readwrite A): int { return 0; }
                def readwrite foo(a: readwrite Object): boolean { return false; }
            }
            class App {
                task pure main: {
                    new A.foo(new A);
                    new A.foo(new A as readwrite Object);
                }
            }
        ''')
        program.task.expr(0).type.assertThat(instanceOf(Int))
        program.task.expr(1).type.assertThat(instanceOf(Boolean))
        
        // (Switch order of declaration to rule out accidental selection of the correct one)
        program = parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def readwrite foo(a: readwrite Object): boolean { return false; }
                def readwrite foo(a: readwrite A): int { return 0; }
            }
            class App {
                task pure main: {
                    new A.foo(new A);
                    new A.foo(new A as readwrite Object);
                }
            }
        ''')
        program.task.expr(0).type.assertThat(instanceOf(Int))
        program.task.expr(1).type.assertThat(instanceOf(Boolean))
        
        program = parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def readwrite foo(a: pure Object, b: pure Object): boolean { return false; }
                def readwrite foo(a: pure      A, b: pure      A): int { return 0; }
            }
            class App {
                task pure main: {
                    new A.foo(new A, new A);
                    new A.foo(new A, new A as readwrite Object);
                    new A.foo(new A, new A as readwrite Object);
                    new A.foo(new A as readwrite Object, new A as readwrite Object);
                }
            }
        ''')
        program.task.expr(0).type.assertThat(instanceOf(Int))
        program.task.expr(1).type.assertThat(instanceOf(Boolean))
        program.task.expr(1).type.assertThat(instanceOf(Boolean))
        program.task.expr(1).type.assertThat(instanceOf(Boolean))
        
        program = parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def readwrite foo(a: pure      A, b: pure      A): int { return 0; }
                def readwrite foo(a: pure Object, b: pure Object): boolean { return false; }
            }
            class App {
                task pure main: {
                    new A.foo(new A, new A);
                    new A.foo(new A, new A as readwrite Object);
                    new A.foo(new A, new A as readwrite Object);
                    new A.foo(new A as readwrite Object, new A as readwrite Object);
                }
            }
        ''')
        program.task.expr(0).type.assertThat(instanceOf(Int))
        program.task.expr(1).type.assertThat(instanceOf(Boolean))
        program.task.expr(1).type.assertThat(instanceOf(Boolean))
        program.task.expr(1).type.assertThat(instanceOf(Boolean))
        
        // The following would be ambiguous for a method call, but not for a task start:
        program = parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Task
            class A {
                def  readwrite foo(a: pure      A, b: pure Object): int { return 0; }
                task readwrite foo(a: pure Object, b: pure      A): boolean { return false; }
            }
            class App {
                task pure main: {
                    new A start foo(new A, new A);
                }
            }
        ''')
        program.classes.map[methods].flatten.filter[isMain].head.lastExpr.type
            .assertRoleType(Pure, taskClassName, Boolean)
        
        // TODO: test generic methods
    }
    
    @Test def testMemberAccessFieldAndMethod() {
        var program = parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                var foo: int
                def readwrite foo: boolean { return false; }
                def readwrite foo(a: boolean): boolean { return false; }
            }
            class App {
                task pure main: {
                    new A.foo;
                    new A.foo();
                    new A.foo[];
                    new A.foo(true);
                }
            }
        ''')
        program.task.expr(0).type.assertThat(instanceOf(Int))
        program.task.expr(1).type.assertThat(instanceOf(Boolean))
        program.task.expr(2).type.assertThat(instanceOf(Boolean))
        program.task.expr(3).type.assertThat(instanceOf(Boolean))
    }
    
    @Test def testMemberAccessMethodFromSuperclass() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A
            class B {
                def readwrite foo(a: readonly A, b: readwrite B, c: readwrite C, d: int): {}
            }
            class C extends B
            class App {
                task pure main: { new C.foo(new A, new C, null, 5); }
            }
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
            class App {
                task pure main: { new B.foo; }
            }
        ''').task.lastExpr as MemberAccess).method.enclosingClass.name.assertThat(is("B"))
     }
    
    @Test def testMemberAccessMethodTypeMismatch() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A { def readwrite foo: {} }
            class App {
                task pure main: { new A.foo(5); }
            }
        ''').assertError(MEMBER_ACCESS, LINKING_DIAGNOSTIC, "foo")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A { def readwrite foo(c: char): {} }
            class App {
                task pure main: { new A.foo(5, false); }
            }
        ''').assertError(MEMBER_ACCESS, LINKING_DIAGNOSTIC, "foo")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A { def readwrite foo(i: int): {} }
            class App {
                task pure main: { new A.foo(); }
            }
        ''').assertError(MEMBER_ACCESS, LINKING_DIAGNOSTIC, "foo")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A { def readwrite foo(i: int, a: readwrite A): {} }
            class App {
                task pure main: { new A.foo(false); }
            }
        ''').assertError(MEMBER_ACCESS, LINKING_DIAGNOSTIC, "foo")
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A { def readwrite foo(i: int): {} }
            class App {
                task pure main: { new A.foo(false); }
            }
        ''').assertError(MEMBER_ACCESS, LINKING_DIAGNOSTIC, "foo")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A { def readwrite foo(a: readwrite A): {} }
            class App {
                task pure main: { new A.foo(new Object); }
            }
        ''').assertError(MEMBER_ACCESS, LINKING_DIAGNOSTIC, "foo")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A { def readwrite foo(a: readwrite A): {} }
            class App {
                task pure main: { new A.foo(new A as readonly A); }
            }
        ''').assertError(MEMBER_ACCESS, LINKING_DIAGNOSTIC, "foo")
    }
    
    @Test def testMemberAccessMethodTypeMismatchGeneric() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Array[T] mapped to rolez.lang.Array {
                mapped new(i: int)
                mapped def readwrite set(i: int, o: T):
            }
            class App {
                task pure main: { new Array[int](1).set(0, true); }
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
            class App {
                task pure main: { new Array[pure A](1).set(0, new B); }
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
            class App {
                task pure main: { new A.foo(new A, new A); }
            }
        ''').assertError(MEMBER_ACCESS, AMBIGUOUS_CALL)
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def readwrite foo(a: readwrite Object, b: readwrite A): {}
                def readwrite foo(a: readwrite A, b: readwrite Object): {}
            }
            class App {
                task pure main: { new A.foo(new A, new A); }
            }
        ''').assertError(MEMBER_ACCESS, AMBIGUOUS_CALL)
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def  readwrite foo(a: readwrite Object, b: readwrite A): {}
                task readwrite foo(a: readwrite A, b: readwrite Object): {}
            }
            class App {
                task pure main: { new A.foo(new A, new A); }
            }
        ''').assertError(MEMBER_ACCESS, AMBIGUOUS_CALL)
        
        // TODO: test generic methods
    }
    
    @Test def testNew() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A
            class B { new {} }
            class C { new(i: int) {} }
            class D { new(a: readonly A, b: readwrite B) {} }
            class App {
                task pure main: {
                    new A;
                    new B;
                    new C(0);
                    new D(new A, new B);
                }
            }
        ''').assertNoErrors
    }
    
    @Test def testNewGeneric() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Array[T] mapped to rolez.lang.Array {
                mapped new(length: int)
            }
            class App {
                task pure main: {
                    new Array[int](10);
                    new Array[readonly Object](0);
                }
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
            class App {
                task pure main: {
                    new A(4);
                    new A(true);
                }
            }
        ''')
        (program.task.expr(0) as New).constr.params.head.type.assertThat(instanceOf(Int))
        (program.task.expr(1) as New).constr.params.head.type.assertThat(instanceOf(Boolean))
        
        program = parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A
            class B {
                new(a: readwrite Object) {}
                new(a: readwrite A) {}
            }
            class App {
                task pure main: {
                    new B(new A);
                    new B(new A as readwrite Object);
                }
            }
        ''')
        ((program.task.expr(0) as New).constr.params.head.type as RoleType).base.clazz.name.assertThat(is("A"))
        ((program.task.expr(1) as New).constr.params.head.type as RoleType).base.clazz.name.assertThat(is(objectClassName.toString))
        
        // (Switch order of declaration to rule out accidental selection of the correct one)
        program = parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A
            class B {
                new(a: readwrite A) {}
                new(a: readwrite Object) {}
            }
            class App {
                task pure main: {
                    new B(new A);
                    new B(new A as readwrite Object);
                }
            }
        ''')
        ((program.task.expr(0) as New).constr.params.head.type as RoleType).base.clazz.name.assertThat(is("A"))
        ((program.task.expr(1) as New).constr.params.head.type as RoleType).base.clazz.name.assertThat(is(objectClassName.toString))
        
        // IMPROVE: test generic constructors
    }
    
    @Test def testNewTypeMismatch() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A
            class App {
                task pure main: { new A(5); }
            }
        ''').assertError(NEW, LINKING_DIAGNOSTIC)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A { new {} }
            class App {
                task pure main: { new A(5); }
            }
        ''').assertError(NEW, LINKING_DIAGNOSTIC)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A { new(c: char) {} }
            class App {
                task pure main: { new A(5, false); }
            }
        ''').assertError(NEW, LINKING_DIAGNOSTIC)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A { new(i: int) {} }
            class App {
                task pure main: { new A; }
            }
        ''').assertError(NEW, LINKING_DIAGNOSTIC)
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A { new(i: int) {} }
            class App {
                task pure main: { new A(false); }
            }
        ''').assertError(NEW, LINKING_DIAGNOSTIC)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A { new(a: readwrite A) {} }
            class App {
                task pure main: { new A(new Object); }
            }
        ''').assertError(NEW, LINKING_DIAGNOSTIC)
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A { new(o: readwrite Object) {} }
            class App {
                task pure main: { new A(new Object as readonly Object); }
            }
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
            class App {
                task pure main: { new B(new A, new A); }
            }
        ''').assertError(NEW, AMBIGUOUS_CALL)
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A
            class B {
                new(a: readwrite Object, b: readwrite A) {}
                new(a: readwrite A, b: readwrite Object) {}
            }
            class App {
                task pure main: { new B(new A, new A); }
            }
        ''').assertError(NEW, AMBIGUOUS_CALL)
        
        // TODO: test generic constructors
    }
    
    @Test def testNewObjectClassRef() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            object A
            class App {
                task pure main: { new A; }
            }
        ''').assertError(NEW, LINKING_DIAGNOSTIC)
    }
    
    @Test def testRef() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            object i
            class A {
                def pure foo(i: int): {
                    i;
                    val j = 5;
                    j;
                    
                    for(var k = 0; k < 10; k++)
                        k;
                    
                    for(var k = 0; k < 10; k++) {
                        k;
                    }
                    
                    i;
                    j;
                }
            }
        ''').assertNoErrors
        
        var program = parse('''
            class rolez.lang.Object mapped to java.lang.Object
            object i
            class A {
                def pure foo: {
                    var i = 0;
                    i;
                }
            }
        ''')
        program.assertNoErrors
        program.classes.filter[name == "A"].head.methods.head.firstExpr as Ref => [
            assertTrue(isVarRef)
        ]
        
        program = parse('''
            class rolez.lang.Object mapped to java.lang.Object
            object i
            class A {
                def pure foo(i: int): {
                    i;
                }
            }
        ''')
        program.assertNoErrors
        program.classes.filter[name == "A"].head.methods.head.firstExpr as Ref => [
            assertTrue(isVarRef)
        ]
        
        program = parse('''
            class rolez.lang.Object mapped to java.lang.Object
            object i
            class A {
                def pure foo: {
                    i;
                }
            }
        ''')
        program.assertNoErrors
        program.classes.filter[name == "A"].head.methods.head.firstExpr as Ref => [
            assertTrue(isSingletonRef)
        ]
        
        parse('''
            i;
            val i = 0;
        '''.withFrame).assertError(REF, LINKING_DIAGNOSTIC, "var", "i")
        parse('''
            {
                val i = 0;
            }
            i;
        '''.withFrame).assertError(REF, LINKING_DIAGNOSTIC, "var", "i")
        
        parse('''
            for(var i = i; true; true) {}
        '''.withFrame).assertError(REF, LINKING_DIAGNOSTIC, "var", "i")
        parse('''
            for(var i = 0; true; true) {}
            i;
        '''.withFrame).assertError(REF, LINKING_DIAGNOSTIC, "var", "i")
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
    
    @Test def testClassOrTypeParam() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class GenericClass[T] mapped to «GenericClass.canonicalName» {
                mapped new(t: T)
            }
        ''').assertNoErrors
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class T {
                new(t: T) {}
            }
        ''').assertNoErrors
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class T
            class GenericClass[T] mapped to «GenericClass.canonicalName» {
                mapped new(t: T)
            }
        ''').classes.filter[name == "GenericClass"].filter(NormalClass)
                .head.constrs.head.params.head.type.assertInstanceOf(TypeParamRef)
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class T {
                new(t: T)
            }
        ''').classes.filter[name == "T"].filter(NormalClass)
                .head.constrs.head.params.head.type.assertInstanceOf(RoleType)
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class T
            class GenericClass[T] mapped to «GenericClass.canonicalName» {
                mapped new(t: T)
            }
        ''').classes.filter[name == "GenericClass"].filter(NormalClass)
                .head.constrs.head.params.head.type.assertInstanceOf(TypeParamRef)
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class GenericClass[T] mapped to «GenericClass.canonicalName» {
                mapped new(t: T)
            }
            class A {
                def pure foo: T { return null; }
            }
        ''').assertError(ROLE_TYPE_OR_TYPE_PARAM_REF, LINKING_DIAGNOSTIC)
    }
    
    @Test def testRoleParam() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def r getThis[r]: r A { return this; }
            }
        ''').assertNoErrors
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def r getThis[r]: r A { return this; }
                def r getThat   : r A { return this; }
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
            pure class rolez.lang.String mapped to java.lang.String
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
            pure class rolez.lang.String mapped to java.lang.String {
                mapped new(s: readonly String)
            }
        ''').assertNoErrors
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object {
                mapped new(i: int)
            }
        ''').assertError(CONSTR, LINKING_DIAGNOSTIC)
        
        // Mapped classes can have no constr
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class NoNoArgConstr mapped to «NoNoArgConstr.canonicalName»
        ''').assertNoErrors
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class NoNoArgConstr mapped to «NoNoArgConstr.canonicalName»
            class A {
                task pure main: { new NoNoArgConstr; }
            }
        ''').assertError(NEW, LINKING_DIAGNOSTIC)
    }
    
    static class NoNoArgConstr {
        new(int i) {}
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

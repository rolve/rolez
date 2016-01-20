package ch.trick17.rolez

import ch.trick17.rolez.rolez.Program
import ch.trick17.rolez.tests.RolezInjectorProvider
import javax.inject.Inject
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.eclipse.xtext.junit4.validation.ValidationTestHelper
import org.junit.Test
import org.junit.runner.RunWith

import static ch.trick17.rolez.rolez.RolezPackage.Literals.*
import static org.eclipse.xtext.diagnostics.Diagnostic.*

@RunWith(XtextRunner)
@InjectWith(RolezInjectorProvider)
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
    
    @Test def testNewClassRef() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            object A
            task Main: {
                new A;
            }
        ''').assertError(NEW, LINKING_DIAGNOSTIC)
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

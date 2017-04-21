package ch.trick17.rolez.validation

import ch.trick17.rolez.TestUtils
import ch.trick17.rolez.rolez.Program
import ch.trick17.rolez.tests.RolezInjectorProvider
import javax.inject.Inject
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.eclipse.xtext.junit4.validation.ValidationTestHelper
import org.junit.Test
import org.junit.runner.RunWith
import rolez.lang.Guarded

import static ch.trick17.rolez.rolez.RolezPackage.Literals.*
import static ch.trick17.rolez.validation.RolezValidator.*

@RunWith(XtextRunner)
@InjectWith(RolezInjectorProvider)
class RolezValidatorTest {
    
    @Inject extension ParseHelper<Program>
    @Inject extension ValidationTestHelper
    @Inject extension TestUtils
    
    @Test def testCircularInheritance() {
        parse('''
            class A extends A
        ''').assertError(CLASS, CIRCULAR_INHERITANCE)
        parse('''
            class A extends B
            class B extends A
        ''').assertError(CLASS, CIRCULAR_INHERITANCE)
        parse('''
            class A extends B
            class B extends C
            class C extends D
            class D extends A
        ''').assertError(CLASS, CIRCULAR_INHERITANCE)
    }
    
    @Test def testSingletonSuperclass() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A extends Object
        ''').assertNoErrors
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            object A
            class B extends A
        ''').assertError(CLASS, SINGLETON_SUPERCLASS)
    }
    
    @Test def testPureSuperclass() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            
            pure class A
            pure class B extends A
            object C extends B
            
            class D
            class E extends D
            object F extends E
        ''').assertNoErrors
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A extends Object
            pure class B extends A
        ''').assertError(CLASS, INCORRECT_SUPERCLASS_PURITY)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            pure class A
            class B extends A
        ''').assertError(CLASS, INCORRECT_SUPERCLASS_PURITY)
    }
    
    @Test def testTypeParam() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A[T]
        ''').assertError(CLASS, INCORRECT_TYPE_PARAM)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.String[S] mapped to java.lang.String
        ''').assertError(TYPE_PARAM, INCORRECT_TYPE_PARAM)
    }
    
    @Test def testValidOverride() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {                def readwrite foo: readwrite A { return new A; } }
            class B extends A { override readwrite foo: readwrite A { return new A; } }
        ''').assertNoErrors
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {               task readwrite foo: readwrite A { return new A; } }
            class B extends A { override readwrite foo: readwrite B { return new B; } }
        ''').assertNoErrors
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {                def readwrite foo: readonly  A { return new A; } }
            class B extends A { override readwrite foo: readwrite A { return new A; } }
        ''').assertNoErrors
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {                def readwrite foo: {} }
            class B extends A { override readonly  foo: {} }
        ''').assertNoErrors
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {               task pure foo(o: readwrite Object): {} }
            class B extends A { override pure foo(o: readwrite Object): {} }
        ''').assertNoErrors
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {                def pure foo(o: readwrite Object): {} }
            class B extends A { override pure foo(o: readonly  Object): {} }
        ''').assertNoErrors
    }
    
    @Test def testValidOverrideGeneric() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class GenericClass[T] mapped to «GenericClass.canonicalName» {
                mapped new(t: T)
                mapped def pure foo: T
            }
            class A extends GenericClass[readwrite Object] {
                new { super(null); }
                override pure foo: readwrite Object { return null; }
            }
        ''').assertNoErrors
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class GenericClass[T] mapped to «GenericClass.canonicalName» {
                mapped new(t: T)
                mapped def pure foo: T
            }
            class A extends GenericClass[readwrite Object] {
                new { super(null); }
                override pure foo: readwrite A { return null; }
            }
        ''').assertNoErrors
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class GenericClass[T] mapped to «GenericClass.canonicalName» {
                mapped new(t: T)
                mapped def pure foo: T
            }
            class A extends GenericClass[readonly Object] {
                new { super(null); }
                override pure foo: readwrite Object { return null; }
            }
        ''').assertNoErrors
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class GenericClass[T] mapped to «GenericClass.canonicalName» {
                mapped new(t: T)
                mapped def pure foo(t: T):
            }
            class A extends GenericClass[readwrite Object] {
                new { super(null); }
                override pure foo(t: readwrite Object): {}
            }
        ''').assertNoErrors
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class GenericClass[T] mapped to «GenericClass.canonicalName» {
                mapped new(t: T)
                mapped def pure foo(t: T):
            }
            class A extends GenericClass[readwrite Object] {
                new { super(null); }
                override pure foo(t: readonly Object): {}
            }
        ''').assertNoErrors
    }
    
    @Test def testValidOverrideRoleParams() {
        val lib = newResourceSet.with('''
            class rolez.lang.Object mapped to java.lang.Object
            pure class rolez.lang.String mapped to java.lang.String
            class Container[E] mapped to «Container.canonicalName» {
                mapped var e: E
                mapped def r get[r includes readonly]: E with r
                mapped def readwrite set(e: E):
            }
        ''')
        
        parse('''
            class IntContainer extends Container[int] {
                override readonly get: int { return this.get; }
            }
        ''', lib).assertNoErrors
        
        parse('''
            class StringContainer extends Container[readonly String] {
                override readonly get: readonly String { return this.get; }
            }
        ''', lib).assertNoErrors
        parse('''
            class StringContainer extends Container[readwrite String] {
                override r get[r includes readonly]: r String { return this.get; }
            }
        ''', lib).assertNoErrors
    }
    
    static class Container<E> extends Guarded {
        public var E e = null
        new() {}
        new(E e) { this.e = e }
        def E get() { e }
        def void set(E e) { this.e = e }
    }
    
    @Test def testValidOverrideIncompatibleReturnType() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {                def pure foo: int    {} }
            class B extends A { override pure foo: double {} }
        ''').assertError(METHOD, INCOMPATIBLE_RETURN_TYPE)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {               task pure foo: pure B {} }
            class B extends A { override pure foo: pure A {} }
        ''').assertError(METHOD, INCOMPATIBLE_RETURN_TYPE)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {                def pure foo: readwrite Object {} }
            class B extends A { override pure foo: readonly  Object {} }
        ''').assertError(METHOD, INCOMPATIBLE_RETURN_TYPE)
    }
    
    @Test def testValidOverrideIncompatibleThisRole() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {                def readonly  foo: {} }
            class B extends A { override readwrite foo: {} }
        ''').assertError(METHOD, INCOMPATIBLE_THIS_ROLE)
    }
    
    @Test def testValidOverrideIncompatibleParamType() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {                def pure foo(o: readonly  Object): {} }
            class B extends A { override pure foo(o: readwrite Object): {} }
        ''').assertError(PARAM, INCOMPATIBLE_PARAM_TYPE)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Array[T] mapped to rolez.lang.Array {
                mapped new(length: int)
            }
            class A {                def pure foo(a: pure Array[readwrite Object]): {} }
            class B extends A { override pure foo(a: pure Array[readonly  Object]): {} }
        ''').assertError(PARAM, INCOMPATIBLE_PARAM_TYPE)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Array[T] mapped to rolez.lang.Array {
                mapped new(length: int)
            }
            class A {                def pure foo(a: pure Array[readonly  Object]): {} }
            class B extends A { override pure foo(a: pure Array[readwrite Object]): {} }
        ''').assertError(PARAM, INCOMPATIBLE_PARAM_TYPE)
    }
    
    @Test def testMissingOverride() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {           def readwrite foo: {} }
            class B extends A { def readwrite foo: {} }
        ''').assertError(METHOD, MISSING_OVERRIDE)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {           def readwrite foo: int  {} }
            class B extends A { def readwrite foo: {} }
        ''').assertError(METHOD, MISSING_OVERRIDE)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {           def readwrite foo(i: int): {} }
            class B extends A { def readwrite foo(i: int): {} }
        ''').assertError(METHOD, MISSING_OVERRIDE)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {          task readwrite foo(i: int): {} }
            class B extends A { def readwrite foo(j: int): {} }
        ''').assertError(METHOD, MISSING_OVERRIDE)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {           task readwrite foo(a: readwrite A): {} }
            class B extends A { task readwrite foo(a: readwrite A): {} }
        ''').assertError(METHOD, MISSING_OVERRIDE)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {            def readwrite foo(a: readwrite A): {} }
            class B extends A { task readwrite foo(b: readwrite A): {} }
        ''').assertError(METHOD, MISSING_OVERRIDE)
    }
    
    @Test def testMissingOverrideGeneric() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class GenericClass[T] mapped to «GenericClass.canonicalName» {
                mapped new(t: T)
                mapped def pure foo(t: T):
            }
            class A extends GenericClass[int] {
                new(i: int) { super(i); }
                def pure foo(i: int): {}
            }
        ''').assertError(METHOD, MISSING_OVERRIDE)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class GenericClass[T] mapped to «GenericClass.canonicalName» {
                mapped new(t: T)
                mapped def pure foo(t: T):
            }
            class A extends GenericClass[readwrite Object] {
                new(o: readwrite Object) { super(o); }
                def pure foo(o: readwrite Object): {}
            }
        ''').assertError(METHOD, MISSING_OVERRIDE)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class GenericClass[T] mapped to «GenericClass.canonicalName» {
                mapped new(t: T)
                mapped def pure foo(t1: T, t2: T):
            }
            class A extends GenericClass[readwrite Object] {
                new(o: readwrite Object) { super(o); }
                def pure foo(t1: readwrite Object, t2: readwrite Object): {}
            }
        ''').assertError(METHOD, MISSING_OVERRIDE)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class GenericClass[T] mapped to «GenericClass.canonicalName» {
                mapped new(t: T)
                mapped def pure foo: T
            }
            class A extends GenericClass[readwrite Object] {
                new(o: readwrite Object) { super(o); }
                def pure foo: readonly Object { return null; }
            }
        ''').assertError(METHOD, MISSING_OVERRIDE)
    }
    
    @Test def testMappedAsync() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                async def pure foo: {}
            }
        ''').assertNoErrors
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class MyString mapped to java.lang.String {
                mapped async def pure length: int
            }
        ''').assertError(METHOD, MAPPED_ASYNC)
    }
    
    static class GenericClass<T> {
        new(T t) {}
        new(T t, int i) {}
        def void foo(T t) {}
        def void foo(T t1, T t2) {}
        def T foo() { null }
    }
    
    @Test def testReturn() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def pure a: {}
                def pure b: {
                    return;
                }
                def pure c(i: int): {
                    if(i == 0)
                        return;
                    else
                        return;
                }
                def pure d(i: int): {
                    if(i == 0)
                        return;
                    return;
                }
                def pure e(i: int): {
                    if(i == 0) {}
                    else
                        return;
                    return;
                }
                
                def pure f: int {
                    return 0;
                }
                def pure g(i: int): int {
                    if(i == 0)
                        return 0;
                    else
                        return 1;
                }
                def pure h(i: int): int {
                    if(i == 0)
                        return 0;
                    return 1;
                }
                def pure i(i: int): int {
                    if(i == 0) {}
                    else
                        return 0;
                    return 1;
                }
                
                task pure foo: int { return 3; }
                task pure bar: {}
                task pure baz: { return; }
            }
        ''').assertNoErrors
    }
    
    @Test def testMissingReturnExpr() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def pure a: int {}
            }
        ''').assertError(BLOCK, MISSING_RETURN_EXPR)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def pure a: int {
                    return;
                }
            }
        ''').assertError(RETURN_NOTHING, MISSING_RETURN_EXPR)
        val program = parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def pure a: int {
                    if(1 == 0)
                        return;
                }
            }
        ''')
        program.assertError(RETURN_NOTHING, MISSING_RETURN_EXPR)
        program.assertError(IF_STMT, MISSING_RETURN_EXPR)
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def pure a(i: int): int {
                    if(i == 0)
                        return 0;
                }
            }
        ''').assertError(IF_STMT, MISSING_RETURN_EXPR)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def pure a(i: int): int {
                    1;
                    if(i == 0)
                        return 0;
                }
            }
        ''').assertError(IF_STMT, MISSING_RETURN_EXPR)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def pure a(i: int): int {
                    if(i == 0) {}
                    else
                        return 0;
                }
            }
        ''').assertError(BLOCK, MISSING_RETURN_EXPR)
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                task pure foo: int {}
            }
        ''').assertError(BLOCK, MISSING_RETURN_EXPR)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                task pure foo: int { return; }
            }
        ''').assertError(RETURN_NOTHING, MISSING_RETURN_EXPR)
    }
    
    @Test def testIncorrectReturn() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                new {
                    return 4;
                }
            }
        ''').assertError(RETURN_EXPR, null, "cannot return", "constructor")
    }
    
    @Test def testDuplicateFields() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                var a: int
                val a: boolean
            }
        ''').assertError(FIELD, DUPLICATE_FIELD)
    }
    
    @Test def testMethodOverloading() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Array[T] mapped to rolez.lang.Array {
                mapped new(length: int)
            }
            class A {
                def pure foo: {}
                def pure foo(i: int): {}
                def pure foo(c: char): {}
                def pure foo(o: readonly Object): {}
                def pure foo(a: readonly A): {}
                def pure foo(a: readwrite A, b: readwrite A): {}
                def pure foo(a: pure Array[int]): {}
            }
            class B {
                task pure foo: {}
                task pure foo(i: int): {}
                task pure foo(c: char): {}
                task pure foo(o: readonly Object): {}
                task pure foo(a: readonly A): {}
                task pure foo(a: readwrite A, b: readwrite A): {}
                task pure foo(a: pure Array[int]): {}
            }
            class C {
                def  pure foo: {}
                task pure foo(i: int): {}
                def  pure foo(c: char): {}
                task pure foo(o: readonly Object): {}
                def  pure foo(a: readonly A): {}
                task pure foo(a: readwrite A, b: readwrite A): {}
                def  pure foo(a: pure Array[int]): {}
            }
        ''').assertNoErrors
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def readwrite foo(a: readwrite A): {}
            }
            class B extends A {
                def readwrite foo(a: readwrite B): {}
                def readwrite foo(a: readwrite Object): {}
            }
            class C extends B {
                def readwrite foo(i: int): {}
            }
            class D extends C {
                def readwrite foo(i: char): int { return 0; }
            }
            class E extends D {
                def readwrite foo(i: int, j: int): readonly A {
                    return new A;
                }
            }
        ''').assertNoErrors
    }
    
    @Test def testDuplicateMethods() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def readwrite foo: {}
                def readwrite foo: {}
            }
        ''').assertError(METHOD, DUPLICATE_METHOD)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                task readwrite foo: {}
                task readwrite foo: {}
            }
        ''').assertError(METHOD, DUPLICATE_METHOD)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def  readwrite foo: {}
                task readwrite foo: {}
            }
        ''').assertError(METHOD, DUPLICATE_METHOD)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def readwrite foo: int {}
                def readwrite foo: {}
            }
        ''').assertError(METHOD, DUPLICATE_METHOD)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def readonly  foo: int {}
                def readwrite foo: {}
            }
        ''').assertError(METHOD, DUPLICATE_METHOD)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def readwrite foo(i: int): {}
                def readwrite foo(i: int): {}
            }
        ''').assertError(METHOD, DUPLICATE_METHOD)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def readwrite foo(i: int): {}
                def readwrite foo(j: int): {}
            }
        ''').assertError(METHOD, DUPLICATE_METHOD)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def readwrite foo(a: readwrite A): {}
                def readwrite foo(a: readwrite A): {}
            }
        ''').assertError(METHOD, DUPLICATE_METHOD)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def readwrite foo(a: readwrite A): {}
                def readwrite foo(b: readonly  A): {}
            }
        ''').assertError(METHOD, DUPLICATE_METHOD)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Array[T] mapped to rolez.lang.Array {
                mapped new(length: int)
            }
            class A {
                def readwrite foo(a: readwrite Array[int]): {}
                def readwrite foo(b: readonly  Array[double]): {}
            }
        ''').assertError(METHOD, DUPLICATE_METHOD)
    }
    
    @Test def testFieldWithSameName() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                var foo: int
                def readwrite foo(i: int): {}
                def readwrite foo(c: char): {}
                def readwrite foo(o: readonly Object): {}
                task readwrite foo(a: readonly A): {}
                task readwrite foo(a: readwrite A, b: readwrite A): {}
            }
        ''').assertNoErrors
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                var foo: int
                def readwrite foo: {}
            }
        ''').assertError(METHOD, FIELD_WITH_SAME_NAME)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                var foo: int
            }
            class B extends A {
                def readwrite foo: {}
            }
        ''').assertError(METHOD, FIELD_WITH_SAME_NAME)
    }
    
    @Test def testMainTask() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class App {
                task pure main: {}
            }
        ''').assertNoErrors
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Array[T] mapped to rolez.lang.Array {
                mapped new(i: int)
            }
            pure class rolez.lang.String mapped to java.lang.String
            class App {
                task pure main(args: readonly Array[readonly String]): {}
            }
        ''').assertNoErrors
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class App {
                def pure main: {}
            }
        ''').assertError(METHOD, INCORRECT_MAIN)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class App {
                task pure main: int { return 0; }
            }
        ''').assertError(INT, INCORRECT_MAIN)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class App {
                task pure main(i: int): {}
            }
        ''').assertError(INT, INCORRECT_MAIN)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Array[T] mapped to rolez.lang.Array
            class rolez.lang.String mapped to java.lang.String
            class App {
                task pure main(args: readwrite Array[readonly String]): {}
            }
        ''').assertError(ROLE_TYPE, INCORRECT_MAIN)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class App {
                task pure main(i: int, j: int): {}
            }
        ''').assertError(METHOD, INCORRECT_MAIN)
    }
    
    @Test def testMainClassInstantiable() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class App1 {
                task pure main: {}
            }
            class App2 {
                new {}
                task pure main: {}
            }
            object App3 {
                task pure main: {}
            }
        ''').assertNoErrors
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class App {
                new(i: int) {}
                task pure main: {}
            }
        ''').assertError(CLASS, INCORRECT_MAIN_CLASS)
        
        // TODO: Test tasks in mapped classes, both normal and singleton
    }
    
    @Test def testConstrOverloading() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A
            class B {
                new {}
                new(i: int) {}
                new(c: char) {}
                new(o: readwrite Object) {}
                new(a: readwrite A) {}
                new(a: readwrite A, b: readwrite A) {}
            }
        ''').assertNoErrors
    }
    
    @Test def testDuplicateConstrs() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                new {}
                new {}
            }
        ''').assertError(CONSTR, DUPLICATE_CONSTR)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                new(i: int) {}
                new(j: int) {}
            }
        ''').assertError(CONSTR, DUPLICATE_CONSTR)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                new(a: readwrite A) {}
                new(b: readwrite A) {}
            }
        ''').assertError(CONSTR, DUPLICATE_CONSTR)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                new(a: readonly  A) {}
                new(b: readwrite A) {}
            }
        ''').assertError(CONSTR, DUPLICATE_CONSTR)
    }
    
    @Test def testDuplicateLocalVars() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def readwrite foo(i: int, b: boolean): {
                    val j = 0;
                    var a: boolean;
                    {
                        var k = 42;
                    }
                    {
                        var k = 0;
                    }
                    for(var k = 0; true; true) {}
                }
            }
        ''').assertNoErrors
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def readwrite foo(a: int, a: boolean): {}
            }
        ''').assertError(PARAM, DUPLICATE_VAR)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                new(a: int, a: boolean) {}
            }
        ''').assertError(PARAM, DUPLICATE_VAR)
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def readwrite foo: {
                    var a: int;
                    val a: boolean = false;
                }
            }
        ''').assertError(LOCAL_VAR, DUPLICATE_VAR)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                new {
                    var a: int;
                    val a: boolean = true;
                }
            }
        ''').assertError(LOCAL_VAR, DUPLICATE_VAR)
        parse('''
            val i = 5;
            {
                val i = true;
                i;
            }
            i;
        '''.withFrame).assertError(LOCAL_VAR, DUPLICATE_VAR)
        
        parse('''
            var k;
            for(var k = 0; true; true) {}
        '''.withFrame).assertError(LOCAL_VAR, DUPLICATE_VAR)
        parse('''
            for(var k = 0; true; true) {
                var k;
            }
        '''.withFrame).assertError(LOCAL_VAR, DUPLICATE_VAR)
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def readwrite foo(a: int): {
                    var a: boolean;
                }
            }
        ''').assertError(LOCAL_VAR, DUPLICATE_VAR)
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                new(a: int) {
                    var a: boolean;
                }
            }
        ''').assertError(LOCAL_VAR, DUPLICATE_VAR)
    }
    
    @Test def testTypeArg() {
        parse('''
            val a1: pure Array[int] = new Array[int](42);
            var a2: readonly Array[readwrite Array[pure A]];
            var a3: readwrite A;
        '''.withFrame).assertNoErrors
        
        parse("var array: pure Array;".withFrame).assertError(SIMPLE_CLASS_REF, MISSING_TYPE_ARG)
        
        parse("val array: pure A[int] = null;".withFrame)
            .assertError(GENERIC_CLASS_REF, INCORRECT_TYPE_ARG, "class A")
        parse("val a: pure A = new A[int];".withFrame)
            .assertError(GENERIC_CLASS_REF, INCORRECT_TYPE_ARG, "class A")
        parse("val a: pure A[readwrite A];".withFrame)
            .assertError(GENERIC_CLASS_REF, INCORRECT_TYPE_ARG, "class A")
    }
    
    @Test def testValFieldsInitialized() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                var i: int
                val j: int = 0
            }
        ''').assertNoErrors
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            pure class rolez.lang.String mapped to java.lang.String
            class rolez.io.PrintStream mapped to java.io.PrintStream {
                mapped new(s: pure String)
            }
            object rolez.lang.System mapped to java.lang.System {
                mapped val out: readonly rolez.io.PrintStream
            }
        ''').assertNoErrors
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                val i: int
                var j: int
                val k: int = 0
                new {
                    this.i = 3 + this.k;
                    3 + this.i;
                }
                new(b: boolean, i: int) {
                    if(b) this.i = i;
                    else  this.i = 0;
                    3 + this.i;
                }
                new(a: pure A) {
                    this.i = 0;
                    while(this.foo)
                        new A;
                }
               def pure foo: boolean { return false; }
            }
        ''').assertNoErrors
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            object A {
                val a: int = 0
            }
            class B {
                new {
                    the A.a;
                }
            }
        ''').assertNoErrors
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                val i: int
            }
        ''').assertError(CONSTR, VAL_FIELD_NOT_INITIALIZED)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            object A {
                val i: int
            }
        ''').assertError(FIELD, VAL_FIELD_NOT_INITIALIZED)
        var program = parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                val i: int
                val j: int
                new {}
            }
        ''')
        program.assertError(CONSTR, VAL_FIELD_NOT_INITIALIZED, "field i")
        program.assertError(CONSTR, VAL_FIELD_NOT_INITIALIZED, "field j")
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                val i: int
                new(b: boolean) {
                    if(b) this.i = 0;
                }
            }
        ''').assertError(CONSTR, VAL_FIELD_NOT_INITIALIZED)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                val i: int
                new(a: boolean, b: boolean) {
                    if(a) this.i = 2;
                    if(b) this.i = 0;
                }
            }
        ''').assertError(CONSTR, VAL_FIELD_NOT_INITIALIZED)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                val i: int
                new(a: boolean, b: boolean) {
                    3 + this.i;
                    this.i = 0;
                }
            }
        ''').assertError(MEMBER_ACCESS, VAL_FIELD_NOT_INITIALIZED)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                val i: int
                new(a: boolean, b: boolean) {
                    this.i++;
                }
            }
        ''').assertError(MEMBER_ACCESS, VAL_FIELD_NOT_INITIALIZED)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                val i: int
                new(a: boolean, b: boolean) {
                    --this.i;
                }
            }
        ''').assertError(MEMBER_ACCESS, VAL_FIELD_NOT_INITIALIZED)
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                val x: int
                new {
                    this.x = 3;
                    this.x = 4;
                }
            }
        ''').assertError(ASSIGNMENT, VAL_FIELD_OVERINITIALIZED)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                val x: int = 0
                new {
                    this.x = 3;
                }
            }
        ''').assertError(ASSIGNMENT, VAL_FIELD_OVERINITIALIZED)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                val x: int
                new(b: boolean) {
                    if(b)
                        this.x = 3;
                    this.x = 4;
                }
            }
        ''').assertError(ASSIGNMENT, VAL_FIELD_OVERINITIALIZED)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                val x: int
                new(b: boolean) {
                    this.x = 3;
                    this.x++;
                }
            }
        ''').assertError(ARITHMETIC_UNARY_EXPR, VAL_FIELD_OVERINITIALIZED)
        program = parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                val x: int
                new(b: boolean) {
                    while(b)
                        this.x = 3;
                }
            }
        ''')
        program.assertError(ASSIGNMENT, VAL_FIELD_OVERINITIALIZED)
        program.assertError(CONSTR, VAL_FIELD_NOT_INITIALIZED)
    }
    
    @Test def testFieldInitializer() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object {
                mapped def readonly toString: pure String
            }
            pure class rolez.lang.String mapped to java.lang.String {
                mapped def pure length: int
            }
            object rolez.lang.System mapped to java.lang.System
            class A {
                val foo: int = 5
                var bar: int = "Hello".length
                val baz: pure String = the System.toString
            }
        ''').assertNoErrors
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            pure class rolez.lang.String mapped to java.lang.String
            class A {
                var foo: int = "Hi"
            }
        ''').assertError(FIELD_INITIALIZER, FIELD_INIT_TYPE_MISMATCH, "String", "int")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Array[T] mapped to rolez.lang.Array {
                mapped val length: int = 0
            }
        ''').assertError(FIELD_INITIALIZER, MAPPED_FIELD_WITH_INIT)
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                val i: int = this.i
            }
        ''').assertError(THIS, THIS_IN_FIELD_INIT)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            object Tasks {
                task pure foo: int { return 0; }
            }
            class A {
                val i: int = (the Tasks start foo).get
            }
        ''').assertError(MEMBER_ACCESS, TASK_START_IN_FIELD_INIT)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            object Asyncer {
                async def pure foo: int { return 0; }
            }
            class A {
                val i: int = the Asyncer.foo
            }
        ''').assertError(MEMBER_ACCESS, ASYNC_IN_FIELD_INIT)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class AsyncerParent {
                async def pure foo: int { return 0; }
            }
            object Asyncer extends AsyncerParent {
                override pure foo: int { return 1; }
            }
            class A {
                val i: int = the Asyncer.foo
            }
        ''').assertError(MEMBER_ACCESS, ASYNC_IN_FIELD_INIT)
    }
    
    @Test def testSingletonClassField() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            object A {
                val foo: int = 0
            }
            class B {
                var foo: int
            }
        ''').assertNoErrors
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            object A {
                var foo: int
            }
        ''').assertError(FIELD, VAR_FIELD_IN_SINGLETON_CLASS)
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            object A {
                val i: int = 0
                val o1: readonly Object = new Object
                val o2: pure Object = new Object
            }
        ''').assertNoIssues
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            object A {
                var foo: readwrite Object
            }
        ''').assertWarning(READ_WRITE, INEFFECTIVE_FIELD_ROLE)
    }
    
    @Test def testPureClassFields() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            pure class A {
                val i: int = 42
                val b: pure B = new B
                val c: pure C = the C
            }
            pure class B
            object C
        ''').assertNoErrors
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            pure class A {
                var i: int
            }
        ''').assertError(FIELD, VAR_FIELD_IN_PURE_CLASS)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            pure class A {
                val b: pure B = new B
            }
            class B
        ''').assertError(FIELD, NON_PURE_FIELD_IN_PURE_CLASS)
    }
    
    @Test def testSingletonClassMethod() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            object A {
                def pure foo: {}
                def readonly bar: {}
                def r baz[r]: {}
                def r foz[r includes readonly]: {}
            }
        ''').assertNoIssues
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            object A {
                def readwrite foo: {}
            }
        ''').assertWarning(READ_WRITE, UNCALLABLE_METHOD, "method", "called")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            object A {
                task r foo[r includes readwrite]: {}
            }
        ''').assertWarning(ROLE_PARAM_REF, UNCALLABLE_METHOD, "task", "started")
    }
    
    @Test def testLocalValInitialized() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def pure foo: {
                    val i = 4;
                    var j = 0;
                }
            }
        ''').assertNoErrors
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def pure foo: {
                    val i: int;
                }
            }
        ''').assertError(LOCAL_VAR, VAL_NOT_INITIALIZED)
    }
    
    @Test def testLocalVarsInitialized() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def pure foo(x: int): int {
                    var i = 0;
                    var j: int;
                    j = 0;
                    var k: int;
                    if(x > 0)
                        k = 42;
                    else
                        k = 3;
                    return i + j + k;
                }
                
                def pure bar: {
                    var i = 0;
                    while(this.foo(5) > i)
                        this.bar;
                }
            }
        ''').assertNoErrors
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def pure foo: int {
                    var i: int;
                    return i;
                }
            }
        ''').assertError(VAR_REF, VAR_NOT_INITIALIZED, "variable i")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def pure foo(x: int): int {
                    var i: int;
                    if(x > 0)
                        i = 5;
                    return i;
                }
            }
        ''').assertError(VAR_REF, VAR_NOT_INITIALIZED, "variable i")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def pure foo(x: int): int {
                    var i: int;
                    if(x > 0)
                        i = 5;
                    return i;
                }
            }
        ''').assertError(VAR_REF, VAR_NOT_INITIALIZED, "variable i")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def pure foo(x: int): int {
                    var i: int;
                    i++;
                }
            }
        ''').assertError(VAR_REF, VAR_NOT_INITIALIZED, "variable i")
    }
    
    @Test def testSuperConstrCall() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            pure class rolez.lang.String mapped to java.lang.String {
                mapped def pure length: int
            }
            class A {
                new {}
                new(i: int) {}
                def pure foo: boolean { return false; }
            }
            class B extends A {
                new {
                    super;
                    this.foo;
                    this.bar;
                }
                new(i: int) {
                    this.foo;
                }
                new(s: pure String) {
                    super(s.length);
                }
                new(a: pure A) {
                    while(this.foo)
                        this.bar;
                }
                def pure bar: {}
            }
        ''').assertNoErrors
    }
    
    @Test def testSuperConstrCallGeneric() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class GenericClass[T] mapped to «GenericClass.canonicalName» {
                mapped new(t: T)
            }
            class A extends GenericClass[int] {
                new {
                    super(0);
                }
            }
        ''').assertNoErrors
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class GenericClass[T] mapped to «GenericClass.canonicalName» {
                mapped new(t: T, i: int)
            }
            class A extends GenericClass[readwrite Object] {
                new {
                    super(new Object, 8);
                }
            }
        ''').assertNoErrors
    }
    
    @Test def testSuperConstrCallFirst() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                new {
                    3;
                    super(1);
                }
            }
        ''').assertError(SUPER_CONSTR_CALL, SUPER_CONSTR_CALL_FIRST)
    }
    
    @Test def testSuperConstrCallThisBefore() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                new(i: int) {}
                def pure foo: int { return 1; }
            }
            class B extends A {
                new {
                    super(this.foo);
                }
            }
        ''').assertError(THIS, THIS_BEFORE_SUPER_CONSTR_CALL)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                new(o: pure Object) {}
            }
            class B extends A {
                new {
                    super(new A(this));
                }
            }
        ''').assertError(THIS, THIS_BEFORE_SUPER_CONSTR_CALL)
    }
    
    @Test def testSuperConstrCallIncorrect() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def pure foo: { super(1); }
            }
        ''').assertError(SUPER_CONSTR_CALL, INCORRECT_SUPER_CONSTR_CALL)
        parse('''
            super(1);
        '''.withFrame).assertError(SUPER_CONSTR_CALL, INCORRECT_SUPER_CONSTR_CALL)
    }
    
    @Test def testSuperConstrCallUncatchableException() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            pure class rolez.lang.String mapped to java.lang.String
            class rolez.io.PrintStream mapped to java.io.PrintStream {
                mapped new(f: pure String)
            }
            class FooStream extends rolez.io.PrintStream {
                new {
                    super("test.txt");
                }
            }
        ''').assertError(SUPER_CONSTR_CALL, UNCATCHABLE_CHECKED_EXCEPTION)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            pure class rolez.lang.String mapped to java.lang.String
            class rolez.io.PrintStream mapped to java.io.PrintStream {
                mapped new(f: pure String)
            }
            class A {
                new(s: pure rolez.io.PrintStream) {}
            }
            class B extends A {
                new {
                    super(new (rolez.io.PrintStream)("test.txt"));
                }
            }
        ''').assertError(NEW, UNCATCHABLE_CHECKED_EXCEPTION)
    }
    
    @Test def testSuperConstrCallTaskStartBefore() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                new(i: int) {}
            }
            class B extends A {
                new {
                    super(the C start foo);
                }
            }
            object C {
                task pure foo: int { return 0; }
            }
        ''').assertError(MEMBER_ACCESS, TASK_START_BEFORE_SUPER_CONSTR_CALL)
    }
    
    @Test def testSuperConstrCallAsyncInvokeBefore() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                new(i: int) {}
            }
            class B extends A {
                new {
                    super(the C.foo);
                }
            }
            object C {
                async def pure foo: int { return 0; }
            }
        ''').assertError(MEMBER_ACCESS, ASYNC_INVOKE_BEFORE_SUPER_CONSTR_CALL)
    }
    
    @Test def testExprStmt() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            pure class rolez.lang.String mapped to java.lang.String {
                mapped def pure length: int
            }
            class rolez.lang.Array[T] mapped to rolez.lang.Array {
                mapped new(l: int)
                mapped def readonly  get(i: int): T
                mapped def readwrite set(i: int, o: T):
            }
            class rolez.lang.Task[V] mapped to rolez.lang.Task
            class App {
                task pure main: {
                    var i: int;
                    i = 5 - 2;
                    new String;
                    new String.length;
                    this start main;
                    new Array[int](1).set(0, 42);
                }
            }
        ''').assertNoIssues
    }
    
    @Test def testExprStmtNoSideFx() {
        parse('''
            true && 4 > 2;
        '''.withFrame).assertWarning( LOGICAL_EXPR, OUTER_EXPR_NO_SIDE_FX)
        parse('''
            3 == 5;
        '''.withFrame).assertWarning(EQUALITY_EXPR, OUTER_EXPR_NO_SIDE_FX)
        parse('''
            val s = new String;
            2 * s.length();
        '''.withFrame).assertWarning(ARITHMETIC_BINARY_EXPR, OUTER_EXPR_NO_SIDE_FX)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A { var i: int }
            class App {
                task pure main: {
                    new A.i;
                }
            }
        '''.withFrame).assertWarning(MEMBER_ACCESS, OUTER_EXPR_NO_SIDE_FX)
        parse('''
            new Array[int](1).get(0);
        '''.withFrame).assertWarning(MEMBER_ACCESS, OUTER_EXPR_NO_SIDE_FX)
        parse('''
            val s = "Hello";
            s as pure Object;
        '''.withFrame).assertWarning(CAST, OUTER_EXPR_NO_SIDE_FX)
        parse('''
            val i = 5;
            i;
        '''.withFrame).assertWarning(VAR_REF, OUTER_EXPR_NO_SIDE_FX)
        parse('''
            new Array[int](1);
        '''.withFrame).assertWarning(NEW, OUTER_EXPR_NO_SIDE_FX)
        parse('''
            var i: int;
            (i = 4);
        '''.withFrame).assertWarning(PARENTHESIZED, OUTER_EXPR_NO_SIDE_FX)
        parse('''
            (new String);
        '''.withFrame).assertWarning(PARENTHESIZED, OUTER_EXPR_NO_SIDE_FX)
        parse('''
            5;
        '''.withFrame).assertWarning(INT_LITERAL, OUTER_EXPR_NO_SIDE_FX)
        parse('''
            null;
        '''.withFrame).assertWarning(NULL_LITERAL, OUTER_EXPR_NO_SIDE_FX)
    }
    
    @Test def testNull() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class App {
                task pure main: Null { return null; }
            }
        ''').assertError(NULL, NULL_TYPE_USED)
    }
    
    @Test def testVoid() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                task pure foo: void {}
                task pure bar: {}
            }
            class B {
                def pure foo: void {}
                def pure bar: {}
            }
        ''').assertNoErrors
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class App {
                task pure main(v: void): {}
            }
        ''').assertError(VOID, VOID_NOT_RETURN_TYPE)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class App {
                task pure main(v:): {}
            }
        ''').assertError(VOID, VOID_NOT_RETURN_TYPE)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                val v: void
            }
        ''').assertError(VOID, VOID_NOT_RETURN_TYPE)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def pure foo(v: void): {}
            }
        ''').assertError(VOID, VOID_NOT_RETURN_TYPE)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def pure foo: {
                    var v: void;
                }
            }
        ''').assertError(VOID, VOID_NOT_RETURN_TYPE)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def pure foo: {
                    var v = this as void;
                }
            }
        ''').assertError(VOID, VOID_NOT_RETURN_TYPE)
    }
    
    @Test def testMappedClass() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Array[T] mapped to rolez.lang.Array {
                mapped new(length: int)
            }
        ''').assertNoErrors
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Slice[T] mapped to rolez.lang.Slice
            class rolez.lang.Array[T] mapped to rolez.lang.Array extends Slice[T] {
                mapped new(length: int)
            }
        ''').assertNoErrors
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class HashMap mapped to java.util.HashMap
        ''').assertError(CLASS, INCORRECT_MAPPED_CLASS, "multiple type parameters")
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Array mapped to rolez.lang.Array {
                mapped new(length: int)
            }
        ''').assertError(CLASS, MISSING_TYPE_PARAM)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Array[S] mapped to rolez.lang.Array {
                mapped new(length: int)
            }
        ''').assertError(TYPE_PARAM, INCORRECT_TYPE_PARAM)
        parse('''
            class rolez.lang.Object[T] mapped to java.lang.Object
        ''').assertError(TYPE_PARAM, INCORRECT_TYPE_PARAM)
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class NonMapped
            class System mapped to java.lang.System extends NonMapped
        ''').assertError(CLASS, INCORRECT_MAPPED_CLASS)
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            pure class rolez.lang.String mapped to java.lang.String
            class rolez.lang.Array[T] mapped to rolez.lang.Array extends String {
                mapped new(length: int)
            }
        ''').assertError(CLASS, INCORRECT_MAPPED_CLASS)
    }
    
    @Test def testMappedField() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                var length: int
            }
            class Mapped mapped to «Mapped.canonicalName» {
                mapped val i: int
            }
            class MappedGuarded mapped to «MappedGuarded.canonicalName» {
                mapped var i: int
            }
        ''').assertNoErrors
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                mapped val length: int
            }
        ''').assertError(FIELD, MAPPED_IN_NORMAL_CLASS)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object {
                var foo: int
            }
        ''').assertError(FIELD, NON_MAPPED_FIELD)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class Mapped mapped to «Mapped.canonicalName» {
                mapped val i: double
            }
        ''').assertError(DOUBLE, INCORRECT_MAPPED_FIELD)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class MappedGuarded mapped to «MappedGuarded.canonicalName» {
                mapped val i: int
            }
        ''').assertError(FIELD, INCORRECT_MAPPED_FIELD)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class Mapped mapped to «Mapped.canonicalName» {
                mapped var j: int
            }
        ''').assertError(FIELD, NON_GUARDED_MAPPED_VAR_FIELD)
    }
    
    static class Mapped {
        public final int i = 42;
        public int j;
    }
    
    static class MappedGuarded extends Guarded {
        public int i;
    }
    
    @Test def testMappedMethod() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object {
                mapped def pure equals(o: pure Object): boolean
                mapped def pure hashCode: int
                mapped def pure toString: pure String
            }
            pure class rolez.lang.String mapped to java.lang.String {
                mapped override pure equals(o: pure Object): boolean
                mapped def pure length: int
                mapped def pure charAt(i: int): char
                mapped def pure equalsIgnoreCase(s: pure String): boolean
                mapped def pure trim: pure String
                mapped def pure substring(b: int, e: int): pure String
            }
            class A {
                def pure length: int { return 0; }
            }
        ''').assertNoErrors
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                mapped def pure length: int
            }
        ''').assertError(METHOD, MAPPED_IN_NORMAL_CLASS)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object {
                def pure foo: {}
            }
        ''').assertError(METHOD, NON_MAPPED_METHOD)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            pure class rolez.lang.String mapped to java.lang.String {
                mapped def pure length: int { return 0; }
            }
        ''').assertError(BLOCK, MAPPED_WITH_BODY)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def pure length: int
            }
        ''').assertError(METHOD, MISSING_BODY)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            pure class rolez.lang.String mapped to java.lang.String {
                mapped def pure length: boolean
            }
        ''').assertError(BOOLEAN, INCORRECT_MAPPED_METHOD)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Array[T] mapped to rolez.lang.Array {
                mapped def readonly get(i: int): readonly Object
            }
        ''').assertError(ROLE_TYPE, INCORRECT_MAPPED_METHOD)
    }
    
    @Test def testMappedTask() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object {
                mapped task pure hashCode: int
            }
        ''').assertError(METHOD, INCORRECT_MAPPED_TASK)
    }
    
    @Test def testMappedConstr() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Array[T] mapped to rolez.lang.Array {
                mapped new(l: int)
            }
            pure class rolez.lang.String mapped to java.lang.String {
                mapped new
                mapped new(original: pure String)
                mapped new(value: pure Array[char])
                mapped new(value: pure Array[char], offset: int, count: int)
                mapped new(codePoints: pure Array[int], offset: int, count: int)
            }
            class A {
                new(length: int) {}
            }
        ''').assertNoErrors
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            pure class rolez.lang.String mapped to java.lang.String
        ''').assertNoErrors
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                mapped new(length: int)
            }
        ''').assertError(CONSTR, MAPPED_IN_NORMAL_CLASS)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object {
                new {}
            }
        ''').assertError(CONSTR, NON_MAPPED_CONSTR)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object {
                def pure foo: {}
            }
        ''').assertError(METHOD, NON_MAPPED_METHOD)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Array[T] mapped to rolez.lang.Array {
                mapped new(l: int) {}
            }
        ''').assertError(BLOCK, MAPPED_WITH_BODY)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Array[T] mapped to rolez.lang.Array {
                new(l: int) {}
            }
        ''').assertError(CONSTR, NON_MAPPED_CONSTR)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                new
            }
        ''').assertError(CONSTR, MISSING_BODY)
    }
    
    @Test def testObjectClass() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
        ''').assertNoErrors
        
        parse('''
            object rolez.lang.Object mapped to java.lang.Object
        ''').assertError(CLASS, INCORRECT_MAPPED_CLASS_KIND)
        parse("class rolez.lang.Object").assertError(CLASS, CLASS_ACTUALLY_MAPPED)
        parse('''
            class A
            class rolez.lang.Object mapped to java.lang.Object extends A
        ''').assertError(CLASS, INCORRECT_MAPPED_CLASS)
    }

    @Test def testStringClass() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            pure class rolez.lang.String mapped to java.lang.String
        ''').assertNoErrors
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            pure class rolez.lang.String mapped to java.lang.String extends Object
        ''').assertNoErrors
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            object rolez.lang.String mapped to java.lang.String
        ''').assertError(CLASS, INCORRECT_MAPPED_CLASS_KIND)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.String
        ''').assertError(CLASS, CLASS_ACTUALLY_MAPPED)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A
            pure class rolez.lang.String mapped to java.lang.String extends A
        ''').assertError(CLASS, INCORRECT_MAPPED_CLASS)
    }
    
    @Test def testArrayClass() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Array[T] mapped to rolez.lang.Array {
                mapped new(length: int)
            }
        ''').assertNoErrors
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Array[T] mapped to rolez.lang.Array extends Object {
                mapped val length: int
                mapped new(length: int)
                mapped def readonly get(i: int): T
                mapped def readwrite set(i: int, o: T):
            }
        ''').assertNoErrors
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            object rolez.lang.Array[T] mapped to rolez.lang.Array
        ''').assertError(CLASS, INCORRECT_MAPPED_CLASS_KIND)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Array[T]
        ''').assertError(CLASS, CLASS_ACTUALLY_MAPPED)
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A
            class rolez.lang.Array[T] mapped to rolez.lang.Array extends A
        ''').assertError(CLASS, INCORRECT_MAPPED_CLASS)
        
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Array[T] mapped to rolez.lang.Array {
                mapped new(length: int)
                mapped val length: double
            }
        ''').assertError(DOUBLE, INCORRECT_MAPPED_FIELD)
    }
}
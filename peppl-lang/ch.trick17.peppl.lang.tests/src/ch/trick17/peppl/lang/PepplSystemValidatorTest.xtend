package ch.trick17.peppl.lang

import ch.trick17.peppl.lang.peppl.PepplPackage
import ch.trick17.peppl.lang.peppl.Program
import javax.inject.Inject
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.eclipse.xtext.junit4.validation.ValidationTestHelper
import org.junit.Test
import org.junit.runner.RunWith

import static ch.trick17.peppl.lang.validation.PepplValidator.*

@RunWith(XtextRunner)
@InjectWith(PepplInjectorProvider)
class PepplSystemValidatorTest {
    
    val PepplPackage peppl = PepplPackage.eINSTANCE
    
    @Inject extension ParseHelper<Program>
    @Inject extension ValidationTestHelper
    
    @Test
    def testObjectExists() {
        parse("class A").assertError(peppl.class_, OBJECT_CLASS_NOT_DEFINED)
    }
    
    @Test
    def testOverloading() {
        parse('''
            class Object
            class A {
                def readwrite foo: void {}
                def readwrite foo(val i: int): void {}
                def readwrite foo(val c: char): void {}
                def readwrite foo(val o: readonly Object): void {}
                def readwrite foo(val o: readwrite Object): void {}
                def readwrite foo(val a: readonly A): void {}
                def readwrite foo(val a: readwrite A): void {}
                def readwrite foo(val a: readwrite A, val b: readwrite A): void {}
            }
        ''').assertNoErrors
        
        parse('''
            class Object
            class A {
                def readwrite foo(val a: readwrite A): void {}
                def readwrite bar(val a: readonly  A): void {}
            }
            class B extends A {
                def readwrite foo(val a: readonly  A): void {}
                def readwrite foo(val a: readwrite B): void {}
                def readwrite foo(val a: readwrite Object): void {}
                def readwrite bar(val a: readwrite A): void {}
            }
            class C extends B {
                def readwrite foo(val i: int): void {}
            }
            class D extends C {
                def readwrite foo(val i: char): int {}
            }
            class E extends D {
                def readwrite foo(val i: int, val j: int): readonly A {}
            }
        ''').assertNoErrors
    }
    
    @Test
    def testDuplicateMethods() {
        parse('''
            class Object
            class A {
                def readwrite foo: void {}
                def readwrite foo: void {}
            }
        ''').assertError(peppl.method, DUPLICATE_METHOD)
        parse('''
            class Object
            class A {
                def readwrite foo: int {}
                def readwrite foo: void {}
            }
        ''').assertError(peppl.method, DUPLICATE_METHOD)
        parse('''
            class Object
            class A {
                def readonly  foo: int {}
                def readwrite foo: void {}
            }
        ''').assertError(peppl.method, DUPLICATE_METHOD)
        parse('''
            class Object
            class A {
                def readwrite foo(val i: int): void {}
                def readwrite foo(val i: int): void {}
            }
        ''').assertError(peppl.method, DUPLICATE_METHOD)
        parse('''
            class Object
            class A {
                def readwrite foo(val i: int): void {}
                def readwrite foo(val j: int): void {}
            }
        ''').assertError(peppl.method, DUPLICATE_METHOD)
        parse('''
            class Object
            class A {
                def readwrite foo(val a: readwrite A): void {}
                def readwrite foo(val a: readwrite A): void {}
            }
        ''').assertError(peppl.method, DUPLICATE_METHOD)
        parse('''
            class Object
            class A {
                def readwrite foo(val a: readwrite A): void {}
                def readwrite foo(val b: readwrite A): void {}
            }
        ''').assertError(peppl.method, DUPLICATE_METHOD)
    }
    
    @Test
    def testOverride() {
        parse('''
            class Object
            class A {                def readwrite foo: void {} }
            class B extends A { override readwrite foo: void {} }
        ''').assertNoErrors
        parse('''
            class Object
            class A {                def readwrite foo(val i: int): void {} }
            class B extends A { override readwrite foo(val i: int): void {} }
        ''').assertNoErrors
        parse('''
            class Object
            class A {                def readwrite foo(val i: int): int {} }
            class B extends A { override readwrite foo(val j: int): int {} }
        ''').assertNoErrors
        parse('''
            class Object
            class A {                def readwrite foo: readwrite A {} }
            class B extends A { override readwrite foo: readwrite B {} }
        ''').assertNoErrors
        parse('''
            class Object
            class A {                def readwrite foo: readonly  A {} }
            class B extends A { override readwrite foo: readwrite A {} }
        ''').assertNoErrors
        
        parse('''
            class Object
            class A {                def readwrite foo: void {} }
            class B extends A { override readonly  foo: void {} }
        ''').assertNoErrors
    }
    
    @Test
    def testMissingOverride() {
        parse('''
            class Object
            class A {           def readwrite foo: void {} }
            class B extends A { def readwrite foo: void {} }
        ''').assertError(peppl.method, MISSING_OVERRIDE)
        parse('''
            class Object
            class A {           def readwrite foo: int  {} }
            class B extends A { def readwrite foo: void {} }
        ''').assertError(peppl.method, MISSING_OVERRIDE)
        parse('''
            class Object
            class A {           def readwrite foo(val i: int): void {} }
            class B extends A { def readwrite foo(val i: int): void {} }
        ''').assertError(peppl.method, MISSING_OVERRIDE)
        parse('''
            class Object
            class A {           def readwrite foo(val i: int): void {} }
            class B extends A { def readwrite foo(val j: int): void {} }
        ''').assertError(peppl.method, MISSING_OVERRIDE)
        parse('''
            class Object
            class A {           def readwrite foo(val a: readwrite A): void {} }
            class B extends A { def readwrite foo(val a: readwrite A): void {} }
        ''').assertError(peppl.method, MISSING_OVERRIDE)
        parse('''
            class Object
            class A {           def readwrite foo(val a: readwrite A): void {} }
            class B extends A { def readwrite foo(val b: readwrite A): void {} }
        ''').assertError(peppl.method, MISSING_OVERRIDE)
    }
    
    @Test
    def testIncorrectOverride() {
        parse('''
            class Object
            class A {                def readonly  foo: void {} }
            class B extends A { override readwrite foo: void {} }
        ''').assertError(peppl.method, INCOMPATIBLE_THIS_ROLE)
        parse('''
            class Object
            class A {                def readwrite foo: void {} }
            class B extends A { override readwrite foo(val i: int): void {} }
        ''').assertError(peppl.method, INCORRECT_OVERRIDE)
        parse('''
            class Object
            class A {                def readwrite foo(val i: int): void {} }
            class B extends A { override readwrite foo(val c: char): void {} }
        ''').assertError(peppl.method, INCORRECT_OVERRIDE)
        parse('''
            class Object
            class A {                def readwrite foo(val a: readonly  A): void {} }
            class B extends A { override readwrite foo(val a: readwrite A): void {} }
        ''').assertError(peppl.method, INCORRECT_OVERRIDE)
        parse('''
            class Object
            class A {                def readwrite foo(val a: readwrite A): void {} }
            class B extends A { override readwrite foo(val a: readonly  A): void {} }
        ''').assertError(peppl.method, INCORRECT_OVERRIDE)
        parse('''
            class Object
            class A {                def readwrite foo(val a: readwrite A): void {} }
            class B extends A { override readwrite foo(val a: readwrite B): void {} }
        ''').assertError(peppl.method, INCORRECT_OVERRIDE)
        parse('''
            class Object
            class A {                def readwrite foo(val a: readwrite B): void {} }
            class B extends A { override readwrite foo(val a: readwrite A): void {} }
        ''').assertError(peppl.method, INCORRECT_OVERRIDE)
    }
    
    // TODO: Test return type compatibility
    
    
    @Test
    def testDuplicateFields() {
        parse('''
            class Object
            class A {
                var a: int
                val a: boolean
            }
        ''').assertError(peppl.field, DUPLICATE_FIELD)
    }
    
    @Test
    def testDuplicateLocalVar() {
        parse('''
            class Object
            class A {
                def readwrite foo(val a: int, val a: boolean): void {}
            }
        ''').assertError(peppl.parameter, DUPLICATE_VARIABLE)
        
        parse('''
            class Object
            class A {
                def readwrite foo: void {
                    var a: int;
                    val a: boolean;
                }
            }
        ''').assertError(peppl.localVariable, DUPLICATE_VARIABLE)
        
        parse('''
            class Object
            class A {
                def readwrite foo(val a: int): void {
                    var a: boolean;
                }
            }
        ''').assertError(peppl.parameter, DUPLICATE_VARIABLE)
    }
}
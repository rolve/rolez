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
                def inaccessible foo: void {}
                def readonly foo: void {}
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
    }
    
    @Test
    def testDuplicateMethods() {
        parse('''
            class Object
            class A {
                def readwrite foo: void {}
                def readwrite foo: void {}
            }
        ''').assertError(peppl.method, DUPLICATE_METHOD, "foo")
        parse('''
            class Object
            class A {
                def readwrite foo: int {}
                def readwrite foo: void {}
            }
        ''').assertError(peppl.method, DUPLICATE_METHOD, "foo")
        parse('''
            class Object
            class A {
                def readwrite foo(val i: int): void {}
                def readwrite foo(val i: int): void {}
            }
        ''').assertError(peppl.method, DUPLICATE_METHOD, "foo")
        parse('''
            class Object
            class A {
                def readwrite foo(val i: int): void {}
                def readwrite foo(val j: int): void {}
            }
        ''').assertError(peppl.method, DUPLICATE_METHOD, "foo")
        parse('''
            class Object
            class A {
                def readwrite foo(val a: readwrite A): void {}
                def readwrite foo(val a: readwrite A): void {}
            }
        ''').assertError(peppl.method, DUPLICATE_METHOD, "foo")
        parse('''
            class Object
            class A {
                def readwrite foo(val a: readwrite A): void {}
                def readwrite foo(val b: readwrite A): void {}
            }
        ''').assertError(peppl.method, DUPLICATE_METHOD, "foo")
    }
}
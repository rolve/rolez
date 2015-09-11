package ch.trick17.peppl.lang

import ch.trick17.peppl.lang.peppl.Program
import javax.inject.Inject
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.eclipse.xtext.junit4.validation.ValidationTestHelper
import org.junit.Test
import org.junit.runner.RunWith

import static ch.trick17.peppl.lang.peppl.PepplPackage.Literals.*
import static ch.trick17.peppl.lang.validation.PepplValidator.*
import org.eclipse.xtext.resource.XtextResourceSet
import org.eclipse.emf.common.util.URI
import org.eclipse.xtext.util.StringInputStream

@RunWith(XtextRunner)
@InjectWith(PepplInjectorProvider)
class PepplValidatorTest {
    
    @Inject extension ParseHelper<Program>
    @Inject extension ValidationTestHelper
    
    @Test
    def testObjectExists() {
        parse('''
            class Object
            class A
        ''').assertNoErrors
        
        val set = new PepplStandaloneSetup().createInjectorAndDoEMFRegistration
            .getInstance(XtextResourceSet)
        val objectRes = set.createResource(URI.createURI("object.peppl"))
        objectRes.load(new StringInputStream("class Object"), emptyMap)
        parse("class A", set).assertNoErrors
        
        parse("class A").assertError(CLASS, OBJECT_CLASS_NOT_DEFINED)
    }
    
    @Test
    def testDuplicateTopLevelElems() {
        parse('''
            class Object
            class A
            class A
        ''').assertError(CLASS, DUPLICATE_TOP_LEVEL_ELEMENT)
        parse('''
            class Object
            task A: {}
            task A: {}
        ''').assertError(TASK, DUPLICATE_TOP_LEVEL_ELEMENT)
        val program = parse('''
            class Object
            class A
            task A: {}
        ''')
        program.assertError(CLASS, DUPLICATE_TOP_LEVEL_ELEMENT)
        program.assertError(TASK, DUPLICATE_TOP_LEVEL_ELEMENT)
    }
    
    @Test
    def testOverloading() {
        parse('''
            class Object
            class A {
                def readwrite foo: {}
                def readwrite foo(val i: int): {}
                def readwrite foo(val c: char): {}
                def readwrite foo(val o: readonly Object): {}
                def readwrite foo(val o: readwrite Object): {}
                def readwrite foo(val a: readonly A): {}
                def readwrite foo(val a: readwrite A): {}
                def readwrite foo(val a: readwrite A, val b: readwrite A): {}
            }
        ''').assertNoErrors
        
        parse('''
            class Object
            class A {
                def readwrite foo(val a: readwrite A): {}
                def readwrite bar(val a: readonly  A): {}
            }
            class B extends A {
                def readwrite foo(val a: readonly  A): {}
                def readwrite foo(val a: readwrite B): {}
                def readwrite foo(val a: readwrite Object): {}
                def readwrite bar(val a: readwrite A): {}
            }
            class C extends B {
                def readwrite foo(val i: int): {}
            }
            class D extends C {
                def readwrite foo(val i: char): int { return 0; }
            }
            class E extends D {
                def readwrite foo(val i: int, val j: int): readonly A {
                    return new A;
                }
            }
        ''').assertNoErrors
    }
    
    @Test
    def testDuplicateMethods() {
        parse('''
            class Object
            class A {
                def readwrite foo: {}
                def readwrite foo: {}
            }
        ''').assertError(METHOD, DUPLICATE_METHOD)
        parse('''
            class Object
            class A {
                def readwrite foo: int {}
                def readwrite foo: {}
            }
        ''').assertError(METHOD, DUPLICATE_METHOD)
        parse('''
            class Object
            class A {
                def readonly  foo: int {}
                def readwrite foo: {}
            }
        ''').assertError(METHOD, DUPLICATE_METHOD)
        parse('''
            class Object
            class A {
                def readwrite foo(val i: int): {}
                def readwrite foo(val i: int): {}
            }
        ''').assertError(METHOD, DUPLICATE_METHOD)
        parse('''
            class Object
            class A {
                def readwrite foo(val i: int): {}
                def readwrite foo(val j: int): {}
            }
        ''').assertError(METHOD, DUPLICATE_METHOD)
        parse('''
            class Object
            class A {
                def readwrite foo(val a: readwrite A): {}
                def readwrite foo(val a: readwrite A): {}
            }
        ''').assertError(METHOD, DUPLICATE_METHOD)
        parse('''
            class Object
            class A {
                def readwrite foo(val a: readwrite A): {}
                def readwrite foo(val b: readwrite A): {}
            }
        ''').assertError(METHOD, DUPLICATE_METHOD)
    }
    
    @Test
    def testOverride() {
        parse('''
            class Object
            class A {                def readwrite foo: {} }
            class B extends A { override readwrite foo: {} }
        ''').assertNoErrors
        parse('''
            class Object
            class A {                def readwrite foo(val i: int): {} }
            class B extends A { override readwrite foo(val i: int): {} }
        ''').assertNoErrors
        parse('''
            class Object
            class A {                def readwrite foo(val i: int): int { return 0; } }
            class B extends A { override readwrite foo(val j: int): int { return 0; } }
        ''').assertNoErrors
        parse('''
            class Object
            class A {                def readwrite foo: readwrite A { return new A; } }
            class B extends A { override readwrite foo: readwrite B { return new B; } }
        ''').assertNoErrors
        parse('''
            class Object
            class A {                def readwrite foo: readonly  A { return new A; } }
            class B extends A { override readwrite foo: readwrite A { return new A; } }
        ''').assertNoErrors
        
        parse('''
            class Object
            class A {                def readwrite foo: {} }
            class B extends A { override readonly  foo: {} }
        ''').assertNoErrors
    }
    
    @Test
    def testMissingOverride() {
        parse('''
            class Object
            class A {           def readwrite foo: {} }
            class B extends A { def readwrite foo: {} }
        ''').assertError(METHOD, MISSING_OVERRIDE)
        parse('''
            class Object
            class A {           def readwrite foo: int  {} }
            class B extends A { def readwrite foo: {} }
        ''').assertError(METHOD, MISSING_OVERRIDE)
        parse('''
            class Object
            class A {           def readwrite foo(val i: int): {} }
            class B extends A { def readwrite foo(val i: int): {} }
        ''').assertError(METHOD, MISSING_OVERRIDE)
        parse('''
            class Object
            class A {           def readwrite foo(val i: int): {} }
            class B extends A { def readwrite foo(val j: int): {} }
        ''').assertError(METHOD, MISSING_OVERRIDE)
        parse('''
            class Object
            class A {           def readwrite foo(val a: readwrite A): {} }
            class B extends A { def readwrite foo(val a: readwrite A): {} }
        ''').assertError(METHOD, MISSING_OVERRIDE)
        parse('''
            class Object
            class A {           def readwrite foo(val a: readwrite A): {} }
            class B extends A { def readwrite foo(val b: readwrite A): {} }
        ''').assertError(METHOD, MISSING_OVERRIDE)
    }
    
    @Test
    def testIncorrectOverride() {
        parse('''
            class Object
            class A {                def readonly  foo: {} }
            class B extends A { override readwrite foo: {} }
        ''').assertError(METHOD, INCOMPATIBLE_THIS_ROLE)
        parse('''
            class Object
            class A {                def readwrite foo: {} }
            class B extends A { override readwrite foo(val i: int): {} }
        ''').assertError(METHOD, INCORRECT_OVERRIDE)
        parse('''
            class Object
            class A {                def readwrite foo(val i: int): {} }
            class B extends A { override readwrite foo(val c: char): {} }
        ''').assertError(METHOD, INCORRECT_OVERRIDE)
        parse('''
            class Object
            class A {                def readwrite foo(val a: readonly  A): {} }
            class B extends A { override readwrite foo(val a: readwrite A): {} }
        ''').assertError(METHOD, INCORRECT_OVERRIDE)
        parse('''
            class Object
            class A {                def readwrite foo(val a: readwrite A): {} }
            class B extends A { override readwrite foo(val a: readonly  A): {} }
        ''').assertError(METHOD, INCORRECT_OVERRIDE)
        parse('''
            class Object
            class A {                def readwrite foo(val a: readwrite A): {} }
            class B extends A { override readwrite foo(val a: readwrite B): {} }
        ''').assertError(METHOD, INCORRECT_OVERRIDE)
        parse('''
            class Object
            class A {                def readwrite foo(val a: readwrite B): {} }
            class B extends A { override readwrite foo(val a: readwrite A): {} }
        ''').assertError(METHOD, INCORRECT_OVERRIDE)
    }
    
    @Test
    def testReturn() {
        parse('''
            class Object
            class A {
                def pure a: {}
                def pure b: {
                    return;
                }
                def pure c(val i: int): {
                    if(i == 0)
                        return;
                    else
                        return;
                }
                def pure d(val i: int): {
                    if(i == 0)
                        return;
                    return;
                }
                def pure e(val i: int): {
                    if(i == 0) {}
                    else
                        return;
                    return;
                }
                
                def pure f: int {
                    return 0;
                }
                def pure g(val i: int): int {
                    if(i == 0)
                        return 0;
                    else
                        return 1;
                }
                def pure h(val i: int): int {
                    if(i == 0)
                        return 0;
                    return 1;
                }
                def pure i(val i: int): int {
                    if(i == 0) {}
                    else
                        return 0;
                    return 1;
                }
            }
        ''').assertNoErrors
    }
    
    @Test
    def testMissingReturn() {
        parse('''
            class Object
            class A {
                def pure a: int {}
            }
        ''').assertError(BLOCK, MISSING_RETURN)
        parse('''
            class Object
            class A {
                def pure a(val i: int): int {
                    if(i == 0)
                        return 0;
                }
            }
        ''').assertError(IF_STMT, MISSING_RETURN)
        parse('''
            class Object
            class A {
                def pure a(val i: int): int {
                    if(i == 0) {}
                    else
                        return 0;
                }
            }
        ''').assertError(BLOCK, MISSING_RETURN)
    }
    
    @Test
    def testDuplicateFields() {
        parse('''
            class Object
            class A {
                var a: int
                val a: boolean
            }
        ''').assertError(FIELD, DUPLICATE_FIELD)
    }
    
    @Test
    def testDuplicateLocalVar() {
        parse('''
            class Object
            class A {
                def readwrite foo(val a: int, val a: boolean): {}
            }
        ''').assertError(PARAM, DUPLICATE_VARIABLE)
        parse('''
            class Object
            class A {
                new(val a: int, val a: boolean) {}
            }
        ''').assertError(PARAM, DUPLICATE_VARIABLE)
        
        parse('''
            class Object
            class A {
                def readwrite foo: {
                    var a: int;
                    val a: boolean;
                }
            }
        ''').assertError(LOCAL_VAR, DUPLICATE_VARIABLE)
        parse('''
            class Object
            class A {
                new {
                    var a: int;
                    val a: boolean;
                }
            }
        ''').assertError(LOCAL_VAR, DUPLICATE_VARIABLE)
        parse('''
            task Main: {
                val i: int = 5;
                {
                    val i: boolean = true;
                    i;
                }
                i;
            }
        ''').assertError(LOCAL_VAR, DUPLICATE_VARIABLE)
        
        parse('''
            class Object
            class A {
                def readwrite foo(val a: int): {
                    var a: boolean;
                }
            }
        ''').assertError(PARAM, DUPLICATE_VARIABLE)
        parse('''
            class Object
            class A {
                new(val a: int) {
                    var a: boolean;
                }
            }
        ''').assertError(PARAM, DUPLICATE_VARIABLE)
    }
    
    @Test
    def testTypeArgs() {
        parse('''
            class Object
            class Array
            class A
            task Main: {
                val a: pure Array[int] = new Array[int];
                var b: readonly Array[readwrite Array[pure A]];
                val c: readwrite A;
            }
        ''').assertNoErrors
        
        parse('''
            class Object
            class Array
            task Main: {
                val a: pure Array;
            }
        ''').assertError(SIMPLE_CLASS_REF, MISSING_TYPE_ARGS, "class Array")
        parse('''
            class Object
            class A
            task Main: {
                val a: pure A[int];
            }
        ''').assertError(GENERIC_CLASS_REF, INCORRECT_TYPE_ARGS, "class A")
        parse('''
            class Object
            class A
            task Main: {
                val a: pure A = new A[int];
            }
        ''').assertError(GENERIC_CLASS_REF, INCORRECT_TYPE_ARGS, "class A")
        parse('''
            class Object
            class A
            task Main: {
                val a: pure A[readwrite A];
            }
        ''').assertError(GENERIC_CLASS_REF, INCORRECT_TYPE_ARGS, "class A")
    }
    
    @Test
    def testObjectClass() {
        parse('''
            class A
            class Object extends A
        ''').assertError(CLASS, INCORRECT_OBJECT_SUPERCLASS)
    }
    
    @Test
    def testArrayClass() {
        parse('''
            class Object
            class Array
        ''').assertNoErrors
        parse('''
            class Object
            class Array extends Object
        ''').assertNoErrors
        
        parse('''
            class Object
            class A
            class Array extends A
        ''').assertError(CLASS, INCORRECT_ARRAY_SUPERCLASS)
    }
    
    @Test
    def testTaskClass() {
        parse('''
            class Object
            class Task
        ''').assertNoErrors
        parse('''
            class Object
            class Task extends Object
        ''').assertNoErrors
        
        parse('''
            class Object
            class A
            class Task extends A
        ''').assertError(CLASS, INCORRECT_TASK_SUPERCLASS)
    }
    
    @Test
    def testCircularInheritance() {
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
}
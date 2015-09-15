package ch.trick17.rolez.lang

import ch.trick17.rolez.lang.rolez.Program
import javax.inject.Inject
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.eclipse.xtext.junit4.validation.ValidationTestHelper
import org.junit.Test
import org.junit.runner.RunWith

import static ch.trick17.rolez.lang.rolez.RolezPackage.Literals.*
import static ch.trick17.rolez.lang.validation.RolezValidator.*

@RunWith(XtextRunner)
@InjectWith(RolezInjectorProvider)
class ValidatorTest {
    
    @Inject extension ParseHelper<Program>
    @Inject extension ValidationTestHelper
    
    @Test
    def testObjectExists() {
        parse('''
            class rolez.lang.Object
            class A
        ''').assertNoErrors
        
        parse("class A").assertError(CLASS, OBJECT_CLASS_NOT_DEFINED)
    }
    
    @Test
    def testDuplicateTopLevelElems() {
        parse('''
            class rolez.lang.Object
            class A
            class A
        ''').assertError(CLASS, DUPLICATE_TOP_LEVEL_ELEMENT)
        parse('''
            class rolez.lang.Object
            task A: {}
            task A: {}
        ''').assertError(TASK, DUPLICATE_TOP_LEVEL_ELEMENT)
        val program = parse('''
            class rolez.lang.Object
            class A
            task A: {}
        ''')
        program.assertError(CLASS, DUPLICATE_TOP_LEVEL_ELEMENT)
        program.assertError(TASK, DUPLICATE_TOP_LEVEL_ELEMENT)
    }
    
    @Test
    def testOverloading() {
        parse('''
            class rolez.lang.Object
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
            class rolez.lang.Object
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
            class rolez.lang.Object
            class A {
                def readwrite foo: {}
                def readwrite foo: {}
            }
        ''').assertError(METHOD, DUPLICATE_METHOD)
        parse('''
            class rolez.lang.Object
            class A {
                def readwrite foo: int {}
                def readwrite foo: {}
            }
        ''').assertError(METHOD, DUPLICATE_METHOD)
        parse('''
            class rolez.lang.Object
            class A {
                def readonly  foo: int {}
                def readwrite foo: {}
            }
        ''').assertError(METHOD, DUPLICATE_METHOD)
        parse('''
            class rolez.lang.Object
            class A {
                def readwrite foo(val i: int): {}
                def readwrite foo(val i: int): {}
            }
        ''').assertError(METHOD, DUPLICATE_METHOD)
        parse('''
            class rolez.lang.Object
            class A {
                def readwrite foo(val i: int): {}
                def readwrite foo(val j: int): {}
            }
        ''').assertError(METHOD, DUPLICATE_METHOD)
        parse('''
            class rolez.lang.Object
            class A {
                def readwrite foo(val a: readwrite A): {}
                def readwrite foo(val a: readwrite A): {}
            }
        ''').assertError(METHOD, DUPLICATE_METHOD)
        parse('''
            class rolez.lang.Object
            class A {
                def readwrite foo(val a: readwrite A): {}
                def readwrite foo(val b: readwrite A): {}
            }
        ''').assertError(METHOD, DUPLICATE_METHOD)
    }
    
    @Test
    def testOverride() {
        parse('''
            class rolez.lang.Object
            class A {                def readwrite foo: {} }
            class B extends A { override readwrite foo: {} }
        ''').assertNoErrors
        parse('''
            class rolez.lang.Object
            class A {                def readwrite foo(val i: int): {} }
            class B extends A { override readwrite foo(val i: int): {} }
        ''').assertNoErrors
        parse('''
            class rolez.lang.Object
            class A {                def readwrite foo(val i: int): int { return 0; } }
            class B extends A { override readwrite foo(val j: int): int { return 0; } }
        ''').assertNoErrors
        parse('''
            class rolez.lang.Object
            class A {                def readwrite foo: readwrite A { return new A; } }
            class B extends A { override readwrite foo: readwrite B { return new B; } }
        ''').assertNoErrors
        parse('''
            class rolez.lang.Object
            class A {                def readwrite foo: readonly  A { return new A; } }
            class B extends A { override readwrite foo: readwrite A { return new A; } }
        ''').assertNoErrors
        
        parse('''
            class rolez.lang.Object
            class A {                def readwrite foo: {} }
            class B extends A { override readonly  foo: {} }
        ''').assertNoErrors
    }
    
    @Test
    def testMissingOverride() {
        parse('''
            class rolez.lang.Object
            class A {           def readwrite foo: {} }
            class B extends A { def readwrite foo: {} }
        ''').assertError(METHOD, MISSING_OVERRIDE)
        parse('''
            class rolez.lang.Object
            class A {           def readwrite foo: int  {} }
            class B extends A { def readwrite foo: {} }
        ''').assertError(METHOD, MISSING_OVERRIDE)
        parse('''
            class rolez.lang.Object
            class A {           def readwrite foo(val i: int): {} }
            class B extends A { def readwrite foo(val i: int): {} }
        ''').assertError(METHOD, MISSING_OVERRIDE)
        parse('''
            class rolez.lang.Object
            class A {           def readwrite foo(val i: int): {} }
            class B extends A { def readwrite foo(val j: int): {} }
        ''').assertError(METHOD, MISSING_OVERRIDE)
        parse('''
            class rolez.lang.Object
            class A {           def readwrite foo(val a: readwrite A): {} }
            class B extends A { def readwrite foo(val a: readwrite A): {} }
        ''').assertError(METHOD, MISSING_OVERRIDE)
        parse('''
            class rolez.lang.Object
            class A {           def readwrite foo(val a: readwrite A): {} }
            class B extends A { def readwrite foo(val b: readwrite A): {} }
        ''').assertError(METHOD, MISSING_OVERRIDE)
    }
    
    @Test
    def testIncorrectOverride() {
        parse('''
            class rolez.lang.Object
            class A {                def readonly  foo: {} }
            class B extends A { override readwrite foo: {} }
        ''').assertError(METHOD, INCOMPATIBLE_THIS_ROLE)
        parse('''
            class rolez.lang.Object
            class A {                def readwrite foo: {} }
            class B extends A { override readwrite foo(val i: int): {} }
        ''').assertError(METHOD, INCORRECT_OVERRIDE)
        parse('''
            class rolez.lang.Object
            class A {                def readwrite foo(val i: int): {} }
            class B extends A { override readwrite foo(val c: char): {} }
        ''').assertError(METHOD, INCORRECT_OVERRIDE)
        parse('''
            class rolez.lang.Object
            class A {                def readwrite foo(val a: readonly  A): {} }
            class B extends A { override readwrite foo(val a: readwrite A): {} }
        ''').assertError(METHOD, INCORRECT_OVERRIDE)
        parse('''
            class rolez.lang.Object
            class A {                def readwrite foo(val a: readwrite A): {} }
            class B extends A { override readwrite foo(val a: readonly  A): {} }
        ''').assertError(METHOD, INCORRECT_OVERRIDE)
        parse('''
            class rolez.lang.Object
            class A {                def readwrite foo(val a: readwrite A): {} }
            class B extends A { override readwrite foo(val a: readwrite B): {} }
        ''').assertError(METHOD, INCORRECT_OVERRIDE)
        parse('''
            class rolez.lang.Object
            class A {                def readwrite foo(val a: readwrite B): {} }
            class B extends A { override readwrite foo(val a: readwrite A): {} }
        ''').assertError(METHOD, INCORRECT_OVERRIDE)
    }
    
    @Test
    def testReturn() {
        parse('''
            class rolez.lang.Object
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
            class rolez.lang.Object
            class A {
                def pure a: int {}
            }
        ''').assertError(BLOCK, MISSING_RETURN)
        parse('''
            class rolez.lang.Object
            class A {
                def pure a(val i: int): int {
                    if(i == 0)
                        return 0;
                }
            }
        ''').assertError(IF_STMT, MISSING_RETURN)
        parse('''
            class rolez.lang.Object
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
            class rolez.lang.Object
            class A {
                var a: int
                val a: boolean
            }
        ''').assertError(FIELD, DUPLICATE_FIELD)
    }
    
    @Test
    def testDuplicateLocalVar() {
        parse('''
            class rolez.lang.Object
            class A {
                def readwrite foo(val a: int, val a: boolean): {}
            }
        ''').assertError(PARAM, DUPLICATE_VARIABLE)
        parse('''
            class rolez.lang.Object
            class A {
                new(val a: int, val a: boolean) {}
            }
        ''').assertError(PARAM, DUPLICATE_VARIABLE)
        
        parse('''
            class rolez.lang.Object
            class A {
                def readwrite foo: {
                    var a: int;
                    val a: boolean;
                }
            }
        ''').assertError(LOCAL_VAR, DUPLICATE_VARIABLE)
        parse('''
            class rolez.lang.Object
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
            class rolez.lang.Object
            class A {
                def readwrite foo(val a: int): {
                    var a: boolean;
                }
            }
        ''').assertError(PARAM, DUPLICATE_VARIABLE)
        parse('''
            class rolez.lang.Object
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
            class rolez.lang.Object
            class rolez.lang.Array
            class A
            task Main: {
                val a: pure Array[int] = new Array[int];
                var b: readonly Array[readwrite Array[pure A]];
                val c: readwrite A;
            }
        ''').assertNoErrors
        
        parse('''
            class rolez.lang.Object
            class rolez.lang.Array
            task Main: {
                val a: pure Array;
            }
        ''').assertError(SIMPLE_CLASS_REF, MISSING_TYPE_ARGS, "class rolez.lang.Array")
        parse('''
            class rolez.lang.Object
            class A
            task Main: {
                val a: pure A[int];
            }
        ''').assertError(GENERIC_CLASS_REF, INCORRECT_TYPE_ARGS, "class A")
        parse('''
            class rolez.lang.Object
            class A
            task Main: {
                val a: pure A = new A[int];
            }
        ''').assertError(GENERIC_CLASS_REF, INCORRECT_TYPE_ARGS, "class A")
        parse('''
            class rolez.lang.Object
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
            class rolez.lang.Object extends A
        ''').assertError(CLASS, INCORRECT_OBJECT_SUPERCLASS)
    }
    
    @Test
    def testArrayClass() {
        parse('''
            class rolez.lang.Object
            class rolez.lang.Array
        ''').assertNoErrors
        parse('''
            class rolez.lang.Object
            class rolez.lang.Array extends Object
        ''').assertNoErrors
        
        parse('''
            class rolez.lang.Object
            class A
            class rolez.lang.Array extends A
        ''').assertError(CLASS, INCORRECT_ARRAY_SUPERCLASS)
    }
    
    @Test
    def testTaskClass() {
        parse('''
            class rolez.lang.Object
            class rolez.lang.Task
        ''').assertNoErrors
        parse('''
            class rolez.lang.Object
            class rolez.lang.Task extends Object
        ''').assertNoErrors
        
        parse('''
            class rolez.lang.Object
            class A
            class rolez.lang.Task extends A
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
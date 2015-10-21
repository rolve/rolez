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
            mapped class rolez.lang.Object
            class A
        ''').assertNoErrors
        
        parse("class A").assertError(CLASS, OBJECT_CLASS_NOT_DEFINED)
    }
    
    @Test
    def testDuplicateTopLevelElems() {
        parse('''
            mapped class rolez.lang.Object
            class A
            class A
        ''').assertError(CLASS, DUPLICATE_TOP_LEVEL_ELEMENT)
        parse('''
            mapped class rolez.lang.Object
            task A: {}
            task A: {}
        ''').assertError(TASK, DUPLICATE_TOP_LEVEL_ELEMENT)
        val program = parse('''
            mapped class rolez.lang.Object
            class A
            task A: {}
        ''')
        program.assertError(CLASS, DUPLICATE_TOP_LEVEL_ELEMENT)
        program.assertError(TASK, DUPLICATE_TOP_LEVEL_ELEMENT)
    }
    
    @Test
    def testOverloading() {
        parse('''
            mapped class rolez.lang.Object
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
            mapped class rolez.lang.Object
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
            mapped class rolez.lang.Object
            class A {
                def readwrite foo: {}
                def readwrite foo: {}
            }
        ''').assertError(METHOD, DUPLICATE_METHOD)
        parse('''
            mapped class rolez.lang.Object
            class A {
                def readwrite foo: int {}
                def readwrite foo: {}
            }
        ''').assertError(METHOD, DUPLICATE_METHOD)
        parse('''
            mapped class rolez.lang.Object
            class A {
                def readonly  foo: int {}
                def readwrite foo: {}
            }
        ''').assertError(METHOD, DUPLICATE_METHOD)
        parse('''
            mapped class rolez.lang.Object
            class A {
                def readwrite foo(val i: int): {}
                def readwrite foo(val i: int): {}
            }
        ''').assertError(METHOD, DUPLICATE_METHOD)
        parse('''
            mapped class rolez.lang.Object
            class A {
                def readwrite foo(val i: int): {}
                def readwrite foo(val j: int): {}
            }
        ''').assertError(METHOD, DUPLICATE_METHOD)
        parse('''
            mapped class rolez.lang.Object
            class A {
                def readwrite foo(val a: readwrite A): {}
                def readwrite foo(val a: readwrite A): {}
            }
        ''').assertError(METHOD, DUPLICATE_METHOD)
        parse('''
            mapped class rolez.lang.Object
            class A {
                def readwrite foo(val a: readwrite A): {}
                def readwrite foo(val b: readwrite A): {}
            }
        ''').assertError(METHOD, DUPLICATE_METHOD)
    }
    
    @Test
    def testOverride() {
        parse('''
            mapped class rolez.lang.Object
            class A {                def readwrite foo: {} }
            class B extends A { override readwrite foo: {} }
        ''').assertNoErrors
        parse('''
            mapped class rolez.lang.Object
            class A {                def readwrite foo(val i: int): {} }
            class B extends A { override readwrite foo(val i: int): {} }
        ''').assertNoErrors
        parse('''
            mapped class rolez.lang.Object
            class A {                def readwrite foo(val i: int): int { return 0; } }
            class B extends A { override readwrite foo(val j: int): int { return 0; } }
        ''').assertNoErrors
        parse('''
            mapped class rolez.lang.Object
            class A {                def readwrite foo: readwrite A { return new A; } }
            class B extends A { override readwrite foo: readwrite B { return new B; } }
        ''').assertNoErrors
        parse('''
            mapped class rolez.lang.Object
            class A {                def readwrite foo: readonly  A { return new A; } }
            class B extends A { override readwrite foo: readwrite A { return new A; } }
        ''').assertNoErrors
        
        parse('''
            mapped class rolez.lang.Object
            class A {                def readwrite foo: {} }
            class B extends A { override readonly  foo: {} }
        ''').assertNoErrors
    }
    
    @Test
    def testMissingOverride() {
        parse('''
            mapped class rolez.lang.Object
            class A {           def readwrite foo: {} }
            class B extends A { def readwrite foo: {} }
        ''').assertError(METHOD, MISSING_OVERRIDE)
        parse('''
            mapped class rolez.lang.Object
            class A {           def readwrite foo: int  {} }
            class B extends A { def readwrite foo: {} }
        ''').assertError(METHOD, MISSING_OVERRIDE)
        parse('''
            mapped class rolez.lang.Object
            class A {           def readwrite foo(val i: int): {} }
            class B extends A { def readwrite foo(val i: int): {} }
        ''').assertError(METHOD, MISSING_OVERRIDE)
        parse('''
            mapped class rolez.lang.Object
            class A {           def readwrite foo(val i: int): {} }
            class B extends A { def readwrite foo(val j: int): {} }
        ''').assertError(METHOD, MISSING_OVERRIDE)
        parse('''
            mapped class rolez.lang.Object
            class A {           def readwrite foo(val a: readwrite A): {} }
            class B extends A { def readwrite foo(val a: readwrite A): {} }
        ''').assertError(METHOD, MISSING_OVERRIDE)
        parse('''
            mapped class rolez.lang.Object
            class A {           def readwrite foo(val a: readwrite A): {} }
            class B extends A { def readwrite foo(val b: readwrite A): {} }
        ''').assertError(METHOD, MISSING_OVERRIDE)
    }
    
    @Test
    def testIncorrectOverride() {
        parse('''
            mapped class rolez.lang.Object
            class A {                def readonly  foo: {} }
            class B extends A { override readwrite foo: {} }
        ''').assertError(METHOD, INCOMPATIBLE_THIS_ROLE)
        parse('''
            mapped class rolez.lang.Object
            class A {                def readwrite foo: {} }
            class B extends A { override readwrite foo(val i: int): {} }
        ''').assertError(METHOD, INCORRECT_OVERRIDE)
        parse('''
            mapped class rolez.lang.Object
            class A {                def readwrite foo(val i: int): {} }
            class B extends A { override readwrite foo(val c: char): {} }
        ''').assertError(METHOD, INCORRECT_OVERRIDE)
        parse('''
            mapped class rolez.lang.Object
            class A {                def readwrite foo(val a: readonly  A): {} }
            class B extends A { override readwrite foo(val a: readwrite A): {} }
        ''').assertError(METHOD, INCORRECT_OVERRIDE)
        parse('''
            mapped class rolez.lang.Object
            class A {                def readwrite foo(val a: readwrite A): {} }
            class B extends A { override readwrite foo(val a: readonly  A): {} }
        ''').assertError(METHOD, INCORRECT_OVERRIDE)
        parse('''
            mapped class rolez.lang.Object
            class A {                def readwrite foo(val a: readwrite A): {} }
            class B extends A { override readwrite foo(val a: readwrite B): {} }
        ''').assertError(METHOD, INCORRECT_OVERRIDE)
        parse('''
            mapped class rolez.lang.Object
            class A {                def readwrite foo(val a: readwrite B): {} }
            class B extends A { override readwrite foo(val a: readwrite A): {} }
        ''').assertError(METHOD, INCORRECT_OVERRIDE)
    }
    
    @Test
    def testReturn() {
        parse('''
            mapped class rolez.lang.Object
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
            task T: int {
                return 3;
            }
            task U: {}
            task V: {
                return;
            }
        ''').assertNoErrors
    }
    
    @Test
    def testMissingReturnExpr() {
        parse('''
            mapped class rolez.lang.Object
            class A {
                def pure a: int {}
            }
        ''').assertError(BLOCK, MISSING_RETURN_EXPR)
        parse('''
            mapped class rolez.lang.Object
            class A {
                def pure a: int {
                    return;
                }
            }
        ''').assertError(RETURN_NOTHING, MISSING_RETURN_EXPR)
        val program = parse('''
            mapped class rolez.lang.Object
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
            mapped class rolez.lang.Object
            class A {
                def pure a(val i: int): int {
                    if(i == 0)
                        return 0;
                }
            }
        ''').assertError(IF_STMT, MISSING_RETURN_EXPR)
        parse('''
            mapped class rolez.lang.Object
            class A {
                def pure a(val i: int): int {
                    1;
                    if(i == 0)
                        return 0;
                }
            }
        ''').assertError(IF_STMT, MISSING_RETURN_EXPR)
        parse('''
            mapped class rolez.lang.Object
            class A {
                def pure a(val i: int): int {
                    if(i == 0) {}
                    else
                        return 0;
                }
            }
        ''').assertError(BLOCK, MISSING_RETURN_EXPR)
        
        parse('''
            task Main: int {}
        ''').assertError(BLOCK, MISSING_RETURN_EXPR)
        parse('''
            task Main: int {
                return;
            }
        ''').assertError(RETURN_NOTHING, MISSING_RETURN_EXPR)
    }
    
    @Test
    def testIncorrectReturn() {
        parse('''
            mapped class rolez.lang.Object
            class A {
                new {
                    return 4;
                }
            }
        ''').assertError(RETURN_EXPR, null, "cannot return", "constructor")
    }
    
    @Test
    def testDuplicateFields() {
        parse('''
            mapped class rolez.lang.Object
            class A {
                var a: int
                val a: boolean
            }
        ''').assertError(FIELD, DUPLICATE_FIELD)
    }
    
    @Test
    def testDuplicateLocalVar() {
        parse('''
            mapped class rolez.lang.Object
            class A {
                def readwrite foo(val a: int, val a: boolean): {}
            }
        ''').assertError(PARAM, DUPLICATE_VARIABLE)
        parse('''
            mapped class rolez.lang.Object
            class A {
                new(val a: int, val a: boolean) {}
            }
        ''').assertError(PARAM, DUPLICATE_VARIABLE)
        
        parse('''
            mapped class rolez.lang.Object
            class A {
                def readwrite foo: {
                    var a: int;
                    val a: boolean;
                }
            }
        ''').assertError(LOCAL_VAR, DUPLICATE_VARIABLE)
        parse('''
            mapped class rolez.lang.Object
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
            mapped class rolez.lang.Object
            class A {
                def readwrite foo(val a: int): {
                    var a: boolean;
                }
            }
        ''').assertError(PARAM, DUPLICATE_VARIABLE)
        parse('''
            mapped class rolez.lang.Object
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
            mapped class rolez.lang.Object
            mapped class rolez.lang.Array {
                mapped new(val length: int)
            }
            class A
            task Main: {
                val a: pure Array[int] = new Array[int](42);
                var b: readonly Array[readwrite Array[pure A]];
                var c: readwrite A;
            }
        ''').assertNoErrors
        
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.Array
            task Main: {
                val a: pure Array;
            }
        ''').assertError(SIMPLE_CLASS_REF, MISSING_TYPE_ARGS, "class rolez.lang.Array")
        parse('''
            mapped class rolez.lang.Object
            class A
            task Main: {
                val a: pure A[int] = null;
            }
        ''').assertError(GENERIC_CLASS_REF, INCORRECT_TYPE_ARGS, "class A")
        parse('''
            mapped class rolez.lang.Object
            class A
            task Main: {
                val a: pure A = new A[int];
            }
        ''').assertError(GENERIC_CLASS_REF, INCORRECT_TYPE_ARGS, "class A")
        parse('''
            mapped class rolez.lang.Object
            class A
            task Main: {
                val a: pure A[readwrite A];
            }
        ''').assertError(GENERIC_CLASS_REF, INCORRECT_TYPE_ARGS, "class A")
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
    
    @Test
    def testValFieldsInitialized() {
        parse('''
            mapped class rolez.lang.Object
            class A {
                val i: int
                var j: int
                new {
                    this.i = 3;
                    3 + this.i;
                }
                new(val b: boolean, val i: int) {
                    if(b) this.i = i;
                    else  this.i = 0;
                    3 + this.i;
                }
                new(val a: pure A) {
                    this.i = 0;
                    while(this.foo())
                        new A;
                }
               def pure foo: boolean { return false; }
            }
        ''').assertNoErrors
        
        parse('''
            mapped class rolez.lang.Object
            class A {
                val i: int
            }
        ''').assertError(FIELD, VAL_FIELD_NOT_INITIALIZED)
        var program = parse('''
            mapped class rolez.lang.Object
            class A {
                val i: int
                val j: int
                new {}
            }
        ''')
        program.assertError(CONSTR, VAL_FIELD_NOT_INITIALIZED, "field i")
        program.assertError(CONSTR, VAL_FIELD_NOT_INITIALIZED, "field j")
        
        parse('''
            mapped class rolez.lang.Object
            class A {
                val i: int
                new(val b: boolean) {
                    if(b) this.i = 0;
                }
            }
        ''').assertError(CONSTR, VAL_FIELD_NOT_INITIALIZED)
        parse('''
            mapped class rolez.lang.Object
            class A {
                val i: int
                new(val a: boolean, val b: boolean) {
                    if(a) this.i = 2;
                    if(b) this.i = 0;
                }
            }
        ''').assertError(CONSTR, VAL_FIELD_NOT_INITIALIZED)
        parse('''
            mapped class rolez.lang.Object
            class A {
                val i: int
                new(val a: boolean, val b: boolean) {
                    3 + this.i;
                    this.i = 0;
                }
            }
        ''').assertError(MEMBER_ACCESS, VAL_FIELD_NOT_INITIALIZED)
        
        parse('''
            mapped class rolez.lang.Object
            class A {
                val x: int
                new {
                    this.x = 3;
                    this.x = 4;
                }
            }
        ''').assertError(ASSIGNMENT, VAL_FIELD_OVERINITIALIZED)
        parse('''
            mapped class rolez.lang.Object
            class A {
                val x: int
                new(val b: boolean) {
                    if(b)
                        this.x = 3;
                    this.x = 4;
                }
            }
        ''').assertError(ASSIGNMENT, VAL_FIELD_OVERINITIALIZED)
        program = parse('''
            mapped class rolez.lang.Object
            class A {
                val x: int
                new(val b: boolean) {
                    while(b)
                        this.x = 3;
                }
            }
        ''')
        program.assertError(ASSIGNMENT, VAL_FIELD_OVERINITIALIZED)
        program.assertError(CONSTR, VAL_FIELD_NOT_INITIALIZED)
    }
    
    @Test
    def testLocalValInitialized() {
        parse('''
            mapped class rolez.lang.Object
            class A {
                def pure foo: {
                    val i: int = 4;
                    var j: int = 0;
                }
            }
        ''').assertNoErrors
        
        parse('''
            mapped class rolez.lang.Object
            class A {
                def pure foo: {
                    val i: int;
                }
            }
        ''').assertError(LOCAL_VAR, VAL_NOT_INITIALIZED)
    }
    
    @Test
    def testLocalVarsInitialized() {
        parse('''
            mapped class rolez.lang.Object
            class A {
                def pure foo(val x: int): int {
                    var i: int = 0;
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
                    var i: int = 0;
                    while(this.foo(5) > i)
                        this.bar();
                }
            }
        ''').assertNoErrors
        
        parse('''
            mapped class rolez.lang.Object
            class A {
                def pure foo: int {
                    var i: int;
                    return i;
                }
            }
        ''').assertError(VAR_REF, VAR_NOT_INITIALIZED, "variable i")
        parse('''
            mapped class rolez.lang.Object
            class A {
                def pure foo(val x: int): int {
                    var i: int;
                    if(x > 0)
                        i = 5;
                    return i;
                }
            }
        ''').assertError(VAR_REF, VAR_NOT_INITIALIZED, "variable i")
        parse('''
            mapped class rolez.lang.Object
            class A {
                def pure foo(val x: int): int {
                    var i: int;
                    if(x > 0)
                        i = 5;
                    return i;
                }
            }
        ''').assertError(VAR_REF, VAR_NOT_INITIALIZED, "variable i")
    }
    
    @Test
    def testSuperConstrCalls() {
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.String {
                mapped def pure length: int
            }
            class A {
                new {}
                new(val i: int) {}
                def pure foo: boolean { return false; }
            }
            class B extends A {
                new {
                    super();
                    this.foo();
                    this.bar();
                }
                new(val i: int) {
                    this.foo();
                }
                new(val s: pure String) {
                    super(s.length());
                }
                new(val a: pure A) {
                    while(this.foo())
                        this.bar();
                }
                def pure bar: {}
            }
        ''').assertNoErrors
        
        parse('''
            mapped class rolez.lang.Object
            class A {
                new {
                    3;
                    super();
                }
            }
        ''').assertError(SUPER_CONSTR_CALL, SUPER_CONSTR_CALL_FIRST)
        
        parse('''
            mapped class rolez.lang.Object
            class A {
                new(val i: int) {}
            }
            class B extends A
        ''').assertError(CLASS, MISSING_SUPER_CONSTR_CALL)
        parse('''
            mapped class rolez.lang.Object
            class A {
                new(val i: int) {}
            }
            class B extends A {
                new {}
            }
        ''').assertError(CONSTR, MISSING_SUPER_CONSTR_CALL)
        
        parse('''
            mapped class rolez.lang.Object
            class A {
                new(val i: int) {}
                def pure foo: {}
            }
            class B extends A {
                new {
                    this.foo();
                    super(5);
                }
            }
        ''').assertError(THIS, THIS_BEFORE_SUPER_CONSTR_CALL)
        parse('''
            mapped class rolez.lang.Object
            class A {
                new(val i: int) {}
                def pure foo: int { return 1; }
            }
            class B extends A {
                new {
                    super(this.foo());
                }
            }
        ''').assertError(THIS, THIS_BEFORE_SUPER_CONSTR_CALL)
        parse('''
            mapped class rolez.lang.Object
            class A {
                new(val o: pure Object) {}
            }
            class B extends A {
                new {
                    super(new A(this));
                }
            }
        ''').assertError(THIS, THIS_BEFORE_SUPER_CONSTR_CALL)
        
        parse('''
            mapped class rolez.lang.Object
            class A {
                def pure foo: { super(); }
            }
        ''').assertError(SUPER_CONSTR_CALL, INCORRECT_SUPER_CONSTR_CALL)
        parse('''
            task Main: { super(); }
        ''').assertError(SUPER_CONSTR_CALL, INCORRECT_SUPER_CONSTR_CALL)
    }
    
    @Test
    def testExprStmt() {
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.String {
                mapped def pure length: int
            }
            class rolez.lang.Task
            task Main: {
                var i: int;
                i = 5 - 2;
                new String;
                new String.length();
                start Main;
            }
        ''').assertNoIssues
        
        parse('''
            task Main: { 5; }
        ''').assertWarning(INT_LITERAL, OUTER_EXPR_NO_SIDE_FX)
        parse('''
            task Main: { 3 == 5; }
        ''').assertWarning(EQUALITY_EXPR, OUTER_EXPR_NO_SIDE_FX)
        parse('''
            task Main: { true && 4 > 2; }
        ''').assertWarning(LOGICAL_EXPR, OUTER_EXPR_NO_SIDE_FX)
        parse('''
            task Main: { null; }
        ''').assertWarning(NULL_LITERAL, OUTER_EXPR_NO_SIDE_FX)
        parse('''
            task Main: {
                val i: int = 5;
                i;
            }
        ''').assertWarning(VAR_REF, OUTER_EXPR_NO_SIDE_FX)
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.String
            task Main: {
                val s: pure String = "Hello";
                s as pure Object;
            }
        ''').assertWarning(CAST, OUTER_EXPR_NO_SIDE_FX)
        
        parse('''
            task Main: {
                var i: int;
                (i = 4);
            }
        ''').assertWarning(PARENTHESIZED, OUTER_EXPR_NO_SIDE_FX)
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.String
            task Main: { (new String); }
        ''').assertWarning(PARENTHESIZED, OUTER_EXPR_NO_SIDE_FX)
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.String {
                mapped def pure length: int
            }
            task Main: {
                val s: pure String = new String;
                2 * s.length();
            }
        ''').assertWarning(ARITHMETIC_BINARY_EXPR, OUTER_EXPR_NO_SIDE_FX)
    }
    
    @Test
    def testNullTypeUsed() {
        parse('''
            task Main: Null { return null; }
        ''').assertError(NULL, NULL_TYPE_USED)
    }
    
    @Test
    def testMappedField() {
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.Array {
                mapped val length: int
                mapped new(val length: int)
            }
            class A {
                var length: int
            }
        ''').assertNoErrors
        
        parse('''
            mapped class rolez.lang.Object
            class A {
                mapped val length: int
            }
        ''').assertError(FIELD, MAPPED_IN_NORMAL_CLASS)
    }
    
    @Test
    def testMappedMethod() {
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.String {
                mapped def pure length: int
            }
            class A {
                def pure length: int { return 0; }
            }
        ''').assertNoErrors
        
        parse('''
            mapped class rolez.lang.Object
            class A {
                mapped def pure length: int
            }
        ''').assertError(METHOD, MAPPED_IN_NORMAL_CLASS)
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.String {
                mapped def pure length: int { return 0; }
            }
        ''').assertError(BLOCK, MAPPED_WITH_BODY)
        parse('''
            mapped class rolez.lang.Object
            class A {
                def pure length: int
            }
        ''').assertError(METHOD, MISSING_BODY)
    }
    
    @Test
    def testMappedConstr() {
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.Array {
                mapped new(val length: int)
            }
            class A {
                new(val length: int) {}
            }
        ''').assertNoErrors
        
        parse('''
            mapped class rolez.lang.Object
            class A {
                mapped new(val length: int)
            }
        ''').assertError(CONSTR, MAPPED_IN_NORMAL_CLASS)
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.Array {
                mapped new(val length: int) {}
            }
        ''').assertError(BLOCK, MAPPED_WITH_BODY)
        parse('''
            mapped class rolez.lang.Object
            class A {
                new
            }
        ''').assertError(CONSTR, MISSING_BODY)
    }
    
    @Test
    def testMappedClass() {
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.String
            mapped class rolez.lang.Array {
                mapped new(val length: int)
            }
            class A
        ''').assertNoErrors
        
        parse("class rolez.lang.Object").assertError(CLASS, CLASS_ACTUALLY_MAPPED)
        parse("class rolez.lang.String").assertError(CLASS, CLASS_ACTUALLY_MAPPED)
        parse("class rolez.lang.Array").assertError(CLASS, CLASS_ACTUALLY_MAPPED)
        
        parse('''
            mapped class rolez.lang.Object
            mapped class A
        ''').assertError(CLASS, UNKNOWN_MAPPED_CLASS)
    }
    
    @Test
    def testObjectClass() {
        parse('''
            class A
            mapped class rolez.lang.Object extends A
        ''').assertError(CLASS, INCORRECT_MAPPED_SUPERCLASS)
    }

    @Test
    def testStringClass() {
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.String
        ''').assertNoErrors
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.String extends Object {
                mapped def pure length: int
            }
        ''').assertNoErrors
        
        parse('''
            mapped class rolez.lang.Object
            class A
            mapped class rolez.lang.String extends A
        ''').assertError(CLASS, INCORRECT_MAPPED_SUPERCLASS)
        
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.String {
                mapped new {}
            }
        ''').assertError(CONSTR, INCORRECT_MAPPED_CONSTR)
        
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.String {
                def pure length: int
            }
        ''').assertError(METHOD, INCORRECT_MAPPED_METHOD)
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.String {
                mapped def pure length: double
            }
        ''').assertError(DOUBLE, INCORRECT_MAPPED_METHOD)
        
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.String {
                mapped var length: int
            }
        ''').assertError(FIELD, UNKNOWN_MAPPED_FIELD)
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.String {
                mapped def pure foo: int
            }
        ''').assertError(METHOD, UNKNOWN_MAPPED_METHOD)
    }
    
    @Test
    def testArrayClass() {
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.Array {
                mapped new(val length: int)
            }
        ''').assertNoErrors
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.Array extends Object {
                mapped new(val length: int)
                mapped val length: int
            }
        ''').assertNoErrors
        
        parse('''
            mapped class rolez.lang.Object
            class A
            mapped class rolez.lang.Array extends A
        ''').assertError(CLASS, INCORRECT_MAPPED_SUPERCLASS)
        
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.Array
        ''').assertError(CLASS, INCORRECT_MAPPED_CONSTR)
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.Array {
                mapped new {}
            }
        ''').assertError(CONSTR, INCORRECT_MAPPED_CONSTR)
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.Array {
                mapped new(val i: int, val j: int) {}
            }
        ''').assertError(CONSTR, INCORRECT_MAPPED_CONSTR)
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.Array {
                mapped new(val i: double) {}
            }
        ''').assertError(DOUBLE, INCORRECT_MAPPED_CONSTR)
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.Array {
                new(val i: int) {}
            }
        ''').assertError(CONSTR, INCORRECT_MAPPED_CONSTR)
        
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.Array {
                mapped new(val length: int)
                val length: int
            }
        ''').assertError(FIELD, INCORRECT_MAPPED_FIELD)
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.Array {
                mapped new(val length: int)
                mapped var length: int
            }
        ''').assertError(FIELD, INCORRECT_MAPPED_FIELD)
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.Array {
                mapped new(val length: int)
                mapped val length: double
            }
        ''').assertError(DOUBLE, INCORRECT_MAPPED_FIELD)
        
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.Array {
                mapped new(val length: int)
                mapped val foo: int
            }
        ''').assertError(FIELD, UNKNOWN_MAPPED_FIELD)
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.Array {
                mapped new(val length: int)
                mapped def pure foo: int
            }
        ''').assertError(METHOD, UNKNOWN_MAPPED_METHOD)
    }
    
    @Test
    def testTaskClass() {
        parse('''
            mapped class rolez.lang.Object
            class rolez.lang.Task
        ''').assertNoErrors
        parse('''
            mapped class rolez.lang.Object
            class rolez.lang.Task extends Object
        ''').assertNoErrors
        
        parse('''
            mapped class rolez.lang.Object
            class A
            class rolez.lang.Task extends A
        ''').assertError(CLASS, INCORRECT_MAPPED_SUPERCLASS)
    }
}
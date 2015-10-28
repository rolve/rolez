package ch.trick17.rolez.lang

import ch.trick17.rolez.lang.generator.RolezGenerator
import ch.trick17.rolez.lang.rolez.Program
import javax.inject.Inject
import org.eclipse.xtext.generator.InMemoryFileSystemAccess
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.eclipse.xtext.junit4.validation.ValidationTestHelper
import org.junit.Test
import org.junit.runner.RunWith

import static org.hamcrest.Matchers.*

import static extension org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert

@RunWith(XtextRunner)
@InjectWith(RolezInjectorProvider)
class GeneratorTest {
    
    @Inject extension ParseHelper<Program>
    @Inject extension ValidationTestHelper
    @Inject extension TestUtilz
    @Inject RolezGenerator generator
    
    def classes() {
        newResourceSet.with('''
            mapped class rolez.lang.Object {
                mapped def readonly equals(val o: readonly Object): boolean
                mapped def readonly hashCode: int
                mapped def readonly toString: pure String
            }
            mapped class rolez.lang.String {
                mapped def pure length: int
                mapped def pure substring(val b: int, val e: int): pure String
            }
            mapped class rolez.lang.Array[T] {
                mapped new(val i: int)
                mapped def readonly  get(val i: int): T
                mapped def readwrite set(val i: int, val o: T):
            }
            class Base {
                var foo: int
            }
            class foo.bar.Base {
                new {}
                new(val i: int) {}
            }
        ''')
    }
    
    @Test
    def testFiles() {
        var program = parse('''
            mapped class rolez.lang.Object
            class A
        ''', classes)
        program.assertNoErrors
        var fsa = new InMemoryFileSystemAccess
        fsa.allFiles.size.assertThat(is(0))
        generator.doGenerate(program.eResource, fsa)
        fsa.allFiles.size.assertThat(is(1))
        fsa.textFiles.size.assertThat(is(1))
        
        program = parse('''
            class A
            class foo.B
            class foo.bar.C
        ''', classes)
        program.assertNoErrors
        fsa = new InMemoryFileSystemAccess
        fsa.allFiles.size.assertThat(is(0))
        generator.doGenerate(program.eResource, fsa)
        fsa.allFiles.size.assertThat(is(3))
        fsa.textFiles.size.assertThat(is(3))
    }
    
    @Test
    def testClass() {
        parse('''
            class A
        ''', classes).generate.assertEquals('''
            public class A extends java.lang.Object {
            }
        ''')
        parse('''
            class foo.bar.A
        ''', classes).generate.assertEquals('''
            package foo.bar;
            
            public class A extends java.lang.Object {
            }
        ''')
        
        parse('''
            class A extends Base
        ''', classes).generate.assertEquals('''
            public class A extends Base {
            }
        ''')
        parse('''
            class A extends foo.bar.Base
        ''', classes).generate.assertEquals('''
            public class A extends foo.bar.Base {
            }
        ''')
    }
    
    @Test
    def testField() {
        parse('''
            class foo.A {
                val i: int
                var b: boolean
                var a: readwrite foo.A
                
                new {
                    this.i = 0;
                }
            }
        ''', classes).generate.assertEquals('''
            package foo;
            
            public class A extends java.lang.Object {
                public final int i;
                public boolean b;
                public foo.A a;
                
                public A() {
                    this.i = 0;
                }
            }
        ''')
    }
    
    @Test
    def testMethod() {
        parse('''
            class A {
                def pure foo: {}
                def readwrite foo(val i: int): int { return i; }
            }
        ''', classes).generate.assertEquals('''
            public class A extends java.lang.Object {
                
                public void foo() {
                }
                
                public int foo(final int i) {
                    return i;
                }
            }
        ''')
    }
    
    @Test
    def testConstr() {
        parse('''
            package foo
            class A {
                new {}
                new(val i: int, val a: pure A) {}
            }
        ''', classes).generate.assertEquals('''
            package foo;
            
            public class A extends java.lang.Object {
                
                public A() {
                }
                
                public A(final int i, final foo.A a) {
                }
            }
        ''')
    }
    
    @Test
    def testLocalVarDecl() {
        parse('''
            var j: int;
            var k: int = 4;
            val a: pure A = null;
        '''.withFrame, classes).generate.assertEquals('''
            int j;
            int k = 4;
            final A a = null;
        '''.withJavaFrame)
    }
    
    @Test
    def testIfStmt() {
        parse('''
            if(b)
                this.bar;
            else
                this.bar;
        '''.withFrame, classes).generate.assertEquals('''
            if(b)
                this.bar();
            else
                this.bar();
        '''.withJavaFrame)
        
        parse('''
            if(b) {
                this.bar;
            }
        '''.withFrame, classes).generate.assertEquals('''
            if(b) {
                this.bar();
            }
        '''.withJavaFrame)
    }
    
    @Test
    def testWhileLoop() {
        parse('''
            while(b)
                this.bar;
        '''.withFrame, classes).generate.assertEquals('''
            while(b)
                this.bar();
        '''.withJavaFrame)
    }
    
    @Test
    def testSuperConstrCall() {
        parse('''
            class A extends Base {
                new { super; }
            }
        ''', classes).generate.assertEquals('''
            public class A extends Base {
                
                public A() {
                    super();
                }
            }
        ''')
        
        parse('''
            class A extends foo.bar.Base {
                new { super(42); }
            }
        ''', classes).generate.assertEquals('''
            public class A extends foo.bar.Base {
                
                public A() {
                    super(42);
                }
            }
        ''')
    }
    
    @Test
    def testReturn() {
        parse('''
            class A {
                def pure foo: int {
                    return 0;
                }
                def pure bar: {
                    return;
                }
            }
        ''', classes).generate.assertEquals('''
            public class A extends java.lang.Object {
                
                public int foo() {
                    return 0;
                }
                
                public void bar() {
                    return;
                }
            }
        ''')
    }
    
    @Test
    def testExprStmt() {
        parse('''
            var j: int;
            j = i;
            new Base;
            new Base.hashCode;
        '''.withFrame, classes).generate.assertEquals('''
            int j;
            j = i;
            new Base();
            new Base().hashCode();
        '''.withJavaFrame)
        
        parse('''
            new Object == new Object;
            new Base.foo;
            new Array[int](new Base.hashCode).get(new Base.hashCode);
            -new Object.hashCode;
        '''.withFrame, classes).generate.assertEquals('''
            new java.lang.Object();
            new java.lang.Object();
            new Base();
            new Base().hashCode();
            new Base().hashCode();
            new java.lang.Object().hashCode();
        '''.withJavaFrame)
    }
    
    @Test
    def testAssignment() {
        parse('''
            var j: int;
            j = i;
            new Base.foo = j = 42;
            j = 2 + 2;
        '''.withFrame, classes).generate.assertEquals('''
            int j;
            j = i;
            new Base().foo = j = 42;
            j = 2 + 2;
        '''.withJavaFrame)
    }
    
    @Test
    def testBinaryExpr() {
        parse('''
            var c: boolean = true || new Base.equals(new Base);
            var d: boolean = b && false || true;
            var e: boolean = true && (b || true);
            var f: boolean = 2 < 3 || 3 < i;
            
            var j: int = 3 + 3 + 3;
            var k: int = 3 - 2 - 1;
            var l: int = 3 - (2 - 1);
            var m: int = (1 + 2) * (3 + 4);
        '''.withFrame, classes).generate.assertEquals('''
            boolean c = true || new Base().equals(new Base());
            boolean d = (b && false) || true;
            boolean e = true && (b || true);
            boolean f = (2 < 3) || (3 < i);
            int j = (3 + 3) + 3;
            int k = (3 - 2) - 1;
            int l = 3 - (2 - 1);
            int m = (1 + 2) * (3 + 4);
        '''.withJavaFrame)
    }
    
    @Test
    def testCast() {
        parse('''
            var o: pure Object = new Base as readonly Object;
            o = ("Hi " + "World!") as readonly Object;
        '''.withFrame, classes).generate.assertEquals('''
            java.lang.Object o = (java.lang.Object) new Base();
            o = (java.lang.Object) ("Hi " + "World!");
        '''.withJavaFrame)
    }
    
    @Test
    def testUnaryExpr() {
        parse('''
            var c: boolean = !false;
            var d: boolean = !(b && false);
            var e: boolean = !new Base.equals(new Base);
            
            var j: int = -3;
            var k: int = -(3 - 2);
            var l: int = -new Base.hashCode;
        '''.withFrame, classes).generate.assertEquals('''
            boolean c = !false;
            boolean d = !(b && false);
            boolean e = !new Base().equals(new Base());
            int j = -3;
            int k = -(3 - 2);
            int l = -new Base().hashCode();
        '''.withJavaFrame)
    }
    
    @Test
    def testMemberAccess() {
        parse('''
            "Hello".toString.length;
            "Hello".equals("Hi");
            ("Hello " + "World!").length;
            (new Base as readonly Object).hashCode;
            "Hello".substring(1, 3);
            this.bar;
            
            var a: readwrite Array[int] = new Array[int](2);
            a.set(0, 42);
            var j: int = a.get(0);
            var aa: readwrite Array[readwrite Array[int]] = new Array[readwrite Array[int]](1);
            aa.set(0, a);
            var l: int = aa.get(0).get(0);
        '''.withFrame, classes).generate.assertEquals('''
            "Hello".toString().length();
            "Hello".equals("Hi");
            ("Hello " + "World!").length();
            ((java.lang.Object) new Base()).hashCode();
            "Hello".substring(1, 3);
            this.bar();
            int[] a = new int[2];
            a[0] = 42;
            int j = a[0];
            int[][] aa = new int[1][];
            aa[0] = a;
            int l = aa[0][0];
        '''.withJavaFrame)
    }
    
    private def generate(Program it) {
        assertNoErrors
        val fsa = new InMemoryFileSystemAccess
        generator.doGenerate(eResource, fsa)
        fsa.allFiles.size.assertThat(is(1))
        fsa.textFiles.size.assertThat(is(1))
        fsa.textFiles.values.head
    }
    
    private def withFrame(CharSequence it) {'''
        class A {
            def readwrite foo(val i: int, val b: boolean): {
                «it»
            }
            def pure bar: {}
        }
    '''}
    
    private def withJavaFrame(CharSequence it) {'''
        public class A extends java.lang.Object {
            
            public void foo(final int i, final boolean b) {
                «it»
            }
            
            public void bar() {
            }
        }
    '''}
    
    private def assertEquals(CharSequence it, CharSequence expected) {
        Assert.assertEquals(expected.toString, it.toString)
    }
}
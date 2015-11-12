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
                new(val i: int, val j: int) {}
            }
        ''')
    }
    
    @Test def testFiles() {
        var program = parse('''
            mapped class rolez.io.PrintStream
            class A
        ''', classes)
        program.assertNoErrors
        var fsa = new InMemoryFileSystemAccess
        fsa.allFiles.size.assertThat(is(0))
        generator.doGenerate(program.eResource, fsa)
        fsa.allFiles.size.assertThat(is(1))
        fsa.textFiles.size.assertThat(is(1))
        fsa.textFiles.keySet.head.assertThat(endsWith("A.java"))
        
        program = parse('''
            class A
            class foo.B
            class foo.bar.C
            object D
            mapped object rolez.lang.System
        ''', classes)
        program.assertNoErrors
        fsa = new InMemoryFileSystemAccess
        fsa.allFiles.size.assertThat(is(0))
        generator.doGenerate(program.eResource, fsa)
        fsa.allFiles.size.assertThat(is(5))
        fsa.textFiles.size.assertThat(is(5))
    }
    
    @Test def testNormalClass() {
        parse('''
            class A
        ''', classes).generate.assertEqualsJava('''
            public class A extends java.lang.Object {
            }
        ''')
        parse('''
            class foo.bar.A
        ''', classes).generate.assertEqualsJava('''
            package foo.bar;
            
            public class A extends java.lang.Object {
            }
        ''')
        
        parse('''
            class A extends Base
        ''', classes).generate.assertEqualsJava('''
            public class A extends Base {
            }
        ''')
        parse('''
            class A extends foo.bar.Base
        ''', classes).generate.assertEqualsJava('''
            public class A extends foo.bar.Base {
            }
        ''')
    }
    
    @Test def testSingletonClass() {
        parse('''
            object A
        ''', classes).generate.assertEqualsJava('''
            public final class A extends java.lang.Object {
                
                public static final A INSTANCE = new A();
                
                private A() {}
            }
        ''')
    }
    
    @Test def testMappedSingletonClass() {
        parse('''
            mapped class rolez.io.PrintStream
            mapped object rolez.lang.System {
                mapped val out: readonly rolez.io.PrintStream
                mapped def readonly exit(val status: int):
                mapped def readonly lineSeparator: pure String
            }
        ''', classes).generate.assertEqualsJava('''
            package rolez.lang;
            
            public final class System extends java.lang.Object {
                
                public static final System INSTANCE = new System();
                
                private System() {}
                
                public final java.io.PrintStream out = java.lang.System.out;
                
                public void exit(final int status) {
                    java.lang.System.exit(status);
                }
                
                public java.lang.String lineSeparator() {
                    return java.lang.System.lineSeparator();
                }
            }
        ''')
    }
    
    @Test def testField() {
        parse('''
            class foo.A {
                val i: int
                val j: int = 0
                var b: boolean
                var a: readwrite foo.A
                
                new {
                    this.i = 0;
                }
            }
        ''', classes).generate.assertEqualsJava('''
            package foo;
            
            public class A extends java.lang.Object {
                
                public final int i;
                
                public final int j = 0;
                
                public boolean b;
                
                public foo.A a;
                
                public A() {
                    this.i = 0;
                }
            }
        ''')
        
        parse('''
            object A {
                val j: int = 0
            }
        ''', classes).generate.assertEqualsJava('''
            public final class A extends java.lang.Object {
                
                public static final A INSTANCE = new A();
                
                private A() {}
                
                public final int j = 0;
            }
        ''')
    }
    
    @Test def testMethod() {
        parse('''
            class A {
                def pure foo: {}
                def readwrite foo(val i: int): int { return i; }
            }
        ''', classes).generate.assertEqualsJava('''
            public class A extends java.lang.Object {
                
                public void foo() {
                }
                
                public int foo(final int i) {
                    return i;
                }
            }
        ''')
        
        parse('''
            object A {
                def pure foo: {}
            }
        ''', classes).generate.assertEqualsJava('''
            public final class A extends java.lang.Object {
                
                public static final A INSTANCE = new A();
                
                private A() {}
                
                public void foo() {
                }
            }
        ''')
    }
    
    @Test def testConstr() {
        parse('''
            package foo
            class A {
                new {}
                new(val i: int, val a: pure A) {}
            }
        ''', classes).generate.assertEqualsJava('''
            package foo;
            
            public class A extends java.lang.Object {
                
                public A() {
                }
                
                public A(final int i, final foo.A a) {
                }
            }
        ''')
    }
    
    @Test def testLocalVarDecl() {
        parse('''
            var j: int;
            var k: int = 4;
            val a: pure A = null;
        '''.withFrame, classes).generate.assertEqualsJava('''
            int j;
            int k = 4;
            final A a = null;
        '''.withJavaFrame)
    }
    
    @Test def testIfStmt() {
        parse('''
            if(b)
                this.bar;
            else
                this.bar;
        '''.withFrame, classes).generate.assertEqualsJava('''
            if(b)
                this.bar();
            else
                this.bar();
        '''.withJavaFrame)
        
        parse('''
            if(b) {
                this.bar;
            }
        '''.withFrame, classes).generate.assertEqualsJava('''
            if(b) {
                this.bar();
            }
        '''.withJavaFrame)
    }
    
    @Test def testWhileLoop() {
        parse('''
            while(b)
                this.bar;
        '''.withFrame, classes).generate.assertEqualsJava('''
            while(b)
                this.bar();
        '''.withJavaFrame)
    }
    
    @Test def testSuperConstrCall() {
        parse('''
            class A extends Base {
                new { super; }
            }
        ''', classes).generate.assertEqualsJava('''
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
        ''', classes).generate.assertEqualsJava('''
            public class A extends foo.bar.Base {
                
                public A() {
                    super(42);
                }
            }
        ''')
    }
    
    @Test def testReturn() {
        parse('''
            class A {
                def pure foo: int {
                    return 0;
                }
                def pure bar: {
                    return;
                }
            }
        ''', classes).generate.assertEqualsJava('''
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
    
    @Test def testExprStmt() {
        parse('''
            var j: int;
            j = i;
            new Base;
            new Base.hashCode;
        '''.withFrame, classes).generate.assertEqualsJava('''
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
        '''.withFrame, classes).generate.assertEqualsJava('''
            new java.lang.Object();
            new java.lang.Object();
            new Base();
            new Base().hashCode();
            new Base().hashCode();
            new java.lang.Object().hashCode();
        '''.withJavaFrame)
    }
    
    @Test def testAssignment() {
        parse('''
            var j: int;
            j = i;
            new Base.foo = j = 42;
            j = 2 + 2;
        '''.withFrame, classes).generate.assertEqualsJava('''
            int j;
            j = i;
            new Base().foo = j = 42;
            j = 2 + 2;
        '''.withJavaFrame)
    }
    
    @Test def testBinaryExpr() {
        parse('''
            var c: boolean = true || new Base.equals(new Base);
            var d: boolean = b && false || true;
            var e: boolean = true && (b || true);
            var f: boolean = 2 < 3 || 3 < i;
            
            var j: int = 3 + 3 + 3;
            var k: int = 3 - 2 - 1;
            var l: int = 3 - (2 - 1);
            var m: int = (1 + 2) * (3 + 4);
        '''.withFrame, classes).generate.assertEqualsJava('''
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
    
    @Test def testCast() {
        parse('''
            var o: pure Object = new Base as readonly Object;
            o = ("Hi " + "World!") as readonly Object;
        '''.withFrame, classes).generate.assertEqualsJava('''
            java.lang.Object o = (java.lang.Object) new Base();
            o = (java.lang.Object) ("Hi " + "World!");
        '''.withJavaFrame)
    }
    
    @Test def testUnaryExpr() {
        parse('''
            var c: boolean = !false;
            var d: boolean = !(b && false);
            var e: boolean = !new Base.equals(new Base);
            
            var j: int = -3;
            var k: int = -(3 - 2);
            var l: int = -new Base.hashCode;
        '''.withFrame, classes).generate.assertEqualsJava('''
            boolean c = !false;
            boolean d = !(b && false);
            boolean e = !new Base().equals(new Base());
            int j = -3;
            int k = -(3 - 2);
            int l = -new Base().hashCode();
        '''.withJavaFrame)
    }
    
    @Test def testMemberAccess() {
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
            aa.set(1 - 1, a);
            var l: int = aa.get(0).get(0);
        '''.withFrame, classes).generate.assertEqualsJava('''
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
            aa[1 - 1] = a;
            int l = aa[0][0];
        '''.withJavaFrame)
    }
    
    @Test def testNew() {
        parse('''
            new Base;
            new (foo.bar.Base)(0);
            new (foo.bar.Base)(3 * 2 + 2);
            new (foo.bar.Base)("Hello".length, 0);
            var a: pure Object = new Array[int](10 * 10);
        '''.withFrame, classes).generate.assertEqualsJava('''
            new Base();
            new foo.bar.Base(0);
            new foo.bar.Base((3 * 2) + 2);
            new foo.bar.Base("Hello".length(), 0);
            java.lang.Object a = new int[10 * 10];
        '''.withJavaFrame)
    }
    
    @Test def testParenthesized() {
        parse('''
            var j: int = (0);
            var k: int = (2 + 2) * 3;
        '''.withFrame, classes).generate.assertEqualsJava('''
            int j = 0;
            int k = (2 + 2) * 3;
        '''.withJavaFrame)
    }
    
    @Test def testStringLiteral() {
        parse('''
            var s: pure String = "Hello World!";
            s = "";
            s = "'";
            s = "\'";
            s = "\n";
            s = "\"";
            s = "\\H";
        '''.withFrame, classes).generate.assertEqualsJava('''
            java.lang.String s = "Hello World!";
            s = "";
            s = "\'";
            s = "\'";
            s = "\n";
            s = "\"";
            s = "\\H";
        '''.withJavaFrame)
    }
    
    @Test def testCharLiteral() {
        parse('''
            var c: char = 'H';
            c = '\'';
            c = '\n';
            c = '"';
            c = '\"';
            c = '\\';
        '''.withFrame, classes).generate.assertEqualsJava('''
            char c = 'H';
            c = '\'';
            c = '\n';
            c = '\"';
            c = '\"';
            c = '\\';
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
    
    private def assertEqualsJava(CharSequence it, CharSequence javaCode) {
        // TODO: check that Java code compiles
        Assert.assertEquals(javaCode.toString, it.toString)
    }
}
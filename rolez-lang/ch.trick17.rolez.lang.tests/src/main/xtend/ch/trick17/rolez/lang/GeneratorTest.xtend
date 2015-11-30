package ch.trick17.rolez.lang

import ch.trick17.rolez.lang.generator.RolezGenerator
import ch.trick17.rolez.lang.rolez.Program
import com.google.common.io.ByteStreams
import com.google.common.io.CharStreams
import java.net.URI
import java.util.regex.Pattern
import javax.inject.Inject
import javax.tools.Diagnostic
import javax.tools.DiagnosticListener
import javax.tools.FileObject
import javax.tools.ForwardingJavaFileManager
import javax.tools.ForwardingJavaFileObject
import javax.tools.JavaFileManager.Location
import javax.tools.JavaFileObject
import javax.tools.SimpleJavaFileObject
import javax.tools.ToolProvider
import org.eclipse.xtext.generator.InMemoryFileSystemAccess
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.eclipse.xtext.junit4.validation.ValidationTestHelper
import org.junit.Test
import org.junit.runner.RunWith

import static ch.trick17.rolez.lang.GeneratorTest.*
import static org.hamcrest.Matchers.*

import static extension org.hamcrest.MatcherAssert.assertThat
import static extension org.junit.Assert.*

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
                mapped def readonly equals(o: readonly Object): boolean
                mapped def readonly hashCode: int
                mapped def readonly toString: pure String
            }
            mapped class rolez.lang.String {
                mapped def pure length: int
                mapped def pure substring(b: int, e: int): pure String
            }
            mapped class rolez.lang.Array[T] {
                mapped new(i: int)
                mapped def readonly  get(i: int): T
                mapped def readwrite set(i: int, o: T):
            }
            mapped class rolez.io.PrintStream {
                mapped new(file: readonly String)
            }
            class Base {
                var foo: int
            }
            class foo.bar.Base {
                new {}
                new(i: int) {}
                new(i: int, j: int) {}
            }
        ''')
    }
    
    @Test def testFiles() {
        var program = parse('''
            mapped class rolez.io.PrintStream {
                mapped new(s: pure String)
            }
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
                
                public A() {
                    super();
                }
            }
        ''')
        parse('''
            class foo.bar.A
        ''', classes).generate.assertEqualsJava('''
            package foo.bar;
            
            public class A extends java.lang.Object {
                
                public A() {
                    super();
                }
            }
        ''')
        
        parse('''
            class A extends Base
        ''', classes).generate.assertEqualsJava('''
            public class A extends Base {
                
                public A() {
                    super();
                }
            }
        ''')
        parse('''
            class A extends foo.bar.Base
        ''', classes).generate.assertEqualsJava('''
            public class A extends foo.bar.Base {
                
                public A() {
                    super();
                }
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
            mapped object rolez.lang.System {
                mapped val out: readonly rolez.io.PrintStream
                mapped def readonly exit(status: int):
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
    
    @Test def testTask() {
        parse('''
            task Main: pure Base {
                return new Base;
            }
        ''', classes).generate.assertEqualsJava('''
            public final class Main implements java.util.concurrent.Callable<Base> {
                
                public Base call() throws java.lang.Exception {
                    return new Base();
                }
            }
        ''')
        parse('''
            task Main: int {
                return 42;
            }
        ''', classes).generate.assertEqualsJava('''
            public final class Main implements java.util.concurrent.Callable<java.lang.Integer> {
                
                public java.lang.Integer call() throws java.lang.Exception {
                    return 42;
                }
            }
        ''')
        parse('''
            task Main: {
                new Base;
            }
        ''', classes).generate.assertEqualsJava('''
            public final class Main implements java.util.concurrent.Callable<java.lang.Void> {
                
                public java.lang.Void call() throws java.lang.Exception {
                    new Base();
                    return null;
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
                    super();
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
                def readwrite foo(i: int): int { return i; }
            }
        ''', classes).generate.assertEqualsJava('''
            public class A extends java.lang.Object {
                
                public A() {
                    super();
                }
                
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
        
        parse('''
            val j: int = 0;
            val o: pure Object = new (rolez.io.PrintStream)("foo.txt");
            val k: int = 0;
        '''.withFrame, classes).generate.assertEqualsJava('''
            try {
                final int j = 0;
                final java.lang.Object o = new java.io.PrintStream("foo.txt");
                final int k = 0;
            }
            catch(java.io.FileNotFoundException e) {
                «throwExceptionWrapper("e")»
            }
        '''.withJavaFrame)
        parse('''
            if(new (rolez.io.PrintStream)("foo.txt").equals("")) {
                new (rolez.io.PrintStream)("bar.txt");
            }
        '''.withFrame, classes).generate.assertEqualsJava('''
            try {
                if(new java.io.PrintStream("foo.txt").equals("")) {
                    new java.io.PrintStream("bar.txt");
                }
                else {
                }
            }
            catch(java.io.FileNotFoundException e) {
                «throwExceptionWrapper("e")»
            }
        '''.withJavaFrame)
        
        // TODO: Test with multiple types of exceptions, with RuntimeException(s), and with methods that throw exceptions
    }
    
    @Test def testConstr() {
        parse('''
            package foo
            class A
        ''', classes).generate.assertEqualsJava('''
            package foo;
            
            public class A extends java.lang.Object {
                
                public A() {
                    super();
                }
            }
        ''')
        parse('''
            package foo
            class A {
                new {}
                new(i: int, a: pure A) {}
            }
        ''', classes).generate.assertEqualsJava('''
            package foo;
            
            public class A extends java.lang.Object {
                
                public A() {
                    super();
                }
                
                public A(final int i, final foo.A a) {
                    super();
                }
            }
        ''')
        
        parse('''
            class A {
                new {
                    new (rolez.io.PrintStream)("test.txt");
                }
            }
        ''', classes).generate.assertEqualsJava('''
            public class A extends java.lang.Object {
                
                public A() {
                    super();
                    try {
                        new java.io.PrintStream("test.txt");
                    }
                    catch(java.io.FileNotFoundException e) {
                        «throwExceptionWrapper("e")»
                    }
                }
            }
        ''')
        
        // TODO: Test implicit constr referring to exception-throwing constr
    }
    
    @Test def testBlock() {
        parse('''
            {
                var j: int;
            }
            {
                var c: char;
                {
                    var k: int;
                    var l: int;
                }
                var m: int;
            }
        '''.withFrame, classes).generate.assertEqualsJava('''
            {
                int j;
            }
            {
                char c;
                {
                    int k;
                    int l;
                }
                int m;
            }
        '''.withJavaFrame)
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
            else {
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
    
    @Test def testForLoop() {
        parse('''
            for(var n: int = 0; n < 10; n += 1)
                this.bar;
        '''.withFrame, classes).generate.assertEqualsJava('''
            {
                int n = 0;
                while(n < 10) {
                    this.bar();
                    n = n + 1;
                }
            }
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
                
                public A() {
                    super();
                }
                
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
        
        parse('''
            var a: boolean = false;
            a |= true;
            a &= false;
            var j: int = 42;
            j += 2;
            j -= 1;
            j *= 2;
            j /= 3;
            j %= 2;
        '''.withFrame, classes).generate.assertEqualsJava('''
            boolean a = false;
            a = a || true;
            a = a && false;
            int j = 42;
            j = j + 2;
            j = j - 1;
            j = j * 2;
            j = j / 3;
            j = j % 2;
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
            def readwrite foo(i: int, b: boolean): {
                «it»
            }
            def pure bar: {}
        }
    '''}
    
    private def withJavaFrame(CharSequence it) {'''
        public class A extends java.lang.Object {
            
            public A() {
                super();
            }
            
            public void foo(final int i, final boolean b) {
                «it»
            }
            
            public void bar() {
            }
        }
    '''}
    
    private def throwExceptionWrapper(String e) {
        '''throw new java.lang.RuntimeException("ROLEZ EXCEPTION WRAPPER", «e»);'''
    }
    
    private def assertEqualsJava(CharSequence it, CharSequence javaCode) {
        javaCode.assertCompilable
        javaCode.toString.assertEquals(it.toString)
    }
    
    static val className = Pattern.compile("public (final )?class (\\w+) ")
    static val filterMsg = Pattern.compile("cannot find symbol|package (\\w|\\.)+ does not exist")
    
    private def assertCompilable(CharSequence code) {
        val matcher = className.matcher(code)
        matcher.find.assertTrue
        
        val uri = URI.create("string:///" + matcher.group(2) + ".java")
        val src = new SimpleJavaFileObject(uri, JavaFileObject.Kind.SOURCE) {
            override getCharContent(boolean _) { code }
        }
        
        // Collect errors
        val errors = new StringBuilder
        val listener = new DiagnosticListener<JavaFileObject> {
            override report(Diagnostic<? extends JavaFileObject> it) {
                if(!filterMsg.matcher(getMessage(null)).find)
                    errors.append(it)
            }
        }
        
        // Discard compiler output
        val compiler = ToolProvider.systemJavaCompiler
        val fileMgr = new ForwardingJavaFileManager(compiler.getStandardFileManager(null, null, null)) {
            override getJavaFileForOutput(Location l, String c, JavaFileObject.Kind k, FileObject s) {
                new ForwardingJavaFileObject(super.getJavaFileForOutput(l, c, k, s)) {
                    override openOutputStream() { ByteStreams.nullOutputStream }
                    override openWriter()       { CharStreams.nullWriter }
                }
            }
        }
        
        compiler.getTask(null, fileMgr, listener, null, null, #[src]).call
        "".assertEquals(errors.toString)
    }
}
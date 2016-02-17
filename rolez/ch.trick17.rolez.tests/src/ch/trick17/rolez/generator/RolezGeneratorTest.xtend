package ch.trick17.rolez.generator

import ch.trick17.rolez.TestUtils
import ch.trick17.rolez.rolez.Program
import ch.trick17.rolez.tests.RolezInjectorProvider
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
import rolez.lang.Guarded

import static ch.trick17.rolez.Constants.*
import static org.hamcrest.Matchers.*

import static extension org.junit.Assert.*

@RunWith(XtextRunner)
@InjectWith(RolezInjectorProvider)
class RolezGeneratorTest {
    
    @Inject extension ParseHelper<Program>
    @Inject extension ValidationTestHelper
    @Inject extension TestUtils
    @Inject RolezGenerator generator
    
    def classes() {
        newResourceSet.with('''
            class rolez.lang.Object mapped to java.lang.Object {
                mapped def readonly equals(o: readonly Object): boolean
                mapped def readonly hashCode: int
                mapped def readonly toString: pure String
            }
            class rolez.lang.Slice[T] mapped to rolez.lang.Slice {
                mapped def readonly  get(index: int): T
                mapped def readwrite set(index: int, component: T):
                mapped def r slice[r](begin: int, end: int, step: int): r Slice[T]
            }
            class rolez.lang.Array[T] mapped to rolez.lang.Array extends Slice[T] {
                mapped val length: int
                mapped new(i: int)
            }
            class rolez.lang.String mapped to java.lang.String {
                mapped def pure length: int
                mapped def pure substring(b: int, e: int): pure String
            }
            class rolez.lang.Task[V] mapped to rolez.lang.Task {
                mapped def pure get: V
            }
            object rolez.lang.System mapped to java.lang.System {
                mapped val out: readonly rolez.io.PrintStream
            }
            class rolez.io.PrintStream mapped to java.io.PrintStream {
                mapped new(file: readonly String)
                mapped def readonly println(i: int):
                mapped def readonly println(s: pure String):
            }
            class Base {
                var foo: int
            }
            class foo.bar.Base {
                new {}
                new(i: int) {}
                new(i: int, j: int) {}
            }
            class Container[E] mapped to «Container.canonicalName» {
                mapped var e: E
                mapped new
                mapped def readonly  get: E
                mapped def readwrite set(e: E):
            }
            object Tasks {
                task pure foo: {}
                task pure bar(o: readwrite Object): {}
                task pure sum(i: int, j: int): int { return i + j; }
            }
        ''')
    }
    
    static class Container<E> extends Guarded {
        public var E e
        new() {}
        def E get() { e }
        def void set(E e) { this.e = e }
    }
    
    def javaClasses() {
        val fsa = new InMemoryFileSystemAccess
        generator.doGenerate(classes.resources.head, fsa, null)
        fsa.textFiles.values
    }
    
    @Test def testFiles() {
        var program = parse('''
            class rolez.io.PrintStream mapped to java.io.PrintStream {
                mapped new(s: pure String)
            }
            class A
        ''', classes)
        program.assertNoErrors
        var fsa = new InMemoryFileSystemAccess
        fsa.allFiles.size.assertThat(is(0))
        generator.doGenerate(program.eResource, fsa, null)
        fsa.allFiles.size.assertThat(is(1))
        fsa.textFiles.size.assertThat(is(1))
        fsa.textFiles.keySet.head.assertThat(endsWith("A.java"))
        
        program = parse('''
            class A
            class foo.B
            class foo.bar.C
            object D
            object rolez.lang.System mapped to java.lang.System
        ''', classes)
        program.assertNoErrors
        fsa = new InMemoryFileSystemAccess
        fsa.allFiles.size.assertThat(is(0))
        generator.doGenerate(program.eResource, fsa, null)
        fsa.allFiles.size.assertThat(is(5))
        fsa.textFiles.size.assertThat(is(5))
    }
    
    @Test def testNormalClass() {
        parse('''
            class A
        ''', classes).generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class A extends «jvmGuardedClassName» {
                
                public A() {
                    super();
                }
            }
        ''')
        parse('''
            class foo.bar.A
        ''', classes).generate.assertEqualsJava('''
            package foo.bar;
            
            import static «jvmGuardedClassName».*;
            
            public class A extends «jvmGuardedClassName» {
                
                public A() {
                    super();
                }
            }
        ''')
    }
    
    @Test def testNormalClassWithSuperclass() {
        parse('''
            class A extends Base
        ''', classes).generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class A extends Base {
                
                public A() {
                    super();
                }
            }
        ''')
        parse('''
            class A extends foo.bar.Base
        ''', classes).generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class A extends foo.bar.Base {
                
                public A() {
                    super();
                }
            }
        ''')
    }
    
    @Test def testNormalClassWithGenericSuperclass() {
        parse('''
            class IntContainer extends Container[int]
        ''', classes.with('''
            class Container[E] mapped to «Container.canonicalName» {
                mapped new
            }
        ''')).generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class IntContainer extends «Container.canonicalName»<java.lang.Integer> {
                
                public IntContainer() {
                    super();
                }
            }
        ''')
    }
    
    @Test def testSingletonClass() {
        parse('''
            object A
        ''', classes).generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public final class A extends java.lang.Object {
                
                public static final A INSTANCE = new A();
                
                private A() {}
            }
        ''')
    }
    
    @Test def testSingletonClassWithSuperclass() {
        parse('''
            object A extends Base
        ''', classes).generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public final class A extends Base {
                
                public static final A INSTANCE = new A();
                
                private A() {}
            }
        ''')
    }
    
    @Test def testSingletonClassMapped() {
        parse('''
            object rolez.lang.System2 mapped to java.lang.System {
                mapped val out: readonly rolez.io.PrintStream
                mapped def readonly exit(status: int):
                mapped def readonly lineSeparator: pure String
            }
        ''', classes).generate.assertEqualsJava('''
            package rolez.lang;
            
            import static «jvmGuardedClassName».*;
            
            public final class System2 extends java.lang.Object {
                
                public static final System2 INSTANCE = new System2();
                
                private System2() {}
                
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
            
            import static «jvmGuardedClassName».*;
            
            public class A extends «jvmGuardedClassName» {
                
                public final int i;
                
                public final int j = 0;
                
                public boolean b;
                
                public foo.A a;
                
                public A() {
                    super();
                    this.i = 0;
                }
                
                @java.lang.Override
                protected java.lang.Iterable<?> guardedRefs() {
                    return java.util.Arrays.asList(a);
                }
            }
        ''')
        
        parse('''
            object A {
                val j: int = 0
            }
        ''', classes).generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
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
            import static «jvmGuardedClassName».*;
            
            public class A extends «jvmGuardedClassName» {
                
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
    }
    
    @Test def testMethodSingletonClass() {
        parse('''
            object A {
                def pure foo: {}
            }
        ''', classes).generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public final class A extends java.lang.Object {
                
                public static final A INSTANCE = new A();
                
                private A() {}
                
                public void foo() {
                }
            }
        ''')
    }
    
    @Test def testMethodCheckedExceptions() {
        parse('''
            val j = 0;
            val o: pure Object = new (rolez.io.PrintStream)("foo.txt");
            val k = 0;
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
            }
            catch(java.io.FileNotFoundException e) {
                «throwExceptionWrapper("e")»
            }
        '''.withJavaFrame)
        
        // TODO: Test with multiple types of exceptions, with RuntimeException(s), and with methods that throw exceptions
    }
    
    @Test def testMethodOverride() {
        parse('''
            class A {
                override readonly equals(o: readonly Object): boolean { return true; }
            }
        ''', classes).generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class A extends «jvmGuardedClassName» {
                
                public A() {
                    super();
                }
                
                @java.lang.Override
                public boolean equals(final java.lang.Object o) {
                    return true;
                }
            }
        ''')
    }
    
    @Test def testMethodOverrideGeneric() {
        val intContainer = '''
            class IntContainer extends Container[int] {
                override readwrite set(i: int): { this.e = i; }
                override readonly get: int { return this.e; }
            }
        '''
        parse(intContainer, classes).generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class IntContainer extends «Container.canonicalName»<java.lang.Integer> {
                
                public IntContainer() {
                    super();
                }
                
                @java.lang.Override
                public void set(final java.lang.Integer i) {
                    guardReadWrite(this).e = i;
                }
                
                @java.lang.Override
                public java.lang.Integer get() {
                    return guardReadOnly(this).e;
                }
            }
        ''')
        
        parse('''
            class SpecialIntContainer extends IntContainer {
                override readwrite set(i: int): { this.e = 2 * i; }
                override readonly get: int { return this.e / 2; }
            }
        ''', classes.with(intContainer)).generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class SpecialIntContainer extends IntContainer {
                
                public SpecialIntContainer() {
                    super();
                }
                
                @java.lang.Override
                public void set(final java.lang.Integer i) {
                    guardReadWrite(this).e = 2 * i;
                }
                
                @java.lang.Override
                public java.lang.Integer get() {
                    return guardReadOnly(this).e / 2;
                }
            }
        ''', parse(intContainer, classes).generate)
    }
    
    @Test def testTask() {
        parse('''
            class App {
                task pure foo: pure Base { return new Base; }
            }
        ''', classes).generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class App extends «jvmGuardedClassName» {
                
                public App() {
                    super();
                }
                
                public Base foo() {
                    return new Base();
                }
                
                public java.util.concurrent.Callable<Base> $fooTask() {
                    return new java.util.concurrent.Callable<Base>() {
                        public Base call() {
                            try {
                                return new Base();
                            }
                            finally {
                            }
                        }
                    };
                }
            }
        ''')
        
        parse('''
            class App {
                task pure foo: int { return 42; }
            }
        ''', classes).generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class App extends «jvmGuardedClassName» {
                
                public App() {
                    super();
                }
                
                public int foo() {
                    return 42;
                }
                
                public java.util.concurrent.Callable<java.lang.Integer> $fooTask() {
                    return new java.util.concurrent.Callable<java.lang.Integer>() {
                        public java.lang.Integer call() {
                            try {
                                return 42;
                            }
                            finally {
                            }
                        }
                    };
                }
            }
        ''')
        
        parse('''
            class App {
                task pure foo(i: int, j: int): { new (foo.bar.Base)(i + j); }
            }
        ''', classes).generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class App extends «jvmGuardedClassName» {
                
                public App() {
                    super();
                }
                
                public void foo(final int i, final int j) {
                    new foo.bar.Base(i + j);
                }
                
                public java.util.concurrent.Callable<java.lang.Void> $fooTask(final int i, final int j) {
                    return new java.util.concurrent.Callable<java.lang.Void>() {
                        public java.lang.Void call() {
                            try {
                                new foo.bar.Base(i + j);
                            }
                            finally {
                            }
                            return null;
                        }
                    };
                }
            }
        ''')
    }
    
    @Test def testTaskWithRoleParam() {
        parse('''
            class App {
                task r foo[r]: {}
                task r bar[r includes readonly ]: {}
                task r baz[r includes readwrite]: {}
            }
        ''', classes).generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class App extends «jvmGuardedClassName» {
                
                public App() {
                    super();
                }
                
                public void foo() {
                }
                
                public java.util.concurrent.Callable<java.lang.Void> $fooTask() {
                    return new java.util.concurrent.Callable<java.lang.Void>() {
                        public java.lang.Void call() {
                            try {
                            }
                            finally {
                            }
                            return null;
                        }
                    };
                }
                
                public void bar() {
                }
                
                public java.util.concurrent.Callable<java.lang.Void> $barTask() {
                    share();
                    return new java.util.concurrent.Callable<java.lang.Void>() {
                        public java.lang.Void call() {
                            try {
                            }
                            finally {
                                releaseShared();
                            }
                            return null;
                        }
                    };
                }
                
                public void baz() {
                }
                
                public java.util.concurrent.Callable<java.lang.Void> $bazTask() {
                    pass();
                    return new java.util.concurrent.Callable<java.lang.Void>() {
                        public java.lang.Void call() {
                            registerNewOwner();
                            try {
                            }
                            finally {
                                releasePassed();
                            }
                            return null;
                        }
                    };
                }
            }
        ''')
    }
    
    @Test def testTaskMain() {
        parse('''
            class App {
                task pure main: {}
            }
        ''', classes).generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class App extends «jvmGuardedClassName» {
                
                public App() {
                    super();
                }
                
                public void main() {
                }
                
                public java.util.concurrent.Callable<java.lang.Void> $mainTask() {
                    return new java.util.concurrent.Callable<java.lang.Void>() {
                        public java.lang.Void call() {
                            try {
                            }
                            finally {
                            }
                            return null;
                        }
                    };
                }
                
                public static void main(final java.lang.String[] args) {
                    rolez.lang.TaskSystem.getDefault().run(new App().$mainTask());
                }
            }
        ''')
        
        parse('''
            class App {
                task pure main(args: readonly Array[pure String]): { args.get(0).length; }
            }
        ''', classes).generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class App extends «jvmGuardedClassName» {
                
                public App() {
                    super();
                }
                
                public void main(final rolez.lang.GuardedArray<java.lang.String[]> args) {
                    guardReadOnly(args).data[0].length();
                }
                
                public java.util.concurrent.Callable<java.lang.Void> $mainTask(final rolez.lang.GuardedArray<java.lang.String[]> args) {
                    args.share();
                    return new java.util.concurrent.Callable<java.lang.Void>() {
                        public java.lang.Void call() {
                            try {
                                args.data[0].length();
                            }
                            finally {
                                args.releaseShared();
                            }
                            return null;
                        }
                    };
                }
                
                public static void main(final java.lang.String[] args) {
                    rolez.lang.TaskSystem.getDefault().run(new App().$mainTask(rolez.lang.GuardedArray.<java.lang.String[]>wrap(args)));
                }
            }
        ''')
    }
    
    @Test def testTaskTransitions() {
        parse('''
            class App {
                task readwrite foo(o1: readwrite Base, o2: readonly Base, o3: pure Base): {
                    the System.out.println("Hello World!");
                }
            }
        ''', classes).generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class App extends «jvmGuardedClassName» {
                
                public App() {
                    super();
                }
                
                public void foo(final Base o1, final Base o2, final Base o3) {
                    java.lang.System.out.println("Hello World!");
                }
                
                public java.util.concurrent.Callable<java.lang.Void> $fooTask(final Base o1, final Base o2, final Base o3) {
                    pass();
                    o1.pass();
                    o2.share();
                    return new java.util.concurrent.Callable<java.lang.Void>() {
                        public java.lang.Void call() {
                            registerNewOwner();
                            o1.registerNewOwner();
                            try {
                                java.lang.System.out.println("Hello World!");
                            }
                            finally {
                                releasePassed();
                                o1.releasePassed();
                                o2.releaseShared();
                            }
                            return null;
                        }
                    };
                }
            }
        ''')
        
        parse('''
            class App {
                task pure foo(o: readwrite Object): {}
            }
        ''', classes).generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class App extends «jvmGuardedClassName» {
                
                public App() {
                    super();
                }
                
                public void foo(final java.lang.Object o) {
                }
                
                public java.util.concurrent.Callable<java.lang.Void> $fooTask(final java.lang.Object o) {
                    if(o instanceof «jvmGuardedClassName»)
                        ((«jvmGuardedClassName») o).pass();
                    return new java.util.concurrent.Callable<java.lang.Void>() {
                        public java.lang.Void call() {
                            if(o instanceof «jvmGuardedClassName»)
                                ((«jvmGuardedClassName») o).registerNewOwner();
                            try {
                            }
                            finally {
                                if(o instanceof «jvmGuardedClassName»)
                                    ((«jvmGuardedClassName») o).releasePassed();
                            }
                            return null;
                        }
                    };
                }
            }
        ''')
    }
    
    @Test def testTaskGuarding() {
        // The field write is guarded in the method body, but not in the task body
        parse('''
            class App {
                task pure foo(o: readwrite Base): {
                    o.foo = 42;
                }
            }
        ''', classes).generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class App extends «jvmGuardedClassName» {
                
                public App() {
                    super();
                }
                
                public void foo(final Base o) {
                    guardReadWrite(o).foo = 42;
                }
                
                public java.util.concurrent.Callable<java.lang.Void> $fooTask(final Base o) {
                    o.pass();
                    return new java.util.concurrent.Callable<java.lang.Void>() {
                        public java.lang.Void call() {
                            o.registerNewOwner();
                            try {
                                o.foo = 42;
                            }
                            finally {
                                o.releasePassed();
                            }
                            return null;
                        }
                    };
                }
            }
        ''')
    }
    
    @Test def testTaskOverloading() {
        parse('''
            class App {
                task pure foo: {}
                task pure foo(i: int): {}
                task pure foo(d: double): {}
                task pure foo(a: pure Array[int], b: pure Array[double]): {}
                task pure foo(a: pure Array[pure Array[int]]): {}
            }
        ''', classes).generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class App extends «jvmGuardedClassName» {
                
                public App() {
                    super();
                }
                
                public void foo() {
                }
                
                public java.util.concurrent.Callable<java.lang.Void> $fooTask() {
                    return new java.util.concurrent.Callable<java.lang.Void>() {
                        public java.lang.Void call() {
                            try {
                            }
                            finally {
                            }
                            return null;
                        }
                    };
                }
                
                public void foo(final int i) {
                }
                
                public java.util.concurrent.Callable<java.lang.Void> $fooTask(final int i) {
                    return new java.util.concurrent.Callable<java.lang.Void>() {
                        public java.lang.Void call() {
                            try {
                            }
                            finally {
                            }
                            return null;
                        }
                    };
                }
                
                public void foo(final double d) {
                }
                
                public java.util.concurrent.Callable<java.lang.Void> $fooTask(final double d) {
                    return new java.util.concurrent.Callable<java.lang.Void>() {
                        public java.lang.Void call() {
                            try {
                            }
                            finally {
                            }
                            return null;
                        }
                    };
                }
                
                public void foo(final rolez.lang.GuardedArray<int[]> a, final rolez.lang.GuardedArray<double[]> b) {
                }
                
                public java.util.concurrent.Callable<java.lang.Void> $fooTask(final rolez.lang.GuardedArray<int[]> a, final rolez.lang.GuardedArray<double[]> b) {
                    return new java.util.concurrent.Callable<java.lang.Void>() {
                        public java.lang.Void call() {
                            try {
                            }
                            finally {
                            }
                            return null;
                        }
                    };
                }
                
                public void foo(final rolez.lang.GuardedArray<rolez.lang.GuardedArray<int[]>[]> a) {
                }
                
                public java.util.concurrent.Callable<java.lang.Void> $fooTask(final rolez.lang.GuardedArray<rolez.lang.GuardedArray<int[]>[]> a) {
                    return new java.util.concurrent.Callable<java.lang.Void>() {
                        public java.lang.Void call() {
                            try {
                            }
                            finally {
                            }
                            return null;
                        }
                    };
                }
            }
        ''')
    }
    
    @Test def testConstr() {
        parse('''
            package foo
            class A
        ''', classes).generate.assertEqualsJava('''
            package foo;
            
            import static «jvmGuardedClassName».*;
            
            public class A extends «jvmGuardedClassName» {
                
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
            
            import static «jvmGuardedClassName».*;
            
            public class A extends «jvmGuardedClassName» {
                
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
            import static «jvmGuardedClassName».*;
            
            public class A extends «jvmGuardedClassName» {
                
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
        
        // Constructor starting a task
        parse('''
            class A {
                new {
                    the Tasks start bar(this);
                }
            }
        ''', classes).generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class A extends «jvmGuardedClassName» {
                
                public A() {
                    super();
                    try {
                        rolez.lang.TaskSystem.getDefault().start(Tasks.INSTANCE.$barTask(this));
                    }
                    finally {
                        guardReadWrite(this);
                    }
                }
            }
        ''')
        
        // TODO: Test implicit constr referring to exception-throwing constr
    }
    
    @Test def testJavaKeywords() {
        parse('''
            package foo.static.native
            
            class final {
                def pure strictfp(volatile: int): int {
                    val synchronized = 2 * volatile;
                    val _synchronized = 42;
                    return synchronized + _synchronized;
                }
                
                def pure transient: {
                    val protected = new (foo.static.native.final);
                    protected.strictfp(5);
                }
            }
        ''', classes).generate.assertEqualsJava('''
            package foo.£static.£native;
            
            import static «jvmGuardedClassName».*;
            
            public class £final extends «jvmGuardedClassName» {
                
                public £final() {
                    super();
                }
                
                public int £strictfp(final int £volatile) {
                    final int £synchronized = 2 * £volatile;
                    final int _synchronized = 42;
                    return £synchronized + _synchronized;
                }
                
                public void £transient() {
                    final foo.£static.£native.£final £protected = new foo.£static.£native.£final();
                    £protected.£strictfp(5);
                }
            }
        ''')
        
        parse('''
            package foo.static.native
            class strictfp {
                task readonly final(do: readwrite Array[int]): {
                    do.set(0, 42);
                }
            }
        ''', classes).generate.assertEqualsJava('''
            package foo.£static.£native;
            
            import static «jvmGuardedClassName».*;
            
            public class £strictfp extends «jvmGuardedClassName» {
                
                public £strictfp() {
                    super();
                }
                
                public void £final(final rolez.lang.GuardedArray<int[]> £do) {
                    guardReadWrite(£do).data[0] = 42;
                }
                
                public java.util.concurrent.Callable<java.lang.Void> $finalTask(final rolez.lang.GuardedArray<int[]> £do) {
                    share();
                    £do.pass();
                    return new java.util.concurrent.Callable<java.lang.Void>() {
                        public java.lang.Void call() {
                            £do.registerNewOwner();
                            try {
                                £do.data[0] = 42;
                            }
                            finally {
                                releaseShared();
                                £do.releasePassed();
                            }
                            return null;
                        }
                    };
                }
            }
        ''')
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
            var k = 4;
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
        
        parse('''
            if(b)
                this.bar;
            else {}
        '''.withFrame, classes).generate.assertEqualsJava('''
            if(b)
                this.bar();
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
            for(var n = 0; n < 10; n += 1)
                this.bar;
        '''.withFrame, classes).generate.assertEqualsJava('''
            for(int n = 0; n < 10; n += 1)
                this.bar();
        '''.withJavaFrame)
        
        parse('''
            for(var n = 0; n < 10; n += 1) {
                this.bar;
            }
        '''.withFrame, classes).generate.assertEqualsJava('''
            for(int n = 0; n < 10; n += 1) {
                this.bar();
            }
        '''.withJavaFrame)
    }
    
    @Test def testSuperConstrCall() {
        parse('''
            class A extends Base {
                new { super; }
            }
        ''', classes).generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
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
            import static «jvmGuardedClassName».*;
            
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
            import static «jvmGuardedClassName».*;
            
            public class A extends «jvmGuardedClassName» {
                
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
            var a = false;
            a |= true;
            a &= false;
            var j = 42;
            j += 2;
            j -= 1;
            j *= 2;
            j /= 3;
            j %= 2;
        '''.withFrame, classes).generate.assertEqualsJava('''
            boolean a = false;
            a |= true;
            a &= false;
            int j = 42;
            j += 2;
            j -= 1;
            j *= 2;
            j /= 3;
            j %= 2;
        '''.withJavaFrame)
    }
    
    @Test def testBinaryExpr() {
        parse('''
            var c = true || new Base.equals(new Base);
            var d = b && false || true;
            var e = true && (b || true);
            var f = 2 < 3 || 3 < i;
            
            var j = 3 + 3 + 3;
            var k = 3 - 2 - 1;
            var l = 3 - (2 - 1);
            var m = (1 + 2) * (3 + 4);
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
            var o = new Base as readonly Object;
            o = ("Hi " + "World!") as readonly Object;
        '''.withFrame, classes).generate.assertEqualsJava('''
            java.lang.Object o = (java.lang.Object) new Base();
            o = (java.lang.Object) ("Hi " + "World!");
        '''.withJavaFrame)
    }
    
    @Test def testUnaryExpr() {
        parse('''
            var c = !false;
            var d = !(b && false);
            var e = !new Base.equals(new Base);
            
            var j = -3;
            var k = -(3 - 2);
            var l = -new Base.hashCode;
        '''.withFrame, classes).generate.assertEqualsJava('''
            boolean c = !false;
            boolean d = !(b && false);
            boolean e = !new Base().equals(new Base());
            int j = -3;
            int k = -(3 - 2);
            int l = -new Base().hashCode();
        '''.withJavaFrame)
    }
    
    @Test def testMemberAccessStartTask() {
        parse('''
            the Tasks start foo;
            
            val sum = the Tasks start sum(1, 2);
            the System.out.println("Parallelism!");
            the System.out.println("The sum: " + sum.get);
            the System.out.println("Twice the sum!: " + (2 * sum.get));
        '''.withFrame, classes).generate.assertEqualsJava('''
            rolez.lang.TaskSystem.getDefault().start(Tasks.INSTANCE.$fooTask());
            final rolez.lang.Task<java.lang.Integer> sum = rolez.lang.TaskSystem.getDefault().start(Tasks.INSTANCE.$sumTask(1, 2));
            java.lang.System.out.println("Parallelism!");
            java.lang.System.out.println("The sum: " + sum.get());
            java.lang.System.out.println("Twice the sum!: " + (2 * sum.get()));
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
            val sum = the Tasks.sum(1, 2);
        '''.withFrame, classes).generate.assertEqualsJava('''
            "Hello".toString().length();
            "Hello".equals("Hi");
            ("Hello " + "World!").length();
            ((java.lang.Object) new Base()).hashCode();
            "Hello".substring(1, 3);
            this.bar();
            final int sum = Tasks.INSTANCE.sum(1, 2);
        '''.withJavaFrame)
    }
    
    @Test def testMemberAccessGuarded() {
        // Field access is guarded, method calls are not (in case this is not obvious...)
        parse('''
            class A {
                var i: int = 0
                val j: int = 0
                def readwrite foo: {}
                def readonly  bar: {}
                def pure      baz: {}
                def readwrite test(a1: readwrite A, a2: readwrite A, a3: readwrite A,
                        a4: readwrite A, a5: readwrite A, a6: readwrite A): int {
                    a1.foo;
                    a2.bar;
                    a3.baz;
                    new A.i = 1;
                    a4.i = 2;
                    return a5.i + a6.j;
                }
            }
        ''', classes).generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class A extends «jvmGuardedClassName» {
                
                public int i = 0;
                
                public final int j = 0;
                
                public A() {
                    super();
                }
                
                public void foo() {
                }
                
                public void bar() {
                }
                
                public void baz() {
                }
                
                public int test(final A a1, final A a2, final A a3, final A a4, final A a5, final A a6) {
                    a1.foo();
                    a2.bar();
                    a3.baz();
                    new A().i = 1;
                    guardReadWrite(a4).i = 2;
                    return guardReadOnly(a5).i + a6.j;
                }
            }
        ''')
    }
    
    @Test def testMemberAccessGuardedMapped() {
        // Only Guarded mapped classes can have var fields
        parse('''
            class A {
                def pure test(c1: readwrite IntContainer, c2: readwrite IntContainer, c3: readwrite IntContainer, c4: readwrite IntContainer, c5: readwrite IntContainer): int {
                    c1.set(42);
                    c2.get;
                    c3.value = 2;
                    return c4.value + c5.fortyTwo;
                }
            }
        ''', classes.with('''
            class IntContainer mapped to «IntContainer.canonicalName» {
                mapped val fortyTwo: int
                mapped var value: int
                mapped def readonly get: int
                mapped def readwrite set(newValue: int):
            }
        ''')).generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class A extends «jvmGuardedClassName» {
                
                public A() {
                    super();
                }
                
                public int test(final «IntContainer.canonicalName» c1, final «IntContainer.canonicalName» c2, final «IntContainer.canonicalName» c3, final «IntContainer.canonicalName» c4, final «IntContainer.canonicalName» c5) {
                    c1.set(42);
                    c2.get();
                    guardReadWrite(c3).value = 2;
                    return guardReadOnly(c4).value + c5.fortyTwo;
                }
            }
        ''')
    }
    
    @Test def testMemberAccessSlice() {
        // Access to slice components is guarded, slicing is not
        parse('''
            class A {
                def pure getFirst(a: readwrite Slice[pure Object]): pure Object {
                    return a.get(0);
                }
                def pure getFirstInt(a: readwrite Slice[int]): int {
                    return a.get(0);
                }
                def pure getFirstDouble(a: readwrite Slice[double]): double {
                    return a.get(0);
                }
                def pure getFirstBoolean(a: readwrite Slice[boolean]): boolean {
                    return a.get(0);
                }
                def pure getFirstChar(a: readwrite Slice[char]): char {
                    return a.get(0);
                }
                
                def pure setFirst(a: readwrite Slice[int]): {
                    a.set(0, 42);
                }
                
                def pure slice(a: readwrite Slice[int]): readwrite Slice[int] {
                    return a.slice(0, 1, 1);
                }
            }
        ''', classes).generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class A extends «jvmGuardedClassName» {
                
                public A() {
                    super();
                }
                
                public java.lang.Object getFirst(final rolez.lang.GuardedSlice<java.lang.Object[]> a) {
                    return guardReadOnly(a).<java.lang.Object>get(0);
                }
                
                public int getFirstInt(final rolez.lang.GuardedSlice<int[]> a) {
                    return guardReadOnly(a).getInt(0);
                }
                
                public double getFirstDouble(final rolez.lang.GuardedSlice<double[]> a) {
                    return guardReadOnly(a).getDouble(0);
                }
                
                public boolean getFirstBoolean(final rolez.lang.GuardedSlice<boolean[]> a) {
                    return guardReadOnly(a).getBoolean(0);
                }
                
                public char getFirstChar(final rolez.lang.GuardedSlice<char[]> a) {
                    return guardReadOnly(a).getChar(0);
                }
                
                public void setFirst(final rolez.lang.GuardedSlice<int[]> a) {
                    guardReadWrite(a).setInt(0, 42);
                }
                
                public rolez.lang.GuardedSlice<int[]> slice(final rolez.lang.GuardedSlice<int[]> a) {
                    return a.slice(0, 1, 1);
                }
            }
        ''')
    }
    
    @Test def testMemberAccessArray() {
        // Access to array components is guarded, access to length field is not
        parse('''
            class A {
                def pure getFirst(a: readwrite Array[int]): int {
                    return a.get(0);
                }
                def pure setFirst(a: readwrite Array[int]): {
                    a.set(0, 42);
                }
                def pure length(a: readwrite Array[int]): int {
                    return a.length;
                }
            }
        ''', classes).generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class A extends «jvmGuardedClassName» {
                
                public A() {
                    super();
                }
                
                public int getFirst(final rolez.lang.GuardedArray<int[]> a) {
                    return guardReadOnly(a).data[0];
                }
                
                public void setFirst(final rolez.lang.GuardedArray<int[]> a) {
                    guardReadWrite(a).data[0] = 42;
                }
                
                public int length(final rolez.lang.GuardedArray<int[]> a) {
                    return a.data.length;
                }
            }
        ''')
    }
    
    @Test def testMemberAccessArrayCoercion() {
        parse('''
            var ia: pure Array[int] = new Array[int](0);
            val c = new ClassWithArrays(ia);
            ia = c.returnsIntArray;
            c.takesIntArray(ia);
            
            var iaa = c.returnsIntArrayArray;
            c.takesIntArrayArray(iaa);
            
            var sa = c.returnsStringArray;
            c.takesStringArray(sa);
        '''.withFrame, classes.with('''
            class ClassWithArrays mapped to «ClassWithArrays.canonicalName» {
                mapped new(a: pure Array[int])
                
                mapped def pure      takesIntArray(a: pure Array[int]            ):
                mapped def pure takesIntArrayArray(a: pure Array[pure Array[int]]):
                mapped def pure   takesStringArray(a: pure Array[pure String]    ):
                
                mapped def pure      returnsIntArray: pure Array[int]
                mapped def pure returnsIntArrayArray: pure Array[pure Array[int]]
                mapped def pure   returnsStringArray: pure Array[pure String]
            }
        ''')).generate.assertEqualsJava('''
            rolez.lang.GuardedArray<int[]> ia = new rolez.lang.GuardedArray<int[]>(new int[0]);
            final «ClassWithArrays.canonicalName» c = new «ClassWithArrays.canonicalName»(rolez.lang.GuardedArray.unwrap(ia, int[].class));
            ia = rolez.lang.GuardedArray.<int[]>wrap(c.returnsIntArray());
            c.takesIntArray(rolez.lang.GuardedArray.unwrap(ia, int[].class));
            rolez.lang.GuardedArray<rolez.lang.GuardedArray<int[]>[]> iaa = rolez.lang.GuardedArray.<rolez.lang.GuardedArray<int[]>[]>wrap(c.returnsIntArrayArray());
            c.takesIntArrayArray(rolez.lang.GuardedArray.unwrap(iaa, int[][].class));
            rolez.lang.GuardedArray<java.lang.String[]> sa = rolez.lang.GuardedArray.<java.lang.String[]>wrap(c.returnsStringArray());
            c.takesStringArray(rolez.lang.GuardedArray.unwrap(sa, java.lang.String[].class));
        '''.withJavaFrame)
    }
    
    static class IntContainer extends Guarded {
        public val fortyTwo = 42
        public var value = 0
        def int get() { value }
        def void set(int newValue) { value = newValue }
    }
    
    static class ClassWithArrays {
        new(int[] a) {}
        
        def void      takesIntArray(   int[] a) {}
        def void takesIntArrayArray( int[][] a) {}
        def void   takesStringArray(String[] a) {}
        
        def    int[]      returnsIntArray() { null }
        def  int[][] returnsIntArrayArray() { null }
        def String[]   returnsStringArray() { null }
    }
    
    @Test def testNew() {
        parse('''
            new Base;
            new (foo.bar.Base)(0);
            new (foo.bar.Base)(3 * 2 + 2);
            new (foo.bar.Base)("Hello".length, 0);
            var ai : pure Object = new Array[int](10 * 10);
            var ab : pure Object = new Array[pure Base](42);
            var aai: pure Object = new Array[pure Array[int]](0);
            new Container[int];
        '''.withFrame, classes).generate.assertEqualsJava('''
            new Base();
            new foo.bar.Base(0);
            new foo.bar.Base((3 * 2) + 2);
            new foo.bar.Base("Hello".length(), 0);
            java.lang.Object ai = new rolez.lang.GuardedArray<int[]>(new int[10 * 10]);
            java.lang.Object ab = new rolez.lang.GuardedArray<Base[]>(new Base[42]);
            java.lang.Object aai = new rolez.lang.GuardedArray<rolez.lang.GuardedArray<int[]>[]>(new rolez.lang.GuardedArray[0]);
            new «Container.canonicalName»<java.lang.Integer>();
        '''.withJavaFrame)
    }
    
    @Test def void testThe() {
        parse('''
            the System.out.println("Hello World!");
            val system = the System;
            system.out.println("Hello again!");
        '''.withFrame, classes).generate.assertEqualsJava('''
            java.lang.System.out.println("Hello World!");
            final rolez.lang.System system = rolez.lang.System.INSTANCE;
            system.out.println("Hello again!");
        '''.withJavaFrame)
    }
    
    @Test def testParenthesized() {
        parse('''
            var j = (0);
            var k = (2 + 2) * 3;
        '''.withFrame, classes).generate.assertEqualsJava('''
            int j = 0;
            int k = (2 + 2) * 3;
        '''.withJavaFrame)
    }
    
    @Test def testStringLiteral() {
        parse('''
            var s = "Hello World!";
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
            var c = 'H';
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
        generator.doGenerate(eResource, fsa, null)
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
        import static «jvmGuardedClassName».*;
        
        public class A extends «jvmGuardedClassName» {
            
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
    
    private def assertEqualsJava(CharSequence it, CharSequence javaCode, CharSequence... moreJavaCode) {
        assertCompilable(#[javaCode] + moreJavaCode + javaClasses)
        javaCode.toString.assertEquals(it.toString)
    }
    
    static val className = Pattern.compile("public (final )?class ([A-Za-z0-9_£]+) ")

    private def assertCompilable(Iterable<CharSequence> sources) {
        val compilationUnits = sources.map[ code |
            val matcher = className.matcher(code)
            matcher.find.assertTrue
            
            val uri = URI.create("string:///" + matcher.group(2) + ".java")
            new SimpleJavaFileObject(uri, JavaFileObject.Kind.SOURCE) {
                override getCharContent(boolean _) { code }
            }
        ]
        
        // Collect errors
        val errors = new StringBuilder
        val listener = new DiagnosticListener<JavaFileObject> {
            override report(Diagnostic<? extends JavaFileObject> it) {
                // IMPROVE: Emit code without issues, not even "notes"
                if(kind === Diagnostic.Kind.ERROR)
                    errors.append(it).append("\n\n")
            }
        }
        
        val compiler = ToolProvider.systemJavaCompiler
        val stdFileMgr = compiler.getStandardFileManager(null, null, null)
        
        val fileMgr = new ForwardingJavaFileManager(stdFileMgr) {
            override getJavaFileForOutput(Location l, String c, JavaFileObject.Kind k, FileObject s) {
                new ForwardingJavaFileObject(super.getJavaFileForOutput(l, c, k, s)) {
                    override openOutputStream() { ByteStreams.nullOutputStream }
                    override openWriter()       { CharStreams.nullWriter }
                }
            }
        }
        
        compiler.getTask(null, fileMgr, listener, null, null, compilationUnits).call
        "".assertEquals(errors.toString)
    }
}

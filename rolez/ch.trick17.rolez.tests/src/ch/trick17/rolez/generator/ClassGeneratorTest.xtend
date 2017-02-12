package ch.trick17.rolez.generator

import ch.trick17.rolez.TestUtils
import ch.trick17.rolez.rolez.Program
import ch.trick17.rolez.tests.RolezInjectorProvider
import javax.inject.Inject
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.junit.Test
import org.junit.runner.RunWith

import static ch.trick17.rolez.Constants.*

@RunWith(XtextRunner)
@InjectWith(RolezInjectorProvider)
class ClassGeneratorTest extends GeneratorTest {
    
    @Inject extension ParseHelper<Program>
    @Inject extension TestUtils
    
    @Inject extension ClassGenerator classGenerator
    
    @Test def testNormalClass() {
        parse('''
            class A
        ''', someClasses).onlyClass.generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class A extends «jvmGuardedClassName» {
                
                public A(final rolez.lang.Task<?> $task) {
                    super();
                }
            }
        ''')
        parse('''
            class foo.bar.A
        ''', someClasses).onlyClass.generate.assertEqualsJava('''
            package foo.bar;
            
            import static «jvmGuardedClassName».*;
            
            public class A extends «jvmGuardedClassName» {
                
                public A(final rolez.lang.Task<?> $task) {
                    super();
                }
            }
        ''')
    }
    
    @Test def testNormalClassWithSuperclass() {
        parse('''
            class A extends Base
        ''', someClasses).onlyClass.generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class A extends Base {
                
                public A(final rolez.lang.Task<?> $task) {
                    super($task);
                }
            }
        ''')
        parse('''
            class A extends foo.bar.Base
        ''', someClasses).onlyClass.generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class A extends foo.bar.Base {
                
                public A(final rolez.lang.Task<?> $task) {
                    super($task);
                }
            }
        ''')
    }
    
    @Test def testNormalClassWithGenericSuperclass() {
        parse('''
            class IntContainer extends Container[int]
        ''', someClasses.with('''
            class Container[E] mapped to «Container.canonicalName» {
                mapped new
            }
        ''')).onlyClass.generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class IntContainer extends «Container.canonicalName»<java.lang.Integer> {
                
                public IntContainer(final rolez.lang.Task<?> $task) {
                    super();
                }
            }
        ''')
    }
    
    @Test def testPureClass() {
        parse('''
            pure class A
        ''', someClasses).onlyClass.generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class A extends java.lang.Object {
                
                public A(final rolez.lang.Task<?> $task) {
                    super();
                }
            }
        ''')
        parse('''
            pure class A extends PureBase
        ''', someClasses).onlyClass.generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class A extends PureBase {
                
                public A(final rolez.lang.Task<?> $task) {
                    super($task);
                }
            }
        ''')
    }
    
    @Test def testSingletonClass() {
        parse('''
            object A
        ''', someClasses).onlyClass.generate.assertEqualsJava('''
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
        ''', someClasses).onlyClass.generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public final class A extends Base {
                
                public static final A INSTANCE = new A();
                
                private A() {
                    super(rolez.lang.Task.currentTask());
                }
            }
        ''')
    }
    
    @Test def testSingletonClassMapped() {
        parse('''
            object test.System mapped to java.lang.System {
                mapped val out: readonly rolez.io.PrintStream
                mapped def readonly exit(status: int):
                mapped def readonly lineSeparator: pure String
            }
        ''', someClasses).onlyClass.generate.assertEqualsJava('''
            package test;
            
            import static «jvmGuardedClassName».*;
            
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
        
        parse('''
            object test.Arrays mapped to java.util.Arrays {
                mapped def pure sort(a: readwrite Array[int]):
            }
        ''', someClasses).onlyClass.generate.assertEqualsJava('''
            package test;
            
            import static «jvmGuardedClassName».*;
            
            public final class Arrays extends java.lang.Object {
                
                public static final Arrays INSTANCE = new Arrays();
                
                private Arrays() {}
                
                public void sort(final int[] a) {
                    java.util.Arrays.sort(a);
                }
            }
        ''')
        
        parse('''
            class test.Channel mapped to java.nio.channels.Channel
            object test.System mapped to java.lang.System {
                mapped def pure inheritedChannel: pure test.Channel
            }
        ''', someClasses).classes.last.generate.assertEqualsJava('''
            package test;
            
            import static «jvmGuardedClassName».*;
            
            public final class System extends java.lang.Object {
                
                public static final System INSTANCE = new System();
                
                private System() {}
                
                public java.nio.channels.Channel inheritedChannel() throws java.io.IOException {
                    return java.lang.System.inheritedChannel();
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
                val base: pure foo.bar.Base = new (foo.bar.Base)
                
                new {
                    this.i = 0;
                }
            }
        ''', someClasses).onlyClass.generate.assertEqualsJava('''
            package foo;
            
            import static «jvmGuardedClassName».*;
            
            public class A extends «jvmGuardedClassName» {
                
                public final int i;
                
                public final int j = 0;
                
                public boolean b;
                
                public foo.A a;
                
                public final foo.bar.Base base = new foo.bar.Base(rolez.lang.Task.currentTask());
                
                public A(final rolez.lang.Task<?> $task) {
                    super();
                    this.i = 0;
                }
                
                @java.lang.Override
                protected java.lang.Iterable<?> guardedRefs() {
                    return java.util.Arrays.asList(a, base);
                }
            }
        ''')
        
        parse('''
            object A {
                val j: int = 0
            }
        ''', someClasses).onlyClass.generate.assertEqualsJava('''
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
        ''', someClasses).onlyClass.generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class A extends «jvmGuardedClassName» {
                
                public A(final rolez.lang.Task<?> $task) {
                    super();
                }
                
                public void foo(final rolez.lang.Task<?> $task) {
                }
                
                public int foo(final int i, final rolez.lang.Task<?> $task) {
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
        ''', someClasses).onlyClass.generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public final class A extends java.lang.Object {
                
                public static final A INSTANCE = new A();
                
                private A() {}
                
                public void foo(final rolez.lang.Task<?> $task) {
                }
            }
        ''')
    }
    
    @Test def testMethodOverride() {
        parse('''
            class A {
                override readonly equals(o: readonly Object): boolean { return true; }
            }
        ''', someClasses).onlyClass.generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class A extends «jvmGuardedClassName» {
                
                public A(final rolez.lang.Task<?> $task) {
                    super();
                }
                
                public boolean equals(final java.lang.Object o, final rolez.lang.Task<?> $task) {
                    return true;
                }
                
                @java.lang.Override
                public boolean equals(final java.lang.Object o) {
                    return this.equals(o, rolez.lang.Task.currentTask());
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
        parse(intContainer, someClasses).onlyClass.generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class IntContainer extends «Container.canonicalName»<java.lang.Integer> {
                
                public IntContainer(final rolez.lang.Task<?> $task) {
                    super();
                }
                
                public void set(final java.lang.Integer i, final rolez.lang.Task<?> $task) {
                    guardReadWrite(this, $task).e = i;
                }
                
                @java.lang.Override
                public void set(final java.lang.Integer i) {
                    this.set(i, rolez.lang.Task.currentTask());
                }
                
                public java.lang.Integer get(final rolez.lang.Task<?> $task) {
                    return guardReadOnly(this, $task).e;
                }
                
                @java.lang.Override
                public java.lang.Integer get() {
                    return this.get(rolez.lang.Task.currentTask());
                }
            }
        ''')
        
        parse('''
            class SpecialIntContainer extends IntContainer {
                override readwrite set(i: int): { this.e = 2 * i; }
                override readonly get: int { return this.e / 2; }
            }
        ''', someClasses.with(intContainer)).onlyClass.generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class SpecialIntContainer extends IntContainer {
                
                public SpecialIntContainer(final rolez.lang.Task<?> $task) {
                    super($task);
                }
                
                @java.lang.Override
                public void set(final java.lang.Integer i, final rolez.lang.Task<?> $task) {
                    guardReadWrite(this, $task).e = 2 * i;
                }
                
                @java.lang.Override
                public java.lang.Integer get(final rolez.lang.Task<?> $task) {
                    return guardReadOnly(this, $task).e / 2;
                }
            }
        ''', parse(intContainer, someClasses).onlyClass.generate)
    }
    
    @Test def testMethodStartingTasks() {
        parse('''
            class A {
                def pure foo: {
                    this start bar;
                    this.bar;
                }
                task pure bar: {}
            }
        ''', someClasses).onlyClass.generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class A extends «jvmGuardedClassName» {
                
                public A(final rolez.lang.Task<?> $task) {
                    super();
                }
                
                public void foo(final rolez.lang.Task<?> $task) {
                    final rolez.internal.Tasks $tasks = new rolez.internal.Tasks();
                    try {
                        $tasks.addInline(rolez.lang.TaskSystem.getDefault().start(this.$barTask()));
                        this.bar($task);
                    }
                    finally {
                        $tasks.joinAll();
                    }
                }
                
                public void bar(final rolez.lang.Task<?> $task) {
                }
                
                public rolez.lang.Task<java.lang.Void> $barTask() {
                    return new rolez.lang.Task<java.lang.Void>(new Object[]{}, new Object[]{}) {
                        @java.lang.Override
                        protected java.lang.Void runRolez() {
                            final rolez.lang.Task<?> $task = this;
                            return null;
                        }
                    };
                }
            }
        ''')
    }
    
    @Test def testAsyncMethod() {
        parse('''
            class A {
                def pure foo: {
                    this.fooAsync;
                }
                async def pure fooAsync: {
                    this start bar;
                    this.bar;
                }
                task pure bar: {}
            }
        ''', someClasses).onlyClass.generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class A extends «jvmGuardedClassName» {
                
                public A(final rolez.lang.Task<?> $task) {
                    super();
                }
                
                public void foo(final rolez.lang.Task<?> $task) {
                    final rolez.internal.Tasks $tasks = new rolez.internal.Tasks();
                    try {
                        this.fooAsync($tasks, $task);
                    }
                    finally {
                        $tasks.joinAll();
                    }
                }
                
                public void fooAsync(final rolez.internal.Tasks $tasks, final rolez.lang.Task<?> $task) {
                    $tasks.addInline(rolez.lang.TaskSystem.getDefault().start(this.$barTask()));
                    this.bar($task);
                }
                
                public void bar(final rolez.lang.Task<?> $task) {
                }
                
                public rolez.lang.Task<java.lang.Void> $barTask() {
                    return new rolez.lang.Task<java.lang.Void>(new Object[]{}, new Object[]{}) {
                        @java.lang.Override
                        protected java.lang.Void runRolez() {
                            final rolez.lang.Task<?> $task = this;
                            return null;
                        }
                    };
                }
            }
        ''')
    }
    
    @Test def testTask() {
        parse('''
            class App {
                task pure foo: pure Base { return new Base; }
            }
        ''', someClasses).onlyClass.generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class App extends «jvmGuardedClassName» {
                
                public App(final rolez.lang.Task<?> $task) {
                    super();
                }
                
                public Base foo(final rolez.lang.Task<?> $task) {
                    return new Base($task);
                }
                
                public rolez.lang.Task<Base> $fooTask() {
                    return new rolez.lang.Task<Base>(new Object[]{}, new Object[]{}) {
                        @java.lang.Override
                        protected Base runRolez() {
                            final rolez.lang.Task<?> $task = this;
                            return new Base($task);
                        }
                    };
                }
            }
        ''')
        
        parse('''
            class App {
                val magic: int = 42
                task pure foo: int { return this.magic; }
            }
        ''', someClasses).onlyClass.generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class App extends «jvmGuardedClassName» {
                
                public final int magic = 42;
                
                public App(final rolez.lang.Task<?> $task) {
                    super();
                }
                
                public int foo(final rolez.lang.Task<?> $task) {
                    return this.magic;
                }
                
                public rolez.lang.Task<java.lang.Integer> $fooTask() {
                    return new rolez.lang.Task<java.lang.Integer>(new Object[]{}, new Object[]{}) {
                        @java.lang.Override
                        protected java.lang.Integer runRolez() {
                            final rolez.lang.Task<?> $task = this;
                            return App.this.magic;
                        }
                    };
                }
            }
        ''')
        
        parse('''
            class App {
                task pure foo(i: int, j: int): { new (foo.bar.Base)(i + j); }
            }
        ''', someClasses).onlyClass.generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class App extends «jvmGuardedClassName» {
                
                public App(final rolez.lang.Task<?> $task) {
                    super();
                }
                
                public void foo(final int i, final int j, final rolez.lang.Task<?> $task) {
                    new foo.bar.Base(i + j, $task);
                }
                
                public rolez.lang.Task<java.lang.Void> $fooTask(final int i, final int j) {
                    return new rolez.lang.Task<java.lang.Void>(new Object[]{}, new Object[]{}) {
                        @java.lang.Override
                        protected java.lang.Void runRolez() {
                            final rolez.lang.Task<?> $task = this;
                            new foo.bar.Base(i + j, $task);
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
        ''', someClasses).onlyClass.generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class App extends «jvmGuardedClassName» {
                
                public App(final rolez.lang.Task<?> $task) {
                    super();
                }
                
                public void foo(final rolez.lang.Task<?> $task) {
                }
                
                public rolez.lang.Task<java.lang.Void> $fooTask() {
                    return new rolez.lang.Task<java.lang.Void>(new Object[]{}, new Object[]{}) {
                        @java.lang.Override
                        protected java.lang.Void runRolez() {
                            final rolez.lang.Task<?> $task = this;
                            return null;
                        }
                    };
                }
                
                public void bar(final rolez.lang.Task<?> $task) {
                }
                
                public rolez.lang.Task<java.lang.Void> $barTask() {
                    return new rolez.lang.Task<java.lang.Void>(new Object[]{}, new Object[]{this}) {
                        @java.lang.Override
                        protected java.lang.Void runRolez() {
                            final rolez.lang.Task<?> $task = this;
                            return null;
                        }
                    };
                }
                
                public void baz(final rolez.lang.Task<?> $task) {
                }
                
                public rolez.lang.Task<java.lang.Void> $bazTask() {
                    return new rolez.lang.Task<java.lang.Void>(new Object[]{this}, new Object[]{}) {
                        @java.lang.Override
                        protected java.lang.Void runRolez() {
                            final rolez.lang.Task<?> $task = this;
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
        ''', someClasses).onlyClass.generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class App extends «jvmGuardedClassName» {
                
                public App(final rolez.lang.Task<?> $task) {
                    super();
                }
                
                public void main(final rolez.lang.Task<?> $task) {
                }
                
                public rolez.lang.Task<java.lang.Void> $mainTask() {
                    return new rolez.lang.Task<java.lang.Void>(new Object[]{}, new Object[]{}) {
                        @java.lang.Override
                        protected java.lang.Void runRolez() {
                            final rolez.lang.Task<?> $task = this;
                            return null;
                        }
                    };
                }
                
                public static void main(final java.lang.String[] args) {
                    rolez.lang.TaskSystem.getDefault().run(new App(null).$mainTask());
                }
            }
        ''')
        
        // Singleton class with main task
        parse('''
            object App {
                task pure main: {}
            }
        ''', someClasses).onlyClass.generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public final class App extends java.lang.Object {
                
                public static final App INSTANCE = new App();
                
                private App() {}
                
                public void main(final rolez.lang.Task<?> $task) {
                }
                
                public rolez.lang.Task<java.lang.Void> $mainTask() {
                    return new rolez.lang.Task<java.lang.Void>(new Object[]{}, new Object[]{}) {
                        @java.lang.Override
                        protected java.lang.Void runRolez() {
                            final rolez.lang.Task<?> $task = this;
                            return null;
                        }
                    };
                }
                
                public static void main(final java.lang.String[] args) {
                    rolez.lang.TaskSystem.getDefault().run(INSTANCE.$mainTask());
                }
            }
        ''')
        
        parse('''
            class App {
                task pure main(args: readonly Array[pure String]): { args.get(0).length; }
            }
        ''', someClasses).onlyClass.generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class App extends «jvmGuardedClassName» {
                
                public App(final rolez.lang.Task<?> $task) {
                    super();
                }
                
                public void main(final rolez.lang.GuardedArray<java.lang.String[]> args, final rolez.lang.Task<?> $task) {
                    guardReadOnly(args, $task).data[0].length();
                }
                
                public rolez.lang.Task<java.lang.Void> $mainTask(final rolez.lang.GuardedArray<java.lang.String[]> args) {
                    return new rolez.lang.Task<java.lang.Void>(new Object[]{}, new Object[]{args}) {
                        @java.lang.Override
                        protected java.lang.Void runRolez() {
                            final rolez.lang.Task<?> $task = this;
                            args.data[0].length();
                            return null;
                        }
                    };
                }
                
                public static void main(final java.lang.String[] args) {
                    rolez.lang.TaskSystem.getDefault().run(new App(null).$mainTask(rolez.lang.GuardedArray.<java.lang.String[]>wrap(args)));
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
        ''', someClasses).onlyClass.generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class App extends «jvmGuardedClassName» {
                
                public App(final rolez.lang.Task<?> $task) {
                    super();
                }
                
                public void foo(final Base o1, final Base o2, final Base o3, final rolez.lang.Task<?> $task) {
                    java.lang.System.out.println("Hello World!");
                }
                
                public rolez.lang.Task<java.lang.Void> $fooTask(final Base o1, final Base o2, final Base o3) {
                    return new rolez.lang.Task<java.lang.Void>(new Object[]{this, o1}, new Object[]{o2}) {
                        @java.lang.Override
                        protected java.lang.Void runRolez() {
                            final rolez.lang.Task<?> $task = this;
                            java.lang.System.out.println("Hello World!");
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
        ''', someClasses).onlyClass.generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class App extends «jvmGuardedClassName» {
                
                public App(final rolez.lang.Task<?> $task) {
                    super();
                }
                
                public void foo(final java.lang.Object o, final rolez.lang.Task<?> $task) {
                }
                
                public rolez.lang.Task<java.lang.Void> $fooTask(final java.lang.Object o) {
                    return new rolez.lang.Task<java.lang.Void>(new Object[]{o}, new Object[]{}) {
                        @java.lang.Override
                        protected java.lang.Void runRolez() {
                            final rolez.lang.Task<?> $task = this;
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
        ''', someClasses).onlyClass.generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class App extends «jvmGuardedClassName» {
                
                public App(final rolez.lang.Task<?> $task) {
                    super();
                }
                
                public void foo(final Base o, final rolez.lang.Task<?> $task) {
                    guardReadWrite(o, $task).foo = 42;
                }
                
                public rolez.lang.Task<java.lang.Void> $fooTask(final Base o) {
                    return new rolez.lang.Task<java.lang.Void>(new Object[]{o}, new Object[]{}) {
                        @java.lang.Override
                        protected java.lang.Void runRolez() {
                            final rolez.lang.Task<?> $task = this;
                            o.foo = 42;
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
        ''', someClasses).onlyClass.generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class App extends «jvmGuardedClassName» {
                
                public App(final rolez.lang.Task<?> $task) {
                    super();
                }
                
                public void foo(final rolez.lang.Task<?> $task) {
                }
                
                public rolez.lang.Task<java.lang.Void> $fooTask() {
                    return new rolez.lang.Task<java.lang.Void>(new Object[]{}, new Object[]{}) {
                        @java.lang.Override
                        protected java.lang.Void runRolez() {
                            final rolez.lang.Task<?> $task = this;
                            return null;
                        }
                    };
                }
                
                public void foo(final int i, final rolez.lang.Task<?> $task) {
                }
                
                public rolez.lang.Task<java.lang.Void> $fooTask(final int i) {
                    return new rolez.lang.Task<java.lang.Void>(new Object[]{}, new Object[]{}) {
                        @java.lang.Override
                        protected java.lang.Void runRolez() {
                            final rolez.lang.Task<?> $task = this;
                            return null;
                        }
                    };
                }
                
                public void foo(final double d, final rolez.lang.Task<?> $task) {
                }
                
                public rolez.lang.Task<java.lang.Void> $fooTask(final double d) {
                    return new rolez.lang.Task<java.lang.Void>(new Object[]{}, new Object[]{}) {
                        @java.lang.Override
                        protected java.lang.Void runRolez() {
                            final rolez.lang.Task<?> $task = this;
                            return null;
                        }
                    };
                }
                
                public void foo(final rolez.lang.GuardedArray<int[]> a, final rolez.lang.GuardedArray<double[]> b, final rolez.lang.Task<?> $task) {
                }
                
                public rolez.lang.Task<java.lang.Void> $fooTask(final rolez.lang.GuardedArray<int[]> a, final rolez.lang.GuardedArray<double[]> b) {
                    return new rolez.lang.Task<java.lang.Void>(new Object[]{}, new Object[]{}) {
                        @java.lang.Override
                        protected java.lang.Void runRolez() {
                            final rolez.lang.Task<?> $task = this;
                            return null;
                        }
                    };
                }
                
                public void foo(final rolez.lang.GuardedArray<rolez.lang.GuardedArray<int[]>[]> a, final rolez.lang.Task<?> $task) {
                }
                
                public rolez.lang.Task<java.lang.Void> $fooTask(final rolez.lang.GuardedArray<rolez.lang.GuardedArray<int[]>[]> a) {
                    return new rolez.lang.Task<java.lang.Void>(new Object[]{}, new Object[]{}) {
                        @java.lang.Override
                        protected java.lang.Void runRolez() {
                            final rolez.lang.Task<?> $task = this;
                            return null;
                        }
                    };
                }
            }
        ''')
    }
    
    @Test def testTaskReturn() {
        parse('''
            class A {
                task pure foo(i: int): {
                    return;
                }
            }
        ''', someClasses).onlyClass.generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class A extends «jvmGuardedClassName» {
                
                public A(final rolez.lang.Task<?> $task) {
                    super();
                }
                
                public void foo(final int i, final rolez.lang.Task<?> $task) {
                    return;
                }
                
                public rolez.lang.Task<java.lang.Void> $fooTask(final int i) {
                    return new rolez.lang.Task<java.lang.Void>(new Object[]{}, new Object[]{}) {
                        @java.lang.Override
                        protected java.lang.Void runRolez() {
                            final rolez.lang.Task<?> $task = this;
                            return null;
                        }
                    };
                }
            }
        ''')
        
        parse('''
            class A {
                task pure foo(i: int): {
                    if(i == 0)
                        return;
                    else {
                        this.foo(i - 1);
                        return;
                    }
                }
            }
        ''', someClasses).onlyClass.generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class A extends «jvmGuardedClassName» {
                
                public A(final rolez.lang.Task<?> $task) {
                    super();
                }
                
                public void foo(final int i, final rolez.lang.Task<?> $task) {
                    if(i == 0)
                        return;
                    else {
                        this.foo(i - 1, $task);
                        return;
                    }
                }
                
                public rolez.lang.Task<java.lang.Void> $fooTask(final int i) {
                    return new rolez.lang.Task<java.lang.Void>(new Object[]{}, new Object[]{}) {
                        @java.lang.Override
                        protected java.lang.Void runRolez() {
                            final rolez.lang.Task<?> $task = this;
                            if(i == 0)
                                return null;
                            else {
                                A.this.foo(i - 1, $task);
                                return null;
                            }
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
        ''', someClasses).onlyClass.generate.assertEqualsJava('''
            package foo;
            
            import static «jvmGuardedClassName».*;
            
            public class A extends «jvmGuardedClassName» {
                
                public A(final rolez.lang.Task<?> $task) {
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
        ''', someClasses).onlyClass.generate.assertEqualsJava('''
            package foo;
            
            import static «jvmGuardedClassName».*;
            
            public class A extends «jvmGuardedClassName» {
                
                public A(final rolez.lang.Task<?> $task) {
                    super();
                }
                
                public A(final int i, final foo.A a, final rolez.lang.Task<?> $task) {
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
        ''', someClasses).onlyClass.generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class A extends «jvmGuardedClassName» {
                
                public A(final rolez.lang.Task<?> $task) {
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
        ''', someClasses).onlyClass.generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class A extends «jvmGuardedClassName» {
                
                public A(final rolez.lang.Task<?> $task) {
                    super();
                    final rolez.internal.Tasks $tasks = new rolez.internal.Tasks();
                    try {
                        $tasks.addInline(rolez.lang.TaskSystem.getDefault().start(Tasks.INSTANCE.$barTask(this)));
                    }
                    finally {
                        $tasks.joinAll();
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
        ''', someClasses).onlyClass.generate.assertEqualsJava('''
            package foo.£static.£native;
            
            import static «jvmGuardedClassName».*;
            
            public class £final extends «jvmGuardedClassName» {
                
                public £final(final rolez.lang.Task<?> $task) {
                    super();
                }
                
                public int £strictfp(final int £volatile, final rolez.lang.Task<?> $task) {
                    final int £synchronized = 2 * £volatile;
                    final int _synchronized = 42;
                    return £synchronized + _synchronized;
                }
                
                public void £transient(final rolez.lang.Task<?> $task) {
                    final foo.£static.£native.£final £protected = new foo.£static.£native.£final($task);
                    £protected.£strictfp(5, $task);
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
        ''', someClasses).onlyClass.generate.assertEqualsJava('''
            package foo.£static.£native;
            
            import static «jvmGuardedClassName».*;
            
            public class £strictfp extends «jvmGuardedClassName» {
                
                public £strictfp(final rolez.lang.Task<?> $task) {
                    super();
                }
                
                public void £final(final rolez.lang.GuardedArray<int[]> £do, final rolez.lang.Task<?> $task) {
                    guardReadWrite(£do, $task).data[0] = 42;
                }
                
                public rolez.lang.Task<java.lang.Void> $finalTask(final rolez.lang.GuardedArray<int[]> £do) {
                    return new rolez.lang.Task<java.lang.Void>(new Object[]{£do}, new Object[]{this}) {
                        @java.lang.Override
                        protected java.lang.Void runRolez() {
                            final rolez.lang.Task<?> $task = this;
                            £do.data[0] = 42;
                            return null;
                        }
                    };
                }
            }
        ''')
    }
}
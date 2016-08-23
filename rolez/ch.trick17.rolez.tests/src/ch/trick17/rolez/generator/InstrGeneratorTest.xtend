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
import rolez.lang.Guarded

import static ch.trick17.rolez.Constants.*

@RunWith(XtextRunner)
@InjectWith(RolezInjectorProvider)
class InstrGeneratorTest extends GeneratorTest {
    
    @Inject extension ParseHelper<Program>
    @Inject extension TestUtils
    
    // IMPROVE: Test InstrGenerator directly
    @Inject extension ClassGenerator classGenerator
    
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
        '''.withFrame, someClasses).onlyClass.generate.assertEqualsJava('''
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
        '''.withFrame, someClasses).onlyClass.generate.assertEqualsJava('''
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
        '''.withFrame, someClasses).onlyClass.generate.assertEqualsJava('''
            if(b)
                this.bar();
            else
                this.bar();
        '''.withJavaFrame)
        
        parse('''
            if(b) {
                this.bar;
            }
        '''.withFrame, someClasses).onlyClass.generate.assertEqualsJava('''
            if(b) {
                this.bar();
            }
        '''.withJavaFrame)
        
        parse('''
            if(b)
                this.bar;
            else {}
        '''.withFrame, someClasses).onlyClass.generate.assertEqualsJava('''
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
        '''.withFrame, someClasses).onlyClass.generate.assertEqualsJava('''
            while(b)
                this.bar();
        '''.withJavaFrame)
    }
    
    @Test def testForLoop() {
        parse('''
            for(var n = 0; n < 10; n += 1)
                this.bar;
        '''.withFrame, someClasses).onlyClass.generate.assertEqualsJava('''
            for(int n = 0; n < 10; n += 1)
                this.bar();
        '''.withJavaFrame)
        
        parse('''
            for(var n = 0; n < 10; n += 1) {
                this.bar;
            }
        '''.withFrame, someClasses).onlyClass.generate.assertEqualsJava('''
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
        ''', someClasses).onlyClass.generate.assertEqualsJava('''
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
        ''', someClasses).onlyClass.generate.assertEqualsJava('''
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
        ''', someClasses).onlyClass.generate.assertEqualsJava('''
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
        
        parse('''
            class A {
                task pure foo(i: int): {
                    if(i == 0)
                        return;
                    else
                        this.foo(i - 1);
                }
            }
        ''', someClasses).onlyClass.generate.assertEqualsJava('''
            import static «jvmGuardedClassName».*;
            
            public class A extends «jvmGuardedClassName» {
                
                public A() {
                    super();
                }
                
                public void foo(final int i) {
                    if(i == 0)
                        return;
                    else
                        this.foo(i - 1);
                }
                
                public rolez.lang.Task<java.lang.Void> $fooTask(final int i) {
                    return new rolez.lang.Task<java.lang.Void>(new Object[]{}, new Object[]{}) {
                        @java.lang.Override
                        protected java.lang.Void runRolez() {
                            if(i == 0)
                                return null;
                            else
                                A.this.foo(i - 1);
                            return null;
                        }
                    };
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
        '''.withFrame, someClasses).onlyClass.generate.assertEqualsJava('''
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
        '''.withFrame, someClasses).onlyClass.generate.assertEqualsJava('''
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
        '''.withFrame, someClasses).onlyClass.generate.assertEqualsJava('''
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
        '''.withFrame, someClasses).onlyClass.generate.assertEqualsJava('''
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
        '''.withFrame, someClasses).onlyClass.generate.assertEqualsJava('''
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
        '''.withFrame, someClasses).onlyClass.generate.assertEqualsJava('''
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
        '''.withFrame, someClasses).onlyClass.generate.assertEqualsJava('''
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
        '''.withFrame, someClasses).onlyClass.generate.assertEqualsJava('''
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
        '''.withFrame, someClasses).onlyClass.generate.assertEqualsJava('''
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
        ''', someClasses).onlyClass.generate.assertEqualsJava('''
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
        ''', someClasses.with('''
            class IntContainer mapped to «IntContainer.canonicalName» {
                mapped val fortyTwo: int
                mapped var value: int
                mapped def readonly get: int
                mapped def readwrite set(newValue: int):
            }
        ''')).onlyClass.generate.assertEqualsJava('''
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
        ''', someClasses).onlyClass.generate.assertEqualsJava('''
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
        ''', someClasses).onlyClass.generate.assertEqualsJava('''
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
        '''.withFrame, someClasses.with('''
            class ClassWithArrays mapped to «ClassWithArrays.canonicalName» {
                mapped new(a: pure Array[int])
                
                mapped def pure      takesIntArray(a: pure Array[int]            ):
                mapped def pure takesIntArrayArray(a: pure Array[pure Array[int]]):
                mapped def pure   takesStringArray(a: pure Array[pure String]    ):
                
                mapped def pure      returnsIntArray: pure Array[int]
                mapped def pure returnsIntArrayArray: pure Array[pure Array[int]]
                mapped def pure   returnsStringArray: pure Array[pure String]
            }
        ''')).onlyClass.generate.assertEqualsJava('''
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
        '''.withFrame, someClasses).onlyClass.generate.assertEqualsJava('''
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
        '''.withFrame, someClasses).onlyClass.generate.assertEqualsJava('''
            java.lang.System.out.println("Hello World!");
            final rolez.lang.System system = rolez.lang.System.INSTANCE;
            system.out.println("Hello again!");
        '''.withJavaFrame)
    }
    
    @Test def testParenthesized() {
        parse('''
            var j = (0);
            var k = (2 + 2) * 3;
        '''.withFrame, someClasses).onlyClass.generate.assertEqualsJava('''
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
        '''.withFrame, someClasses).onlyClass.generate.assertEqualsJava('''
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
        '''.withFrame, someClasses).onlyClass.generate.assertEqualsJava('''
            char c = 'H';
            c = '\'';
            c = '\n';
            c = '\"';
            c = '\"';
            c = '\\';
        '''.withJavaFrame)
    }
    
    @Test def testGenerateWithTryCatch() {
        parse('''
            val j = 0;
            val o: pure Object = new (rolez.io.PrintStream)("foo.txt");
            val k = 0;
        '''.withFrame, someClasses).onlyClass.generate.assertEqualsJava('''
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
        '''.withFrame, someClasses).onlyClass.generate.assertEqualsJava('''
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
    
    /* Test infrastructure */
    
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
}
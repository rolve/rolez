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
            mapped class rolez.lang.Object
            class Base
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
        '''.frame, classes).generate.assertEquals('''
            int j;
            int k = 4;
            final A a = null;
        '''.frameJava)
    }
    
    @Test
    def testIfStmt() {
        parse('''
            if(b)
                this.bar;
            else
                this.bar;
        '''.frame, classes).generate.assertEquals('''
            if(b)
                this.bar();
            else
                this.bar();
        '''.frameJava)
        
        parse('''
            if(b) {
                this.bar;
            }
        '''.frame, classes).generate.assertEquals('''
            if(b) {
                this.bar();
            }
        '''.frameJava)
    }
    
    @Test
    def testWhileLoop() {
        parse('''
            while(b)
                this.bar;
        '''.frame, classes).generate.assertEquals('''
            while(b)
                this.bar();
        '''.frameJava)
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
    
    private def generate(Program it) {
        assertNoErrors
        val fsa = new InMemoryFileSystemAccess
        generator.doGenerate(eResource, fsa)
        fsa.allFiles.size.assertThat(is(1))
        fsa.textFiles.size.assertThat(is(1))
        fsa.textFiles.values.head
    }
    
    private def frame(CharSequence it) {'''
        class A {
            def readwrite foo(val i: int, val b: boolean): {
                «it»
            }
            def pure bar: {}
        }
    '''}
    
    private def frameJava(CharSequence it) {'''
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
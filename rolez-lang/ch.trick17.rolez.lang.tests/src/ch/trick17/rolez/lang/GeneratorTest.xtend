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
            class foo.bar.Base
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
    def testMembers() {
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
        
        parse('''
            class A {
                def pure foo: {}
                def readwrite foo(val i: int): int { return i; }
            }
        ''', classes).generate.assertEquals('''
            public class A extends java.lang.Object {
                
                public void foo() {
                }
                
                public int foo(int i) {
                    return i;
                }
            }
        ''')
        
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
                
                public A(int i, foo.A a) {
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
    
    private def assertEquals(CharSequence it, CharSequence expected) {
        Assert.assertEquals(expected.toString, it.toString)
    }
}
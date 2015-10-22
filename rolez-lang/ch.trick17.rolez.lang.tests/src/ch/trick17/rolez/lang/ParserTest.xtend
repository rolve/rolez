package ch.trick17.rolez.lang

import ch.trick17.rolez.lang.rolez.Program
import javax.inject.Inject
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.eclipse.xtext.junit4.validation.ValidationTestHelper
import org.junit.Test
import org.junit.runner.RunWith

import static org.hamcrest.Matchers.*

import static extension org.hamcrest.MatcherAssert.assertThat

@RunWith(XtextRunner)
@InjectWith(RolezInjectorProvider)
class ParserTest {
    
    @Inject extension RolezExtensions
    @Inject extension ParseHelper<Program>
    @Inject extension ValidationTestHelper
    
    @Test
    def testEmptyClass() {
        val program = parse("mapped class rolez.lang.Object")
        program.assertNoErrors
        program.elements.size.assertThat(is(1))
        program.classes.size.assertThat(is(1))
        
        val clazz = program.classes.head
        clazz.name.assertThat(is("rolez.lang.Object"))
        clazz.superclass.assertThat(is(nullValue))
        clazz.members.assertThat(empty)
        clazz.constrs.assertThat(empty) // Why can't I use is(empty) here?
    }
    
    @Test
    def testQualifiedClassRef() {
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.Array {
                mapped new(val length: int)
                mapped val length: int
            }
            class foo.A
            class foo.B extends foo.A {
                def pure foo: {
                    val a: pure foo.A = new (foo.A);
                    new rolez.lang.Array[int](5).length;
                }
            }
        ''').assertNoErrors
    }
    
    @Test
    def testNewAndMemberAccess() {
        parse('''
            mapped class rolez.lang.Object
            class A {
                var i: int
                def pure f: {}
            }
            class B {
                def pure foo: {
                    new A.i;
                    new A.f();
                }
            }
        ''').assertNoErrors
    }
}
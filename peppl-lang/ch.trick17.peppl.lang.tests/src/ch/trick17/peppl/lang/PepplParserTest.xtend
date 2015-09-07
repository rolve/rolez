package ch.trick17.peppl.lang

import ch.trick17.peppl.lang.peppl.Program
import javax.inject.Inject
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.junit.Test
import org.junit.runner.RunWith

import static org.hamcrest.Matchers.*

import static extension org.hamcrest.MatcherAssert.assertThat
import org.eclipse.xtext.junit4.validation.ValidationTestHelper
import ch.trick17.peppl.lang.typesystem.PepplUtils

@RunWith(XtextRunner)
@InjectWith(PepplInjectorProvider)
class PepplParserTest {
    
    @Inject extension ParseHelper<Program>
    @Inject extension PepplUtils
    @Inject extension ValidationTestHelper
    
    @Test
    def testEmptyClass() {
        val program = parse("class Object")
        program.elements.size.assertThat(is(1))
        program.classes.size.assertThat(is(1))
        
        val clazz = program.classes.head
        clazz.name.assertThat(is("Object"))
        clazz.superclass.assertThat(is(nullValue))
        clazz.members.assertThat(empty)
        clazz.constructors.assertThat(empty) // Why can't I use is(empty) here?
    }
    
    @Test
    def testCast() {
        parse('''
            main {
                (int) 5;
                (int) (int) 5;
                ((int) 5);
                (int) (5);
                (int) ((int) 5);
            }
        ''').assertNoErrors
    }
    
}
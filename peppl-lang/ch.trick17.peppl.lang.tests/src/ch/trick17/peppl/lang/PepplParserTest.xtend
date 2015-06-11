package ch.trick17.peppl.lang

import ch.trick17.peppl.lang.peppl.Program
import ch.trick17.peppl.lang.typesystem.PepplTypeUtils
import javax.inject.Inject
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.junit.Test
import org.junit.runner.RunWith

import static org.hamcrest.Matchers.*

import static extension org.hamcrest.MatcherAssert.assertThat

@RunWith(XtextRunner)
@InjectWith(PepplInjectorProvider)
class PepplParserTest {
    
    @Inject extension ParseHelper<Program>
    @Inject extension PepplTypeUtils
    
    @Test
    def testEmptyClass() {
        val program = parse("class A")
        program.elements.size.assertThat(is(1))
        program.classes.size.assertThat(is(1))
        
        val clazz = program.classes.head
        clazz.name.assertThat(is("A"))
        clazz.superclass.assertThat(is(nullValue))
        clazz.members.assertThat(empty)
        clazz.constructors.assertThat(empty) // Why can't I use is(empty) here?
    }
}
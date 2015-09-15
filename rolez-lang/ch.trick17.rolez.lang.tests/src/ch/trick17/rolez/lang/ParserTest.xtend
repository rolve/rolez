package ch.trick17.rolez.lang

import ch.trick17.rolez.lang.rolez.Program
import ch.trick17.rolez.lang.typesystem.Utilz
import javax.inject.Inject
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.junit.Test
import org.junit.runner.RunWith

import static org.hamcrest.Matchers.*

import static extension org.hamcrest.MatcherAssert.assertThat

@RunWith(XtextRunner)
@InjectWith(RolezInjectorProvider)
class ParserTest {
    
    @Inject extension ParseHelper<Program>
    @Inject extension Utilz
    
    @Test
    def testEmptyClass() {
        val program = parse("class rolez.lang.Object")
        program.elements.size.assertThat(is(1))
        program.classes.size.assertThat(is(1))
        
        val clazz = program.classes.head
        clazz.name.assertThat(is("rolez.lang.Object"))
        clazz.superclass.assertThat(is(nullValue))
        clazz.members.assertThat(empty)
        clazz.constructors.assertThat(empty) // Why can't I use is(empty) here?
    }
}
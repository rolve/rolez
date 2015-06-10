package ch.trick17.peppl.lang

import ch.trick17.peppl.lang.peppl.Program
import javax.inject.Inject
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.junit.Test
import org.junit.runner.RunWith

import static org.junit.Assert.*
import ch.trick17.peppl.lang.typesystem.PepplTypeUtils

@RunWith(XtextRunner)
@InjectWith(PepplInjectorProvider)
class PepplParserTest {
    
    @Inject extension ParseHelper<Program>
    @Inject extension PepplTypeUtils
    
    @Test
    def testEmptyClass() {
        val program = "class A".parse
        assertEquals(1, program.elements.size)
        assertEquals(1, program.classes.size)
        
        val clazz = program.classes.head
        assertEquals("A", clazz.name)
        assertNull(clazz.superclass)
        assertTrue(clazz.members.empty)
        assertTrue(clazz.constructors.empty)
    }
}
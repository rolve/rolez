package ch.trick17.peppl.lang

import ch.trick17.peppl.lang.peppl.Class
import ch.trick17.peppl.lang.peppl.Program
import javax.inject.Inject
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.junit.Test
import org.junit.runner.RunWith

import static org.junit.Assert.*

@RunWith(XtextRunner)
@InjectWith(PepplInjectorProvider)
class ParserTest {
    
    @Inject private ParseHelper<Program> parser
    
    @Test
    def testEmptyClass() {
        val program = parser.parse("class A")        
        assertEquals(1, program.elements.size)
        
        val classes = program.elements.filter(Class)
        assertEquals(1, classes.size)
        
        val clazz = classes.head
        assertEquals("A", clazz.name)
        assertNull(clazz.superclass)
        assertTrue(clazz.members.empty)
        assertTrue(clazz.constructors.empty)
    }
}
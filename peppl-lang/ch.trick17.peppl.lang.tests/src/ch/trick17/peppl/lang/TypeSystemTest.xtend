package ch.trick17.peppl.lang

import ch.trick17.peppl.lang.peppl.Class
import ch.trick17.peppl.lang.peppl.ExpressionStatement
import ch.trick17.peppl.lang.peppl.Main
import ch.trick17.peppl.lang.peppl.Program
import ch.trick17.peppl.lang.typesystem.PepplSystem
import javax.inject.Inject
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.junit.Test
import org.junit.runner.RunWith

import static org.junit.Assert.*
import ch.trick17.peppl.lang.peppl.RoleType
import ch.trick17.peppl.lang.peppl.Role

@RunWith(XtextRunner)
@InjectWith(PepplInjectorProvider)
class TypeSystemTest {
    
    @Inject private ParseHelper<Program> parser
    @Inject private PepplSystem system
    
    @Test
    def testNew() {
        val program = parser.parse("
            class A
            main {
                new A;
            }
        ")
        val main = program.elements.filter(Main).head
        val newExpr = main.body.statements.filter(ExpressionStatement).head.expr
        
        val type = system.type(newExpr).value
        assertTrue(type instanceof RoleType)
        
        val roleType = type as RoleType
        assertEquals(Role.READWRITE, roleType.role)
        assertEquals(program.elements.filter(Class).head, roleType.base)
    }
}
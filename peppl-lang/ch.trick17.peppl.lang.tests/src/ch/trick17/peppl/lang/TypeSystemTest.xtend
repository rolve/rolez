package ch.trick17.peppl.lang

import ch.trick17.peppl.lang.peppl.Expression
import ch.trick17.peppl.lang.peppl.ExpressionStatement
import ch.trick17.peppl.lang.peppl.Program
import ch.trick17.peppl.lang.peppl.Role
import ch.trick17.peppl.lang.peppl.RoleType
import ch.trick17.peppl.lang.typesystem.PepplSystem
import ch.trick17.peppl.lang.typesystem.PepplTypeUtils
import javax.inject.Inject
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.junit.Test
import org.junit.runner.RunWith

import static org.junit.Assert.*
import it.xsemantics.runtime.RuleFailedException
import ch.trick17.peppl.lang.peppl.Type
import java.util.ArrayList

@RunWith(XtextRunner)
@InjectWith(PepplInjectorProvider)
class TypeSystemTest {
    
    @Inject extension ParseHelper<Program>
    @Inject extension PepplSystem system
    @Inject extension PepplTypeUtils
    
    @Test
    def testTAssignment() {
        val program = '''
            class A
            class B extends A
            main {
                var a: readwrite A;
                a = new B;
            }
        '''.parse
        
        val type = program.mainExpr.type.asRoleType
        assertEquals(Role.READWRITE, type.role)
        assertEquals(program.classes.findFirst[name=="A"], type.base)
    }
    
    @Test(expected=RuleFailedException)
    def void testTAssignmentInvalidLeft() {
        val program = '''
            main {
                !5 = 5;
            }
        '''.parse
        
        program.mainExpr.type
    }
    
    @Test
    def testTNew() {
        val program = '''
            class A
            main { new A; }
        '''.parse
        
        val type = program.mainExpr.type.asRoleType
        assertEquals(Role.READWRITE, type.role)
        assertEquals(program.classes.head, type.base)
    }
    
    def mainExpr(Program program) {
        program.main.body.statements.filter(ExpressionStatement).head.expr
    }
    
    def type(Expression expr) {
        val result = system.type(expr)
        if(result.failed) {
            val exceptions = new ArrayList<RuleFailedException>
            var Throwable current = result.ruleFailedException
            while(current instanceof RuleFailedException) {
                exceptions.add(current)
                current = current.cause
            }
            throw exceptions.findLast[errorInformations.exists[source != null]]
        }
        result.value
    }
    
    def asRoleType(Type type) {
        assertTrue("Expected role type, but was " + type, type instanceof RoleType)
        type as RoleType
    }
}
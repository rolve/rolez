package ch.trick17.peppl.lang

import ch.trick17.peppl.lang.peppl.Expression
import ch.trick17.peppl.lang.peppl.ExpressionStatement
import ch.trick17.peppl.lang.peppl.PepplPackage
import ch.trick17.peppl.lang.peppl.Program
import ch.trick17.peppl.lang.peppl.Role
import ch.trick17.peppl.lang.peppl.RoleType
import ch.trick17.peppl.lang.peppl.Type
import ch.trick17.peppl.lang.typesystem.PepplSystem
import ch.trick17.peppl.lang.typesystem.PepplTypeUtils
import it.xsemantics.runtime.TraceUtils
import javax.inject.Inject
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.eclipse.xtext.junit4.validation.ValidationTestHelper
import org.junit.Test
import org.junit.runner.RunWith

import static org.junit.Assert.*
import static ch.trick17.peppl.lang.typesystem.PepplSystem.*

@RunWith(XtextRunner)
@InjectWith(PepplInjectorProvider)
class PepplSystemTest {
    
    val PepplPackage peppl = PepplPackage.eINSTANCE
    
    @Inject extension ParseHelper<Program>
    @Inject extension ValidationTestHelper
    @Inject extension PepplSystem system
    @Inject extension PepplTypeUtils
    
    @Inject
    TraceUtils traceUtils;
    
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
    
    @Test
    def void testTAssignmentInvalidLeft() {
        '''
            main {
                !5 = 5;
            }
        '''.parse.assertError(peppl.intLiteral, SUBTYPEEXPRESSION, "int", "boolean")
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
        if(result.failed)
            throw traceUtils.innermostRuleFailedExceptionWithNodeModelSources(
                result.ruleFailedException
            )
        result.value
    }
    
    def asRoleType(Type type) {
        assertTrue("Expected role type, but was " + type, type instanceof RoleType)
        type as RoleType
    }
}
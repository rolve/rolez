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
import ch.trick17.peppl.lang.peppl.Boolean
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
    def void testTAssignmentErrorInOp() {
        "main { !5 = 5; }".parse
            .assertError(peppl.intLiteral, SUBTYPEEXPRESSION, "int", "boolean")
        
        '''
            main {
                var x: int;
                x = !5;
            }
        '''.parse.assertError(peppl.intLiteral, SUBTYPEEXPRESSION, "int", "boolean")
    }
    
    @Test
    def void testTAssignmentNotAssignable() {
        '''
            main {
                val x: int;
                x = 5;
            }
        '''.parse.assertError(peppl.variableRef, AVARIABLEREF, "assign", "value")
    }
    
    @Test
    def void testTAssignmentTypeMismatch() {
        '''
            main {
                var x: int;
                x = true;
            }
        '''.parse.assertError(peppl.booleanLiteral, SUBTYPEEXPRESSION, "int", "boolean")
    }
    
    @Test
    def testTBooleanExpression() {
        assertTrue("main { true || false; }".parse.mainExpr.type instanceof Boolean)
        assertTrue("main { true && false; }".parse.mainExpr.type instanceof Boolean)
    }
    
    @Test
    def void testTBooleanExpressionErrorInOp() {
        "main { !5 || false; }".parse
            .assertError(peppl.intLiteral, SUBTYPEEXPRESSION, "int", "boolean")
        "main { true || !5; }".parse
            .assertError(peppl.intLiteral, SUBTYPEEXPRESSION, "int", "boolean")
    }
    
    @Test
    def void testTBooleanExpressionInvalidOp() {
        "main { 5 || false; }".parse
            .assertError(peppl.intLiteral, SUBTYPEEXPRESSION, "int", "boolean")
        "main { true || 5; }".parse
            .assertError(peppl.intLiteral, SUBTYPEEXPRESSION, "int", "boolean")
    }
    
    @Test
    def testTEqualityExpression() {
        assertTrue("main { true == false; }".parse.mainExpr.type instanceof Boolean)
        assertTrue("main { 5 != 3; }".parse.mainExpr.type instanceof Boolean)

        '''
            class Object
            class A
            main {
                new Object == new A;
                new A == new Object;
                new A == new A;
            }
        '''.parse.assertNoErrors
    }
    
    @Test
    def void testTEqualityExpressionErrorInOp() {
        "main { !5 == false; }".parse
            .assertError(peppl.intLiteral, SUBTYPEEXPRESSION, "int", "boolean")
        "main { true != !5; }".parse
            .assertError(peppl.intLiteral, SUBTYPEEXPRESSION, "int", "boolean")
    }
    
    @Test
    def testTEqualityExpressionIncompatibleTypes() {
        '''
            class Object
            class A
            class B
            main { new A == new B; }
        '''.parse.assertError(peppl.equalityExpression, null, "compare", "A", "B")
        // IMPROVE: Find a way to include an issue code for explicit failures?
        
        "main { 42 != false; }".parse
            .assertError(peppl.equalityExpression, null, "compare", "int", "boolean")
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
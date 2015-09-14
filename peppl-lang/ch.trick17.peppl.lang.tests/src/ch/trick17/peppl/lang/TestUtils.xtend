package ch.trick17.peppl.lang

import ch.trick17.peppl.lang.peppl.Block
import ch.trick17.peppl.lang.peppl.Class
import ch.trick17.peppl.lang.peppl.ClassRef
import ch.trick17.peppl.lang.peppl.Expr
import ch.trick17.peppl.lang.peppl.ExprStmt
import ch.trick17.peppl.lang.peppl.GenericClassRef
import ch.trick17.peppl.lang.peppl.ParameterizedBody
import ch.trick17.peppl.lang.peppl.PrimitiveType
import ch.trick17.peppl.lang.peppl.Program
import ch.trick17.peppl.lang.peppl.Role
import ch.trick17.peppl.lang.peppl.RoleType
import ch.trick17.peppl.lang.peppl.SimpleClassRef
import ch.trick17.peppl.lang.peppl.Task
import ch.trick17.peppl.lang.peppl.Type
import ch.trick17.peppl.lang.typesystem.PepplSystem
import ch.trick17.peppl.lang.typesystem.PepplUtils
import java.util.Random
import javax.inject.Inject
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.xtext.junit4.validation.ValidationTestHelper
import org.eclipse.xtext.resource.XtextResourceSet
import org.eclipse.xtext.util.StringInputStream
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher

import static org.hamcrest.Matchers.*

import static extension org.hamcrest.MatcherAssert.assertThat

class TestUtils {
    
    @Inject extension PepplSystem system
    @Inject extension ValidationTestHelper
    @Inject extension PepplUtils
    
    private val r = new Random()

    def newResourceSet() {
        new PepplStandaloneSetup().createInjectorAndDoEMFRegistration
            .getInstance(XtextResourceSet)
    }
    
    def with(ResourceSet it, String program) {
        createResource(URI.createURI(r.ints(12, 0, 9).toArray.join + ".peppl"))
            .load(new StringInputStream(program), emptyMap)
        it
    }
    def main(Program p) {
        p.elements.filter(Task).filter[name == "Main"].head
    }
    
    def findClass(Program program, String name) {
        program.assertNoErrors
        val result = program.classes.findFirst[it.name == name]
        result.assertThat(notNullValue)
        result
    }
    
    def findMethod(Class clazz, String name) {
        val result = clazz.methods.findFirst[it.name == name]
        result.assertThat(notNullValue)
        result
    }
    
    def expr(ParameterizedBody b, int i) { expr(b.body, i) }
    
    def expr(Block b, int i) {
        b.assertNoErrors;
        b.stmts.filter(ExprStmt).get(i).expr
    }
    
    def lastExpr(ParameterizedBody b) { b.body.lastExpr }
    
    def lastExpr(Block b) {
        b.assertNoErrors;
        b.stmts.filter(ExprStmt).last.expr
    }
    
    def type(Expr expr) {
        val result = system.type(envFor(expr), expr)
        result.failed.assertThat(is(false))
        result.value
    }
    
    def asRoleType(Type type) {
        type.assertThat(instanceOf(RoleType))
        type as RoleType
    }
    
    def Matcher<Type> isRoleType(Role role, ClassRef base) {
        new RoleTypeMatcher(system, roleType(role, base))
    }
    
    static class RoleTypeMatcher extends BaseMatcher<Type> {
        
        extension PepplSystem system
        val RoleType expected
    
        new(PepplSystem system, RoleType expected) {
            this.system = system
            this.expected = expected
        }
        
        override matches(Object actual) {
            expected.equalTo(actual)
        }
        
        private def dispatch boolean equalTo(RoleType _, Object __) { false }
        
        private def dispatch boolean equalTo(RoleType it, RoleType other) {
            role.equals(other.role)
            base.equalTo(other.base)
        }
        
        private def dispatch boolean equalTo(PrimitiveType it, PrimitiveType other) {
            class == other.class
        }
        
        private def dispatch boolean equalTo(ClassRef _, Object __) { false }
        
        private def dispatch boolean equalTo(SimpleClassRef it, SimpleClassRef other) {
            clazz.equals(other.clazz)
        }
        
        private def dispatch boolean equalTo(GenericClassRef it, GenericClassRef other) {
            clazz.equals(other.clazz)
            typeArg.equalTo(other.typeArg)
        }
        
        override describeTo(Description description) {
            description.appendText(expected.stringRep)
        }
        
        override describeMismatch(Object actual, Description description) {
            description.appendText(actual.stringRep)
        }
        
    }
}
package ch.trick17.rolez.lang

import ch.trick17.rolez.lang.rolez.Block
import ch.trick17.rolez.lang.rolez.Class
import ch.trick17.rolez.lang.rolez.ClassRef
import ch.trick17.rolez.lang.rolez.Expr
import ch.trick17.rolez.lang.rolez.ExprStmt
import ch.trick17.rolez.lang.rolez.GenericClassRef
import ch.trick17.rolez.lang.rolez.NormalClass
import ch.trick17.rolez.lang.rolez.ParameterizedBody
import ch.trick17.rolez.lang.rolez.PrimitiveType
import ch.trick17.rolez.lang.rolez.Program
import ch.trick17.rolez.lang.rolez.Role
import ch.trick17.rolez.lang.rolez.RoleType
import ch.trick17.rolez.lang.rolez.SimpleClassRef
import ch.trick17.rolez.lang.rolez.Task
import ch.trick17.rolez.lang.rolez.Type
import ch.trick17.rolez.lang.typesystem.RolezSystem
import javax.inject.Inject
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.xtext.junit4.validation.ValidationTestHelper
import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.xtext.resource.XtextResourceSet
import org.eclipse.xtext.util.StringInputStream
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher

import static org.hamcrest.Matchers.*

import static extension org.hamcrest.MatcherAssert.assertThat
import java.util.Random

class TestUtilz {
    
    @Inject RolezSystem system
    @Inject extension RolezExtensions
    @Inject extension RolezUtils
    @Inject extension ValidationTestHelper
    
    private val r = new Random

    def newResourceSet() {
        new RolezStandaloneSetup().createInjectorAndDoEMFRegistration
            .getInstance(XtextResourceSet)
    }
    
    def with(ResourceSet it, String program) {
        createResource(URI.createURI((1..12).map[r.nextInt(10)].join + ".rz"))
            .load(new StringInputStream(program), emptyMap)
        it
    }
    
    def main(Program p) {
        p.elements.filter(Task).filter[name == "Main"].head
    }
    
    def findClass(Program program, String name) {
        program.assertNoErrors
        name.assertThat(not(containsString(".")))
        val result = program.classes.findFirst[it.name == name]
        result.assertThat(notNullValue)
        result
    }
    
    def findClass(Program program, QualifiedName name) {
        program.assertNoErrors
        val result = program.classes.findFirst[qualifiedName == name]
        result.assertThat(notNullValue)
        result
    }
    
    def findNormalClass(Program program, String name) {
        program.assertNoErrors
        name.assertThat(not(containsString(".")))
        val result = program.classes.filter(NormalClass).findFirst[it.name == name]
        result.assertThat(notNullValue)
        result
    }
    
    def findNormalClass(Program program, QualifiedName name) {
        program.assertNoErrors
        val result = program.classes.filter(NormalClass).findFirst[qualifiedName == name]
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
        new RoleTypeMatcher(system, newRoleType(role, base))
    }
    
    static class RoleTypeMatcher extends BaseMatcher<Type> {
        
        extension RolezSystem system
        val RoleType expected
    
        new(RolezSystem system, RoleType expected) {
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
package ch.trick17.rolez

import ch.trick17.rolez.rolez.Block
import ch.trick17.rolez.rolez.Class
import ch.trick17.rolez.rolez.Expr
import ch.trick17.rolez.rolez.ExprStmt
import ch.trick17.rolez.rolez.GenericClassRef
import ch.trick17.rolez.rolez.LocalVarDecl
import ch.trick17.rolez.rolez.NormalClass
import ch.trick17.rolez.rolez.ParameterizedBody
import ch.trick17.rolez.rolez.PrimitiveType
import ch.trick17.rolez.rolez.Program
import ch.trick17.rolez.rolez.Role
import ch.trick17.rolez.rolez.RoleType
import ch.trick17.rolez.rolez.SimpleClassRef
import ch.trick17.rolez.rolez.Task
import ch.trick17.rolez.rolez.Type
import ch.trick17.rolez.rolez.Var
import ch.trick17.rolez.typesystem.RolezSystem
import java.util.Random
import javax.inject.Inject
import javax.inject.Provider
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.xtext.junit4.validation.ValidationTestHelper
import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.xtext.resource.XtextResourceSet
import org.eclipse.xtext.util.StringInputStream

import static org.hamcrest.Matchers.*

import static extension org.hamcrest.MatcherAssert.assertThat

class TestUtils {
    
    @Inject RolezSystem system
    @Inject extension RolezExtensions extensions
    @Inject extension RolezUtils
    @Inject extension ValidationTestHelper
    @Inject Provider<XtextResourceSet> resourceSetProvider
    
    private val r = new Random

    def newResourceSet() { resourceSetProvider.get }
    
    def with(ResourceSet it, String program) {
        val res = createResource(URI.createURI((1..12).map[r.nextInt(10)].join + ".rz"))
        res.load(new StringInputStream(program), emptyMap)
        res.assertNoErrors
        it
    }
    
    def main(Program it) {
        assertNoErrors
        elements.filter(Task).filter[name == "Main"].head
    }
    
    def findClass(Program it, String name) {
        findClass(QualifiedName.create(name.split("\\.")))
    }
    
    def findClass(Program it, QualifiedName name) {
        assertNoErrors
        val result = classes.findFirst[qualifiedName == name]
        result.assertThat(notNullValue)
        result
    }
    
    def findNormalClass(Program it, String name) {
        findNormalClass(QualifiedName.create(name.split("\\.")))
    }
    
    def findNormalClass(Program it, QualifiedName name) {
        assertNoErrors
        val result = classes.filter(NormalClass).findFirst[qualifiedName == name]
        result.assertThat(notNullValue)
        result
    }
    
    def findMethod(Class it, String name) {
        val result = methods.findFirst[it.name == name]
        result.assertThat(notNullValue)
        result
    }
    
    def expr(ParameterizedBody it, int i) { body.expr(i) }
    
    def expr(Block it, int i) {
        stmts.filter(ExprStmt).get(i).expr
    }
    
    def lastExpr(ParameterizedBody it) { body.lastExpr }
    
    def lastExpr(Block it) {
        stmts.filter(ExprStmt).last.expr
    }
    
    def type(Expr e) {
        val result = system.type(createEnv(e), e)
        result.failed.assertThat(is(false))
        result.value
    }
    
    def variable(ParameterizedBody b, int i) { b.body.variable(i) }
    
    def variable(Block b, int i) {
        b.stmts.filter(LocalVarDecl).get(i).variable
    }
    
    def varType(Var v) {
        val result = system.varType(createEnv(v), v)
        result.failed.assertThat(is(false))
        result.value
    }
    
    def <T> assertInstanceOf(Object it, java.lang.Class<T> clazz) {
        assertThat(instanceOf(clazz))
        clazz.cast(it)
    }
    
    def void assertRoleType(Type it, java.lang.Class<? extends Role> r, String n) {
        assertInstanceOf(RoleType) => [
            role.assertThat(instanceOf(r))
            base.assertInstanceOf(SimpleClassRef) => [ clazz.name.assertThat(is(n)) ]
        ]
    }
    
    def void assertRoleType(Type it, java.lang.Class<? extends Role> r, QualifiedName n) {
        assertRoleType(r, n.toString)
    }
    
    def void assertRoleType(Type it, java.lang.Class<? extends Role> r, String n, java.lang.Class<? extends PrimitiveType> t) {
        assertInstanceOf(RoleType) => [
            role.assertThat(instanceOf(r))
            base.assertInstanceOf(GenericClassRef) => [
                clazz.name.assertThat(is(n))
                typeArg.class == t
            ]
        ]
    }
    
    def void assertRoleType(Type it, java.lang.Class<? extends Role> r, QualifiedName n, java.lang.Class<? extends PrimitiveType> t) {
        assertRoleType(r, n.toString, t)
    }
}

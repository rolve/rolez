package ch.trick17.rolez

import ch.trick17.rolez.rolez.Block
import ch.trick17.rolez.rolez.Constr
import ch.trick17.rolez.rolez.Executable
import ch.trick17.rolez.rolez.Expr
import ch.trick17.rolez.rolez.ExprStmt
import ch.trick17.rolez.rolez.GenericClassRef
import ch.trick17.rolez.rolez.LocalVarDecl
import ch.trick17.rolez.rolez.Method
import ch.trick17.rolez.rolez.NormalClass
import ch.trick17.rolez.rolez.PrimitiveType
import ch.trick17.rolez.rolez.Program
import ch.trick17.rolez.rolez.Role
import ch.trick17.rolez.rolez.RoleType
import ch.trick17.rolez.rolez.SimpleClassRef
import ch.trick17.rolez.rolez.Type
import ch.trick17.rolez.rolez.Var
import ch.trick17.rolez.rolez.VarRef
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
import static org.junit.Assert.assertEquals

import static extension org.hamcrest.MatcherAssert.assertThat

class TestUtils {
    
    @Inject RolezSystem system
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
    
    def withFrame(CharSequence it) '''
        class rolez.lang.Object mapped to java.lang.Object
        class rolez.lang.Slice[T] mapped to rolez.lang.Slice {
            mapped def r get[r includes readonly](i: int): T with r
            mapped def readwrite set(i: int, o: T):
            mapped def r partition[r](p: pure Partitioner, n: int): readwrite Array[r Slice[T]]
        }
        class rolez.lang.Array[T] mapped to rolez.lang.Array extends Slice[T] {
            mapped new(length: int)
        }
        class rolez.lang.Partitioner mapped to rolez.lang.Partitioner
        pure class rolez.lang.String mapped to java.lang.String
        class A {
            var i: int
        }
        class B {
            val a: readwrite A = new A
        }
        class App {
            task pure frameTask(a: boolean, b: boolean): { «it» }
            def pure getA: readwrite A { return new A; }
        }
    '''
    
    def task(Program it) {
        assertNoErrors
        val tasks = classes.map[methods].flatten.filter[isTask].toList
        tasks.size.assertThat(is(1))
        tasks.head
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
    
    def findMethod(ch.trick17.rolez.rolez.Class it, String name) {
        val result = methods.findFirst[it.name == name]
        result.assertThat(notNullValue)
        result
    }
    
    def expr(Method it, int i) { body.expr(i) }
    
    def expr(Block it, int i) {
        stmts.filter(ExprStmt).get(i).expr
    }
    
    def lastExpr(Method it) { body.lastExpr }
    def lastExpr(Constr it) { body.lastExpr }
    
    def lastExpr(Block it) {
        stmts.filter(ExprStmt).last.expr
    }
    
    def lastVarRef(Executable it) { eAllContents.filter(VarRef).last }
    
    def type(Expr e) {
        val result = system.type(null, e)
        if(result.failed)
            assertEquals("", result.ruleFailedException.message)
        result.value
    }
    
    def variable(Method it, int i) { body.variable(i) }
    
    def variable(Block b, int i) {
        b.stmts.filter(LocalVarDecl).get(i).variable
    }
    
    def varType(Var v) {
        val result = system.varType(null, v)
        result.failed.assertThat(is(false))
        result.value
    }
    
    def <T> assertInstanceOf(Object it, Class<T> clazz) {
        assertThat(instanceOf(clazz))
        clazz.cast(it)
    }
    
    def void assertRoleType(Type it, Class<? extends Role> r, String n) {
        assertInstanceOf(RoleType) => [
            role.assertThat(instanceOf(r))
            base.assertInstanceOf(SimpleClassRef) => [ clazz.name.assertThat(is(n)) ]
        ]
    }
    
    def void assertRoleType(Type it, Class<? extends Role> r, QualifiedName n) {
        assertRoleType(r, n.toString)
    }
    
    def void assertRoleType(Type it, Class<? extends Role> r, String n, Class<? extends PrimitiveType> t) {
        assertInstanceOf(RoleType) => [
            role.assertThat(instanceOf(r))
            base.assertInstanceOf(GenericClassRef) => [
                clazz.name.assertThat(is(n))
                typeArg.class == t
            ]
        ]
    }
    
    def void assertRoleType(Type it, Class<? extends Role> r, QualifiedName n, Class<? extends PrimitiveType> t) {
        assertRoleType(r, n.toString, t)
    }
}

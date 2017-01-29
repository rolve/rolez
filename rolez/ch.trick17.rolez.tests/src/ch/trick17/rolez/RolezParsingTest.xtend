package ch.trick17.rolez

import ch.trick17.rolez.rolez.CharLiteral
import ch.trick17.rolez.rolez.IntLiteral
import ch.trick17.rolez.rolez.Method
import ch.trick17.rolez.rolez.Program
import ch.trick17.rolez.rolez.Pure
import ch.trick17.rolez.rolez.StringLiteral
import ch.trick17.rolez.tests.RolezInjectorProvider
import javax.inject.Inject
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.eclipse.xtext.junit4.validation.ValidationTestHelper
import org.junit.Test
import org.junit.runner.RunWith

import static org.hamcrest.Matchers.*

import static extension org.hamcrest.MatcherAssert.assertThat

@RunWith(XtextRunner)
@InjectWith(RolezInjectorProvider)
class RolezParsingTest{

    @Inject extension ParseHelper<Program>
    @Inject extension ValidationTestHelper
    @Inject extension TestUtils
    
    @Test def testGenericSuperclassRef() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Array[T] mapped to rolez.lang.Array {
                mapped new(length: int)
                mapped val length: int
            }
            class IntArray extends Array[int] {
                new(length: int) {
                    super(length);
                }
            }
        ''').assertNoErrors
    }
    
    @Test def testQualifiedClassRef() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Array[T] mapped to rolez.lang.Array {
                mapped new(length: int)
                mapped val length: int
            }
            class foo.A
            class foo.B extends foo.A {
                def pure foo: {
                    val a: pure foo.A = new (foo.A);
                    new (rolez.lang.Array)[int](5).length;
                }
            }
        ''').assertNoErrors
    }
    
    @Test def testThisParam() {
        (parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                def pure f: {}
            }
        ''').findClass("A").members.head as Method).thisParam.type.role.assertThat(instanceOf(Pure))
    }
    
    @Test def testNewAndMemberAccess() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                var i: int
                def pure f: {}
            }
            class B {
                def pure foo: {
                    new A.i;
                    new A.f;
                }
            }
        ''').assertNoErrors
    }
    
    @Test def testCharLiteral() {
        val program = parse('''
            'H';
            '"';
            '\"';
            '\'';
            '\\';
            '\n';
            '\t';
        '''.withFrame)
        (program.task.expr(0) as CharLiteral).value.assertThat(is(Character.valueOf('H')))
        (program.task.expr(1) as CharLiteral).value.assertThat(is(Character.valueOf('"')))
        (program.task.expr(2) as CharLiteral).value.assertThat(is(Character.valueOf('"')))
        (program.task.expr(3) as CharLiteral).value.assertThat(is(Character.valueOf('\'')))
        (program.task.expr(4) as CharLiteral).value.assertThat(is(Character.valueOf('\\')))
        (program.task.expr(5) as CharLiteral).value.assertThat(is(Character.valueOf('\n')))
        (program.task.expr(6) as CharLiteral).value.assertThat(is(Character.valueOf('\t')))
    }
    
    @Test def testStringLiteral() {
        val program = parse('''
            "Hello World!";
            "\"";
            "'";
            "\'";
            "\\";
            "\n\n";
            "\t";
        '''.withFrame)
        (program.task.expr(0) as StringLiteral).value.assertThat(is("Hello World!"))
        (program.task.expr(1) as StringLiteral).value.assertThat(is("\""))
        (program.task.expr(2) as StringLiteral).value.assertThat(is("'"))
        (program.task.expr(3) as StringLiteral).value.assertThat(is("'"))
        (program.task.expr(4) as StringLiteral).value.assertThat(is("\\"))
        (program.task.expr(5) as StringLiteral).value.assertThat(is("\n\n"))
        (program.task.expr(6) as StringLiteral).value.assertThat(is("\t"))
    }
    
    @Test def testIntLiteral() {
        val program = parse('''
            0;
            42;
            «Integer.MAX_VALUE»;
        '''.withFrame)
        (program.task.expr(0) as IntLiteral).value.assertThat(is(0));
        (program.task.expr(1) as IntLiteral).value.assertThat(is(42));
        (program.task.expr(2) as IntLiteral).value.assertThat(is(Integer.MAX_VALUE));
        // IMPROVE: Support for Integer.MIN_VALUE
    }
}

package ch.trick17.rolez.lang

import ch.trick17.rolez.lang.rolez.CharLiteral
import ch.trick17.rolez.lang.rolez.IntLiteral
import ch.trick17.rolez.lang.rolez.Program
import ch.trick17.rolez.lang.rolez.StringLiteral
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
class ParserTest {
    
    @Inject extension ParseHelper<Program>
    @Inject extension ValidationTestHelper
    @Inject extension TestUtilz
    
    @Test def testQualifiedClassRef() {
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.Array[T] {
                mapped new(val length: int)
                mapped val length: int
            }
            class foo.A
            class foo.B extends foo.A {
                def pure foo: {
                    val a: pure foo.A = new (foo.A);
                    new rolez.lang.Array[int](5).length;
                }
            }
        ''').assertNoErrors
    }
    
    @Test def testNewAndMemberAccess() {
        parse('''
            mapped class rolez.lang.Object
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
            task Main: {
                'H';
                '"';
                '\"';
                '\'';
                '\\';
                '\n';
                '\t';
            }
        ''')
        (program.main.expr(0) as CharLiteral).value.assertThat(is(Character.valueOf('H')))
        (program.main.expr(1) as CharLiteral).value.assertThat(is(Character.valueOf('"')))
        (program.main.expr(2) as CharLiteral).value.assertThat(is(Character.valueOf('"')))
        (program.main.expr(3) as CharLiteral).value.assertThat(is(Character.valueOf('\'')))
        (program.main.expr(4) as CharLiteral).value.assertThat(is(Character.valueOf('\\')))
        (program.main.expr(5) as CharLiteral).value.assertThat(is(Character.valueOf('\n')))
        (program.main.expr(6) as CharLiteral).value.assertThat(is(Character.valueOf('\t')))
    }
    
    @Test def testStringLiteral() {
        val program = parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.String
            task Main: {
                "Hello World!";
                "\"";
                "'";
                "\'";
                "\\";
                "\n\n";
                "\t";
            }
        ''')
        (program.main.expr(0) as StringLiteral).value.assertThat(is("Hello World!"))
        (program.main.expr(1) as StringLiteral).value.assertThat(is("\""))
        (program.main.expr(2) as StringLiteral).value.assertThat(is("'"))
        (program.main.expr(3) as StringLiteral).value.assertThat(is("'"))
        (program.main.expr(4) as StringLiteral).value.assertThat(is("\\"))
        (program.main.expr(5) as StringLiteral).value.assertThat(is("\n\n"))
        (program.main.expr(6) as StringLiteral).value.assertThat(is("\t"))
    }
    
    @Test def testIntLiteral() {
        val program = parse('''
            task Main: {
                0;
                42;
                «Integer.MAX_VALUE»;
            }
        ''')
        (program.main.expr(0) as IntLiteral).value.assertThat(is(0));
        (program.main.expr(1) as IntLiteral).value.assertThat(is(42));
        (program.main.expr(2) as IntLiteral).value.assertThat(is(Integer.MAX_VALUE));
        // IMPROVE: Support for Integer.MIN_VALUE
    }
}
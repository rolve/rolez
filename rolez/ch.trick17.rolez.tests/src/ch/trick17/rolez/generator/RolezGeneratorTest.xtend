package ch.trick17.rolez.generator

import ch.trick17.rolez.rolez.Program
import ch.trick17.rolez.tests.RolezInjectorProvider
import javax.inject.Inject
import org.eclipse.xtext.generator.InMemoryFileSystemAccess
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.eclipse.xtext.junit4.validation.ValidationTestHelper
import org.junit.Test
import org.junit.runner.RunWith

import static org.hamcrest.Matchers.*

import static extension org.junit.Assert.*
import ch.trick17.rolez.TestUtils

@RunWith(XtextRunner)
@InjectWith(RolezInjectorProvider)
class RolezGeneratorTest {
    
    @Inject extension ParseHelper<Program>
    @Inject extension ValidationTestHelper
    @Inject extension TestUtils
    
    @Inject RolezGenerator generator
    val fsa = new InMemoryFileSystemAccess
    
    @Test def testClasses() {
        var program = parse('''
            class A
            class foo.B
            class foo.bar.C
            object D
            object rolez.lang.System mapped to java.lang.System
        ''', newResourceSet.with('''
            class rolez.lang.Object mapped to java.lang.Object
        '''))
        program.assertNoErrors
        
        fsa.allFiles.size.assertThat(is(0))
        generator.doGenerate(program.eResource, fsa, null)
        fsa.allFiles.size.assertThat(is(5))
        fsa.textFiles.size.assertThat(is(5))
    }
    
    @Test def testMappedClasses() {
        var program = parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.String mapped to java.lang.String
            class rolez.io.PrintStream mapped to java.io.PrintStream {
                mapped new(s: pure String)
            }
            class A
        ''')
        program.assertNoErrors
        
        fsa.allFiles.size.assertThat(is(0))
        generator.doGenerate(program.eResource, fsa, null)
        fsa.allFiles.size.assertThat(is(1))
        fsa.textFiles.size.assertThat(is(1))
        fsa.textFiles.keySet.head.assertThat(endsWith("A.java"))
    }
}

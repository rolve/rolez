package ch.trick17.peppl.lang

import ch.trick17.peppl.lang.peppl.Program
import javax.inject.Inject
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.eclipse.xtext.junit4.validation.ValidationTestHelper
import org.junit.Test
import org.junit.runner.RunWith

import static ch.trick17.peppl.lang.peppl.PepplPackage.Literals.*
import static org.eclipse.xtext.diagnostics.Diagnostic.*

@RunWith(XtextRunner)
@InjectWith(PepplInjectorProvider)
class LinkingTest {
    
    @Inject extension ParseHelper<Program>
    @Inject extension ValidationTestHelper
    @Inject extension TestUtils
    
    @Test
    def testMultipleResources() {
        val set = newResourceSet.with("class Object").with("class A")
        parse("class B extends A", set).assertNoErrors
    }
    
    @Test
    def testPackagesAndImports() {
        // "Unpackaged" classes are visible from everywhere
        var set = newResourceSet.with("class Object").with("class A")
        parse('''
            package foo.bar
            class B extends A
        ''', set).assertNoErrors
        
        // Classes in same package are visible
        set = newResourceSet.with("class Object").with('''
            package foo.bar
            class A
        ''')
        parse('''
            package foo.bar
            class B extends A
        ''', set).assertNoErrors
        
        // Classes can be imported
        set = newResourceSet.with("class Object").with('''
            package foo.bar
            class A
        ''')
        parse('''
            package a.b
            import foo.bar.A
            class B extends A
        ''', set).assertNoErrors
        
        // Also with wildcards
        set = newResourceSet.with("class Object").with('''
            package foo.bar
            class A
        ''')
        parse('''
            package a.b
            import foo.bar.*
            class B extends A
        ''', set).assertNoErrors
        
        // Class in same package is chosen, not "unpackaged" class
        set = newResourceSet.with("class Object").with('''
            class A
        ''').with('''
            package foo.bar
            class A {
                def pure foo: {}
            }
        ''')
        parse('''
            package foo.bar
            task B: {
                new A.foo();
            }
        ''', set).assertNoErrors
        
        set = newResourceSet.with("class Object").with('''
            package foo.bar
            class A
        ''')
        parse('''
            package a.b
            class B extends A
        ''', set).assertError(CLASS, LINKING_DIAGNOSTIC)
        set = newResourceSet.with("class Object").with('''
            package foo.bar
            class A
        ''')
        parse('''
            package a.b
            class B extends A
        ''', set).assertError(CLASS, LINKING_DIAGNOSTIC)
    }
    
    @Test
    def testVarRef() {
        parse('''
            task Main: {
                val i: int = 5;
                i;
            }
        ''').assertNoErrors
        parse('''
            class Object
            class A {
                def pure foo(val i: int): {
                    i;
                }
            }
            task Main: {}
        ''').assertNoErrors
        
        parse('''
            task Main: {
                i;
                val i: int = 0;
            }
        ''').assertError(VAR_REF, LINKING_DIAGNOSTIC, "var", "i")
        parse('''
            task Main: {
                {
                    val i: int = 0;
                }
                i;
            }
        ''').assertError(VAR_REF, LINKING_DIAGNOSTIC, "var", "i")
    }
}
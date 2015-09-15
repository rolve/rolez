package ch.trick17.rolez.lang

import ch.trick17.rolez.lang.rolez.Program
import javax.inject.Inject
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.eclipse.xtext.junit4.validation.ValidationTestHelper
import org.junit.Test
import org.junit.runner.RunWith

import static ch.trick17.rolez.lang.rolez.RolezPackage.Literals.*
import static org.eclipse.xtext.diagnostics.Diagnostic.*

@RunWith(XtextRunner)
@InjectWith(RolezInjectorProvider)
class LinkingTest {
    
    @Inject extension ParseHelper<Program>
    @Inject extension ValidationTestHelper
    @Inject extension TestUtilz
    
    @Test
    def testMultipleResources() {
        val set = newResourceSet.with("class rolez.lang.Object").with("class A")
        parse("class B extends A", set).assertNoErrors
    }
    
    @Test
    def testPackagesAndImports() {
        // "Unpackaged" classes are visible from everywhere
        var set = newResourceSet.with("class rolez.lang.Object").with("class A")
        parse('''
            package foo.bar
            class B extends A
        ''', set).assertNoErrors
        
        // Classes in same package are visible
        set = newResourceSet.with("class rolez.lang.Object").with('''
            package foo.bar
            class A
        ''')
        parse('''
            package foo.bar
            class B extends A
        ''', set).assertNoErrors
        
        // Classes can specify package directly in declaration
        set = newResourceSet.with("class rolez.lang.Object").with('''
            class foo.bar.A
        ''')
        parse('''
            package foo.bar
            class B extends A
        ''', set).assertNoErrors
        
        // Also partially
        set = newResourceSet.with("class rolez.lang.Object").with('''
            package foo
            class bar.A
        ''')
        parse('''
            package foo.bar
            class B extends A
        ''', set).assertNoErrors
        
        // Classes can be referred to using their fully qualified name
        set = newResourceSet.with("class rolez.lang.Object").with('''
            package foo.bar
            class A
        ''')
        parse('''
            package a.b
            class B extends foo.bar.A {
                def pure foo: {
                    val a: pure foo.bar.A;
                }
            }
        ''', set).assertNoErrors
        
        // Classes can be imported
        set = newResourceSet.with("class rolez.lang.Object").with('''
            package foo.bar
            class A
        ''')
        parse('''
            package a.b
            import foo.bar.A
            class B extends A
        ''', set).assertNoErrors
        
        // Also with wildcards
        set = newResourceSet.with("class rolez.lang.Object").with('''
            package foo.bar
            class A
        ''')
        parse('''
            package a.b
            import foo.bar.*
            class B extends A
        ''', set).assertNoErrors
        
        // Class in same package is chosen, not "unpackaged" class
        set = newResourceSet.with("class rolez.lang.Object").with('''
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
        
        // Classes in rolez.lang are always visible
        set = newResourceSet.with('''
            class rolez.lang.Object
            class rolez.lang.A
        ''')
        parse('''
            package foo.bar
            class B extends A
            class C extends rolez.lang.A
        ''', set).assertNoErrors
        
        set = newResourceSet.with("class rolez.lang.Object").with('''
            package foo.bar
            class A
        ''')
        parse('''
            package a.b
            class B extends A
        ''', set).assertError(CLASS, LINKING_DIAGNOSTIC)
        set = newResourceSet.with("class rolez.lang.Object").with('''
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
            class rolez.lang.Object
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
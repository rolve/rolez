package ch.trick17.rolez

import ch.trick17.rolez.rolez.Program
import ch.trick17.rolez.tests.RolezInjectorProvider
import javax.inject.Inject
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.XtextRunner
import org.eclipse.xtext.testing.util.ParseHelper
import org.eclipse.xtext.testing.validation.ValidationTestHelper
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

@RunWith(XtextRunner)
@InjectWith(RolezInjectorProvider)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class MiscTests {
    
    @Inject extension ParseHelper<Program>
    @Inject extension ValidationTestHelper
    @Inject extension TestUtils
    
    @Test(timeout = 1000) def testNestedStringLiteral() {
        parse('''"1" + "2" + "3" + "4" + "5" + "6" + "7" + "8" + "9" + "10";'''.withFrame).assertNoErrors
    }
}

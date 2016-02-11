package ch.trick17.rolez

import ch.trick17.rolez.rolez.Program
import ch.trick17.rolez.tests.RolezInjectorProvider
import javax.inject.Inject
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.eclipse.xtext.junit4.validation.ValidationTestHelper
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
    
    @Test(timeout = 1000) def testNestedStringLiteral() {
        parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.String mapped to java.lang.String
            task Main: {
                "1" + "2" + "3" + "4" + "5" + "6" + "7" + "8" + "9" + "10";
            }
        ''').assertNoErrors
    }
}

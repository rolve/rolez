package ch.trick17.rolez

import ch.trick17.rolez.rolez.Program
import javax.inject.Inject
import org.eclipse.xtext.junit4.util.ParseHelper
import org.eclipse.xtext.junit4.validation.ValidationTestHelper

class PerformanceTests {
    
    @Inject extension ParseHelper<Program>
    @Inject extension ValidationTestHelper
    
    static def main(String[] args) {
        val tests = new PerformanceTests
        new RolezStandaloneSetup().createInjectorAndDoEMFRegistration.injectMembers(tests)
        tests.test
    }
    
    def test() {
        // a little warmup
        for(i : 1..10)
            parse('''class rolez.lang.Object mapped to java.lang.Object''').assertNoErrors
        
        for(i : 0..7) {
            println(i)
            
            val start = System.nanoTime
            val program = parse('''
                class rolez.lang.Object mapped to java.lang.Object
                class Main {
                    task readonly main: {
                        «FOR j : 0..i»
                        {
                        «ENDFOR»
                            new Object;
                        «FOR j : 0..i»
                        }
                        «ENDFOR»
                    }
                }
            ''')
            printTime("  parse", start)
            
            program.assertNoErrors
            printTime("  check", start)
        }
    }
    
    def void printTime(String prefix, long start) {
        System.out.printf("%s: %.2f s\n", prefix, (System.nanoTime - start) / 1000000000.0)
    }
}
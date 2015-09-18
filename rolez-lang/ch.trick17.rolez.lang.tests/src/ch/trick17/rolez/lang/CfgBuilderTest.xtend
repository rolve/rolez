package ch.trick17.rolez.lang

import ch.trick17.rolez.lang.cfg.CfgBuilder
import ch.trick17.rolez.lang.cfg.ControlFlowGraph
import ch.trick17.rolez.lang.cfg.Node
import ch.trick17.rolez.lang.rolez.ParameterizedBody
import ch.trick17.rolez.lang.rolez.Program
import java.util.HashSet
import java.util.Set
import javax.inject.Inject
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.eclipse.xtext.junit4.validation.ValidationTestHelper
import org.junit.Test
import org.junit.runner.RunWith

import static org.hamcrest.Matchers.*

import static extension org.hamcrest.MatcherAssert.assertThat
import static extension org.junit.Assert.assertEquals

@RunWith(XtextRunner)
@InjectWith(RolezInjectorProvider)
class CfgBuilderTest {
    
    @Inject extension ParseHelper<Program>
    @Inject extension ValidationTestHelper
    @Inject extension TestUtilz
    @Inject extension CfgBuilder
    
    // TODO: Test the WHOLE structure!
    
    @Test
    def testBlock() {
        parse('''
            task Main: {}
        ''').main.cfg.assertStructure('''
            0 -> 
        ''')
        
        parse('''
            task Main: {
                0;
                1;
                2;
            }
        ''').main.cfg.assertStructure('''
            0 -> 1
            1 -> 2
            2 -> 3
            3 -> 
        ''')
        
        parse('''
            class rolez.lang.Object
            class rolez.lang.String
            task Main: {
                0;
                return;
                "Hi";
                5+5;
            }
        ''').main.cfg.assertStructure('''
            0 -> 1
            1 -> 2
            2 -> 
        ''')
    }
    
    @Test
    def testIfStmt() {
        parse('''
            task Main: {
                0;
                if(1 == 1) {
                    2;
                    3;
                }
                else
                    4;
            }
        ''').main.cfg.assertStructure('''
            0 -> 1
            1 -> 2, 4
            2 -> 3
            3 -> 5
            4 -> 5
            5 -> 
        ''')
        
        parse('''
            task Main: {
                if(0 == 0) {}
                else {}
            }
        ''').main.cfg.assertStructure('''
            0 -> 1, 1
            1 -> 
        ''') // That's pretty crazy...
        
        parse('''
            task Main: {
                0;
                if(1 == 1)
                    2;
                else
                    3;
                4;
            }
        ''').main.cfg.assertStructure('''
            0 -> 1
            1 -> 2, 3
            2 -> 4
            3 -> 4
            4 -> 5
            5 -> 
        ''')
        
        parse('''
            task Main: {
                if(0 == 0)
                    1;
            }
        ''').main.cfg.assertStructure('''
            0 -> 1, 2
            1 -> 2
            2 -> 
        ''')
        
        parse('''
            task Main: {
                if(0 != 0)
                    return;
                else {
                    return;
                }
            }
        ''').main.cfg.assertStructure('''
            0 -> 1, 2
            1 -> 3
            2 -> 3
            3 -> 
        ''')
        
        parse('''
            task Main: {
                if(0 == 0)
                    return;
                else
                    return;
                3;
            }
        ''').main.cfg.assertStructure('''
            0 -> 1, 2
            1 -> 3
            2 -> 3
            3 -> 
        ''')
        
        parse('''
            task Main: {
                if(0 == 0) {
                    return;
                }
            }
        ''').main.cfg.assertStructure('''
            0 -> 1, 2
            1 -> 2
            2 -> 
        ''')
        
        parse('''
            task Main: {
                if(0 < 0)
                    return;
                2;
            }
        ''').main.cfg.assertStructure('''
            0 -> 1, 2
            1 -> 3
            2 -> 3
            3 -> 
        ''')
    }
    
    @Test
    def testWhileLoop() {
        parse('''
            task Main: {
                while(0 == 0)
                    1;
            }
        ''').main.cfg.assertStructure('''
            0 -> 1, 2
            1 -> 0
            2 -> 
        ''')
        
        parse('''
            task Main: {
                while(0 != 0)
                    return;
            }
        ''').main.cfg.assertStructure('''
            0 -> 1, 2
            1 -> 2
            2 -> 
        ''')
    }
    
    def cfg(ParameterizedBody it) {
        assertNoErrors
        controlFlowGraph
    }
    
    def assertStructure(ControlFlowGraph it, String expected) {
        assertInvariants
        expected.assertEquals(dumpStructure)
    }
    
    def assertInvariants(ControlFlowGraph it) {
        nodes.assertThat(hasItem(entry))
        nodes.assertThat(hasItem(exit))
        
        if(entry !== exit) {
            nodes.size.assertThat(greaterThan(1))
            entry.successors.assertThat(not(empty))
            exit.predecessors.assertThat(not(empty))
        }
        exit.successors.assertThat(empty)
        
        val reachable = new HashSet
        entry.collectReachableNodes(reachable)
        nodes.toSet.assertThat(equalTo(reachable))
        
        for(node : reachable)
            for(successor : node.successors)
                successor.predecessors.assertThat(hasItem(node))
    }
    
    def void collectReachableNodes(Node it, Set<Node> nodes) {
        if(nodes += it) {
            successors.forEach[collectReachableNodes(nodes)]
            predecessors.forEach[collectReachableNodes(nodes)]
        }
    }
    
    def dumpStructure(ControlFlowGraph it) {
        val i = (0..nodes.size).iterator
        val map = nodes.toInvertedMap[i.next]
        map.entrySet.map['''
            «value» -> «key.successors.map[map.get(it)].join(", ")»
        '''].join
    }
}
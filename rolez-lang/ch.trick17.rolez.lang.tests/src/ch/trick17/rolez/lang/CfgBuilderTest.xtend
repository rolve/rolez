package ch.trick17.rolez.lang

import ch.trick17.rolez.lang.cfg.CfgBuilder
import ch.trick17.rolez.lang.cfg.ControlFlowGraph
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
import ch.trick17.rolez.lang.cfg.Node

@RunWith(XtextRunner)
@InjectWith(RolezInjectorProvider)
class CfgBuilderTest {
    
    @Inject extension CfgBuilder
    @Inject extension TestUtilz
    @Inject extension ParseHelper<Program>
    @Inject extension ValidationTestHelper
    
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
                2;
                4;
            }
        ''').main.cfg.assertStructure('''
            0 -> 1
            1 -> 2
            2 -> 3
            3 -> 4
            4 -> 5
            5 -> 6
            6 -> 
        ''') // Note there's a node for every stmt and for every expr
        
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
            2 -> 3
            3 -> 
        ''')
    }
    
    @Test
    def testIfStmt() {
        parse('''
            task Main(val b: boolean): {
                0;
                if(b) {
                    3;
                    5;
                }
                else
                    7;
            }
        ''').main.cfg.assertStructure('''
            0 -> 1
            1 -> 2
            2 -> 3, 7
            3 -> 4
            4 -> 5
            5 -> 6
            6 -> 9
            7 -> 8
            8 -> 9
            9 -> 10
            10 -> 
        ''')
        
        parse('''
            task Main(val b: boolean): {
                if(b) {}
                else {}
            }
        ''').main.cfg.assertStructure('''
            0 -> 1, 1« /* That's pretty crazy...*/ »
            1 -> 2
            2 -> 
        ''')
        
        parse('''
            task Main(val b: boolean): {
                0;
                if(b)
                    3;
                else
                    5;
                8;
            }
        ''').main.cfg.assertStructure('''
            0 -> 1
            1 -> 2
            2 -> 3, 5
            3 -> 4
            4 -> 7
            5 -> 6
            6 -> 7
            7 -> 8
            8 -> 9
            9 -> 10
            10 -> 
        ''')
        
        parse('''
            task Main(val b: boolean): {
                if(b)
                    1;
            }
        ''').main.cfg.assertStructure('''
            0 -> 1, 3
            1 -> 2
            2 -> 3
            3 -> 4
            4 -> 
        ''')
        
        parse('''
            task Main(val b: boolean): {
                if(b)
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
            task Main(val b: boolean): {
                if(b)
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
            task Main(val b: boolean): {
                if(b) {
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
            task Main(val b: boolean): {
                if(b)
                    return;
                3;
            }
        ''').main.cfg.assertStructure('''
            0 -> 1, 2
            1 -> 5
            2 -> 3
            3 -> 4
            4 -> 5
            5 -> 
        ''')
    }
    
    @Test
    def testWhileLoop() {
        parse('''
            task Main(val b: boolean): {
                while(b)
                    2;
            }
        ''').main.cfg.assertStructure('''
            0 -> 1
            1 -> 2, 4
            2 -> 3
            3 -> 0
            4 -> 5
            5 -> 
        ''')
        
        parse('''
            task Main(val b: boolean): {
                while(b)
                    return;
            }
        ''').main.cfg.assertStructure('''
            0 -> 1
            1 -> 2, 3
            2 -> 4
            3 -> 4
            4 -> 
        ''')
    }
    
    @Test
    def testExpr() {
        parse('''
            task Main: {
                0;
            }
        ''').main.cfg.assertStructure('''
            0 -> 1
            1 -> 2
            2 -> 
        ''')
        parse('''
            task Main: {
                (0);
            }
        ''').main.cfg.assertStructure('''
            0 -> 1
            1 -> 2
            2 -> 3
            3 -> 
        ''')
        parse('''
            task Main: {
                -0;
            }
        ''').main.cfg.assertStructure('''
            0 -> 1
            1 -> 2
            2 -> 3
            3 -> 
        ''')
        parse('''
            task Main: {
                !true;
            }
        ''').main.cfg.assertStructure('''
            0 -> 1
            1 -> 2
            2 -> 3
            3 -> 
        ''')
        
        parse('''
            task Main: {
                0 + 1;
            }
        ''').main.cfg.assertStructure('''
            0 -> 1
            1 -> 2
            2 -> 3
            3 -> 4
            4 -> 
        ''')
        
        parse('''
            task Main: {
                0 == 1;
            }
        ''').main.cfg.assertStructure('''
            0 -> 1
            1 -> 2
            2 -> 3
            3 -> 4
            4 -> 
        ''')
        
        // Short-circuit!
        parse('''
            task Main(val a: boolean, val b: boolean): {
                a && b;
            }
        ''').main.cfg.assertStructure('''
            0 -> 1, 2
            1 -> 2
            2 -> 3
            3 -> 4
            4 -> 
        ''')
        parse('''
            task Main(val a: boolean, val b: boolean): {
                a || b;
            }
        ''').main.cfg.assertStructure('''
            0 -> 2, 1
            1 -> 2
            2 -> 3
            3 -> 4
            4 -> 
        ''')
        parse('''
            task Main(val b: boolean): {
                0 == 1 && b;
            }
        ''').main.cfg.assertStructure('''
            0 -> 1
            1 -> 2
            2 -> 3, 4
            3 -> 4
            4 -> 5
            5 -> 6
            6 -> 
        ''')
        parse('''
            task Main(val a: boolean, val b: boolean): {
                a && (1 > 2 || b);
            }
        ''').main.cfg.assertStructure('''
            0 -> 1, 7
            1 -> 2
            2 -> 3
            3 -> 5, 4
            4 -> 5
            5 -> 6
            6 -> 7
            7 -> 8
            8 -> 9
            9 -> 
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
        
        // Test that expressions have been evaluated
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
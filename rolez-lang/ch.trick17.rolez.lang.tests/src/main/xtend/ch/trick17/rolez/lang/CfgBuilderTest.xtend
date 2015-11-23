package ch.trick17.rolez.lang

import ch.trick17.rolez.lang.cfg.CfgProvider
import ch.trick17.rolez.lang.cfg.ControlFlowGraph
import ch.trick17.rolez.lang.cfg.ExitNode
import ch.trick17.rolez.lang.cfg.InstrNode
import ch.trick17.rolez.lang.cfg.Node
import ch.trick17.rolez.lang.rolez.Boolean
import ch.trick17.rolez.lang.rolez.Expr
import ch.trick17.rolez.lang.rolez.IfStmt
import ch.trick17.rolez.lang.rolez.LogicalExpr
import ch.trick17.rolez.lang.rolez.ParameterizedBody
import ch.trick17.rolez.lang.rolez.Program
import ch.trick17.rolez.lang.typesystem.RolezSystem
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
    
    @Inject RolezSystem system
    @Inject RolezUtils utils
    @Inject extension CfgProvider
    @Inject extension TestUtilz
    @Inject extension ParseHelper<Program>
    @Inject extension ValidationTestHelper
    
    @Test def testBlock() {
        parse('''
            task Main: {}
        ''').main.cfg.assertStructure('''
            entry -> 1
            1 -> exit
        ''')
        
        parse('''
            task Main: {
                0;
                2;
                4;
            }
        ''').main.cfg.assertStructure('''
            entry -> 1
            1 -> 2
            2 -> 3
            3 -> 4
            4 -> 5
            5 -> 6
            6 -> 7
            7 -> exit
        ''') // Note there's a node for every stmt (incl. blocks) and for every expr
        
        parse('''
            mapped class rolez.lang.Object
            mapped class rolez.lang.String
            task Main: {
                0;
                return;
                "Hi";
                5+5;
            }
        ''').main.cfg.assertStructure('''
            entry -> 1
            1 -> 2
            2 -> 3
            3 -> exit
        ''')
    }
    
    @Test def testIfStmt() {
        parse('''
            task Main(b: boolean): {
                0;
                if(b) {
                    3;
                    5;
                }
                else
                    8;
            }
        ''').main.cfg.assertStructure('''
            entry -> 1
            1 -> 2
            2 -> 3
            3 -> 4, 9
            4 -> 5
            5 -> 6
            6 -> 7
            7 -> 8
            8 -> 11
            9 -> 10
            10 -> 11
            11 -> 12
            12 -> exit
        ''')
        
        parse('''
            task Main(b: boolean): {
                if(b) {}
                else {}
            }
        ''').main.cfg.assertStructure('''
            entry -> 1
            1 -> 2, 3
            2 -> 4
            3 -> 4
            4 -> 5
            5 -> exit
        ''')
        
        parse('''
            task Main(b: boolean): {
                0;
                if(b)
                    3;
                else
                    5;
                8;
            }
        ''').main.cfg.assertStructure('''
            entry -> 1
            1 -> 2
            2 -> 3
            3 -> 4, 6
            4 -> 5
            5 -> 8
            6 -> 7
            7 -> 8
            8 -> 9
            9 -> 10
            10 -> 11
            11 -> exit
        ''')
        
        parse('''
            task Main(b: boolean): {
                if(b)
                    1;
            }
        ''').main.cfg.assertStructure('''
            entry -> 1
            1 -> 2, 4
            2 -> 3
            3 -> 5
            4 -> 5
            5 -> 6
            6 -> exit
        ''')
        
        parse('''
            task Main(b: boolean): {
                if(b)
                    return;
                else {
                    return;
                }
            }
        ''').main.cfg.assertStructure('''
            entry -> 1
            1 -> 2, 3
            2 -> exit
            3 -> exit
        ''')
        
        parse('''
            task Main(b: boolean): {
                if(b)
                    return;
                else
                    return;
                3;
            }
        ''').main.cfg.assertStructure('''
            entry -> 1
            1 -> 2, 3
            2 -> exit
            3 -> exit
        ''')
        
        parse('''
            task Main(b: boolean): {
                if(b) {
                    return;
                }
            }
        ''').main.cfg.assertStructure('''
            entry -> 1
            1 -> 2, 3
            2 -> exit
            3 -> 4
            4 -> 5
            5 -> exit
        ''')
        
        parse('''
            task Main(b: boolean): {
                if(b)
                    return;
                3;
            }
        ''').main.cfg.assertStructure('''
            entry -> 1
            1 -> 2, 3
            2 -> exit
            3 -> 4
            4 -> 5
            5 -> 6
            6 -> 7
            7 -> exit
        ''')
        
        parse('''
            task Main(b: boolean): {
                if(b) {}
                else
                    return;
            }
        ''').main.cfg.assertStructure('''
            entry -> 1
            1 -> 2, 5
            2 -> 3
            3 -> 4
            4 -> exit
            5 -> exit
        ''')
    }
    
    @Test def testWhileLoop() {
        parse('''
            task Main(b: boolean): {
                while(b)
                    2;
            }
        ''').main.cfg.assertStructure('''
            entry -> 1
            1 -> 2
            2 -> 3, 5
            3 -> 4
            4 -> 1
            5 -> 6
            6 -> exit
        ''')
        
        parse('''
            task Main(b: boolean): {
                while(b)
                    return;
            }
        ''').main.cfg.assertStructure('''
            entry -> 1
            1 -> 2
            2 -> 3, 4
            3 -> exit
            4 -> 5
            5 -> exit
        ''')
    }
    
    @Test def testExpr() {
        parse('''
            task Main: {
                0;
            }
        ''').main.cfg.assertStructure('''
            entry -> 1
            1 -> 2
            2 -> 3
            3 -> exit
        ''')
        parse('''
            task Main: {
                (0);
            }
        ''').main.cfg.assertStructure('''
            entry -> 1
            1 -> 2
            2 -> 3
            3 -> 4
            4 -> exit
        ''')
        parse('''
            task Main: {
                -0;
            }
        ''').main.cfg.assertStructure('''
            entry -> 1
            1 -> 2
            2 -> 3
            3 -> 4
            4 -> exit
        ''')
        parse('''
            task Main: {
                !true;
            }
        ''').main.cfg.assertStructure('''
            entry -> 1
            1 -> 2
            2 -> 3
            3 -> 4
            4 -> exit
        ''')
        
        parse('''
            task Main: {
                0 + 1;
            }
        ''').main.cfg.assertStructure('''
            entry -> 1
            1 -> 2
            2 -> 3
            3 -> 4
            4 -> 5
            5 -> exit
        ''')
        
        parse('''
            task Main: {
                0 == 1;
            }
        ''').main.cfg.assertStructure('''
            entry -> 1
            1 -> 2
            2 -> 3
            3 -> 4
            4 -> 5
            5 -> exit
        ''')
        
        // Short-circuit!
        parse('''
            task Main(a: boolean, b: boolean): {
                a && b;
            }
        ''').main.cfg.assertStructure('''
            entry -> 1
            1 -> 2, 3
            2 -> 3
            3 -> 4
            4 -> 5
            5 -> exit
        ''')
        parse('''
            task Main(a: boolean, b: boolean): {
                a || b;
            }
        ''').main.cfg.assertStructure('''
            entry -> 1
            1 -> 3, 2
            2 -> 3
            3 -> 4
            4 -> 5
            5 -> exit
        ''')
        parse('''
            task Main(b: boolean): {
                0 == 1 && b;
            }
        ''').main.cfg.assertStructure('''
            entry -> 1
            1 -> 2
            2 -> 3
            3 -> 4, 5
            4 -> 5
            5 -> 6
            6 -> 7
            7 -> exit
        ''')
        parse('''
            task Main(a: boolean, b: boolean): {
                a && (1 > 2 || b);
            }
        ''').main.cfg.assertStructure('''
            entry -> 1
            1 -> 2, 8
            2 -> 3
            3 -> 4
            4 -> 6, 5
            5 -> 6
            6 -> 7
            7 -> 8
            8 -> 9
            9 -> 10
            10 -> exit
        ''')
    }
    
    private def cfg(ParameterizedBody it) {
        assertNoErrors
        controlFlowGraph
    }
    
    private def assertStructure(ControlFlowGraph it, String expected) {
        assertInvariants
        expected.assertEquals(dumpStructure)
    }
    
    private def assertInvariants(ControlFlowGraph it) {
        nodes.size.assertThat(greaterThan(2))
        nodes.assertThat(hasItem(entry))
        nodes.assertThat(hasItem(exit))
        entry.predecessors.assertThat(empty)
        entry.successors.assertThat(not(empty))
        exit.successors.assertThat(empty)
        exit.predecessors.assertThat(not(empty))
        
        val reachable = new HashSet
        entry.collectReachableNodes(reachable)
        nodes.toSet.assertThat(equalTo(reachable))
        
        for(node : reachable) {
            for(successor : node.successors)
                successor.predecessors.assertThat(hasItem(node))
            
            if(node.isSplit) {
                node.successors.size.assertThat(is(2))
                node.assertThat(instanceOf(InstrNode))
                val instr = (node as InstrNode).instr
                instr.assertThat(instanceOf(Expr))
                system.type(utils.envFor(instr), instr as Expr).value
                    .assertThat(instanceOf(Boolean))
            }
            else
                node.successors.size.assertThat(lessThan(2))
            
            if(node.isJoin) {
                node.predecessors.size.assertThat(is(2))
                if(node instanceof InstrNode)
                    node.instr.assertThat(either(instanceOf(IfStmt))
                        .or(instanceOf(LogicalExpr)))
            }
        }
        
        reachable.filter[successors.empty].size.assertThat(is(1))
        
        // IMPROVE: Check things like head nodes of loops or expressions have
        // been evaluated
    }
    
    private def void collectReachableNodes(Node it, Set<Node> nodes) {
        if(nodes += it) {
            successors.forEach[collectReachableNodes(nodes)]
            predecessors.forEach[collectReachableNodes(nodes)]
        }
    }
    
    private def dumpStructure(ControlFlowGraph it) {
        val i = (0..nodes.size).iterator
        val map = nodes.toInvertedMap[i.next.toString]
        map.put(entry, "entry")
        map.put(exit, "exit")
        
        map.entrySet.filter[!(key instanceof ExitNode)].map['''
            «value» -> «key.successors.map[map.get(it)].join(", ")»
        '''].join
    }
}
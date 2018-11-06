package ch.trick17.rolez.validation.cfg

import ch.trick17.rolez.TestUtils
import ch.trick17.rolez.rolez.Assignment
import ch.trick17.rolez.rolez.Boolean
import ch.trick17.rolez.rolez.Expr
import ch.trick17.rolez.rolez.IfStmt
import ch.trick17.rolez.rolez.Instr
import ch.trick17.rolez.rolez.LogicalExpr
import ch.trick17.rolez.rolez.Program
import ch.trick17.rolez.tests.RolezInjectorProvider
import ch.trick17.rolez.typesystem.RolezSystem
import java.util.HashSet
import java.util.Set
import javax.inject.Inject
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.XtextRunner
import org.eclipse.xtext.testing.util.ParseHelper
import org.eclipse.xtext.testing.validation.ValidationTestHelper
import org.junit.Test
import org.junit.runner.RunWith

import static org.hamcrest.Matchers.*

import static extension org.hamcrest.MatcherAssert.assertThat
import static extension org.junit.Assert.assertEquals

@RunWith(XtextRunner)
@InjectWith(RolezInjectorProvider)
class CfgBuilderTest {
    
    @Inject RolezSystem system
    @Inject extension CfgProvider
    @Inject extension TestUtils
    @Inject extension ParseHelper<Program>
    @Inject extension ValidationTestHelper
    
    @Test def testBlock() {
        parse("".withFrame).task.body.cfg.assertStructure('''
            entry -> 1
            1 -> exit
        ''')
        
        parse('''
            0;
            2;
            4;
        '''.withFrame).task.body.cfg.assertStructure('''
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
            0;
            return;
            "Hi";
            5+5;
        '''.withFrame).task.body.cfg.assertStructure('''
            entry -> 1
            1 -> 2
            2 -> 3
            3 -> exit
        ''')
    }
    
    @Test def testIfStmt() {
        parse('''
            0;
            if(b) {
                3;
                5;
            }
            else
                8;
        '''.withFrame).task.body.cfg.assertStructure('''
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
            if(b) {}
            else {}
        '''.withFrame).task.body.cfg.assertStructure('''
            entry -> 1
            1 -> 2, 3
            2 -> 4
            3 -> 4
            4 -> 5
            5 -> exit
        ''')
        
        parse('''
            0;
            if(b)
                3;
            else
                5;
            8;
        '''.withFrame).task.body.cfg.assertStructure('''
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
            if(b)
                1;
        '''.withFrame).task.body.cfg.assertStructure('''
            entry -> 1
            1 -> 2, 4
            2 -> 3
            3 -> 4
            4 -> 5
            5 -> exit
        ''')
        
        parse('''
            if(b)
                return;
            else {
                return;
            }
        '''.withFrame).task.body.cfg.assertStructure('''
            entry -> 1
            1 -> 2, 3
            2 -> exit
            3 -> exit
        ''')
        
        parse('''
            if(b)
                return;
            else
                return;
            3;
        '''.withFrame).task.body.cfg.assertStructure('''
            entry -> 1
            1 -> 2, 3
            2 -> exit
            3 -> exit
        ''')
        
        parse('''
            if(b) {
                return;
            }
        '''.withFrame).task.body.cfg.assertStructure('''
            entry -> 1
            1 -> 2, 3
            2 -> exit
            3 -> 4
            4 -> exit
        ''')
        
        parse('''
            if(b)
                return;
            3;
        '''.withFrame).task.body.cfg.assertStructure('''
            entry -> 1
            1 -> 2, 3
            2 -> exit
            3 -> 4
            4 -> 5
            5 -> 6
            6 -> exit
        ''')
        
        parse('''
            if(b) {}
            else
                return;
        '''.withFrame).task.body.cfg.assertStructure('''
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
            while(b)
                2;
        '''.withFrame).task.body.cfg.assertStructure('''
            entry -> 1
            1 -> 2
            2 -> 3, 5
            3 -> 4
            4 -> 1
            5 -> 6
            6 -> exit
        ''')
        
        parse('''
            while(b)
                return;
        '''.withFrame).task.body.cfg.assertStructure('''
            entry -> 1
            1 -> 2
            2 -> 3, 4
            3 -> exit
            4 -> 5
            5 -> exit
        ''')
    }
    
    @Test def testForLoop() {
        parse('''
            for(var i = 0; i < 10; i = i + 1)
                0;
        '''.withFrame).task.body.cfg.assertStructure('''
            entry -> 1
            1 -> 2
            2 -> 3
            3 -> 4
            4 -> 5
            5 -> 6
            6 -> 7, 14
            7 -> 8
            8 -> 9
            9 -> 10
            10 -> 11
            11 -> 12
            12 -> 13
            13 -> 3
            14 -> 15
            15 -> exit
        ''')
    }
    
    @Test def testLogicalExpr() {
        // Short-circuit!
        parse("a || b;".withFrame).task.body.cfg.assertStructure('''
            entry -> 1
            1 -> 3, 2
            2 -> 3
            3 -> 4
            4 -> 5
            5 -> exit
        ''')
        parse("a && b;".withFrame).task.body.cfg.assertStructure('''
            entry -> 1
            1 -> 2, 3
            2 -> 3
            3 -> 4
            4 -> 5
            5 -> exit
        ''')
        
        parse("a && (1 > 2 || b);".withFrame).task.body.cfg.assertStructure('''
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
        parse("0 == 1 && b;".withFrame).task.body.cfg.assertStructure('''
            entry -> 1
            1 -> 2
            2 -> 3
            3 -> 4, 5
            4 -> 5
            5 -> 6
            6 -> 7
            7 -> exit
        ''')
    }
    
    @Test def testAssignment() {
        // Short-circuit!
        parse('''
            var c = a;
            c |= b;
        '''.withFrame).task.body.cfg.assertStructure('''
            entry -> 1
            1 -> 2
            2 -> 3
            3 -> 5, 4
            4 -> 5
            5 -> 6
            6 -> 7
            7 -> exit
        ''')
        parse('''
            var c = a;
            c &= b;
        '''.withFrame).task.body.cfg.assertStructure('''
            entry -> 1
            1 -> 2
            2 -> 3
            3 -> 4, 5
            4 -> 5
            5 -> 6
            6 -> 7
            7 -> exit
        ''')
    }
    
    @Test def testBinaryExpr() {
        parse("0 + 1;".withFrame).task.body.cfg.assertStructure('''
            entry -> 1
            1 -> 2
            2 -> 3
            3 -> 4
            4 -> 5
            5 -> exit
        ''')
        
        parse("0 == 1;".withFrame).task.body.cfg.assertStructure('''
            entry -> 1
            1 -> 2
            2 -> 3
            3 -> 4
            4 -> 5
            5 -> exit
        ''')
        
        parse("1 << 2;".withFrame).task.body.cfg.assertStructure('''
            entry -> 1
            1 -> 2
            2 -> 3
            3 -> 4
            4 -> 5
            5 -> exit
        ''')
    }
    
    @Test def testUnaryExpr() {
        parse("(0);".withFrame).task.body.cfg.assertStructure('''
            entry -> 1
            1 -> 2
            2 -> 3
            3 -> 4
            4 -> exit
        ''')
        parse("-0;".withFrame).task.body.cfg.assertStructure('''
            entry -> 1
            1 -> 2
            2 -> 3
            3 -> 4
            4 -> exit
        ''')
        parse("!true;".withFrame).task.body.cfg.assertStructure('''
            entry -> 1
            1 -> 2
            2 -> 3
            3 -> 4
            4 -> exit
        ''')
        parse("~2;".withFrame).task.body.cfg.assertStructure('''
            entry -> 1
            1 -> 2
            2 -> 3
            3 -> 4
            4 -> exit
        ''')
    }
    
    @Test def testLiteral() {
        parse("0;".withFrame).task.body.cfg.assertStructure('''
            entry -> 1
            1 -> 2
            2 -> 3
            3 -> exit
        ''')
    }
    
    // IMPROVE: More tests, for MemberAccess and the like
    
    private def cfg(Instr it) {
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
                system.type(instr as Expr).value.assertThat(instanceOf(Boolean))
            }
            else
                node.successors.size.assertThat(lessThan(2))
            
            if(node.isJoin) {
                node.predecessors.size.assertThat(is(2))
                if(node instanceof InstrNode)
                    node.instr.assertThat(either(instanceOf(IfStmt))
                        .or(instanceOf(LogicalExpr)).or(instanceOf(Assignment)))
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

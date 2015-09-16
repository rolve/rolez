package ch.trick17.rolez.lang

import ch.trick17.rolez.lang.cfg.CfgBuilder
import ch.trick17.rolez.lang.rolez.Program
import javax.inject.Inject
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.junit.Test
import org.junit.runner.RunWith

import static org.hamcrest.Matchers.*

import static extension org.hamcrest.MatcherAssert.assertThat
import ch.trick17.rolez.lang.cfg.ControlFlowGraph

@RunWith(XtextRunner)
@InjectWith(RolezInjectorProvider)
class CfgBuilderTest {
    
    @Inject extension ParseHelper<Program>
    @Inject extension TestUtilz
    @Inject extension CfgBuilder
    
    @Test
    def testBlock() {
        var graph = parse('''
            task Main: {
                1;
                "Hi";
                5+5;
            }
        ''').main.controlFlowGraph
        graph.assertInvariants
        graph.allBlocks.size.assertThat(is(2))
        graph.enter.stmts.size.assertThat(is(3))
        graph.enter.successors.assertThat(is(newArrayList(graph.exit)))
        graph.exit.predecessors.assertThat(is(newArrayList(graph.enter)))
        
        graph = parse('''
            task Main: {
                1;
                "Hi";
                return;
                5+5;
            }
        ''').main.controlFlowGraph
        graph.assertInvariants
        graph.allBlocks.size.assertThat(is(2))
        graph.enter.stmts.size.assertThat(is(2))
        graph.enter.successors.assertThat(is(newArrayList(graph.exit)))
        graph.exit.predecessors.assertThat(is(newArrayList(graph.enter)))
    }
    
    @Test
    def testIfStmt() {
        var graph = parse('''
            task Main: {
                1;
                if(1 == 1)
                    2;
                else
                    3;
            }
        ''').main.controlFlowGraph
        graph.assertInvariants
        graph.allBlocks.size.assertThat(is(4))
        graph.enter.stmts.size.assertThat(is(2))
        graph.enter.successors.size.assertThat(is(2))
        graph.enter.successors.assertThat(is(graph.exit.predecessors))
        
        graph = parse('''
            task Main: {
                1;
                if(1 == 1)
                    2;
                else
                    3;
                4;
            }
        ''').main.controlFlowGraph
        graph.assertInvariants
        graph.allBlocks.size.assertThat(is(5))
        
        graph = parse('''
            task Main: {
                if(1 == 1)
                    2;
            }
        ''').main.controlFlowGraph
        graph.assertInvariants
        graph.allBlocks.size.assertThat(is(3))
        graph.enter.stmts.size.assertThat(is(1))
        graph.enter.successors.size.assertThat(is(2))
        graph.enter.successors.assertThat(hasItem(graph.exit))
        graph.exit.predecessors.assertThat(hasItem(graph.enter))
        
        graph = parse('''
            task Main: {
                if(1 == 1)
                    return;
                else {
                    return;
                }
            }
        ''').main.controlFlowGraph
        graph.assertInvariants
        graph.allBlocks.size.assertThat(is(4))
        graph.enter.successors.size.assertThat(is(2))
        graph.enter.successors.assertThat(is(graph.exit.predecessors))
        
        graph = parse('''
            task Main: {
                if(1 == 1)
                    return;
                else
                    return;
                2;
            }
        ''').main.controlFlowGraph
        graph.assertInvariants
        graph.allBlocks.size.assertThat(is(4))
        graph.enter.successors.size.assertThat(is(2))
        graph.enter.successors.assertThat(is(graph.exit.predecessors))
        
        graph = parse('''
            task Main: {
                if(1 == 1) {
                    return;
                }
            }
        ''').main.controlFlowGraph
        graph.assertInvariants
        graph.allBlocks.size.assertThat(is(3))
        graph.enter.successors.size.assertThat(is(2))
        graph.enter.successors.assertThat(hasItem(graph.exit))
        graph.exit.predecessors.size.assertThat(is(2))
        graph.exit.predecessors.assertThat(hasItem(graph.enter))
        
        graph = parse('''
            task Main: {
                if(1 == 1)
                    return;
                2;
            }
        ''').main.controlFlowGraph
        graph.assertInvariants
        graph.allBlocks.size.assertThat(is(4))
        graph.enter.successors.size.assertThat(is(2))
        graph.exit.predecessors.size.assertThat(is(2))
    }
    
    @Test
    def testWhileLoop() {
        var graph = parse('''
            task Main: {
                while(1 == 1)
                    1;
            }
        ''').main.controlFlowGraph
        graph.assertInvariants
        graph.allBlocks.size.assertThat(is(4))
        graph.enter.stmts.size.assertThat(is(0))
        graph.enter.successors.size.assertThat(is(1))
        graph.enter.successors.head.successors.size.assertThat(is(2))
        graph.enter.successors.head.predecessors.size.assertThat(is(2))
        graph.exit.predecessors.size.assertThat(is(1))
        
        graph = parse('''
            task Main: {
                while(1 == 2)
                    return;
            }
        ''').main.controlFlowGraph
        graph.assertInvariants
        graph.allBlocks.size.assertThat(is(4))
        graph.enter.successors.head.successors.size.assertThat(is(2))
        graph.enter.successors.head.predecessors.size.assertThat(is(1))
        graph.exit.predecessors.size.assertThat(is(2))
    }
    
    def assertInvariants(ControlFlowGraph it) {
        allBlocks.assertThat(is(enter.reachableBlocks))
        allBlocks.assertThat(hasItem(enter))
        allBlocks.assertThat(hasItem(exit))
        allBlocks.size.assertThat(greaterThanOrEqualTo(2))
        enter.assertThat(not(sameInstance((exit))))
        
        enter.predecessors.assertThat(empty)
        enter.successors.assertThat(not(empty))
        
        exit.predecessors.assertThat(not(empty))
        exit.successors.assertThat(empty)
        exit.stmts.assertThat(empty)
    }
}
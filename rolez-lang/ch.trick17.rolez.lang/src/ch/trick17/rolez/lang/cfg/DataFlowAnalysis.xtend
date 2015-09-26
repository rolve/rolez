package ch.trick17.rolez.lang.cfg

import ch.trick17.rolez.lang.rolez.Instr
import java.util.HashMap
import java.util.LinkedList
import java.util.Map

import static extension java.util.Objects.requireNonNull

/**
 * Objects of type F must be immutable
 */
abstract class DataFlowAnalysis<F> {
    
    protected val ControlFlowGraph graph
    protected val boolean forward
    
    val Map<Node, F> beforeFlows = new HashMap
    val Map<Node, F> afterFlows = new HashMap
    
    new (ControlFlowGraph graph, boolean forward) {
        this.graph = graph;
        this.forward = forward
        
        if(newFlow != newFlow)
            throw new AssertionError("flow type must provide a sensible equals")
    }
    
    protected def F newFlow()
    protected def F entryFlow()
    protected def F flowThrough(Instr i, F before)
    protected def F merge(F flow1, F flow2)
    
    protected def analyze() {
        val worklist = new LinkedList(graph.nodes(!forward))
        
        for(node : worklist)
            afterFlows.put(node, newFlow)
        afterFlows.put(worklist.remove, entryFlow)
        
        while(!worklist.isEmpty) {
            val node = worklist.remove
            val before = node.prevNodes.map[afterFlow].reduce[f1, f2 | merge(f1, f2)]
            beforeFlows.put(node, before)
            val after =
                if(node instanceof InstrNode) flowThrough(node.instr, before)
                else before
            
            val oldAfter = afterFlows.put(node, after)
            if(after != oldAfter)
                for(s : node.nextNodes)
                    if(!worklist.contains(s)) worklist.add(s)
        }
    }
    
    private def prevNodes(Node it) { if(forward) predecessors else successors }
    private def nextNodes(Node it) { if(forward) successors else predecessors }
    
    protected def beforeFlow(Node it) { beforeFlows.get(it).requireNonNull }
    protected def afterFlow(Node it)  { afterFlows.get(it).requireNonNull }
}
package ch.trick17.rolez.validation.dataflow

import ch.trick17.rolez.rolez.Instr
import ch.trick17.rolez.validation.cfg.ControlFlowGraph
import ch.trick17.rolez.validation.cfg.InstrNode
import ch.trick17.rolez.validation.cfg.Node
import java.util.HashMap
import java.util.LinkedList
import java.util.Map

/**
 * Objects of type F must be immutable
 */
abstract class DataFlowAnalysis<F> {
    
    protected val ControlFlowGraph cfg
    protected val boolean forward
    
    val Map<Node, F>  inFlows = new HashMap
    val Map<Node, F> outFlows = new HashMap
    
    new (ControlFlowGraph cfg, boolean forward) {
        this.cfg = cfg;
        this.forward = forward
        
        if(newFlow != newFlow)
            throw new AssertionError("flow type must provide a sensible equals")
    }
    
    protected def F newFlow()
    protected def F entryFlow()
    protected def F flowThrough(Instr i, F in)
    protected def F merge(F in1, F in2)
    
    protected def analyze() {
        val worklist = new LinkedList(cfg.nodes(!forward))
        // IMPROVE: Replace with LinkedHashSet for more efficient insertions?
        
        for(node : worklist)
            outFlows.put(node, newFlow)
        outFlows.put(worklist.remove, entryFlow)
        
        while(!worklist.isEmpty) {
            val node = worklist.remove
            val in = node.prevNodes.map[outFlow].reduce[f1, f2 | merge(f1, f2)]
            inFlows.put(node, in)
            val out =
                if(node instanceof InstrNode) flowThrough(node.instr, in)
                else in
            
            val oldOut = outFlows.put(node, out)
            if(out != oldOut)
                for(s : node.nextNodes)
                    if(!worklist.contains(s)) worklist.add(s)
        }
    }
    
    private def prevNodes(Node it) { if(forward) predecessors else successors }
    private def nextNodes(Node it) { if(forward) successors else predecessors }
    
    protected def  inFlow(Node it) {  inFlows.get(it) }
    protected def outFlow(Node it) { outFlows.get(it) }
}

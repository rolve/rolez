package ch.trick17.rolez.validation.cfg

import ch.trick17.rolez.rolez.Instr
import java.util.HashSet
import java.util.LinkedList
import java.util.List
import java.util.Map
import java.util.Set

class ControlFlowGraph {
    
    public val EntryNode entry
    public val ExitNode exit
    val Map<Instr, Node> instrMap
    
    package new (EntryNode entry, ExitNode exit, Map<Instr, Node> instrMap) {
        this.entry = entry
        this.exit = exit
        this.instrMap = instrMap
    }
    
    /**
     * Returns all nodes in this graph, in reverse post-order
     */
    def List<Node> nodes() { nodes(false) }
    
    /**
     * Returns all nodes in this graph, in reverse post-order.
     * If <code>reverse</code> is <code>true</code>, the reverse CFG is taken
     * to compute the order (so nodes are returned in <em>reverse post-order of
     * the reverse CFG</em> :P).
     */
    def List<Node> nodes(boolean reverse) {
        val list = new LinkedList
        val seen = new HashSet
        collectNodes(if(reverse) exit else entry, list, seen, reverse)
        list
    }
    
    private def void collectNodes(Node n, LinkedList<Node> list, Set<Node> seen, boolean reverse) {
        if(seen += n) {
            (if(reverse) n.preds else n.succs)
                .reverseView.forEach[collectNodes(list, seen, reverse)]
            list.addFirst(n)
        }
    }
    
    def nodeOf(Instr i) { instrMap.get(i) }
}

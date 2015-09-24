package ch.trick17.rolez.lang.cfg

import java.util.HashSet
import java.util.LinkedList
import java.util.List
import java.util.Set

class ControlFlowGraph {
    
    public val Node entry
    public val Node exit
    
    package new (Node entry, Node exit) {
        this.entry = entry
        this.exit = exit
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
}
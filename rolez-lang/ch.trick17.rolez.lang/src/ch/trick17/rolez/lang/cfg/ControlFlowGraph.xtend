package ch.trick17.rolez.lang.cfg

import java.util.HashSet
import java.util.LinkedList
import java.util.List
import java.util.Set

class ControlFlowGraph {
    
    public val Node entry
    public val ExitNode exit
    
    new (Node entry, ExitNode exit) {
        this.entry = entry
        this.exit = exit
    }
    
    /**
     * Returns all nodes in this graph, in reverse post-order
     */
    def List<Node> nodes() {
        val list = new LinkedList
        val seen = new HashSet
        collectSuccessors(entry, list, seen)
        list
    }
    
    private def void collectSuccessors(Node n, LinkedList<Node> list, Set<Node> seen) {
        if(seen += n) {
            n.successors.reverseView.forEach[collectSuccessors(list, seen)]
            list.addFirst(n)
        }
    }
}
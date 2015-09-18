package ch.trick17.rolez.lang.cfg

import ch.trick17.rolez.lang.rolez.Block
import ch.trick17.rolez.lang.rolez.IfStmt
import ch.trick17.rolez.lang.rolez.ParameterizedBody
import ch.trick17.rolez.lang.rolez.Return
import ch.trick17.rolez.lang.rolez.Stmt
import ch.trick17.rolez.lang.rolez.WhileLoop

import static extension java.util.Objects.requireNonNull

class CfgBuilder {
    
    /* Linkers are used to elegantly connect CFG nodes */
    
    package interface Linker {
        def boolean link(Node n)
    }
    
    private static class NodeHolder implements Linker {
        public var Node node
        override link(Node n) {
            if(node != null) throw new IllegalStateException
            this.node = n
            true
        }
    }
    
    private def Linker linker(StmtNode it) {[ node |
        if(successor != null) throw new IllegalStateException
        successor = node.requireNonNull
        node.addPredecessor(it)
    ]}
    
    private def Linker thenLinker(ConditionNode it) {[node | setSuccessor(node, 0)]}
    private def Linker elseLinker(ConditionNode it) {[node | setSuccessor(node, 1)]}
    
    private def setSuccessor(ConditionNode it, Node node, int index) {
        if(successors.get(index) != null) throw new IllegalStateException
        successors.set(index, node.requireNonNull)
        node.addPredecessor(it)
    }
    
    /* Here comes the implementation */
    
    def controlFlowGraph(ParameterizedBody it) {
        val enter = new NodeHolder
        val exit = new ExitNode
        process(body, enter, exit).link(exit)
        return new ControlFlowGraph(enter.node, exit)
    }
    
    private def dispatch Linker process(Block block, Linker prev, ExitNode exit) {
        block.stmts.fold(prev, [p, stmt | process(stmt, p, exit)])
    }
    
    private def dispatch Linker process(IfStmt s, Linker prev, ExitNode exit) {
        val node = new ConditionNode(s.condition)
        if(!prev.link(node))
            return [false]
        
        val thenLinker = process(s.thenPart, node.thenLinker, exit)
        val elseLinker = 
            if(s.elsePart == null) node.elseLinker
            else process(s.elsePart, node.elseLinker, exit);
        
        [val linked = elseLinker.link(it); thenLinker.link(it) || linked]
    }
    
    private def dispatch Linker process(WhileLoop w, Linker prev, ExitNode exit) {
        val node = new ConditionNode(w.condition)
        if(!prev.link(node))
            return [false]
        
        process(w.body, node.thenLinker, exit).link(node)
        node.elseLinker
    }
    
    private def dispatch Linker process(Stmt s, Linker prev, ExitNode exit) {
        val node = new StmtNode(s)
        if(prev.link(node))
            node.linker
        else
            [false]
    }
    
    private def dispatch Linker process(Return r, Linker prev, ExitNode exit) {
        val node = new StmtNode(r)
        if(prev.link(node))
            node.linker.link(exit);
        [false]
    }
}
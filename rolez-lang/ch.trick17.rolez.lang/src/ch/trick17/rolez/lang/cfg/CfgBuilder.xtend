package ch.trick17.rolez.lang.cfg

import ch.trick17.rolez.lang.rolez.BinaryExpr
import ch.trick17.rolez.lang.rolez.Block
import ch.trick17.rolez.lang.rolez.Expr
import ch.trick17.rolez.lang.rolez.ExprStmt
import ch.trick17.rolez.lang.rolez.FieldSelector
import ch.trick17.rolez.lang.rolez.IfStmt
import ch.trick17.rolez.lang.rolez.LocalVarDecl
import ch.trick17.rolez.lang.rolez.LogicalExpr
import ch.trick17.rolez.lang.rolez.MemberAccess
import ch.trick17.rolez.lang.rolez.MethodSelector
import ch.trick17.rolez.lang.rolez.New
import ch.trick17.rolez.lang.rolez.ParameterizedBody
import ch.trick17.rolez.lang.rolez.ReturnExpr
import ch.trick17.rolez.lang.rolez.ReturnNothing
import ch.trick17.rolez.lang.rolez.Start
import ch.trick17.rolez.lang.rolez.UnaryExpr
import ch.trick17.rolez.lang.rolez.WhileLoop

import static extension java.util.Objects.requireNonNull
import static ch.trick17.rolez.lang.rolez.OpLogical.*

class CfgBuilder {
    
    /* Linkers are used to elegantly connect CFG nodes */
    
    private abstract static class Linker {
        def boolean link(Node n)
        def Linker linkAndReturn(Node n) {
            if(link(n)) n.linker else [false]
        }
    }
    
    private static def Linker linker(Node it) {[ node |
        if(succs.size > 1) throw new IllegalStateException
        // If there are two successors, the first corresponds to the "true"
        // case, the second to the "false" case. So the order of linking
        // matters!
        succs.add(node.requireNonNull)
        node.preds.add(it)
    ]}
    
    private static class NodeHolder extends Linker {
        public var Node node
        override link(Node n) {
            if(node != null) throw new IllegalStateException
            this.node = n
            true
        }
    }
    
    /* Here comes the implementation */
    
    def controlFlowGraph(ParameterizedBody it) {
        val enter = new NodeHolder
        val exit = new ExitNode
        processStmt(body, enter, exit).link(exit)
        return new ControlFlowGraph(enter.node, exit)
    }
    
    private def dispatch Linker processStmt(Block block, Linker prev, ExitNode exit) {
        block.stmts.fold(prev, [p, stmt | processStmt(stmt, p, exit)])
    }
    
    private def dispatch Linker processStmt(LocalVarDecl d, Linker prev, ExitNode exit) {
        val linker =
            if(d.initializer == null) prev
            else processExpr(d.initializer, prev)
        linker.linkAndReturn(new StmtNode(d))
    }
    
    private def dispatch Linker processStmt(IfStmt s, Linker prev, ExitNode exit) {
        val conditionLinker = processExpr(s.condition, prev)
        val thenLinker = processStmt(s.thenPart, conditionLinker, exit)
        val elseLinker = 
            if(s.elsePart == null) conditionLinker
            else processStmt(s.elsePart, conditionLinker, exit);
        
        val node = new StmtNode(s)
        thenLinker.link(node)
        elseLinker.linkAndReturn(node)
    }
    
    private def dispatch Linker processStmt(WhileLoop l, Linker prev, ExitNode exit) {
        val headNode = new LoopHeadNode(l)
        if(!prev.link(headNode))
            return [false]
        
        val conditionLinker = processExpr(l.condition, headNode.linker)
        processStmt(l.body, conditionLinker, exit).link(headNode)
        conditionLinker.linkAndReturn(new StmtNode(l))
    }
    
    private def dispatch Linker processStmt(ReturnNothing r, Linker prev, ExitNode exit) {
        prev.linkAndReturn(new StmtNode(r)).link(exit);
        [false]
    }
    
    private def dispatch Linker processStmt(ReturnExpr r, Linker prev, ExitNode exit) {
        processExpr(r.expr, prev).linkAndReturn(new StmtNode(r)).link(exit);
        [false]
    }
    
    private def dispatch Linker processStmt(ExprStmt s, Linker prev, ExitNode exit) {
        processExpr(s.expr, prev).linkAndReturn(new StmtNode(s))
    }
    
    private def dispatch Linker processExpr(BinaryExpr e, Linker prev) {
        val leftLinker = processExpr(e.left, prev)
        processExpr(e.right, leftLinker).linkAndReturn(new ExprNode(e))
    }
    
    private def dispatch Linker processExpr(LogicalExpr e, Linker prev) {
        val leftLinker = processExpr(e.left, prev)
        val node = new ExprNode(e)
        if(e.op == OR) {
            // Short-circuit to "&&" node if left is "true", so link "&&" node first
            leftLinker.link(node)
            processExpr(e.right, leftLinker).linkAndReturn(node)
        }
        else {
            // Short-circuit to "&&" node if left is "false", so link "&&" node second
            processExpr(e.right, leftLinker).link(node)
            leftLinker.linkAndReturn(node)
        }
    }
    
    private def dispatch Linker processExpr(UnaryExpr e, Linker prev) {
        processExpr(e.expr, prev).linkAndReturn(new ExprNode(e))
    }
    
    private def dispatch Linker processExpr(MemberAccess a, Linker prev) {
        val targetLinker = processExpr(a.target, prev)
        val selector = a.selector
        val lastLinker = switch(selector) {
            FieldSelector: targetLinker.linkAndReturn(new ExprNode(a))
            MethodSelector:
                selector.args.fold(targetLinker, [p, e | processExpr(e, p)])
        }
        lastLinker.linkAndReturn(new ExprNode(a))
    }
    
    private def dispatch Linker processExpr(New n, Linker prev) {
        val lastLinker = n.args.fold(prev, [p, e | processExpr(e, p)])
        lastLinker.linkAndReturn(new ExprNode(n))
    }
    
    private def dispatch Linker processExpr(Start s, Linker prev) {
        val lastLinker = s.args.fold(prev, [p, e | processExpr(e, p)])
        lastLinker.linkAndReturn(new ExprNode(s))
    }
    
    // Everything else (This, VarRef, Literals)
    private def dispatch Linker processExpr(Expr e, Linker prev) {
        prev.linkAndReturn(new ExprNode(e))
    }
}
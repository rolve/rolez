package ch.trick17.rolez.lang.cfg

import ch.trick17.rolez.lang.rolez.BinaryExpr
import ch.trick17.rolez.lang.rolez.Block
import ch.trick17.rolez.lang.rolez.Expr
import ch.trick17.rolez.lang.rolez.ExprStmt
import ch.trick17.rolez.lang.rolez.FieldSelector
import ch.trick17.rolez.lang.rolez.IfStmt
import ch.trick17.rolez.lang.rolez.Instr
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
import java.util.HashMap
import java.util.Map

import static ch.trick17.rolez.lang.rolez.OpLogical.*

import static extension ch.trick17.rolez.lang.cfg.CfgBuilder.*
import static extension java.util.Objects.requireNonNull
import ch.trick17.rolez.lang.rolez.SuperConstrCall

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
    
    private static def Linker +(Linker l1, Linker l2) {[ node |
        val linked = l1.link(node); l2.link(node) || linked // Avoid short-circuit
    ]}
    
    /* Here comes the implementation */
    
    val ParameterizedBody body
    val ExitNode exit
    val Map<Instr, Node> instrMap = new HashMap
    
    new(ParameterizedBody body) {
        if(body.body == null) throw new AssertionError
        this.body = body
        this.exit = new ExitNode
    }
    
    def build() {
        val entry = new EntryNode
        process(body.body, entry.linker).link(exit)
        return new ControlFlowGraph(entry, exit, instrMap)
    }
    
    private def newInstrNode(Instr i) {
        val node = new InstrNode(i)
        instrMap.put(i, node)
        node
    }
    
    private def dispatch Linker process(Block block, Linker prev) {
        block.stmts.fold(prev, [p, stmt | process(stmt, p)])
            .linkAndReturn(newInstrNode(block))
    }
    
    private def dispatch Linker process(LocalVarDecl d, Linker prev) {
        val linker =
            if(d.initializer == null) prev
            else process(d.initializer, prev)
        linker.linkAndReturn(newInstrNode(d))
    }
    
    private def dispatch Linker process(IfStmt s, Linker prev) {
        val conditionLinker = process(s.condition, prev)
        val thenLinker = process(s.thenPart, conditionLinker)
        val elseLinker = 
            if(s.elsePart == null) conditionLinker
            else process(s.elsePart, conditionLinker);
        
        (thenLinker + elseLinker).linkAndReturn(newInstrNode(s))
    }
    
    private def dispatch Linker process(WhileLoop l, Linker prev) {
        val headNode = new LoopHeadNode(l)
        if(!prev.link(headNode))
            return [false]
        
        val conditionLinker = process(l.condition, headNode.linker)
        process(l.body, conditionLinker).link(headNode)
        conditionLinker.linkAndReturn(newInstrNode(l))
    }
    
    private def dispatch Linker process(ReturnNothing r, Linker prev) {
        prev.linkAndReturn(newInstrNode(r)).link(exit);
        [false]
    }
    
    private def dispatch Linker process(SuperConstrCall c, Linker prev) {
        c.args.fold(prev, [p, e | process(e, p)]).linkAndReturn(new InstrNode(c))
    }
    
    private def dispatch Linker process(ReturnExpr r, Linker prev) {
        process(r.expr, prev).linkAndReturn(newInstrNode(r)).link(exit);
        [false]
    }
    
    private def dispatch Linker process(ExprStmt s, Linker prev) {
        process(s.expr, prev).linkAndReturn(newInstrNode(s))
    }
    
    private def dispatch Linker process(BinaryExpr e, Linker prev) {
        val leftLinker = process(e.left, prev)
        process(e.right, leftLinker).linkAndReturn(newInstrNode(e))
    }
    
    private def dispatch Linker process(LogicalExpr e, Linker prev) {
        val leftLinker = process(e.left, prev)
        val node = newInstrNode(e)
        if(e.op == OR) {
            // Short-circuit to "&&" node if left is "true", so link "&&" node first
            leftLinker.link(node)
            process(e.right, leftLinker).linkAndReturn(node)
        }
        else {
            // Short-circuit to "&&" node if left is "false", so link "&&" node second
            process(e.right, leftLinker).link(node)
            leftLinker.linkAndReturn(node)
        }
    }
    
    private def dispatch Linker process(UnaryExpr e, Linker prev) {
        process(e.expr, prev).linkAndReturn(newInstrNode(e))
    }
    
    private def dispatch Linker process(MemberAccess a, Linker prev) {
        val targetLinker = process(a.target, prev)
        val selector = a.selector
        val lastLinker = switch(selector) {
            FieldSelector: targetLinker.linkAndReturn(newInstrNode(a))
            MethodSelector:
                selector.args.fold(targetLinker, [p, e | process(e, p)])
        }
        lastLinker.linkAndReturn(newInstrNode(a))
    }
    
    private def dispatch Linker process(New n, Linker prev) {
        val lastLinker = n.args.fold(prev, [p, e | process(e, p)])
        lastLinker.linkAndReturn(newInstrNode(n))
    }
    
    private def dispatch Linker process(Start s, Linker prev) {
        val lastLinker = s.args.fold(prev, [p, e | process(e, p)])
        lastLinker.linkAndReturn(newInstrNode(s))
    }
    
    // Everything else (This, VarRef, Literals)
    private def dispatch Linker process(Expr e, Linker prev) {
        prev.linkAndReturn(newInstrNode(e))
    }
}
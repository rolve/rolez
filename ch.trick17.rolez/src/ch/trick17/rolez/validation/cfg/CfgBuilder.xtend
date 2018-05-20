package ch.trick17.rolez.validation.cfg

import ch.trick17.rolez.rolez.Assignment
import ch.trick17.rolez.rolez.BinaryExpr
import ch.trick17.rolez.rolez.Block
import ch.trick17.rolez.rolez.ExprStmt
import ch.trick17.rolez.rolez.ForLoop
import ch.trick17.rolez.rolez.IfStmt
import ch.trick17.rolez.rolez.Instr
import ch.trick17.rolez.rolez.Literal
import ch.trick17.rolez.rolez.LocalVarDecl
import ch.trick17.rolez.rolez.LogicalExpr
import ch.trick17.rolez.rolez.MemberAccess
import ch.trick17.rolez.rolez.New
import ch.trick17.rolez.rolez.OpAssignment
import ch.trick17.rolez.rolez.ParallelStmt
import ch.trick17.rolez.rolez.Parfor
import ch.trick17.rolez.rolez.Ref
import ch.trick17.rolez.rolez.ReturnExpr
import ch.trick17.rolez.rolez.ReturnNothing
import ch.trick17.rolez.rolez.Slicing
import ch.trick17.rolez.rolez.SuperConstrCall
import ch.trick17.rolez.rolez.The
import ch.trick17.rolez.rolez.UnaryExpr
import ch.trick17.rolez.rolez.WhileLoop
import java.util.HashMap
import java.util.Map

import static ch.trick17.rolez.rolez.OpLogical.*

import static extension java.util.Objects.requireNonNull

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
    
    val Instr instr
    val ExitNode exit
    val Map<Instr, Node> instrMap = new HashMap
    
    package new(Instr instr) {
        if(instr === null) throw new AssertionError
        this.instr = instr
        this.exit = new ExitNode
    }
    
    def build() {
        val entry = new EntryNode
        process(instr, entry.linker).link(exit)
        return new ControlFlowGraph(entry, exit, instrMap)
    }
    
    private def newInstrNode(Instr i) {
        val node = new InstrNode(i)
        instrMap.put(i, node)
        node
    }
    
    // not quite sure what the consequences are here for putting the calls sequentially. might be fine
    private def dispatch Linker process(ParallelStmt p, Linker prev) {
        val part1Linker = process(p.part1, prev)
        val part2Linker = process(p.part2, part1Linker)
        part2Linker.linkAndReturn(newInstrNode(p))
    }
    
    // so far just same as for loop. it's' probably all right
    private def dispatch Linker process(Parfor l, Linker prev) {
        val headNode = new LoopHeadNode
        if(!process(l.initializer, prev).link(headNode)) return [false]
        
        val conditionLinker = process(l.condition, headNode.linker)
        val bodyLinker = process(l.body, conditionLinker)
        process(l.step, bodyLinker).link(headNode)
        conditionLinker.linkAndReturn(newInstrNode(l))
    }
    
    private def dispatch Linker process(Block block, Linker prev) {
        block.stmts.fold(prev, [p, stmt | process(stmt, p)])
            .linkAndReturn(newInstrNode(block))
    }
    
    private def dispatch Linker process(LocalVarDecl d, Linker prev) {
        val linker =
            if(d.initializer === null) prev
            else process(d.initializer, prev)
        linker.linkAndReturn(newInstrNode(d))
    }
    
    private def dispatch Linker process(IfStmt s, Linker prev) {
        val conditionLinker = process(s.condition, prev)
        val thenLinker = process(s.thenPart, conditionLinker)
        val elseLinker = 
            if(s.elsePart === null) conditionLinker
            else process(s.elsePart, conditionLinker)
        
        (thenLinker + elseLinker).linkAndReturn(newInstrNode(s))
    }
    
    private def dispatch Linker process(WhileLoop l, Linker prev) {
        val headNode = new LoopHeadNode
        if(!prev.link(headNode)) return [false]
        
        val conditionLinker = process(l.condition, headNode.linker)
        process(l.body, conditionLinker).link(headNode)
        conditionLinker.linkAndReturn(newInstrNode(l))
    }
    
    private def dispatch Linker process(ForLoop l, Linker prev) {
        val headNode = new LoopHeadNode
        if(!process(l.initializer, prev).link(headNode)) return [false]
        
        val conditionLinker = process(l.condition, headNode.linker)
        val bodyLinker = process(l.body, conditionLinker)
        process(l.step, bodyLinker).link(headNode)
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
    
    private def dispatch Linker process(LogicalExpr e, Linker prev) {
        val leftLinker = process(e.left, prev)
        val node = newInstrNode(e)
        if(e.op == OR) {
            // Short-circuit to "||" node if left is "true", so link "||" node first
            leftLinker.link(node)
            process(e.right, leftLinker).linkAndReturn(node)
        }
        else {
            // Short-circuit to "&&" node if left is "false", so link "&&" node second
            process(e.right, leftLinker).link(node)
            leftLinker.linkAndReturn(node)
        }
    }
    
    private def dispatch Linker process(Assignment a, Linker prev) {
        val leftLinker = process(a.left, prev)
        val node = newInstrNode(a)
        if(a.op == OpAssignment.OR_ASSIGN) {
            // Short-circuit to "|=" node if left is "true", so link "|=" node first
            leftLinker.link(node)
            process(a.right, leftLinker).linkAndReturn(node)
        }
        else if(a.op == OpAssignment.AND_ASSIGN) {
            // Short-circuit to "&=" node if left is "false", so link "&=" node second
            process(a.right, leftLinker).link(node)
            leftLinker.linkAndReturn(node)
        }
        else
            process(a.right, leftLinker).linkAndReturn(node)
    }
    
    private def dispatch Linker process(BinaryExpr e, Linker prev) {
        val leftLinker = process(e.left, prev)
        process(e.right, leftLinker).linkAndReturn(newInstrNode(e))
    }
    
    private def dispatch Linker process(UnaryExpr e, Linker prev) {
        process(e.expr, prev).linkAndReturn(newInstrNode(e))
    }
    
    private def dispatch Linker process(Slicing s, Linker prev) {
        process(s.target, prev).linkAndReturn(newInstrNode(s))
    }
    
    private def dispatch Linker process(MemberAccess a, Linker prev) {
        a.allArgs.fold(prev, [p, e | process(e, p)]).linkAndReturn(newInstrNode(a))
    }
    
    private def dispatch Linker process(New n, Linker prev) {
        n.args.fold(prev, [p, e | process(e, p)]).linkAndReturn(newInstrNode(n))
    }
    
    /* Simple cases: */
    
    private def dispatch Linker process(The t, Linker prev) {
        prev.linkAndReturn(newInstrNode(t))
    }
    
    private def dispatch Linker process(Ref r, Linker prev) {
        prev.linkAndReturn(newInstrNode(r))
    }
    
    private def dispatch Linker process(Literal l, Linker prev) {
        prev.linkAndReturn(newInstrNode(l))
    }
}

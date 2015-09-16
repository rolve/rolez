package ch.trick17.rolez.lang.cfg

import ch.trick17.rolez.lang.rolez.Block
import ch.trick17.rolez.lang.rolez.Expr
import ch.trick17.rolez.lang.rolez.IfStmt
import ch.trick17.rolez.lang.rolez.ParameterizedBody
import ch.trick17.rolez.lang.rolez.RolezFactory
import ch.trick17.rolez.lang.rolez.Stmt
import ch.trick17.rolez.lang.rolez.WhileLoop
import ch.trick17.rolez.lang.rolez.Return
import ch.trick17.rolez.lang.rolez.ReturnExpr

class CfgBuilder {
    
    def controlFlowGraph(ParameterizedBody it) {
        val enter = new BasicBlock
        val exit = new BasicBlock
        val finalBlock = process(body, enter, exit)
        
        // "Merge" final block with exit if it's empty
        if(finalBlock != null)
            if(finalBlock.stmts.empty)
                for(p : finalBlock.predecessors.clone) {
                    finalBlock.unlinkPredecessor(p)
                    exit.linkPredecessor(p)
                }
            else
                exit.linkPredecessor(finalBlock)
        
        return new ControlFlowGraph(enter, exit)
    }
    
    private def dispatch BasicBlock process(Block block, BasicBlock current, BasicBlock exit) {
        block.stmts.fold(current, [b, stmt |
            if(b == null) null else process(stmt, b, exit)
        ])
    }
    
    private def dispatch BasicBlock process(IfStmt s, BasicBlock current, BasicBlock exit) {
        current.stmts += s.condition.asStmt
        val thenBlock = process(s.thenPart, new BasicBlock(current), exit)
        val next =
            if(thenBlock == null) new BasicBlock
            else new BasicBlock(thenBlock)
        if(s.elsePart != null) {
            val elseBlock = process(s.elsePart, new BasicBlock(current), exit)
            if(elseBlock != null)
                next.linkPredecessor(elseBlock)
        }
        else
            next.linkPredecessor(current)
        
        if(next.predecessors.empty) null
        else next
    }
    
    private def dispatch BasicBlock process(WhileLoop w, BasicBlock current, BasicBlock exit) {
        val conditionBlock = new BasicBlock(current)
        conditionBlock.stmts += w.condition.asStmt
        val bodyBlock = process(w.body, new BasicBlock(conditionBlock), exit)
        val next = new BasicBlock(conditionBlock)
        if(bodyBlock != null)
            conditionBlock.linkPredecessor(bodyBlock)
        next
    }
    
    private def dispatch BasicBlock process(Return s, BasicBlock current, BasicBlock exit) {
        if(s instanceof ReturnExpr)
            current.stmts += s.expr.asStmt
        exit.linkPredecessor(current)
        null
    }
    
    private def dispatch BasicBlock process(Stmt s, BasicBlock current, BasicBlock exit) {
        current.stmts += s
        current
    }
    
    private def asStmt(Expr e) {
        RolezFactory.eINSTANCE.createExprStmt => [expr = e]
    }
}
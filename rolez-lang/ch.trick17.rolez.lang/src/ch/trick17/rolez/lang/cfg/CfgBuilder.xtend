package ch.trick17.rolez.lang.cfg

import ch.trick17.rolez.lang.rolez.Block
import ch.trick17.rolez.lang.rolez.IfStmt
import ch.trick17.rolez.lang.rolez.ParameterizedBody
import ch.trick17.rolez.lang.rolez.Return
import ch.trick17.rolez.lang.rolez.Stmt
import ch.trick17.rolez.lang.rolez.WhileLoop
import java.util.Optional

class CfgBuilder {
    
    def controlFlowGraph(ParameterizedBody it) {
        val enter = new BasicBlock(body)
        val exit = new BasicBlock(body)
        exit.setNoSuccessors()
        process(body.lift, enter.lift, exit).ifPresent[setSuccessors(exit)]
        return new ControlFlowGraph(enter, exit)
    }
    
    private def process(Optional<Stmt> s, Optional<BasicBlock> current, BasicBlock exit) {
        current.flatMap[c | s.flatMap[doProcess(it, c, exit)]]
    }
    
    private def dispatch Optional<BasicBlock> doProcess(Block block, BasicBlock current, BasicBlock exit) {
        block.stmts.fold(current.lift, [b, stmt | process(stmt.lift, b, exit)])
    }
    
    private def dispatch Optional<BasicBlock> doProcess(IfStmt s, BasicBlock current, BasicBlock exit) {
        val thenBlock = new BasicBlock(s)
        val elseBlock = s.elsePart.asOptional.map[new BasicBlock(s)]
        val next = new BasicBlock(current.associatedStmt)
        
        current.setSuccessors(thenBlock, elseBlock.orElse(next), s.condition)
        process(s.thenPart.lift, thenBlock.lift, exit).ifPresent[setSuccessors(next)]
        process(s.elsePart.asOptional, elseBlock, exit).ifPresent[setSuccessors(next)]
        
        if(next.predecessors.empty) Optional.empty
        else next.lift
    }
    
    private def dispatch Optional<BasicBlock> doProcess(WhileLoop w, BasicBlock current, BasicBlock exit) {
        val conditionBlock = new BasicBlock(w)
        val bodyBlock = new BasicBlock(w.body)
        val next = new BasicBlock(current.associatedStmt)
        
        current.setSuccessors(conditionBlock)
        conditionBlock.setSuccessors(bodyBlock, next, w.condition)
        process(w.body.lift, bodyBlock.lift, exit).ifPresent[setSuccessors(conditionBlock)]
        next.lift
    }
    
    private def dispatch Optional<BasicBlock> doProcess(Return s, BasicBlock current, BasicBlock exit) {
        current.stmts += s
        current.setSuccessors(exit)
        Optional.empty
    }
    
    private def dispatch Optional<BasicBlock> doProcess(Stmt s, BasicBlock current, BasicBlock exit) {
        current.stmts += s
        current.lift
    }
    
    private def <T> lift(T t) { Optional.of(t) }
    private def <T> asOptional(T t) { Optional.ofNullable(t) }
}
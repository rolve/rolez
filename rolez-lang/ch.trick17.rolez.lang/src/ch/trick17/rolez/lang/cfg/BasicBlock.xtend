package ch.trick17.rolez.lang.cfg

import ch.trick17.rolez.lang.rolez.Expr
import ch.trick17.rolez.lang.rolez.Stmt
import java.util.ArrayList
import java.util.HashSet
import java.util.List
import java.util.Set

import static extension java.util.Objects.*

class BasicBlock {
    public val Stmt associatedStmt
    public val List<Stmt> stmts = new ArrayList
    
    val List<BasicBlock> predecessors = new ArrayList
    var Successors successors
    
    new(Stmt associated) { associatedStmt = associated }
    
    def setNoSuccessors() {
        successors = new NoSuccessor
    }
    
    def setSuccessors(BasicBlock one) {
        if(successors != null) throw new IllegalStateException
        successors = new OneSuccessor(one)
        one.predecessors.add(this)
    }
    
    def setSuccessors(BasicBlock one, BasicBlock two, Expr condition) {
        if(successors != null) throw new IllegalStateException
        successors = new TwoSuccessors(one, two, condition)
        one.predecessors.add(this)
        two.predecessors.add(this)
    }
    
    def getPredecessors() { return predecessors.unmodifiableView }
    def getSuccessors() { return successors.requireNonNull }
    
    def Set<BasicBlock> getReachableBlocks() {
        new HashSet => [collectReachableBlocks(it)]
    }
    
    private def void collectReachableBlocks(Set<BasicBlock> blocks) {
        if(successors == null)
            throw new AssertionError
        if(blocks += this)
            successors.forEach[collectReachableBlocks(blocks)]
    }
}
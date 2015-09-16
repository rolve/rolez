package ch.trick17.rolez.lang.cfg

import ch.trick17.rolez.lang.rolez.Stmt
import java.util.ArrayList
import java.util.List

import static extension java.util.Objects.requireNonNull
import java.util.HashSet
import java.util.Set

class BasicBlock {
    val List<BasicBlock> predecessors = new ArrayList
    val List<BasicBlock> successors = new ArrayList
    
    public val List<Stmt> stmts = new ArrayList
    
    new() {}
    new(BasicBlock predecessor) {
        predecessors += predecessor.requireNonNull
        predecessor.successors += this
    }
    
    def linkPredecessor(BasicBlock predecessor) {
        predecessors += predecessor.requireNonNull
        predecessor.successors += this
    }
    
    def unlinkPredecessor(BasicBlock predecessor) {
        predecessors -= predecessor.requireNonNull
        predecessor.successors -= this
    }
    
    def getPredecessors() {
        return predecessors.unmodifiableView
    }
    
    def getSuccessors() {
        return successors.unmodifiableView
    }
    
    def Set<BasicBlock> getReachableBlocks() {
        new HashSet => [collectReachableBlocks(it)]
    }
    
    private def void collectReachableBlocks(Set<BasicBlock> blocks) {
        if(blocks += this)
            successors.forEach[collectReachableBlocks(blocks)]
    }
}
package ch.trick17.rolez.lang.cfg

import ch.trick17.rolez.lang.rolez.Expr
import ch.trick17.rolez.lang.rolez.Stmt
import com.google.common.collect.ImmutableList
import java.util.ArrayList
import java.util.List

import static extension java.util.Objects.*

abstract class Node {
    val List<Node> predecessors = new ArrayList
    package def addPredecessor(Node p) { predecessors += p }
    
    def getPredecessors() { return predecessors.unmodifiableView }
    def List<Node> getSuccessors()
}

class ExitNode extends Node {
    override getSuccessors() { emptyList }
}

class StmtNode extends Node {
    public val Stmt stmt
    package var Node successor
    
    override getSuccessors() { ImmutableList.of(successor) }
    new (Stmt s) { stmt = s.requireNonNull }
}

class ConditionNode extends Node {
    public val Expr condition
    package var List<Node> successors = newArrayList(null, null)
    
    new (Expr condition) { this.condition = condition }
    
    override getSuccessors() { successors.unmodifiableView }
}
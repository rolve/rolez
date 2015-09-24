package ch.trick17.rolez.lang.cfg

import ch.trick17.rolez.lang.rolez.Expr
import ch.trick17.rolez.lang.rolez.Stmt
import java.util.ArrayList
import java.util.List

import static extension java.util.Objects.*
import ch.trick17.rolez.lang.rolez.WhileLoop

abstract class Node {
    package val List<Node> preds = new ArrayList(1)
    package val List<Node> succs = new ArrayList(1)
    
    def getPredecessors() { preds.unmodifiableView }
    def getSuccessors()   { succs.unmodifiableView }
    def getSoleSuccessor() {
        if(succs.size != 0) throw new AssertionError
        succs.get(0)
    }
    def getTrueSuccessor() {
        if(succs.size != 2) throw new AssertionError
        succs.get(0)
    }
    def getFalseSuccessor() {
        if(succs.size != 2) throw new AssertionError
        succs.get(1)
    }
    def isJoin()  { preds.size > 1 }
    def isSplit() { succs.size > 1 }
}

class StmtNode extends Node {
    public val Stmt stmt    
    new (Stmt s) { stmt = s.requireNonNull }
}

class ExprNode extends Node {
    public val Expr expr
    new (Expr expr) { this.expr = expr.requireNonNull }
}

class LoopHeadNode extends Node {
    public val WhileLoop loop
    new (WhileLoop loop) { this.loop = loop.requireNonNull }
}

class ExitNode extends Node {}
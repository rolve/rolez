package ch.trick17.rolez.cfg

import ch.trick17.rolez.rolez.Instr
import ch.trick17.rolez.rolez.WhileLoop
import java.util.ArrayList
import java.util.List

import static extension java.util.Objects.*

abstract class Node {
    package val List<Node> preds = new ArrayList(1)
    package val List<Node> succs = new ArrayList(1)
    
    def getPredecessors() { preds.unmodifiableView }
    def getSolePredecessor() {
        if(preds.size != 1) throw new UnsupportedOperationException
        preds.get(0)
    }
    
    def getSuccessors()   { succs.unmodifiableView }
    def getSoleSuccessor() {
        if(succs.size != 1) throw new UnsupportedOperationException
        succs.get(0)
    }
    def getTrueSuccessor() {
        if(succs.size != 2) throw new UnsupportedOperationException
        succs.get(0)
    }
    def getFalseSuccessor() {
        if(succs.size != 2) throw new UnsupportedOperationException
        succs.get(1)
    }
    
    def isJoin()  { preds.size > 1 }
    def isSplit() { succs.size > 1 }
}

class InstrNode extends Node {
    public val Instr instr    
    new(Instr i) { instr = i.requireNonNull }
}

class LoopHeadNode extends Node {
    public val WhileLoop loop
    new(WhileLoop loop) { this.loop = loop.requireNonNull }
}

class EntryNode extends Node {}
class ExitNode extends Node {}
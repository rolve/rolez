package ch.trick17.rolez.lang.cfg

import ch.trick17.rolez.lang.rolez.Expr
import java.util.AbstractList

import static extension java.util.Objects.*

abstract class Successors extends AbstractList<BasicBlock> {
    def boolean hasCondition()
}

class NoSuccessor extends Successors {
    override get(int index) { throw new IndexOutOfBoundsException }
    override size() { 0 }
    override hasCondition() { false }
}

class OneSuccessor extends Successors {
    public val BasicBlock one
    
    new (BasicBlock one) { this.one = one.requireNonNull }

    override get(int index) {
        if(index == 0) one
        else throw new IndexOutOfBoundsException
    }
    override size() { 1 }
    override hasCondition() { false }
}

class TwoSuccessors extends Successors {
    public val BasicBlock one
    public val BasicBlock two
    public val Expr condition
    
    new (BasicBlock one, BasicBlock two, Expr condition) {
        this.one = one.requireNonNull
        this.two = two.requireNonNull
        this.condition = condition.requireNonNull
    }
    
    override get(int index) {
        if(index == 0) one
        else if(index == 1) two
        else throw new IndexOutOfBoundsException
    }
    override size() { 2 }
    override hasCondition() { true }
}
package ch.trick17.rolez.lang.cfg

class ControlFlowGraph {
    
    public val BasicBlock enter
    public val BasicBlock exit
    
    new (BasicBlock enter, BasicBlock exit) {
        this.enter = enter
        this.exit = exit
    }
    
    def getAllBlocks() {
        enter.reachableBlocks
    }
}
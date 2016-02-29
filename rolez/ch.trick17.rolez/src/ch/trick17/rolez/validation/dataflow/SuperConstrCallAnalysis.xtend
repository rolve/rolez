package ch.trick17.rolez.validation.dataflow

import ch.trick17.rolez.rolez.Instr
import ch.trick17.rolez.rolez.SuperConstrCall
import ch.trick17.rolez.validation.cfg.ControlFlowGraph
import ch.trick17.rolez.validation.cfg.InstrNode
import ch.trick17.rolez.validation.cfg.Node

class SuperConstrCallAnalysis extends DataFlowAnalysis<Boolean> {
    
    new(ControlFlowGraph cfg) {
        super(cfg, true)
        analyze
    }
    
    protected override newFlow() { false }
    protected override entryFlow() {
        // If there are no explicit super constr calls, the no-param constr
        // is called implicitly before the first statement
        cfg.nodes.filter(InstrNode).exists[instr instanceof SuperConstrCall]
    }
    
    protected def dispatch flowThrough(SuperConstrCall c, Boolean in) { false }
    protected def dispatch flowThrough(Instr i, Boolean in) { in }
    
    protected override merge(Boolean in1, Boolean in2) { in1 || in2 }
    
    def boolean isBeforeSuperConstrCall(Node it) { inFlow }
}

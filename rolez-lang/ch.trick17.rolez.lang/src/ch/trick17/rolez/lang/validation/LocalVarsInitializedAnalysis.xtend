package ch.trick17.rolez.lang.validation

import ch.trick17.rolez.lang.cfg.ControlFlowGraph
import ch.trick17.rolez.lang.cfg.DataFlowAnalysis
import ch.trick17.rolez.lang.rolez.Assignment
import ch.trick17.rolez.lang.rolez.Instr
import ch.trick17.rolez.lang.rolez.Var
import ch.trick17.rolez.lang.rolez.VarRef
import java.util.Set

import static com.google.common.collect.ImmutableSet.copyOf
import static extension com.google.common.collect.Sets.intersection
import ch.trick17.rolez.lang.cfg.Node
import ch.trick17.rolez.lang.rolez.LocalVarDecl

class LocalVarsInitializedAnalysis extends DataFlowAnalysis<Set<Var>> {
    
    new(ControlFlowGraph cfg) {
        super(cfg, true)
        analyze()
    }
    
    protected override newFlow() { emptySet }
    protected override entryFlow() { emptySet }
    
    protected def dispatch flowThrough(Assignment a, Set<Var> in) {
        val left = a.left
        if(left instanceof VarRef) in.with(left.variable)
        else in
    }
    
    protected def dispatch flowThrough(LocalVarDecl d, Set<Var> in) {
        if(d.initializer != null) in.with(d.variable)
        else in
    }
    
    protected def dispatch flowThrough(Instr i, Set<Var> in) { in }
    
    protected override merge(Set<Var> in1, Set<Var> in2) {
        copyOf(in1.intersection(in2))
    }
    
    private def with(Set<Var> it, Var v) { copyOf(it + #[v]) }
    
    def isInitializedBefore(Var it, Node n) { n.inFlow.contains(it) }
}
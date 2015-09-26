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
    
    new(ControlFlowGraph graph) {
        super(graph, true)
        analyze()
    }
    
    protected override newFlow() { emptySet }
    protected override entryFlow() { emptySet }
    
    protected def dispatch flowThrough(Assignment a, Set<Var> before) {
        val left = a.left
        if(left instanceof VarRef) before.with(left.variable)
        else before
    }
    
    protected def dispatch flowThrough(LocalVarDecl d, Set<Var> before) {
        if(d.initializer != null) before.with(d.variable)
        else before
    }
    
    protected def dispatch flowThrough(Instr i, Set<Var> before) { before }
    
    protected override merge(Set<Var> flow1, Set<Var> flow2) {
        copyOf(flow1.intersection(flow2))
    }
    
    private def with(Set<Var> it, Var v) { copyOf(it + #[v]) }
    
    def initializedBefore(Var it, Node n) {
        n.beforeFlow.contains(it)
    }
}
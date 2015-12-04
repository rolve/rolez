package ch.trick17.rolez.lang.validation

import ch.trick17.rolez.lang.cfg.ControlFlowGraph
import ch.trick17.rolez.lang.cfg.DataFlowAnalysis
import ch.trick17.rolez.lang.cfg.InstrNode
import ch.trick17.rolez.lang.cfg.Node
import ch.trick17.rolez.lang.rolez.Assignment
import ch.trick17.rolez.lang.rolez.Instr
import ch.trick17.rolez.lang.rolez.LocalVarDecl
import ch.trick17.rolez.lang.rolez.Var
import ch.trick17.rolez.lang.rolez.VarRef
import java.util.Set

import static com.google.common.collect.ImmutableSet.copyOf

import static extension com.google.common.collect.Sets.intersection

class LocalVarsInitializedAnalysis extends DataFlowAnalysis<Set<Var>> {
    
    val Set<Var> fullSet
    
    new(ControlFlowGraph cfg) {
        super(cfg, true)
        fullSet = copyOf(cfg.nodes.filter(InstrNode).map[instr]
            .filter(LocalVarDecl).map[variable])
        analyze()
    }
    
    protected override newFlow() { fullSet }
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
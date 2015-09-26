package ch.trick17.rolez.lang.validation

import ch.trick17.rolez.lang.cfg.ControlFlowGraph
import ch.trick17.rolez.lang.cfg.DataFlowAnalysis
import ch.trick17.rolez.lang.cfg.Node
import ch.trick17.rolez.lang.rolez.Assignment
import ch.trick17.rolez.lang.rolez.Field
import ch.trick17.rolez.lang.rolez.FieldSelector
import ch.trick17.rolez.lang.rolez.Instr
import ch.trick17.rolez.lang.rolez.MemberAccess
import ch.trick17.rolez.lang.rolez.This
import ch.trick17.rolez.lang.validation.ValFieldsInitializedAnalysis.Initialized
import java.util.Set
import org.eclipse.xtend.lib.annotations.Data

import static ch.trick17.rolez.lang.rolez.VarKind.*
import static com.google.common.collect.ImmutableSet.*

import static extension com.google.common.collect.Sets.*

class ValFieldsInitializedAnalysis extends DataFlowAnalysis<Initialized> {
    
    static def isValFieldInit(Assignment it) {
        !(#[left].filter(MemberAccess).filter[target instanceof This]
            .map[selector].filter(FieldSelector).map[field]
            .filter[kind == VAL].isEmpty)
    }
    
    static def assignedField(Assignment it) {
        ((left as MemberAccess).selector as FieldSelector).field
    }
    
    new(ControlFlowGraph graph) {
        super(graph, true)
        analyze()
    }
    
    protected override newFlow()   { new Initialized(emptySet, emptySet) }
    protected override entryFlow() { new Initialized(emptySet, emptySet) }
    
    protected def dispatch flowThrough(Assignment a, Initialized before) {
        if(a.isValFieldInit) before.with(a.assignedField)
        else before
    }
    
    protected def dispatch flowThrough(Instr i, Initialized before) { before }
    
    protected override merge(Initialized flow1, Initialized flow2) {
        new Initialized(copyOf(flow1.possibly.union(flow2.possibly)),
            copyOf(flow1.definitely.intersection(flow2.definitely)))
    }
    
    def definitelyInitializedBefore(Field it, Node n) {
        n.beforeFlow.definitely.contains(it)
    }
    
    def definitelyInitializedAfter(Field it, Node n) {
        n.afterFlow.definitely.contains(it)
    }
    
    def possiblyInitializedBefore(Field it, Node n) {
        n.beforeFlow.possibly.contains(it)
    }
    
    @Data static class Initialized {
        Set<Field> possibly
        Set<Field> definitely
        
        def with(Field f) {
            new Initialized(copyOf(possibly + #[f]), copyOf(definitely + #[f]))
        }
    }
}
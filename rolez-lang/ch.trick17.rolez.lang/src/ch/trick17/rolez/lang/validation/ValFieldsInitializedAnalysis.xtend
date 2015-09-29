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
    
    new(ControlFlowGraph cfg) {
        super(cfg, true)
        analyze()
    }
    
    protected override newFlow()   { new Initialized(emptySet, emptySet) }
    protected override entryFlow() { new Initialized(emptySet, emptySet) }
    
    protected def dispatch flowThrough(Assignment a, Initialized in) {
        if(a.isValFieldInit) in.with(a.assignedField)
        else in
    }
    
    protected def dispatch flowThrough(Instr i, Initialized in) { in }
    
    protected override merge(Initialized in1, Initialized in2) {
        new Initialized(copyOf(in1.possibly.union(in2.possibly)),
            copyOf(in1.definitely.intersection(in2.definitely)))
    }
    
    def definitelyInitializedBefore(Field it, Node n) {
        n.inFlow.definitely.contains(it)
    }
    
    def definitelyInitializedAfter(Field it, Node n) {
        n.outFlow.definitely.contains(it)
    }
    
    def possiblyInitializedBefore(Field it, Node n) {
        n.inFlow.possibly.contains(it)
    }
    
    @Data static class Initialized {
        Set<Field> possibly
        Set<Field> definitely
        
        def with(Field f) {
            new Initialized(copyOf(possibly + #[f]), copyOf(definitely + #[f]))
        }
    }
}
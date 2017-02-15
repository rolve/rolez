package ch.trick17.rolez.validation.dataflow

import ch.trick17.rolez.rolez.Assignment
import ch.trick17.rolez.rolez.Class
import ch.trick17.rolez.rolez.Field
import ch.trick17.rolez.rolez.Instr
import ch.trick17.rolez.validation.dataflow.ValFieldsInitializedAnalysis.Initialized
import ch.trick17.rolez.validation.cfg.ControlFlowGraph
import ch.trick17.rolez.validation.cfg.Node
import com.google.inject.MembersInjector
import java.util.Set
import javax.inject.Inject

import static ch.trick17.rolez.rolez.VarKind.VAL
import static ch.trick17.rolez.RolezUtils.*
import static com.google.common.collect.ImmutableSet.*

import static extension com.google.common.collect.Sets.*

class ValFieldsInitializedAnalysis extends DataFlowAnalysis<Initialized> {
    
    static class Provider {
        @Inject MembersInjector<ValFieldsInitializedAnalysis> injector
        
        def analyze(ControlFlowGraph cfg, Class clazz) {
            val analysis = new ValFieldsInitializedAnalysis(cfg, clazz)
            injector.injectMembers(analysis)
            analysis.analyze
            analysis
        }
    }
    
    val Class clazz
    val newFlow = new Initialized(null, null)
    
    private new(ControlFlowGraph cfg, Class clazz) {
        super(cfg, true)
        this.clazz = clazz
    }
    
    override protected analyze() { super.analyze() }
    
    protected override newFlow()   { newFlow }
    
    protected override entryFlow() {
        val initialized = clazz.fields.filter[kind == VAL && initializer != null].toSet
        new Initialized(initialized, initialized)
    }
    
    protected def dispatch flowThrough(Assignment a, Initialized in) {
        // while isValFieldInit may also be true for increment or decrement ops,
        // here we only consider assignments, since the latter are an error anyway
        if(isValFieldInit(a)) in.with(assignedField(a))
        else in
    }
    
    protected def dispatch flowThrough(Instr i, Initialized in) { in }
    
    protected override merge(Initialized in1, Initialized in2) {
        if(in1 === newFlow) in2
        else if(in2 === newFlow) in1
        else new Initialized(copyOf(in1.possibly.union(in2.possibly)),
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
    
    package static class Initialized {
        val Set<Field> possibly
        val Set<Field> definitely
        
        new(Set<Field> possibly, Set<Field> definitely) {
            this.possibly = possibly
            this.definitely = definitely
        }
        
        def with(Field f) {
            new Initialized(copyOf(possibly + #[f]), copyOf(definitely + #[f]))
        }
        
        override equals(Object obj) {
            if(obj === this) return true
            switch(obj) {
                Initialized:
                    possibly == obj.possibly && definitely == obj.definitely
                default: false
            }
        }
        
        override int hashCode() {
            val prime = 31;
            var result = 1;
            result = prime * result + if(definitely == null) 0 else definitely.hashCode;
            result = prime * result + if(possibly == null) 0 else possibly.hashCode;
            return result;
        }
    }
}

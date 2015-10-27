package ch.trick17.rolez.lang.validation

import ch.trick17.rolez.lang.cfg.ControlFlowGraph
import ch.trick17.rolez.lang.cfg.DataFlowAnalysis
import ch.trick17.rolez.lang.cfg.Node
import ch.trick17.rolez.lang.rolez.Assignment
import ch.trick17.rolez.lang.rolez.Field
import ch.trick17.rolez.lang.rolez.Instr
import ch.trick17.rolez.lang.typesystem.RolezUtils
import ch.trick17.rolez.lang.validation.ValFieldsInitializedAnalysis.Initialized
import com.google.inject.MembersInjector
import java.util.Set
import javax.inject.Inject
import org.eclipse.xtend.lib.annotations.Data

import static com.google.common.collect.ImmutableSet.*

import static extension com.google.common.collect.Sets.*

class ValFieldsInitializedAnalysis extends DataFlowAnalysis<Initialized> {
    
    static class Provider {
        @Inject MembersInjector<ValFieldsInitializedAnalysis> injector
        
        def analyze(ControlFlowGraph cfg) {
            val analysis = new ValFieldsInitializedAnalysis(cfg)
            injector.injectMembers(analysis)
            analysis.analyze
            analysis
        }
    }
    
    @Inject RolezUtils utils
    
    val newFlow = new Initialized(null, null)
    
    private new(ControlFlowGraph cfg) {
        super(cfg, true)
    }
    
    override protected analyze() { super.analyze() }
    
    protected override newFlow()   { newFlow }
    protected override entryFlow() { new Initialized(emptySet, emptySet) }
    
    protected def dispatch flowThrough(Assignment a, Initialized in) {
        if(utils.isValFieldInit(a)) in.with(utils.assignedField(a))
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
    
    @Data static class Initialized {
        Set<Field> possibly
        Set<Field> definitely
        
        def with(Field f) {
            new Initialized(copyOf(possibly + #[f]), copyOf(definitely + #[f]))
        }
    }
}
package ch.trick17.rolez.lang.validation

import ch.trick17.rolez.lang.RolezExtensions
import ch.trick17.rolez.lang.cfg.ControlFlowGraph
import ch.trick17.rolez.lang.cfg.DataFlowAnalysis
import ch.trick17.rolez.lang.cfg.Node
import ch.trick17.rolez.lang.rolez.Assignment
import ch.trick17.rolez.lang.rolez.Class
import ch.trick17.rolez.lang.rolez.Field
import ch.trick17.rolez.lang.rolez.Instr
import ch.trick17.rolez.lang.typesystem.RolezUtils
import ch.trick17.rolez.lang.validation.ValFieldsInitializedAnalysis.Initialized
import com.google.inject.MembersInjector
import java.util.Set
import javax.inject.Inject
import org.eclipse.xtend.lib.annotations.Data

import static ch.trick17.rolez.lang.rolez.VarKind.VAL
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
    
    @Inject extension RolezExtensions
    @Inject RolezUtils utils
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
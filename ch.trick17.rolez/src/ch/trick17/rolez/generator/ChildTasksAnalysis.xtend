package ch.trick17.rolez.generator

import ch.trick17.rolez.rolez.Constr
import ch.trick17.rolez.rolez.FieldInitializer
import ch.trick17.rolez.rolez.Instr
import ch.trick17.rolez.rolez.MemberAccess
import ch.trick17.rolez.rolez.Method
import ch.trick17.rolez.validation.cfg.CfgProvider
import ch.trick17.rolez.validation.cfg.ControlFlowGraph
import ch.trick17.rolez.validation.dataflow.DataFlowAnalysis
import javax.inject.Inject

import static ch.trick17.rolez.Constants.noChildTasksAnalysis
import static ch.trick17.rolez.generator.MethodKind.*

/**
 * A simple analysis that determines whether there may currently exist any child
 * tasks of the current task. The program state is a single boolean that is true
 * iff this may be the case.
 * <p>
 * Note that the result of the analysis depends on the context for which the code
 * is generated. In particular, it depends on the generated method kind, i.e.,
 * task, guarded method, or unguarded method.
 */
interface ChildTasksAnalysis {
    def boolean childTasksMayExist(Instr it)
}

class ChildTasksAnalysisProvider {
    
    @Inject extension CfgProvider
    
    def newChildTasksAnalysis(FieldInitializer initializer) {
        if(System.getProperty(noChildTasksAnalysis) != null)
            new NullChildTasksAnalysis
        else
            new DefaultChildTasksAnalysis(initializer.expr.controlFlowGraph, null)
    }
    
    def newChildTasksAnalysis(Constr constr) {
        if(System.getProperty(noChildTasksAnalysis) != null)
            new NullChildTasksAnalysis
        else
            new DefaultChildTasksAnalysis(constr.body.controlFlowGraph, null)
    }
    
    def newChildTasksAnalysis(Method method, MethodKind methodKind) {
        if(System.getProperty(noChildTasksAnalysis) != null)
            new NullChildTasksAnalysis
        else
            new DefaultChildTasksAnalysis(method.code.controlFlowGraph, methodKind)
    }
}

class DefaultChildTasksAnalysis extends DataFlowAnalysis<Boolean> implements ChildTasksAnalysis {
    
    val MethodKind methodKind
    
    package new(ControlFlowGraph cfg, MethodKind methodKind) {
        super(cfg, true)
        this.methodKind = methodKind;
        
        analyze
    }
    
    override protected newFlow() { false  }
    
    override protected entryFlow() {
        return methodKind != UNGUARDED_METHOD && methodKind != TASK
    }
    
    protected def dispatch flowThrough(MemberAccess a, Boolean in) {
        return in || a.isTaskStart || a.isMethodInvoke && a.method.isAsync
    }
    
    protected def dispatch flowThrough(Instr _, Boolean in) { in }
    
    override protected merge(Boolean in1, Boolean in2) { in1 || in2 }
    
    override childTasksMayExist(Instr it) { cfg.nodeOf(it).inFlow }
}

class NullChildTasksAnalysis implements ChildTasksAnalysis {
    override childTasksMayExist(Instr it) { true }
}
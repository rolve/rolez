package ch.trick17.rolez.lang.cfg

import ch.trick17.rolez.lang.rolez.ParameterizedBody
import java.util.concurrent.ConcurrentHashMap

class CfgProvider {
    
    val cfgs = new ConcurrentHashMap<ParameterizedBody, ControlFlowGraph>
    
    def controlFlowGraph(ParameterizedBody it) {
        cfgs.computeIfAbsent(it, [new CfgBuilder(it).build])
    }
}
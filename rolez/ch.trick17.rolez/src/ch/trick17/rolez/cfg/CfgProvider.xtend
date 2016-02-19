package ch.trick17.rolez.cfg

import ch.trick17.rolez.rolez.Instr
import org.eclipse.xtext.util.OnChangeEvictingCache

class CfgProvider {
    
    val cfgs = new OnChangeEvictingCache
    
    def controlFlowGraph(Instr it) {
        cfgs.get(it, eResource, [new CfgBuilder(it).build])
    }
}
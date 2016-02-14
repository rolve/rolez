package ch.trick17.rolez.cfg

import org.eclipse.xtext.util.OnChangeEvictingCache
import ch.trick17.rolez.rolez.Executable

class CfgProvider {
    
    val cfgs = new OnChangeEvictingCache
    
    def controlFlowGraph(Executable it) {
        cfgs.get(it, eResource, [new CfgBuilder(it).build])
    }
}
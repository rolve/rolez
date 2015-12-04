package ch.trick17.rolez.lang.cfg

import ch.trick17.rolez.lang.rolez.ParameterizedBody
import com.google.inject.MembersInjector
import javax.inject.Inject
import org.eclipse.xtext.util.OnChangeEvictingCache

class CfgProvider {
    
    @Inject MembersInjector<CfgBuilder> injector
    
    val cfgs = new OnChangeEvictingCache
    
    def controlFlowGraph(ParameterizedBody it) {
        cfgs.get(it, eResource, [
            val builder = new CfgBuilder(it)
            injector.injectMembers(builder)
            builder.build
        ])
    }
}
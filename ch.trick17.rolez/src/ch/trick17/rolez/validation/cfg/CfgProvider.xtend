package ch.trick17.rolez.validation.cfg

import ch.trick17.rolez.rolez.Instr
import java.util.Optional
import org.eclipse.xtext.resource.XtextSyntaxDiagnostic
import org.eclipse.xtext.util.OnChangeEvictingCache

import static java.util.Optional.empty

class CfgProvider {
    
    val cfgs = new OnChangeEvictingCache
    
    def controlFlowGraph(Instr it) {
        if(eResource.errors.filter(XtextSyntaxDiagnostic).isEmpty)
            cfgs.get(it, eResource, [Optional.of(new CfgBuilder(it).build)])
        else
            empty
    }
}
